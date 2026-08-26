# ShuttleUp トレーニング分析サービス

Spring Bootが管理する業務データをHTTPで取得し、pandasで期間分析するFastAPIサービスです。MySQLへの直接接続や業務データの永続化は行いません。Spring Bootはデータ管理・所有者確認を、FastAPIは分析計算を担当します。

## 動作環境とセットアップ

Python 3.12以上が必要です。

```bash
cd python-analysis
python -m venv .venv
.venv\Scripts\activate
pip install -e ".[test]"
copy .env.example .env
```

環境変数は必須の`SPRING_API_BASE_URL`と、`SPRING_API_TIMEOUT_SECONDS`（既定値10秒）です。実際の`.env`はGit管理対象外です。

## 起動と利用

Spring Bootを起動後、次を実行します。

```bash
cd python-analysis
.\.venv\Scripts\Activate.ps1
python -m uvicorn app.main:app --reload --port 8000
```

Swagger UIは`http://localhost:8000/docs`です。

```bash
curl.exe "http://localhost:8000/api/analysis/users/1/training-report?from=2026-08-01&to=2026-08-31"
```
## テスト

```bash
cd C:\Users\27357\ShuttleUp\python-analysis
.\.venv\Scripts\Activate.ps1
python -m pytest
```

HTTPクライアントテストはモックを使用するため、Spring Bootを起動する必要はありません。

## Category分類

カテゴリ名をNFKC正規化し、空白除去・大文字小文字の正規化後に専用キーワードと照合します。フィジカル系、バドミントン技術系、OTHERへ分類します。複数分類に一致したカテゴリはOTHERとして警告へ追加します。OTHERのカテゴリ名は`unclassifiedCategories`で確認できます。

## 現時点の制限

- 複数ゲーム制、機械学習、文章生成には対応していません。
- 認証はSpring Boot側に現時点で導入されていないため引き継ぐ認証ヘッダーはありません。
- `currentTrainingStreakDays`は期間終了日に練習がなければ0です。
- 時間未入力のRecordへSession時間を配分しません。
