import { computed, ref } from "vue";
import { Modal } from "ant-design-vue";

export function useUnsavedChanges(snapshot: () => unknown) {
  const savedSnapshot = ref("");
  const ready = ref(false);
  const currentSnapshot = () => JSON.stringify(snapshot());
  const isDirty = computed(
    () => ready.value && savedSnapshot.value !== currentSnapshot(),
  );

  function reset(): void {
    savedSnapshot.value = currentSnapshot();
    ready.value = true;
  }

  function confirmLeave(): Promise<boolean> {
    if (!isDirty.value) return Promise.resolve(true);

    return new Promise((resolve) =>
      Modal.confirm({
        title: "未保存の変更があります。移動しますか？",
        content:
          "トレーニング基本情報への変更は破棄されます。",
        okText: "移動する",
        cancelText: "キャンセル",
        onOk: () => resolve(true),
        onCancel: () => resolve(false),
      }),
    );
  }

  return { isDirty, reset, confirmLeave };
}
