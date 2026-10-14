# ShuttleUp API 設計

更新日：2026-10-14。現在のリポジトリと設定を基準とする。「現行仕様」「改善案」「決定待ち」を区別し、未実行の検証は成功として記載しない。

## 契約

業務APIはSpring Boot、分析APIはFastAPI。JSON UTF-8、Bearer認証。開発時はViteの/apiを利用し、/api/analysisをFastAPIへ優先転送する。通常GET/PUT=200、POST作成=201、DELETE=204。ログインPOST=200、パスワードPUT=204。DTOで受入・応答しEntityを直接公開しない。

## 認証・認可

公開業務APIはPOST /api/auth/registerと/login。JWTはHS256、issuer=shuttleup、sub=本人ID、有効86400秒、tokenVersionを含む。ACTIVEと署名・期限・versionをSpringが検証。パスワードはBCryptで保存。登録・変更する新パスワードは6文字以上・UTF-8で72byte以内。変更後は旧JWTを失効。ログアウトは現在タブのトークン消去。FastAPIはBearerを要求し、共有クライアントの要求ごとに同じAuthorizationをSpringへ転送する。

## エンドポイント一覧

| ID | 機能 | 処理 | Method | Path | 成功 |
|---|---|---|---|---|---|
| TS-001 | TrainingSession | ユーザー別トレーニング一覧取得 | GET | /api/training-sessions/users/{userId} | 200 OK |
| TS-002 | TrainingSession | トレーニング詳細取得 | GET | /api/training-sessions/{id} | 200 OK |
| TS-003 | TrainingSession | トレーニング新規登録 | POST | /api/training-sessions | 201 Created |
| TS-004 | TrainingSession | トレーニング更新 | PUT | /api/training-sessions/{id} | 200 OK |
| TS-005 | TrainingSession | トレーニング削除 | DELETE | /api/training-sessions/{id} | 204 No Content |
| TR-001 | TrainingRecord | トレーニング明細一覧取得 | GET | /api/training-sessions/{sessionId}/records | 200 OK |
| TR-002 | TrainingRecord | トレーニング明細詳細取得 | GET | /api/training-records/{recordId} | 200 OK |
| TR-003 | TrainingRecord | トレーニング明細新規登録 | POST | /api/training-sessions/{sessionId}/records | 201 Created |
| TR-004 | TrainingRecord | トレーニング明細更新 | PUT | /api/training-records/{recordId} | 200 OK |
| TR-005 | TrainingRecord | トレーニング明細削除 | DELETE | /api/training-records/{recordId} | 204 No Content |
| EX-001 | Exercise | 利用可能種目一覧取得 | GET | /api/exercises | 200 OK |
| EX-002 | Exercise | 種目詳細取得 | GET | /api/exercises/{id} | 200 OK |
| EX-003 | Exercise | ユーザー種目新規登録 | POST | /api/exercises | 201 Created |
| EX-004 | Exercise | ユーザー種目更新 | PUT | /api/exercises/{id} | 200 OK |
| EX-005 | Exercise | ユーザー種目削除 | DELETE | /api/exercises/{id} | 204 No Content |
| AU-001 | Auth | ログイン | POST | /api/auth/login | 200 OK |
| AU-002 | Auth | アカウント登録 | POST | /api/auth/register | 201 Created |
| AU-003 | Auth | 本人プロフィール取得 | GET | /api/auth/me | 200 OK |
| AU-004 | Auth | 本人プロフィール更新 | PUT | /api/auth/me | 200 OK |
| AU-005 | Auth | 本人パスワード変更 | PUT | /api/auth/me/password | 204 No Content |
| MS-001 | ExerciseMaster | 大分類一覧取得 | GET | /api/exercise-categories | 200 OK |
| MS-002 | ExerciseMaster | 中分類一覧取得 | GET | /api/exercise-types | 200 OK |
| MT-001 | Match | セッション別試合一覧取得 | GET | /api/training-sessions/{sessionId}/matches | 200 OK |
| MT-002 | Match | 試合詳細取得 | GET | /api/matches/{matchId} | 200 OK |
| MT-003 | Match | 試合新規登録 | POST | /api/training-sessions/{sessionId}/matches | 201 Created |
| MT-004 | Match | 試合更新 | PUT | /api/matches/{matchId} | 200 OK |
| MT-005 | Match | 試合削除 | DELETE | /api/matches/{matchId} | 204 No Content |
| OP-001 | Opponent | ユーザー別対戦相手一覧取得 | GET | /api/users/{userId}/opponents | 200 OK |
| OP-002 | Opponent | 対戦相手詳細取得 | GET | /api/opponents/{opponentId} | 200 OK |
| OP-003 | Opponent | 対戦相手新規登録 | POST | /api/users/{userId}/opponents | 201 Created |
| OP-004 | Opponent | 対戦相手更新 | PUT | /api/opponents/{opponentId} | 200 OK |
| OP-005 | Opponent | 対戦相手削除 | DELETE | /api/opponents/{opponentId} | 204 No Content |
| DB-001 | Dashboard | Dashboard集計取得 | GET | /api/dashboard/users/{userId} | 200 OK |
| AD-001 | AnalysisData | 分析用データ一括取得 | GET | /api/analysis-data/users/{userId} | 200 OK |
| AN-001 | Analysis | トレーニング詳細分析 | GET | /api/analysis/users/{user_id}/training-report | 200 OK |

## 呼出例

```powershell
$headers = @{ Authorization = 'Bearer <本人のJWT>' }
Invoke-RestMethod -Uri 'http://localhost:8080/api/training-sessions/users/<本人ID>' -Headers $headers
Invoke-RestMethod -Uri 'http://localhost:8000/api/analysis/users/<本人ID>/training-report?from=2026-10-01&to=2026-10-14' -Headers $headers
```

例のIDとJWTは専用テストユーザーの実値に置換する。パスワード・Bearer値を文書やログへ保存しない。項目・Request/Response例は[API仕様書](仕様書/ID書/API仕様書.xlsx)を参照。

## エラー・更新・集計

Spring業務例外の本文はmessage。400入力、401認証、403権限、404不存在、409重複／使用中／DB制約。型不正・必須Queryなどは標準形式になる場合がある。FastAPIはdetail形式、日付検証422、上流不正応答502、接続／上流5xx503、timeout504。

TS-003／TS-004は同一ユーザー・同一trainingDateの重複を409で拒否する（1日1セッション）。更新対象自身は除外し、別ユーザーの同日登録は許可。並行保存もDB複合UKで拒否する。応答codeはTRAINING_DATE_ALREADY_EXISTS、messageは「この日付のトレーニングは既に登録されています。1日に1件のみ登録できます。」。画面は保存前にも本人の一覧で日付重複を確認し、選択言語の持続するエラー表示と日付欄の強調で通知して入力を保持する。旧応答のmessageのみの形式も認識する。

PUTはRequest DTOの必要項目を送る。任意値の省略はnullとして反映し得るがplantTypeだけは更新省略時保持。PATCHはない。空一覧[]・集計0・未算出率nullを区別する。Dashboardは欠損得点を件数分母に含め、Pythonは除外する。

## 現行の未検証条件

明細数値の範囲・成功≤試行、種目名／セッション・明細メモのAPI長さに不足がある。videoUrlは最大2048文字だがURL形式のAPI検証なし。改善時は要件・CHK・API・TCを同時改訂する。

根拠：Controller、Request/Response DTO、Service、python-analysis/app/api/reports.py・clients/spring_api.py。参照：[要件](requirements.md)、[試験](testing.md)。
