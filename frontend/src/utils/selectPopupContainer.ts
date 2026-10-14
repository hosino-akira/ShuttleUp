/** ポップアップをモーダル/ドロワーと同じ重なり順に配置し、スクロール領域の外に表示する。 */
export function selectPopupContainer(trigger: HTMLElement): HTMLElement {
  return trigger.closest<HTMLElement>('.ant-modal, .ant-drawer') ?? trigger.ownerDocument.body
}
