# ShuttleUp 運用・リリース

更新日：2026-10-14。現在のリポジトリと設定を基準とする。「現行仕様」「改善案」「決定待ち」を区別し、未実行の検証は成功として記載しない。

## 現在の運用状態

開発はローカルVue5173／Spring8080／FastAPI8000／MySQL3306。本番ホスト・ドメイン・TLS・サービス管理・監視・バックアップ保存先は未決定。Docker、CI/CD、管理されたDB移行は未導入。

現行prodはDB接続値が空欄、ddl-auto=update、show-sql=true。設定名と本番方針を明記し、配置先に合わせて外部上書きする。秘密値は文書に記載しない。

## 設定

| 対象 | 設定名 | 値／決定内容 | 状態 |
|---|---|---|---|
| 開発 | サービスPort | Vue5173／Spring8080／FastAPI8000／MySQL3306。起動スクリプトはポート競合時に中断。 | 確定 |
| Spring | SPRING_PROFILES_ACTIVE | 既定dev。本番はprodを外部指定。。devのみ初期マスター補完。prodは自動補完しない。 | 本番指定必須 |
| Spring | SPRING_DATASOURCE_URL／USERNAME／PASSWORD | 本番は外部提供。秘密値は記載しない。。prodファイルの接続値は空欄。実DBの文字セット／権限を確認。 | 本番決定待ち |
| Spring | SHUTTLEUP_JWT_SECRET | Base64、復号後32byte以上。未設定時は起動ごとにランダム鍵。多インスタンス／再起動を考慮し固定鍵を外部管理。 | 本番設定必須 |
| Spring | SPRING_JPA_HIBERNATE_DDL_AUTO | 現行dev／prod=update。本番は承認DDL反映後にvalidate等の運用へ外部上書きする案。自動移行ツールなし。 | 本番方針決定待ち |
| Spring | SPRING_JPA_SHOW_SQL | 現行true。本番はfalseを外部指定する案。ログ内容・保持方針を決定。 | 本番方針決定待ち |
| FastAPI | SPRING_API_BASE_URL | http://localhost:8080（開発例）。必須。/apiではなくSpringの基底URL。 | 確定 |
| FastAPI | SPRING_API_TIMEOUT_SECONDS | 10。HTTPクライアントの既定秒。負荷SLOではない。 | 確定 |
| FastAPI | CORS_ALLOWED_ORIGINS | ["http://localhost:5173"]（開発）。本番OriginとSpring側Originも合わせて決定。 | 本番決定待ち |
| Frontend | VITE_ANALYSIS_API_BASE_URL | 未指定なら相対URL経由。分析APIのbaseURL。設定時は/api/analysisとの二重付加を避ける。ビルド時に反映。 | 構成に合わせ設定 |
| 本番 | SPA配信とProxy | 配信先／URLは未決定。/api/analysis優先、/api→Spring。ページURL再読込はindex.htmlへ。 | 本番決定待ち |
| 本番 | 監視・バックアップ・保持 | 未構築／未承認。担当者、通知先、RTO/RPO、保管先、保留期間を決める。 | 本番決定待ち |

## 開発・リリース・ロールバック

| ID | 段階 | 作業 | 内容 | コマンド／検査 | 完了条件 |
|---|---|---|---|---|---|
| DEV-001 | 開発 | 前提確認 | JDK21、Node/npm、Python>=3.12、MySQL、依存導入。 | node --version／java -version／python --version。 | バージョンを記録。 |
| DEV-002 | 開発 | Frontend依存 | frontendでnpm ci（package-lock使用）。 | npm ci | 依存導入成功。 |
| DEV-003 | 開発 | Python依存 | python-analysisでvenv作成、実行依存を導入。 | python -m venv .venv<br>.venv\Scripts\python.exe -m pip install -e . | venv内に依存が揃う。 |
| DEV-004 | 開発 | Python設定 | 未作成時のみ.env.exampleを.envへコピーしSpring基底URL等を設定。 | Copy-Item .env.example .env（既存.envを上書きしない） | .envをGitに含めない。 |
| DEV-005 | 開発 | DB準備 | 開発専用shuttleupと開発利用者を準備。接続設定を外部上書きできる。 | MySQLで接続確認。dev=ddl-auto update。 | 開発DBのみ対象。 |
| DEV-006 | 開発 | 依存・ポート検査 | プロジェクトルートで依存・8080/8000/5173を確認。 | node scripts/dev.mjs --check | 依存・設定が揃い、対象ポートが空く。 |
| DEV-007 | 開発 | 一括起動 | MySQL起動後、プロジェクトルートで開始。 | start-dev.cmd --no-open | 3サービスの起動確認。ポート確認後に業務smokeも実施。 |
| DEV-008 | 開発 | 停止 | 起動したコンソールで停止。 | Ctrl+C または q＋Enter | この起動処理の子プロセスが停止。MySQLは別管理。 |
| REL-001 | 準備 | 変更・対象固定 | 変更番号、commit、担当、検証環境、前版資材と設定を記録。 | git status／git rev-parse HEAD | 対象差分と前版が特定できる。 |
| REL-002 | 検証 | 主要操作確認 | 自動テストソースは整理で削除済み。専用環境で認証・記録・日付重複・森林・分析を手動確認。 | docs/testing.mdの現行手動ケース | 実行したケースとwarningを記録。 |
| REL-003 | 検証 | Frontendビルド | 未使用importのTS6133を解消した後に再実行。 | frontend: npm run build | 型検査とVite buildが成功しdistを生成。 |
| REL-004 | 検証 | Backend資材 | packageでコンパイルとJAR作成。業務操作確認は別途実施。 | backend: mvnw.cmd package | JAR作成、ビルド結果と未実施の確認を記録。 |
| REL-005 | 検証 | Python資材 | ソースcommit、pyproject依存、利用環境を固定。 | python -c "from app.main import app"<br>実環境の依存一覧とPython版を記録。 | 再構築可能な資材・依存が確認できる。 |
| REL-006 | DB | DDL差分検査 | 現DBのSHOW CREATE TABLEを取得しEntity・テーブル定義と照合。 | 列追加・制約・既存値・索引を審査。 | 稼働DBに無い列を重複追加しない。 |
| REL-007 | DB | バックアップ | 復旧可能なDB／設定／前版資材を退避。 | バックアップ・復旧シートのBKP-001～005。 | 別DBで復元検証済み。 |
| REL-008 | 設定 | 本番設定の確定 | prod、外部DB、固定JWT鍵、TLS、Origin、DDL方針、ログを用意。 | 秘密値は環境変数／秘密管理から提供。 | prod空欄を解消。update／SQL表示を本番方針に合わせて上書き。 |
| REL-009 | 切替 | 段階起動・配信 | 承認DDL→Spring→FastAPI→静的dist／Proxyの順で切替。 | Spring例: java -jar target/backend-0.0.1-SNAPSHOT.jar<br>Python例: python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 | 実際のサービス管理・ホスト・URLは環境決定後に具体化。 |
| REL-010 | 確認 | Smoketest | 専用テストユーザーで登録／ログイン／記録／試合／Dashboard／分析を確認。 | TC-001／003／014／026／031／037。深いURLの再読込も確認。 | 対象機能とAPI分岐・SPA fallback正常。 |
| REL-011 | 確認 | 稼働監視 | API失敗率・時間・プロセス・DB接続・分析ログを確認。 | 監視の基準・観察時間・通知先は決定後に記入。 | 重大異常なし。判断者・時刻・結果を記録。 |
| REL-012 | 完了 | リリース記録 | commit、資材hash、設定差分、DB変更、テスト結果、実施者を記録。 | 作業結果欄を更新。 | 障害時に前後版・変更が追跡可能。 |
| RBK-001 | ロールバック | 切替中止判断 | 重大異常・復旧条件未達なら新規書込を停止し責任者へ判断を依頼。 | 障害の影響、DB変更の有無、戻せる範囲を記録。 | 追加データ喪失を避け、復旧方式を決定。 |
| RBK-002 | ロールバック | 旧版へ戻す | DBが互換なら前版資材・設定へ。非互換なら検証済み復旧手順に従う。 | フロント・Spring・FastAPIを対応する版へ揃える。 | JWT鍵が同じでもversion・DB状態を確認。 |
| RBK-003 | ロールバック | 復旧確認 | 件数・FK・ログイン・CRUD・分析を再確認。 | TC-054とsmoke、復旧時刻・差分を記録。 | データ整合性・サービスを確認後に再開。 |

リリースに使う資材は同じcommitのFrontend／Spring／Pythonで揃える。prodではdevのプリセット補完が動かない。必要なマスターの導入と実DDL差分を別途確認する。静的配信ではページ再読込のindex.html fallback、/api/analysis優先分岐を確認する。

## 本番の起動例

本番のサービス管理方法は配置先決定後に具体化する。以下は配信設定が済んだ環境の起動形だけを示す。環境変数SPRING_PROFILES_ACTIVE=prod、DB設定とJWT鍵は外部で提供する。

```powershell
# backend（承認DDL反映・設定投入後）
java -jar target/backend-0.0.1-SNAPSHOT.jar
# python-analysis（必要依存・環境変数投入後）
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```

本番でVite dev／uvicorn --reloadを運用しない。静的distを配信し、SpringとFastAPIは決定したサービス管理方式で起動する。

## バックアップ・復旧

| ID | 段階 | 手順 | 注意 |
|---|---|---|---|
| BKP-001 | 計画 | バックアップ周期・保存期間・RPO/RTO・保管先・暗号化・担当者を決定。 | 未承認値を実施済みにしない。 |
| BKP-002 | 整合した退避 | MySQL用の最小権限アカウントとlogin-path等を準備。DDL同時変更を避ける。 | InnoDBでsingle-transactionを用いる例。環境に合わせ権限を確認。 |
| BKP-003 | DBダンプ例 | mysqldump --login-path=shuttleup-backup --single-transaction --routines --triggers --events --hex-blob --no-tablespaces --result-file=<退避先.sql> shuttleup | 秘密値を引数に書かない。PowerShellの文字コード変換を避けresult-fileを使用。 |
| BKP-004 | 資材・設定 | 前版dist・JAR・Pythonソース／依存一覧・設定項目名・資材hashを退避。 | 秘密設定そのものは指定の秘密保管先で管理。 |
| BKP-005 | 復元検証 | 別の復旧専用DBを作成し、mysqlクライアント内でSOURCE <退避先.sql>; を実行。 | 本番DBへ直接上書きしない。ダンプ内USE文／対象DBを先に確認。 |
| BKP-006 | データ検査 | 8表の件数、ID、FK、email一意、本人所属、植物、JWTversionを照合。 | SHOW CREATE TABLE、件数集計、代表的なCRUD／分析を確認。 |
| BKP-007 | 復旧の実施 | 作業承認後に書込停止、影響・失う可能性のある更新・復旧先を記録して復元。 | 復旧演習と実障害作業を分離。ログと時刻を残す。 |
| BKP-008 | 復旧後 | Spring→FastAPI→配信を戻し、smokeと監視を確認。RTO/RPO実績を記録。 | 原因・再発防止・バックアップ周期を見直す。 |

DB復旧例は復旧専用環境で試してから利用する。本書作成時にDBダンプ・リストア・本番切替は実施していない。

## 障害切分け

| ID | 現象 | 候補 | 確認 | 対応 |
|---|---|---|---|---|
| INC-001 | 全画面でAPI失敗 | Spring停止／Proxy分岐／DB接続 | 8080プロセス・API応答・Springログ・DB到達性。 | 環境設定と対象サービスを復旧。秘密値を共有ログに出さない。 |
| INC-002 | Statisticsだけ失敗 | FastAPI停止／Spring向けURL／15秒timeout | 8000・SPRING_API_BASE_URL・request_idの分析ログ。 | 503は接続／Spring5xx、504はtimeout、502は応答形式を切分け。 |
| INC-003 | ログインに戻る | JWT期限・鍵変更・DISABLED・version更新 | exp・状態・version・再起動履歴を確認。JWT本文そのものをログへ貼らない。 | 再ログイン。鍵未設定の再起動は全旧JWTが無効。本番鍵運用を確認。 |
| INC-004 | 他人データ操作403 | 所有者不一致／共通種目変更 | 利用者と対象資源の所属を確認。 | 権限を迂回せず正しい本人資源を指定。 |
| INC-005 | 削除409 | 子明細・試合／使用中相手／DB制約 | ServiceとDB FKを確認。409文言だけで削除原因と断定しない。 | 対象関連を確認し、仕様に沿って子を先に削除するか操作中断。 |
| INC-006 | 森の表示不足 | 未来日／月フィルター／区画／日付変更 | 端末当日、trainingDate、期間、区画を確認。 | 再読込。未来日や旧NULL植物の仕様を照合。 |
| INC-007 | フロントbuild失敗 | TS6133等の型検査 | npm run buildのエラー対象・行を確認。 | 現状はGlobalOutlined／BellOutlinedの使用または削除を判断して再検証。 |
| INC-008 | DB schema不一致 | 列／制約不足・手動移行漏れ | 実DDLとEntity・テーブル定義の差分。 | 本番でupdateを無条件実行せず、承認DDL・退避・復元検証を行う。 |
| INC-009 | 開発起動失敗 | 依存不足／既存ポート／Python.env | node scripts/dev.mjs --checkと起動ログ。 | 当該サービスだけ原因解消。既存の無関係なプロセスを停止しない。 |

## リリース前に確定するもの

担当者／承認者／連絡先、配置先とURL、TLSとOrigin、最小権限DB利用者、固定JWT鍵保管、SLOとRTO/RPO、バックアップ周期・保持期間、監視通知先、DDL移行／ロールバック方法を決める。現在のbuild TS6133を解消し、実DB・画面smokeの証跡を用意する。

[運用・リリース手順書](仕様書/運用/運用・リリース手順書.xlsx)、[テスト](testing.md)、[DB](database-design.md)、[要件](requirements.md)。
