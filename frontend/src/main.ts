import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { message } from 'ant-design-vue'
import { useAuthStore } from './stores/auth'
import { setUnauthorizedHandler } from './utils/authSession'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import './style.css'
import VxeUI from 'vxe-pc-ui'
import jaJP from 'vxe-pc-ui/es/language/ja-JP'
import 'vxe-pc-ui/es/style.css'
import VxeUITable from 'vxe-table'
import 'vxe-table/es/style.css'
import App from './App.vue'
import router from './router'

VxeUI.setI18n('ja-JP', {
  ...jaJP,
  vxe: {
    ...jaJP.vxe,
    pager: {
      ...jaJP.vxe.pager,
      pagesize: '{0}件/ページ',
      total: '全{0}件',
      goto: '移動',
      gotoTitle: '移動先のページ',
    },
  },
})
VxeUI.setLanguage('ja-JP')

const app = createApp(App)

app.use(createPinia())
setUnauthorizedHandler(() => {
  useAuthStore().logout()
  message.warning('ログインの有効期限が切れました。もう一度ログインしてください。')
  const currentRoute = router.currentRoute.value
  if (currentRoute.matched.length && currentRoute.meta.requiresAuth !== false) {
    void router.replace({ name: 'login', query: { redirect: currentRoute.fullPath } })
  }
})
app.use(router)
app.use(Antd)

app.use(VxeUI)
app.use(VxeUITable)

app.mount('#app')
