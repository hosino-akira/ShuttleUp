# ShuttleUp トレーニング分析サービス

更新日：2026-10-14。FastAPIがSpring BootからHTTPで本人の期間データを取得し、pandasで分析する。MySQLへ直接接続・業務データ永続化はしない。

## セットアップ

Python>=3.12。

```powershell
cd python-analysis
python -m venv .venv
.\.venv\Scripts\python.exe -m pip install -e .
# .envが未作成の場合のみコピー
Copy-Item .env.example .env
```

必須SPRING_API_BASE_URL、既定SPRING_API_TIMEOUT_SECONDS=10、CORS_ALLOWED_ORIGINS=["http://localhost:5173"]。実.envはGitに含めない。本番Originは配置先で決定。

## 起動・API

```powershell
.\.venv\Scripts\python.exe -m uvicorn app.main:app --reload --port 8000
$headers = @{Authorization='Bearer <本人のJWT>'}
Invoke-RestMethod -Uri 'http://localhost:8000/api/analysis/users/<本人ID>/training-report?from=2026-10-01&to=2026-10-14' -Headers $headers
```

GET /api/analysis/users/{user_id}/training-report、必須from/to。Swaggerはhttp://localhost:8000/docs。分析APIはBearer必須。同じトークンをSpring AD-001へ要求単位で転送し、Springに署名・本人所属検証を委ねる。共有クライアントに利用者トークンを固定しない。

## 計算・エラー

実施日は重複排除、週は月曜開始、現在連続はto起点（to未実施なら0）。重量は同日最大・最新−初回、初回0の率はnull。欠損／同点試合は除外と警告。カテゴリはNFKC正規化、複数一致OTHERと警告。時間未入力の明細へセッション時間を配分しない。

未認証401、権限403、不存在404、日付／入力422、上流形式502、接続／上流5xx503、timeout504。空データは200で構造を保持。

## 動作確認と制限

```powershell
.\.venv\Scripts\python.exe -c "from app.main import app; print('FastAPI import OK')"
```

2026-10-14の整理でtests、テスト専用依存、pytest設定と生成egg-infoを削除。起動後は認証・期間・空データ・集計結果・上流エラーを専用データで手動確認する。複数ゲーム制・機械学習・文章生成・DB直接接続は提供しない。

[API仕様](../docs/api-design.md)、[構成](../docs/architecture.md)、[試験](../docs/testing.md)、[運用](../docs/deployment.md)。
