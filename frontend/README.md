# ShuttleUp フロントエンド

ShuttleUp の画面表示とユーザー操作を担当する Vue 3 アプリケーションです。

Spring Boot API からトレーニング・試合データを取得し、Python FastAPI から詳細な分析結果を取得します。

## 主な機能

* トレーニング履歴の一覧表示
* トレーニングセッションの登録・編集・削除
* トレーニング記録の登録・編集・削除
* 大分類・中分類・種目の連動選択
* 試合結果の登録・編集・削除
* 対戦相手の管理
* Dashboard による基本統計の表示
* Statistics 画面による詳細分析の表示
* PC・タブレット・スマートフォンへのレスポンシブ対応

## 表示言語

ログイン・登録画面と画面右上の言語セレクターで、English・中文（簡体字）・日本語を切り替えられます。初期言語は日本語です。選択はブラウザーの `localStorage`（`shuttleup.language`）に保存し、再読み込み後も維持します。

UI 文言、入力エラー、日付・時間の表示、Ant Design Vue と VXE の言語が切り替わります。入力中のフォームや選択中の条件は保持します。API が返す種目名・対戦相手名・メモ・分析説明・サーバー独自のエラー文言は原文を表示します。

翻訳を追加する場合は `src/i18n/messages.ts` に日本語のキーと英語・中国語を登録し、画面から `t('日本語のキー')` を呼びます。変数を含む文は `t('例：{name}', { name })` とします。選択肢やグラフの定義は `computed` 内で翻訳し、切り替えを反映してください。

## 使用技術

* Vue 3
* TypeScript
* Vite
* Vue Router
* Pinia
* Axios
* Ant Design Vue
* VXE-Table
* ECharts

## 動作環境

* Node.js
* npm
* Spring Boot：`http://localhost:8080`
* FastAPI：`http://localhost:8000`

Node.js の推奨バージョンは `package.json` またはプロジェクトの設定に従ってください。

## セットアップ

```powershell
cd frontend
npm install
```

## 開発サーバーの起動

Spring Boot と FastAPI を起動した後、次を実行します。

```powershell
cd frontend
npm run dev
```

## ビルド

```powershell
npm run build
```

生成されたファイルは通常 `dist` ディレクトリに出力されます。

## プレビュー

```powershell
npm run preview
```

## 注意事項

* Spring Boot が停止している場合、通常の業務データを取得できません。
* FastAPI が停止している場合、Statistics 画面の分析結果を取得できません。
* `vite.config.ts` を変更した場合は、Vite 開発サーバーを再起動してください。
* 本番環境では Vite の開発用 Proxy は使用されないため、リバースプロキシまたは本番用 API URL の設定が必要です。
