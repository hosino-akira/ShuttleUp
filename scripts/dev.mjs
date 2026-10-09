import { spawn, spawnSync } from 'node:child_process'
import { existsSync } from 'node:fs'
import { createConnection } from 'node:net'
import { dirname, join, resolve } from 'node:path'
import { createInterface } from 'node:readline'
import { setTimeout as delay } from 'node:timers/promises'
import { fileURLToPath } from 'node:url'

const projectRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const commandShell = process.env.ComSpec || 'cmd.exe'
const python = join(projectRoot, 'python-analysis', '.venv', 'Scripts', 'python.exe')
const options = new Set(process.argv.slice(2))
const children = []
let stopping = false

const services = [
  {
    name: 'Spring Boot',
    directory: 'backend',
    port: 8080,
    executable: commandShell,
    args: ['/d', '/s', '/c', 'mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=8080'],
  },
  {
    name: 'FastAPI',
    directory: 'python-analysis',
    port: 8000,
    executable: python,
    args: ['-m', 'uvicorn', 'app.main:app', '--reload', '--host', '127.0.0.1', '--port', '8000'],
  },
  {
    name: 'Vue',
    directory: 'frontend',
    port: 5173,
    executable: commandShell,
    args: ['/d', '/s', '/c', 'npm.cmd run dev -- --host 127.0.0.1 --port 5173 --strictPort'],
  },
]

// 仮想環境の Python を直接使うため、Activate.ps1 の実行は不要。
function checkDependencies() {
  if (process.platform !== 'win32') throw new Error('この起動スクリプトは Windows 用です。')
  for (const option of options) {
    if (!['--check', '--no-open'].includes(option)) throw new Error(`不明なオプション: ${option}`)
  }
  if (!existsSync(join(projectRoot, 'frontend', 'node_modules'))) {
    throw new Error('frontend で npm install を実行してください。')
  }
  if (!existsSync(python)) throw new Error('python-analysis の .venv を作成してください。')
  if (!existsSync(join(projectRoot, 'backend', 'mvnw.cmd'))) {
    throw new Error('backend/mvnw.cmd が見つかりません。')
  }
  if (spawnSync('java', ['-version'], { windowsHide: true, stdio: 'ignore' }).status !== 0) {
    throw new Error('Java 21 をインストールし、PATH を確認してください。')
  }
  if (spawnSync(commandShell, ['/d', '/c', 'where npm.cmd'], { windowsHide: true, stdio: 'ignore' }).status !== 0) {
    throw new Error('Node.js と npm の PATH を確認してください。')
  }
  const pythonCheck = spawnSync(python, [
    '-c', 'import uvicorn; from app.core.config import get_settings; get_settings()',
  ], { cwd: join(projectRoot, 'python-analysis'), windowsHide: true, stdio: 'ignore' })
  if (pythonCheck.status !== 0) {
    throw new Error('Python の依存パッケージと .env を確認してください。python-analysis/README.md を参照してください。')
  }
}

function isPortOpen(port) {
  return new Promise((resolvePort) => {
    const socket = createConnection({ host: '127.0.0.1', port })
    const finish = (open) => { socket.destroy(); resolvePort(open) }
    socket.setTimeout(500)
    socket.once('connect', () => finish(true))
    socket.once('error', () => finish(false))
    socket.once('timeout', () => finish(false))
  })
}

// サービス名を付け、三つのログを一つのターミナルに表示する。
function showLogs(stream, name) {
  let pending = ''
  stream.setEncoding('utf8')
  stream.on('data', (chunk) => {
    const lines = (pending + chunk).split(/\r?\n/)
    pending = lines.pop()
    for (const line of lines) console.log(`[${name}] ${line}`)
  })
  stream.on('end', () => { if (pending) console.log(`[${name}] ${pending}`) })
}

function startService(service) {
  const child = spawn(service.executable, service.args, {
    cwd: join(projectRoot, service.directory),
    env: { ...process.env, PYTHONIOENCODING: 'utf-8', PYTHONUNBUFFERED: '1' },
    windowsHide: true,
    windowsVerbatimArguments: service.executable === commandShell,
    stdio: ['ignore', 'pipe', 'pipe'],
  })
  children.push(child)
  showLogs(child.stdout, service.name)
  showLogs(child.stderr, service.name)
  child.once('error', (error) => {
    console.error(`[${service.name}] 起動できません: ${error.message}`)
    void stopServices(1)
  })
  child.once('exit', (code) => {
    if (!stopping) {
      console.error(`[${service.name}] 終了しました (code: ${code})。他のサービスも停止します。`)
      void stopServices(1)
    }
  })
}

async function waitForService(service) {
  const deadline = Date.now() + 180_000
  while (!stopping && Date.now() < deadline) {
    if (await isPortOpen(service.port)) {
      console.log(`[${service.name}] ポート ${service.port} で起動を確認しました。`)
      return
    }
    await delay(500)
  }
  if (!stopping) throw new Error(`${service.name} の起動がタイムアウトしました。上のログを確認してください。`)
}

async function stopServices(exitCode = 0) {
  if (stopping) return
  stopping = true
  console.log('\nShuttleUp のサービスを停止しています...')
  // このスクリプトで起動したプロセスと子プロセスだけを停止する。
  await Promise.all(children
    .filter((child) => child.pid && child.exitCode === null && child.signalCode === null)
    .map((child) => new Promise((resolveStop) => {
      const killer = spawn('taskkill.exe', ['/PID', String(child.pid), '/T', '/F'], {
        windowsHide: true, stdio: 'ignore',
      })
      killer.once('error', () => resolveStop())
      killer.once('exit', () => resolveStop())
    })))
  process.exit(exitCode)
}

async function main() {
  checkDependencies()
  const occupied = []
  for (const service of services) {
    if (await isPortOpen(service.port)) occupied.push(`${service.name}: ${service.port}`)
  }
  if (occupied.length) {
    throw new Error(`ポートが使用中です (${occupied.join(', ')})。既存の開発サーバーを停止してから実行してください。`)
  }
  if (options.has('--check')) {
    console.log('実行環境、Python の設定、三つのポートを確認しました。起動できます。')
    return
  }

  console.log('ShuttleUp を起動しています。MySQL は事前に起動してください。')
  console.log('停止するには Ctrl+C、または q を入力して Enter を押してください。\n')
  process.on('SIGINT', () => { void stopServices() })
  process.on('SIGTERM', () => { void stopServices() })
  const input = createInterface({ input: process.stdin })
  input.on('line', (line) => { if (line.trim().toLowerCase() === 'q') void stopServices() })
  for (const service of services) startService(service)
  await Promise.all(services.map(waitForService))
  if (stopping) return

  console.log('\n起動しました: http://localhost:5173')
  console.log('FastAPI の API ドキュメント: http://localhost:8000/docs\n')
  if (!options.has('--no-open')) {
    const browser = spawn(commandShell, ['/d', '/s', '/c', 'start "" "http://localhost:5173"'], {
      windowsHide: true, windowsVerbatimArguments: true, stdio: 'ignore',
    })
    browser.on('error', () => console.log('ブラウザーで http://localhost:5173 を開いてください。'))
  }
}

main().catch(async (error) => {
  console.error(`\n起動エラー: ${error.message}`)
  await stopServices(1)
})
