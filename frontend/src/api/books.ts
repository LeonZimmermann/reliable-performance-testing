import type { Book, BookPage, NewBook } from '../types/book'

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message)
  }
}

async function handleResponse<T>(res: Response): Promise<T> {
  if (!res.ok) {
    const text = await res.text().catch(() => res.statusText)
    throw new ApiError(res.status, text || res.statusText)
  }
  return res.json() as Promise<T>
}

export async function getBooks(page = 0, size = 10): Promise<BookPage> {
  const res = await fetch(`/books?page=${page}&size=${size}`)
  return handleResponse<BookPage>(res)
}

export async function getBook(id: number): Promise<Book> {
  const res = await fetch(`/books/${id}`)
  return handleResponse<Book>(res)
}

export async function createBook(data: NewBook): Promise<Book> {
  const res = await fetch('/books', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  return handleResponse<Book>(res)
}

export async function updateBook(id: number, data: NewBook): Promise<Book> {
  const res = await fetch(`/books/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  })
  return handleResponse<Book>(res)
}

export async function deleteBook(id: number): Promise<void> {
  const res = await fetch(`/books/${id}`, { method: 'DELETE' })
  if (!res.ok) {
    const text = await res.text().catch(() => res.statusText)
    throw new ApiError(res.status, text || res.statusText)
  }
}
