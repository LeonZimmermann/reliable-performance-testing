import { BooksApi, AuthorsApi, Configuration } from '../generated'

export { ResponseError } from '../generated/runtime'

const config = new Configuration({ basePath: '' })

export const booksApi = new BooksApi(config)
export const authorsApi = new AuthorsApi(config)
