import { keycloak } from '../main'
import { BooksApi, AuthorsApi, Configuration } from '../generated'

export { ResponseError } from '../generated/runtime'

const config = new Configuration({
  basePath: '',
  fetchApi: async (input, init) => {
    if (keycloak.authenticated) {
      await keycloak.updateToken(30).catch(() => {})
    }
    const headers = new Headers(init?.headers as HeadersInit)
    if (keycloak.token) headers.set('Authorization', `Bearer ${keycloak.token}`)
    return fetch(input, { ...(init ?? {}), headers })
  },
})

export const booksApi = new BooksApi(config)
export const authorsApi = new AuthorsApi(config)
