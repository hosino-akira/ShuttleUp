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
