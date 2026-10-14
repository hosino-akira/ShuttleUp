# ShuttleUp フロントエンド

更新日：2026-10-14。Vue3／TypeScript／Vite、Pinia、Vue Router、Ant Design Vue、VXE-Table、EChartsで画面を提供。本人記録とDashboardはSpring、詳細分析はFastAPI。

## セットアップ・起動

```powershell
cd frontend
npm ci
npm run dev
```

Node固定版は未設定。package.jsonのVite8に互換なNodeを用意し実版を記録する。開発Proxyは/api/analysis→8000、他の/api→8080。分析baseURLはVITE_ANALYSIS_API_BASE_URL。未指定時は相対URL。

## 検証

```powershell
npm run build
npm run preview
```

npm run buildはvue-tscとVite。2026-10-14の整理でtestsとtestスクリプトを削除。ブラウザーで主要操作と表示を手動確認する。npm run type-check／lintスクリプトは現行package.jsonにない。

## 表示と認証

5テーマ、3言語をlocalStorageに保存。初期forest／ja。入力・APIデータは原文保持、グラフとUIは共通定義に同期。JWTはsessionStorage、有効86400秒。再読込は/auth/meで本人復元。署名・状態・versionはSpringが確認し、認証エラーでログインへ。

保護画面には共通メニュー、基本情報は2ルート、明細・種目・試合はModal、相手はDrawer。森は1セッション1株、未来日除外、6植物種、36株区画。

## 配信

本番はdist静的配信＋API Proxy＋SPA fallback。Vite開発Proxyはbuild後に存在しない。正式ブラウザー・OSは決定待ち。

[画面仕様・遷移図](../docs/README.md)、[UI設計](../docs/ui-design.md)、[テスト](../docs/testing.md)、[運用](../docs/deployment.md)。
