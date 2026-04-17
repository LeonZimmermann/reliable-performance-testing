<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { booksApi } from '../api/index'
import type { Book } from '../types/book'

const books = ref<Book[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

async function load() {
  loading.value = true
  error.value = null
  try {
    books.value = await booksApi.getAllBooks()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to load books'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page-header">
    <h1>Books <span class="version-badge">v1</span></h1>
    <span class="hint">All books loaded at once — no pagination</span>
  </div>

  <div v-if="error" class="error-banner">{{ error }}</div>

  <div v-if="loading" class="loading">Loading…</div>

  <template v-else-if="books.length > 0">
    <p class="count">{{ books.length }} books loaded</p>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Title</th>
            <th>Author</th>
            <th>ISBN</th>
            <th class="col-price">Price</th>
            <th>Publisher</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in books" :key="book.id">
            <td>
              <RouterLink :to="{ name: 'book-detail', params: { id: book.id } }" class="book-link">
                {{ book.title }}
              </RouterLink>
            </td>
            <td>{{ book.author }}</td>
            <td class="isbn">{{ book.isbn }}</td>
            <td class="col-price">€ {{ book.price.toFixed(2) }}</td>
            <td class="muted">{{ book.publisher ?? '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </template>

  <div v-else-if="!loading" class="empty">
    No books yet.
    <RouterLink to="/books/create">Add the first one.</RouterLink>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: baseline;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.version-badge {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 600;
  background: #fef3c7;
  color: #92400e;
  border: 1px solid #fde68a;
  border-radius: 4px;
  padding: 0.1rem 0.4rem;
  vertical-align: middle;
}

.hint {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.count {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0 0 1rem;
}

.loading,
.empty {
  color: var(--color-text-muted);
  padding: 2rem 0;
  text-align: center;
}

.empty a {
  color: var(--color-primary);
}

.table-wrap {
  overflow-x: auto;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: var(--color-bg);
  box-shadow: var(--shadow-sm);
}

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

thead th {
  background: var(--color-bg-alt);
  text-align: left;
  padding: 0.65rem 1rem;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
  border-bottom: 1px solid var(--color-border);
}

tbody tr {
  border-bottom: 1px solid var(--color-border);
  transition: background 0.1s;
}

tbody tr:last-child {
  border-bottom: none;
}

tbody tr:hover {
  background: var(--color-bg-alt);
}

td {
  padding: 0.75rem 1rem;
  vertical-align: middle;
}

.book-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.book-link:hover {
  text-decoration: underline;
}

.isbn {
  font-family: monospace;
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}

.muted {
  color: var(--color-text-muted);
}

.col-price {
  text-align: right;
  white-space: nowrap;
}
</style>
