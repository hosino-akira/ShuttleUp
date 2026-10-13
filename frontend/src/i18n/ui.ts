import { computed, watch } from 'vue'
import dayjs from 'dayjs'
import 'dayjs/locale/ja'
import 'dayjs/locale/zh-cn'
import enUS from 'ant-design-vue/es/locale/en_US'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import jaJP from 'ant-design-vue/es/locale/ja_JP'
import VxeUI from 'vxe-pc-ui'
import vxeEn from 'vxe-pc-ui/es/language/en-US'
import vxeZh from 'vxe-pc-ui/es/language/zh-CN'
import vxeJa from 'vxe-pc-ui/es/language/ja-JP'
import { intlLocale, locale, setLanguage } from './index'

export const antLocale = computed(() => ({ en: enUS, zh: zhCN, ja: jaJP })[locale.value])

export function initializeLanguage(): void {
  VxeUI.setI18n('en-US', vxeEn)
  VxeUI.setI18n('zh-CN', vxeZh)
  VxeUI.setI18n('ja-JP', {
    ...vxeJa,
    vxe: {
      ...vxeJa.vxe,
      pager: {
        ...vxeJa.vxe.pager,
        pagesize: '{0}件/ページ',
        total: '全{0}件',
        goto: '移動',
        gotoTitle: '移動先のページ',
      },
    },
  })
  watch(locale, language => {
    dayjs.locale({ en: 'en', zh: 'zh-cn', ja: 'ja' }[language])
    VxeUI.setLanguage(intlLocale.value)
    setLanguage(language)
  }, { immediate: true, flush: 'sync' })
}
