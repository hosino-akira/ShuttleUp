import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    layout?: 'auth'
    requiresAuth?: boolean
    guestOnly?: boolean
  }
}
