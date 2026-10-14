# ShuttleUp データベース設計

更新日：2026-10-14。現在のリポジトリと設定を基準とする。「現行仕様」「改善案」「決定待ち」を区別し、未実行の検証は成功として記載しない。

## モデルと責務

Spring Data JPAのEntityを論理モデルの基準とする。MySQLの実DDL・文字セット・精度・索引は稼働DB照合が必要で、文書作成時に実DBを変更・検証していない。

| 表 | 列数 | 用途・主要関連 |
|---|---|---|
| users | 9 | email UK、password_hash nullable、token_version、status。 |
| training_sessions | 9 | 本人user_id、日付・時間・feeling・メモ・plant_type。 |
| exercise_categories | 6 | 共通大分類・display_order・enabled。 |
| exercise_types | 7 | 大分類category_id、中分類・display_order・enabled。 |
| exercises | 7 | 中分類exercise_type_id、system_preset、所有user_id nullable。 |
| training_records | 13 | 親セッションと種目、任意の実績7数値・メモ。 |
| matches | 9 | 親セッションと相手、得点・video_url(2048)・メモ。 |
| opponents | 6 | 本人user_id、名前・メモ。 |

## ER・関連

[編集可能なER図](仕様書/ID書/ER図.xlsx)、[画像](仕様書/ID書/ER図.svg)、[テーブル定義](テーブル定義書.xlsx)。8表66列9関連。各表はIDENTITY BIGINT id、created_at／updated_atはService設定。試合日は親training_sessions.training_dateから取得し、matches.match_dateは物理列として持たない。

共通プリセットはexercises.user_id=NULL、独自種目は本人ID。マスターはexercise_categories／exercise_types／共通exercises、本人独自種目とopponents・セッション・明細・試合は本人所属を検証する。

## 型・制約・索引

VARCHARの長さはEntityの文字数。byte長欄はutf8mb4データ部の最大目安で、長さ接頭辞を含まない。DATETIMEの精度・格納量、statusの実ENUM/VARCHAR型は実DDL依存。NOT NULL、FK、UKはテーブル定義を参照。明示UKはusers.emailとtraining_sessions(user_id, training_date)（uk_training_sessions_user_date）。後者は同一ユーザーの1日1セッションを保証する。同一利用者内の相手同名はService制約でDB複合UKではない。追加索引・FK索引の実在はSHOW INDEX／SHOW CREATE TABLEで確認し、性能実測後に追加判断する。

## 初期値と削除

users.token_versionはDB DEFAULT 0、statusはACTIVE指定。system_presetのfalseはJava初期値でDB DEFAULT指定なし。plant_typeの新規省略／旧NULLのcedar補完はService／画面で行う。更新省略時は保存値保持。DB DEFAULT・Java値・画面補完を混同しない。

削除は物理削除。cascade／orphanRemoval指定なし。子明細・試合のあるセッション削除は実DBのFKによって409となり得る。使用中相手はServiceで409。

## スキーマ変更と移行

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
