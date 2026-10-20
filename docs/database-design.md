# ShuttleUp データベース設計

更新日：2026-10-14。現在のリポジトリと設定を基準とする。「現行仕様」「改善案」「決定待ち」を区別し、未実行の検証は成功として記載しない。

## モデルと責務

Spring Data JPAのEntityを論理モデルの基準とする。MySQLの実DDL・文字セット・精度・索引は稼働DB照合が必要で、文書作成時に実DBを変更・検証していない。

| 表 | 列数 | 用途・主要関連 |
|---|---|---|
| users | 12 | email UK、password_hash nullable、token_version、status、現在の身体データ3項目。 |
| training_sessions | 9 | 本人user_id、日付・時間・feeling・メモ・plant_type。 |
| exercise_categories | 8 | 大分類・display_order・enabled、所有user_id nullable、固定statistics_type。 |
| exercise_types | 8 | 大分類category_id、中分類・display_order・enabled、所有user_id nullable。 |
| exercises | 8 | 中分類exercise_type_id、system_preset、所有user_id nullable、enabled。 |
| training_records | 13 | 親セッションと種目、任意の実績7数値・メモ。 |
| matches | 9 | 親セッションと相手、得点・video_url(2048)・メモ。 |
| opponents | 7 | 本人user_id、名前・メモ、enabled。 |
| exercise_master_settings | 6 | 本人user_id、target_kind、target_id、個人名name・enabled。 |

## ER・関連

[編集可能なER図](仕様書/ID書/ER図.xlsx)、[画像](仕様書/ID書/ER図.svg)、[テーブル定義](テーブル定義書.xlsx)。旧ER成果物は8表の版。2026-10-20追加の個人設定表・所有者・統計区分・enabled列はEntityと本節を参照。各表はIDENTITY BIGINT id、created_at／updated_atはService設定。試合日は親training_sessions.training_dateから取得し、matches.match_dateは物理列として持たない。

共通プリセットはexercises.user_id=NULL、独自種目は本人ID。マスターはexercise_categories／exercise_types／共通exercises、本人独自種目とopponents・セッション・明細・試合は本人所属を検証する。

## 型・制約・索引

VARCHARの長さはEntityの文字数。byte長欄はutf8mb4データ部の最大目安で、長さ接頭辞を含まない。DATETIMEの精度・格納量、statusの実ENUM/VARCHAR型は実DDL依存。NOT NULL、FK、UKはテーブル定義を参照。明示UKはusers.emailとtraining_sessions(user_id, training_date)（uk_training_sessions_user_date）。後者は同一ユーザーの1日1セッションを保証する。同一利用者内の相手同名はService制約でDB複合UKではない。追加索引・FK索引の実在はSHOW INDEX／SHOW CREATE TABLEで確認し、性能実測後に追加判断する。

## 初期値と削除

users.token_versionはDB DEFAULT 0、statusはACTIVE指定。system_presetのfalseはJava初期値でDB DEFAULT指定なし。plant_typeの新規省略／旧NULLのcedar補完はService／画面で行う。更新省略時は保存値保持。DB DEFAULT・Java値・画面補完を混同しない。

業務記録の削除は物理削除。マスタは2026-10-20から停用／復元のみ。cascade／orphanRemoval指定なし。子明細・試合のあるセッション削除は実DBのFKによって409となり得る。対戦相手・種目のDELETEエンドポイントは提供しない。

## スキーマ変更と移行

2026-10-20 身体データ：usersにheight_cm、body_weight_kg、body_fat_percentage（各DECIMAL(5,2)、NULL可）を追加。既存ユーザーはNULLのままとし、現在値だけを保持する。体重はトレーニング負荷のtraining_records.weight_kgとは別。BMIは画面で算出し、DBには保存しない。同日、旧バックエンドの再起動後にddl-auto=updateによる実MySQLへの3列追加と、JWT認証を通した保存・再取得・消去を確認した。

現行dev・prodともddl-auto=updateで、Flyway／Liquibase・SQL migrationは未導入。本番では外部設定でvalidate等の運用へ切替える案を採用環境で検証する。

1日1セッションへの変更では、既存の重複を次のSQLで事前確認する。重複がある場合は明細・試合の所属も確認してデータを整理してからUKを追加する。アプリは既存レコードを自動削除しない。ddl-auto=updateの結果はSHOW INDEXで確認し、外部DDL管理の場合も同じUKを適用する。

```sql
SELECT user_id, training_date, COUNT(*) AS session_count
FROM training_sessions
GROUP BY user_id, training_date
HAVING COUNT(*) > 1;

-- 同名制約が未適用で、既存の重複がない場合に実行する。
ALTER TABLE training_sessions
  ADD CONSTRAINT uk_training_sessions_user_date UNIQUE (user_id, training_date);
```

1. 変更前の実DDL・件数・前版Entityを保存する。
2. 更新後のEntity／テーブル定義と差分を比較する。plant_type等が既にあるか確認する。
3. 追加列・NULL・初期値・既存値補完・索引を変更SQLとして審査する。
4. 復旧用DBへバックアップを戻し、DDLと新旧版の互換性を確認する。
5. 承認した変更だけ反映し、件数・FK・主要操作を確認する。

開発プリセット補完はdevだけで6大分類・36中分類・216種目を不足分だけ追加する。prodにはこの補完が動かないため、必要なマスターの導入を別途計画する。

根拠：backend/src/main/java/com/shuttleup/backend/entity・config/DevDataInitializer.java。参照：[運用](deployment.md)、[テスト](testing.md)。


## 2026-10-20 マスタ管理のスキーマ更新

既存のddl-auto=updateを利用する。新しいexercises.enabledとopponents.enabledにはDB DEFAULT trueを宣言し、既存行も有効として保持する。category/typeのuser_id=NULLは共有原本、本人IDは個人追加。既存ID・FK・名前・履歴は保持する。

exercise_master_settingsは(user_id,target_kind,target_id)にUK（uk_master_setting_user_target）を持つ。target_kindはCATEGORY／TYPE／EXERCISE。異なる表を指すtarget_idはサービスで可視性と存在を検証し、マスタ自体の削除を提供しない。個人追加データは元行を更新し、共有行への個人変更だけをこの表に保存する。

大分類statistics_typeはPHYSICAL／SKILL／OTHER。起動時CategoryStatisticsInitializerがNULLだけを旧名称ルールで補完する。dev補完は共有行のみ照合してから実行する。統計区分はその後の名称変更で再計算しない。

個人別重名チェックは共有原本＋個人設定＋本人追加を合成して実施する。同一ユーザーのマスタ書込・相手書込ではusers行をPESSIMISTIC_WRITEでロックして同時登録を直列化する。同名照合には停用項目も含む。既存の同名データは削除せず、名前を維持したSwitch操作は許可する。

稼働DBを使ったDDL適用・既存データ移行・実並行リクエストは未検証。起動前にバックアップを取り、別の検証DBで追加列の初期値、FK、UK、件数維持と2ユーザーの分離を確認する。JavaとPythonを両方更新・再起動する。
