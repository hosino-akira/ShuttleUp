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

### 一括起動（Windows）

MySQL を起動した後、プロジェクトルートの `start-dev.cmd` をダブルクリックしてください。
Spring Boot、FastAPI、Vue を起動し、三つの起動を確認した後にブラウザーを開きます。
ターミナルからは次の一つのコマンドでも起動できます。

```powershell
.\start-dev.cmd
```

* 各サービスのログは同じターミナルにサービス名付きで表示します。
* 停止するときは、そのターミナルで `Ctrl+C` または `q` と Enter を押してください。
* Python は `.venv` 内の実行ファイルを直接使うため、仮想環境の有効化は不要です。
* 事前にフロントエンド・Python の依存パッケージをインストールし、Python の `.env` を設定してください。
* ポート `8080`、`8000`、`5173` が使用中の場合は、既存のサーバーを停止してから実行してください。
* 一つのサービスが終了・起動失敗した場合、このスクリプトで起動した他のサービスも停止します。
* 実行環境の確認だけを行う場合は `node scripts/dev.mjs --check` を実行してください。
* ブラウザーを自動で開かない場合は `.\start-dev.cmd --no-open` を使用してください。

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

### アカウント登録

`/login` の「新規登録」から `/register` を開けます。
ユーザー名、メールアドレス、パスワード、確認用パスワードを入力してください。
パスワードは6文字以上、UTF-8で72バイト以内です。

* `POST /api/auth/register`：ユーザー名、メールアドレス、パスワードを送信します。
* 登録成功時は `201` とユーザーの基本情報を返します。パスワードやハッシュ値は返しません。
* 入力エラーは `400`、メールアドレスの重複は `409` です。
* メールアドレスは小文字に統一し、パスワードは BCrypt でハッシュ化して保存します。
* 登録後はメールアドレスを入力済みのログイン画面に移動します。
* ログインとプロフィールは以下の認証 API を利用します。

### ログインとプロフィール

* `POST /api/auth/login`：メールアドレスとパスワードを BCrypt の `matches` で照合し、JWT とユーザー情報を返します。
* `GET /api/auth/me`：JWT のユーザーIDに対応するプロフィールを取得します。
* `PUT /api/auth/me`：本人のユーザー名とメールアドレスを変更します。パスワードとアカウント状態は変更できません。
* `PUT /api/auth/me/password`：現在のパスワード、新しいパスワード、確認用パスワードを送信します。成功時は `204` を返します。
* パスワード変更は現在のパスワードを BCrypt の `matches` で確認し、新しいパスワードを `encode` で保存します。6文字以上かつUTF-8で72バイト以内が必要です。同じパスワードへの変更はできません。
* 変更後は `users.token_version` を増やし、変更前に発行したすべての JWT を失効させます。新しいパスワードで再ログインしてください。
* 開発環境の `ddl-auto=update` では、バックエンド再起動時に `token_version` 列（初期値0）が追加されます。既存パスワードは変更しません。
* ログイン成功後は Dashboard に移動します。右上のユーザーアイコンからプロフィールを開けます。
* JWT は1時間有効です。フロントエンドはタブの `sessionStorage` に保存し、API 呼び出し時に `Authorization: Bearer ...` を送信します。
* ページを再読み込みすると `/api/auth/me` でユーザー情報を復元します。未ログインや期限切れの場合はログイン画面に戻ります。
* Spring Security は登録・ログイン以外の API を保護します。各ユーザーは本人の記録と共通の種目のみ利用できます。
* FastAPI は受け取った JWT を Spring Boot に転送し、認証とデータの所属ユーザーの確認を委ねます。
* プロフィールからログアウトすると、このタブの JWT とユーザー情報を削除します。発行済み JWT のサーバー側失効はまだ実装していません。
* 開発環境で `SHUTTLEUP_JWT_SECRET` が未設定の場合、署名鍵は起動ごとに生成されるため、バックエンド再起動後は再ログインが必要です。固定する場合は32バイト以上のランダムな鍵を Base64 で環境変数に設定してください。
* 既存ユーザーのパスワードハッシュが未設定の場合はログインできません。事前に作成した Java Scratch File で生成したハッシュを設定してください。

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

実際の MySQL を使わず、認証と業務ロジックの回帰テストのみ実行する場合：

```powershell
.\mvnw.cmd '-Dtest=!BackendApplicationTests' test
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

* JWT の更新・ログアウト時のサーバー側失効
* API テストの拡充
* 分析結果の改善
* 外部バドミントンデータとの連携
* Docker による開発環境の統一
* クラウド環境へのデプロイ
