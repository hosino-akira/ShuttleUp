# ShuttleUp Java バックエンド

更新日：2026-10-14。Java21、Spring Boot4.1.0（pom指定）、Spring Web MVC／Data JPA／Validation／OAuth2 Resource Server、MySQL。Controller→Service→Repository→MySQLの構成で本人データを管理する。

## 開発セットアップ

JDK21とMySQLの開発専用shuttleup DBを準備。接続値はapplication-dev.propertiesまたは外部SPRING_DATASOURCE_URL／USERNAME／PASSWORDで提供する。秘密値を文書へ複製しない。既定profileはdev、ddl-auto=update。devのみ不足プリセット6大分類36中分類216種目を補完する。

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

既定8080。prodを指定する場合は空のDB設定を外部提供し、DDL管理・SQLログ・JWT固定鍵を先に確定する。

## 認証・契約

登録とログインのみ公開。JWT HS256、issuer shuttleup、期限86400秒、tokenVersion。署名・期限・ACTIVE・versionを検証しCurrentUserと資源所有者を照合。JWT鍵SHUTTLEUP_JWT_SECRETはBase64復号32byte以上。未設定なら起動ごとにランダム鍵で旧JWTが使えなくなる。

パスワードはBCrypt。変更成功204、tokenVersion増加で旧JWT失効。ログアウトAPI／refreshなし。EntityではなくResponse DTOを公開。業務例外はmessage、400／401／403／404／409を使用。

## ビルド・動作確認

```powershell
.\mvnw.cmd package
```

2026-10-14の整理でsrc/testとテスト専用依存を削除。packageはコンパイルとJAR作成を行い、自動回帰の合格を示さない。起動後は本人所属、同日重複409、保存・削除・分析APIを専用環境で手動確認する。

## 現行の境界

8表66列。match_dateは親から応答生成、plant_type新規省略はcedar・更新省略保持。物理削除で自動カスケードなし。明細数値／成功≤試行、種目名・メモのAPI長さには未検証条件がある。実DDL・FK・索引は本番投入前に照合する。

[API仕様](../docs/api-design.md)、[DB](../docs/database-design.md)、[試験](../docs/testing.md)、[運用](../docs/deployment.md)。
