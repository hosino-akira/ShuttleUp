import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { message } from 'ant-design-vue'
import { useAuthStore } from './stores/auth'
import { setUnauthorizedHandler } from './utils/authSession'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import './style.css'
import VxeUI from 'vxe-pc-ui'
import { initializeLanguage } from './i18n/ui'
import { t } from './i18n'
import 'vxe-pc-ui/es/style.css'
import VxeUITable from 'vxe-table'
import 'vxe-table/es/style.css'
import App from './App.vue'
import router from './router'
import { initializeTheme } from './theme'

initializeLanguage()
initializeTheme()

const app = createApp(App)

app.use(createPinia())
setUnauthorizedHandler(() => {
  useAuthStore().logout()
  message.warning(t('ログインの有効期限が切れました。もう一度ログインしてください。'))
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
