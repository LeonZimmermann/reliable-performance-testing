export interface NewBook {
  title: string
  author: string
  isbn: string
  price: number
  publisher?: string
}

export interface Book extends NewBook {
  id: number
}

export interface BookPage {
  content: Book[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
