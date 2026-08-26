# ShuttleUp

ShuttleUp は、バドミントンとフィジカルトレーニングの記録・分析を行う Web アプリケーションです。

トレーニング内容、使用種目、試合結果、対戦相手を記録し、Dashboard と Statistics 画面でトレーニング状況を可視化します。

日本でのエンジニア転職活動に向けたポートフォリオとして開発しています。

## 主な機能

* トレーニングセッションの登録・編集・削除
* トレーニング記録の管理
* 大分類・中分類・種目の連動選択
* ユーザー独自種目の登録
* 試合結果の管理
* 対戦相手の管理
* Dashboard による基本統計
* Python による詳細トレーニング分析
* ECharts によるデータ可視化
* レスポンシブレイアウト

## システム構成

```text
Vue 3 / TypeScript
        |
        | HTTP
        v
+-----------------------+
| Vite Proxy            |
| /api/analysis → 8000  |
| /api          → 8080  |
+-----------------------+
        |         |
        v         v
   FastAPI    Spring Boot
        |         |
        | HTTP    | JPA
        +-------> |
                  v
                MySQL
```

各サービスの役割：

| サービス        | 役割              |  ポート |
| ----------- | --------------- | ---: |
| Vue         | 画面表示・ユーザー操作     | 5173 |
| Spring Boot | 業務データと REST API | 8080 |
| FastAPI     | トレーニングの詳細分析     | 8000 |
| MySQL       | データ保存           | 3306 |

## 使用技術

### フロントエンド

* Vue 3
* TypeScript
* Vite
* Vue Router
* Pinia
* Axios
* Ant Design Vue
* VXE-Table
* ECharts

### Java バックエンド

* Java 21
* Spring Boot
* Spring Data JPA
* Hibernate Validator
* Maven

### Python 分析サービス

* Python 3.12+
* FastAPI
* pandas
* httpx
* Pydantic
* pytest

### データベース

* MySQL 8

## ディレクトリ構成

```text
ShuttleUp/
├── frontend/          Vue フロントエンド
├── backend/           Spring Boot バックエンド
├── python-analysis/   FastAPI 分析サービス
└── README.md
```

実際のディレクトリ名が異なる場合は、プロジェクト構成に合わせて読み替えてください。

## セットアップ

各ディレクトリの詳細なセットアップ方法は、それぞれの README を参照してください。

* `frontend/README.md`
* `backend/README.md`
* `python-analysis/README.md`

## 起動順序

### 1. MySQL

MySQL を起動し、`shuttleup` データベースへ接続できることを確認します。

### 2. Spring Boot

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

または IntelliJ IDEA から `BackendApplication` を実行します。

### 3. FastAPI

```powershell
cd python-analysis
.\.venv\Scripts\Activate.ps1
python -m uvicorn app.main:app --reload --port 8000
```

### 4. Vue

```powershell
cd frontend
npm install
npm run dev
```

ブラウザで以下を開きます。

```text
http://localhost:5173
```

## API

### Spring Boot

```text
http://localhost:8080/api
```

### FastAPI

```text
http://localhost:8000/api/analysis
```

### FastAPI Swagger UI

```text
http://localhost:8000/docs
```

## Dashboard と Statistics の役割

### Dashboard

Spring Boot API を利用して、現在の状況を素早く確認するための基本統計を表示します。

* トレーニング回数
* トレーニング時間
* 月別推移
* 試合結果
* 勝率
* 重量推移

### Statistics

FastAPI を利用して、トレーニングデータから一歩進んだ分析結果を表示します。

* 週平均トレーニング日数
* 連続トレーニング日数
* 重量の増加量と自己ベスト
* 対戦相手別の傾向
* データ入力完全率
* フィジカル練習と技術練習の比率

## テスト

### Spring Boot

```powershell
cd backend
.\mvnw.cmd test
```

### FastAPI

```powershell
cd python-analysis
.\.venv\Scripts\Activate.ps1
python -m pytest
```

### Vue

`package.json` に定義されたスクリプトを確認してください。

```powershell
cd frontend
npm run
```

型チェックや Lint のスクリプトが存在する場合は実行します。

```powershell
npm run type-check
npm run lint
npm run build
```

## 開発環境の注意事項

* Spring Boot が停止している場合、業務データを取得できません。
* FastAPI が停止している場合、Statistics の分析結果を取得できません。
* Vite の Proxy 設定を変更した場合、Vue 開発サーバーを再起動してください。
* `.env`、データベースのパスワード、認証情報を Git にコミットしないでください。
* 本番環境では Vite 開発用 Proxy の代わりに、リバースプロキシまたは環境別 API 設定が必要です。

## 今後の予定

* 認証・認可
* API テストの拡充
* 分析結果の改善
* 外部バドミントンデータとの連携
* Docker による開発環境の統一
* クラウド環境へのデプロイ
