import { createApp, reactive } from 'vue'
import Keycloak from 'keycloak-js'
import App from './App.vue'
import router from './router'

export const keycloak = new Keycloak({
  url: 'http://localhost:8180',
  realm: 'bookstore',
  clientId: 'bookstore-frontend',
})

export const auth = reactive({ authenticated: false, isAdmin: false, username: '' })

function syncAuth() {
  auth.authenticated = keycloak.authenticated ?? false
  auth.isAdmin = keycloak.hasRealmRole('admin')
  auth.username = (keycloak.tokenParsed as Record<string, string> | undefined)?.preferred_username ?? ''
}

keycloak.onAuthSuccess = syncAuth
keycloak.onAuthRefreshSuccess = syncAuth
keycloak.onAuthLogout = () => Object.assign(auth, { authenticated: false, isAdmin: false, username: '' })

keycloak
  .init({ onLoad: 'check-sso', silentCheckSsoRedirectUri: `${location.origin}/silent-check-sso.html`, pkceMethod: 'S256' })
  .then(syncAuth)
  .catch(() => console.warn('Keycloak unavailable — running unauthenticated'))
  .finally(() => createApp(App).use(router).mount('#app'))
