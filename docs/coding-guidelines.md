# ShuttleUp コーディングガイドライン

更新日：2026-10-14。現在のリポジトリと設定を基準とする。「現行仕様」「改善案」「決定待ち」を区別し、未実行の検証は成功として記載しない。

## 責務と配置

Frontendはviews（画面）、components（共通／機能部品）、composables（状態と処理共有）、api（通信）、types（DTO型）、utils（純粋処理）、theme／i18n（共通表示）。通常Viewから機能API層を呼び、Bearer・401はhttp／analysisClientへ集約する。

Javaはcontroller→service→repository、entityはDBモデル、dto/request・responseはAPI契約、config／securityは認証。Controllerで@Valid、Serviceで本人所属と業務条件、更新はトランザクションを境界にする。Pythonはapi→client／service→analysis、modelsで契約、coreで環境設定。MySQL直接接続を追加する際は構成・要件を見直す。

## 命名・型

Vueコンポーネント・JavaクラスはPascalCase、TS／Javaの変数関数はcamelCase、DBはsnake_case複数形、Pythonはsnake_case。DTOのNULL・0・未指定を区別し、Request/Response型を同期する。TypeScriptは型の不明値をunknownから確認し、anyの追加には理由を残す。

## Vueと表示

Composition APIとscript setupを使用。propsを直接変更せずemit、表示派生はcomputed、非同期候補は旧応答が現状態を上書きしない。翻訳はt、動的選択肢・グラフはcomputed内で翻訳する。パレットはtheme定義を参照し、各画面で5テーマ色を複製しない。選択Popupはモーダル／Drawer内のスタッキングコンテナを利用。未使用importを残さずnpm run buildで型検査する。

## 認証・DB・ログ

JWTのクライアント形式確認は署名認証を代替しない。APIで本人所有者を確認し、共通プリセットの変更を拒否する。パスワード・hash・Bearer・DB秘密値をレスポンス／共有ログ／文書に出さない。DBカスケードやDB DEFAULTをJava初期値から推定しない。

## テスト・レビュー

不具合修正は境界・所有者・NULL・競合など挙動を検証する。2026-10-14の整理後は自動テストソースを同梱しない。Frontend npm run build、Java package、Pythonの構文・import確認と、変更範囲の手動操作確認を行う。手動ケース・実DB確認の実施状況も残す。

## Git運用

作業前にgit statusで既存変更を確認し、他の変更を上書きしない。小さな変更単位でレビュー可能にする。コミットメッセージは現行履歴に合わせfeat／fix／docs／test／refactor等を使い具体的な変更を記す。ブランチ・PR運用はリポジトリ運用者の設定に従い、文書作成時には新規ブランチ・コミット・pushを実施しない。認証値と.envをGitへ追加しない。

## 文書更新

FR/NF→SC/EV/CHK/MSG/API/DB→TCの影響を確認して同じ変更で更新する。現行実装、改善案、将来計画を区別する。出力・集計では分母・期間・NULL・丸めを明記する。

参照：[索引](README.md)、[テスト](testing.md)、[API](api-design.md)。
