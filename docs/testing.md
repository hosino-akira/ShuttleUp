# ShuttleUp テスト計画・実行記録

更新日：2026-10-14。現在のリポジトリと設定を基準とする。「現行仕様」「改善案」「決定待ち」を区別し、未実行の検証は成功として記載しない。

整理後の現行検証：Frontend production build成功、Backend package成功（自動テストなし）、Python 20モジュールの構文とpyprojectのTOML検証・FastAPI import成功。削除したテストを成功扱いにせず、以下の既存自動実行記録は整理前の履歴として保持する。

## 対象とレベル

2026-10-14の整理でbackend/src/test、frontend/tests、python-analysis/tests、手工APIテスト、機械可読の実行記録とテスト専用依存を削除した。この文書の自動試験件数・ファイル一覧は削除前の実行履歴で、現在のcheckoutで再実行できるものではない。現行の検証はビルドと以下の手動ケースを基準とし、未実施の結合試験を合格として記載しない。

## 再実行

```powershell
cd frontend
npm run build
# プロジェクトルートへ戻ってからbackendへ
cd ../backend
.\mvnw.cmd package
cd ../python-analysis
.\.venv\Scripts\python.exe -c "from app.main import app; print('FastAPI import OK')"
```

これらのビルド・import確認は業務回帰テストを代替しない。実DBを変更する操作確認は専用検証環境で実施する。

## 2026-10-14の実行結果

| 検証 | 件数 | 判定 | 備考 |
|---|---|---|---|
| Frontend自動 | 41 | 成功 | 6ファイル。UI描画の実ブラウザーE2Eではない。 |
| Python自動 | 22 | 成功 | 22項（パラメータ化を含む）。TestClientの依存廃止予告warning1件。 |
| Java回帰 | 65 | 成功 | コピーした既存Maven依存でオフライン実行。実DB contextLoadsを除外。 |
| Frontendビルド | 0 | 失敗 | TS6133：LanguageSwitcher.vue GlobalOutlined、MainLayout.vue BellOutlined。 |
| 実DB・画面・非機能 | 0 | 未実行 | 専用検証環境で実施後、TCごとの実測結果・判定・証跡を入力。 |

自動回帰は合計128項。フロントエンドビルドはLanguageSwitcher.vueのGlobalOutlined、MainLayout.vueのBellOutlinedの未使用importによるTS6133で失敗。既存ソースの修正と再実行がリリース前提。PythonはTestClient依存の廃止予告warning1件。Javaは既存Mavenキャッシュを作業用領域にコピーし、offlineで回帰のみ実行した。

## 結合・画面・非機能ケース

| ID | 要件 | 対象 | 観点 | 期待結果 |
|---|---|---|---|---|
| TC-001 | FR-001 | SC-002 | 新規登録 | 201、email正規化。ログインへ遷移。応答にpasswordHashなし。 |
| TC-002 | FR-001 | SC-002 | 重複email | 409。ユーザー増加なし。入力画面を保持。 |
| TC-003 | FR-002 | SC-001 | ログイン成功 | 200。JWT expiresIn=86400、sub=本人ID。Dashboardへ。 |
| TC-004 | FR-002 | SC-001 | 認証失敗・無効アカウント | 保護画面へ入れない。誤認証は401、無効状態は403。 |
| TC-005 | FR-002 | SC-001 | 安全なredirect | 安全な内部対象だけ採用。不正値ならDashboard。 |
| TC-006 | FR-003 | SC-013 | 再読込・期限切れ | 有効なら本人状態復元。失効ならログインへ、redirect保持。 |
| TC-007 | FR-003／NF-011 | 全API | 他人の資源拒否 | 本人外の操作403。Bの内容・件数が変わらない。 |
| TC-008 | FR-004 | SC-012 | ログアウト | 現在タブの保存JWTを消去、ログインへ。サーバーlogoutを呼ばない。 |
| TC-009 | FR-005 | SC-012 | 本人情報更新 | 通常200、重複409。パスワード・statusは変更しない。 |
| TC-010 | FR-006 | SC-012 | パスワード変更と失効 | 204、tokenVersion増加、旧JWT拒否。email付きログインへ。 |
| TC-011 | FR-006 | SC-012 | パスワード境界・不一致 | 400、hash／version不変。正常境界値6文字・72byte以内は許可。 |
| TC-012 | FR-007 | SC-004 | 履歴から詳細 | 対象sessionIdの詳細URLへ。明細・試合・相手も取得。 |
| TC-013 | FR-007 | SC-005 | 不正ID・取得失敗 | エラーを通知し履歴へreplace。 |
| TC-014 | FR-008 | SC-005 | 新規保存・採番 | 201、同じSC-005の詳細URLへreplace。明細・試合登録が可能。本人の同日追加・並行登録は409、他人の同日は許可。重複時は入力保持・翻訳済み通知。 |
| TC-015 | FR-008 | SC-005 | 入力境界 | 時間0・feeling0／6・不正植物400。省略plantはcedar。 |
| TC-016 | FR-009 | SC-005 | 更新時の植物保持・日付重複 | 省略時oak保持。変更後sakura。同一ID、株増加なし。同日維持・未使用日へ変更は許可、本人の他セッションと重複する日付は409（並行更新も含む）。 |
| TC-017 | FR-010 | SC-004 | 子なしセッション削除 | 204、一覧から消え、森の対応株も次の読込で消える。 |
| TC-018 | FR-010／NF-011 | SC-004 | 子ありセッション削除 | 自動で子を消さない。実DDLのFKにより409。整合性を確認。 |
| TC-019 | FR-011 | SC-006 | 明細登録・更新 | 登録201・更新200。モーダルを閉じ一覧再取得。 |
| TC-020 | FR-011 | SC-006 | 明細削除 | 204、閉じて一覧更新。基本情報は維持。 |
| TC-021 | FR-011 | SC-006 | 未保存基本情報と独立保存 | 保存済み明細は維持。基本情報だけ破棄。 |
| TC-022 | FR-012 | SC-006 | 親未選択の分類選択 | 全候補から選択可能。互換選択保持、不一致の子だけ解除。 |
| TC-023 | FR-012 | SC-006／SC-007 | 候補取得の競合 | 古い応答が新状態を上書きしない。失敗後再開で再取得。 |
| TC-024 | FR-013 | SC-007 | 独自種目登録 | 本人所有、systemPreset=false、201。SC-006に戻り新種目選択。 |
| TC-025 | FR-013／NF-011 | 種目API | 共通／他人種目の変更拒否 | 共通更新削除403。他人独自種目使用・変更拒否。 |
| TC-026 | FR-014 | SC-008 | 試合登録・編集・削除 | 201／200／204。成功後に閉じ一覧更新。日付は親日付。 |
| TC-027 | FR-014 | SC-008 | 得点欠損・同点 | 欠損は許可。同点・負数は400。 |
| TC-028 | FR-014 | SC-008 | 動画URLの長さ | 2048以内許可、2049は400。URL形式のAPI検証は現行なし。 |
| TC-029 | FR-015 | SC-009 | 相手登録と同名拒否 | A内同名409、別ユーザーは許可。保存後もDrawerに留まる。 |
| TC-030 | FR-015 | SC-009 | 使用中相手の削除 | 使用中409、未使用204。試合の参照整合性維持。 |
| TC-031 | FR-016 | SC-003 | 全体集計と個別絞込 | 全体summaryは同じ。個別グラフ・相手別結果だけ変更。 |
| TC-032 | FR-016／FR-019 | SC-003／SC-011 | 勝率分母の違い | Dashboard33.3%、Python50.0%。欠損試合の扱いを説明。 |
| TC-033 | FR-017 | SC-010 | 未来日・植物フォールバック | 昨日・当日だけ表示。旧NULLはcedar。明日になった読込で未来分表示。 |
| TC-034 | FR-017 | SC-010 | 月絞込・36株区画 | 月別に絞込、36株ずつ。初期表示・月変更後は最新日付を含む最後の区画。82株は区画3に10株、72株は区画2に36株、空なら区画1。 |
| TC-035 | FR-017／FR-019 | 森林／分析 | 連続日数の差 | 森3日、分析0日。異なる起点を維持。 |
| TC-036 | FR-018 | SC-010→SC-005 | 森林の新規／思い出／強調 | plantを初期値へ。思い出から正しいID、plantedで対象区画を表示。 |
| TC-037 | FR-019 | SC-011 | 分析空データ・期間 | 空データ200、from=to許可。逆転・欠落422。 |
| TC-038 | FR-019 | SC-011 | 重量改善 | NULL除外。初回0なら改善率null。自己ベストは期間内。 |
| TC-039 | FR-019 | SC-011 | カテゴリ分類・比率 | NFKC分類。複数一致OTHER＋警告。時間0なら時間比率null。 |
| TC-040 | FR-019／NF-001 | 分析API | 認証転送と通信失敗 | トークン混入なし。401／403を維持、500→503、timeout→504、不正応答502。 |
| TC-041 | FR-020 | SC-005／SC-006／SC-008 | 未保存の離脱確認 | キャンセルなら入力保持。許可なら破棄。未変更では確認不要。 |
| TC-042 | FR-003 | SC-001／SC-013 | 古い401と新ログイン | 旧401で新ログインを消さない。現JWTの401は一度だけログインへ。 |
| TC-043 | FR-021 | 全画面 | 5テーマ切替・保存不可 | カード・表・グラフ・森が同期。forest初期、保存不可でも適用。 |
| TC-044 | FR-022 | 全画面 | 3言語と入力保持 | UI・日付・ライブラリ文言を変更。名前・メモ・入力・条件を保持。 |
| TC-045 | FR-023 | Router | 未定義URL | 認証済みDashboard、未認証はログインへ。 |
| TC-046 | FR-023 | 全画面 | 空データ・再送抑止 | 空状態正常、二重送信抑止、取得失敗と再試行を表示。 |
| TC-047 | FR-023 | SC-005／SC-006／SC-008 | 保存失敗時の保持 | 現在画面を保持し内容・選択値を消さない。 |
| TC-048 | NF-006 | 通信 | タイムアウト | UI失敗表示と適切な504／503。性能SLO達成とは判定しない。 |
| TC-049 | NF-012 | ブラウザー | 対応ブラウザー確認 | 対象ごとの表示・入力・ルート再読込が正常。 |
| TC-050 | NF-013 | レイアウト | レスポンシブ確認 | 横はみ出し・文字切れ・操作不能がない。 |
| TC-051 | NF-004／NF-005 | 本番構成 | 公開・秘密設定 | 証明書、秘密値の外部提供、最小権限、適切なCORS。 |
| TC-052 | NF-007／NF-008 | 性能 | 負荷・最大データ | p95・エラー率・メモリ等を記録し、承認目標と比較。 |
| TC-053 | NF-018 | リリース | ビルド・自動回帰 | 全対象成功。構築失敗・除外テストは明記。 |
| TC-054 | NF-009／NF-010／NF-019 | 復旧 | バックアップ復元・ロールバック | 件数・FK・認証・記録の整合性が一致。RTO/RPOの測定記録。 |
| TC-055 | NF-016 | 操作性 | キーボード・ラベル | フォーカス可視、ラベル読上げ、操作到達性を確認。 |
| TC-056 | NF-017 | 監視 | ログ・障害通知 | request_id・時間を追跡。秘密値なし。通知は構築後に検証。 |

TC-053はCLI検証済みで失敗（buildのTS6133）。その他55件は実測空欄・判定未実行。検証時には利用者A/Bの専用データ、日時、環境・版、操作、実測、証跡、欠陥番号を記録する。性能／ブラウザー／復旧の目標はNF要件承認後に判定する。

## 自動試験の根拠

| 区分 | ファイル | 実行項数 | 判定 |
|---|---|---|---|
| frontend | frontend/tests/authSession.test.mjs | 10 | 成功 |
| frontend | frontend/tests/exerciseCreateModal.test.mjs | 9 | 成功 |
| frontend | frontend/tests/exerciseFilters.test.mjs | 8 | 成功 |
| frontend | frontend/tests/forest.test.mjs | 3 | 成功 |
| frontend | frontend/tests/i18n.test.mjs | 7 | 成功 |
| frontend | frontend/tests/theme.test.mjs | 4 | 成功 |
| python | python-analysis/tests/test_analysis.py | 8 | 成功 |
| python | python-analysis/tests/test_api.py | 6 | 成功 |
| python | python-analysis/tests/test_client.py | 8 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/BackendApplicationTests.java | 0 | 未実行 |
| backend | backend/src/test/java/com/shuttleup/backend/config/DevDataInitializerTest.java | 5 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/config/PasswordConfigTest.java | 1 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/config/SecurityIntegrationTest.java | 12 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/controller/ApiExceptionHandlerTest.java | 1 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/controller/AuthControllerTest.java | 5 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/controller/DashboardControllerTest.java | 1 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/entity/MatchTest.java | 2 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/service/AnalysisDataServiceTest.java | 3 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/service/AuthenticatedServiceTest.java | 0 | 未実行 |
| backend | backend/src/test/java/com/shuttleup/backend/service/AuthServiceTest.java | 16 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/service/DashboardServiceTest.java | 5 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/service/ExerciseServiceTest.java | 2 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/service/MatchServiceTest.java | 3 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/service/TrainingSessionServiceTest.java | 6 | 成功 |
| backend | backend/src/test/java/com/shuttleup/backend/service/UserDataAuthorizationTest.java | 3 | 成功 |

## 完了条件

2026-10-14の1日1セッション・新規ボタン統一変更後の追加検証：Frontend npm testは41件成功、npm run build成功。JavaはBackendApplicationTestsを除いた75件成功（TrainingSessionServiceTest 14件、TrainingSessionControllerTest 2件）。新規登録・日付変更の重複、自己除外、別ユーザー、並行保存のDB例外変換を検証。実DBのUK適用・実並行リクエスト・実画面検証は未実施。上記のbuild失敗は変更前の実行記録であり、この再検証では解消済み。

採用したFR/NFとSC/EV/APIのケースが実行済み、重大欠陥なし、ビルド成功、除外と決定待ちが承認済み、証跡が追跡できることをリリース判定とする。現在はフロントbuild成功だが、実DB・実画面・負荷・復旧未実施のためリリース判定未達。

同日の追加検証：保存エラーの持続表示・日付強調変更後はFrontend 49件成功、build成功。TrainingSessionControllerTest／ApiExceptionHandlerTestの3件成功。保存前の重複判定、自己除外、日付修正後の再保存、競合時の固定codeと旧message、通信失敗と別原因409の区別を実コンポーネントの処理で検証。実画面の見た目は未検証。

[手動検証仕様書](仕様書/テスト/テスト仕様書.xlsx)、[要件](requirements.md)、[運用](deployment.md)。


## 2026-10-20 マスタ管理 初版の検証

今回の変更からJava/Pythonの対象回帰テストを追加した。過去に削除したテスト群を再実行したものではない。

| 検証 | 結果 | 範囲 |
|---|---|---|
| Java package・回帰 | 21件成功 | ExerciseCatalogTest 5件、MasterDataServiceTest 8件、InactiveMasterRecordTest 8件。共有原本と個人設定の分離、祖先停用と下位Switch保持、停用を含む同名照合、旧同名項目のSwitch操作、本人追加、不可視ID拒否、履歴名称・値保持、停用項目の新規／変更拒否と元項目保持。 |
| Python unittest | 4件成功 | 固定統計区分を改名から分離、明示OTHER、旧応答フォールバック、camelCase転送。 |
| Frontend production build | 成功 | vue-tscとVite。既存のbundleサイズ警告あり。 |
| ブラウザー確認 | 隔離fixtureで実施 | 実コンポーネント＋一時的なメモリAPI。分類3階層の新規、個人名保存、上位Switchと下位Switch保持、重名エラーと入力保持、対戦相手の停用・名称／メモ編集と停用保持、日中切替、狭い幅の縦配置。実DB接続は使用しない。 |
| 実MySQL・実HTTP結合 | 未実施 | DDL適用、既存行default、FK/UK、並行書込、実アカウント2名での分離は検証DBで確認が必要。 |

再実行: backendでmvnw.cmd package、python-analysisで.venv/Scripts/python.exe -m unittest discover -s tests -v、frontendでnpm run build。今回の実行環境ではJDKのJAR実パス取得がサンドボックスで拒否されるため、依存をtarget/master-m2/repositoryへ複写し、許可されたローカル実行で生成クラスを再作成してJava packageを完了した。実DBのddl-auto更新をビルド成功から推定しない。

追加手動ケース（未実施）: Aが共有大分類・中分類・小分類を改名／停用してもBは原名・元の可用性を維持すること。Aの過去明細の値・日付・FKと件数が保持されること。元の停用種目／対戦相手を保持した実HTTP更新は成功し、別の停用項目への変更は400となること。Java/Python双方更新後、名称を変えても統計割合が変わらないこと。

2026-10-20追加UI回帰：新規大分類で技術練習をクリックしてもその他のままになる問題を隔離fixtureで再現（FAIL）。DOMの実クリック対象と算出styleを確認し、メニューのpointer-events:none継承を特定。既存BaseSelectと同じポップアップコンテナ・pointerEvents:auto設定を適用後、技術練習選択、モーダルを開き直したフィジカル選択の両方がPASS。分類一覧の標準／自分で追加の非表示も確認。実MySQLを使わないUI確認。

2026-10-20 vxe-table変更の検証：Frontend production build成功。隔離fixtureで大分類の単クリック選択とハイライト・ダブルクリック編集、小分類の単クリックで編集を開かないこと・ダブルクリック編集、Switchが選択を変更しないことを確認。対戦相手13行を全件DOM描画（ページャーなし）し、末尾行のダブルクリック編集を確認。名称／有効・無効の表頭とSwitchの状態文字、説明文を画面確認した。バックエンド変更はないためJava/Pythonは今回再実行していない。
