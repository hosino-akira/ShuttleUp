import { createApp } from 'vue'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import './style.css'
import VxeUI from 'vxe-pc-ui'
import 'vxe-pc-ui/es/style.css'
import VxeUITable from 'vxe-table'
import 'vxe-table/es/style.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(router)
app.use(Antd)

app.use(VxeUI)
app.use(VxeUITable)

app.mount('#app')
