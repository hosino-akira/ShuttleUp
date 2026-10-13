import { nextTick, watch, type Ref } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import type { NamePath } from 'ant-design-vue/es/form/interface'
import { locale } from '../i18n'

/** Refresh existing validation errors without submitting or changing form data. */
export function useLocalizedForm(form: Ref<FormInstance | undefined>) {
  const invalidFields = new Map<string, NamePath>()
  function onValidate(name: NamePath, valid: boolean): void {
    const key = JSON.stringify(name)
    if (valid) invalidFields.delete(key)
    else invalidFields.set(key, name)
  }
  function reset(): void {
    invalidFields.clear()
  }
  watch(locale, async () => {
    await nextTick()
    if (!form.value || !invalidFields.size) return
    try {
      await form.value.validateFields([...invalidFields.values()])
    } catch {
      // Expected: invalid fields retain their errors in the newly selected language.
    }
  }, { flush: 'post' })
  watch(form, reset)
  return { onValidate, reset }
}
