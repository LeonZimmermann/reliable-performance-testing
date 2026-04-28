<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { booksApi } from '../api/index'
import { auth } from '../main'
import Pagination from '../components/Pagination.vue'
import type { Book, BookPage } from '../types/book'

const router = useRouter()
const route = useRoute()

const page = ref(Number(route.query.page) || 0)
const pageSize = 10
const result = ref<BookPage | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const deletingId = ref<number | null>(null)

async function load() {
  loading.value = true
  error.value = null
  try {
    result.value = await booksApi.getBooks({ page: page.value, size: pageSize })
    // If deleted last item on page, go back one page
    if (result.value.content.length === 0 && page.value > 0) {
      page.value--
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to load books'
  } finally {
    loading.value = false
  }
}

async function handleDelete(book: Book) {
  if (!confirm(`Delete "${book.title}"?`)) return
  deletingId.value = book.id
  try {
    await booksApi.deleteBook({ id: book.id })
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to delete book'
  } finally {
    deletingId.value = null
  }
}

function onPageChange(newPage: number) {
  page.value = newPage
  router.replace({ query: { page: newPage > 0 ? String(newPage) : undefined } })
}

watch(page, load)
onMounted(load)
</script>

<template>
  <div class="page-header">
    <div class="page-title">
      <h1>Books <span class="version-badge">v2</span></h1>
      <span class="hint">Paginated</span>
    </div>
  </div>

  <div v-if="error" class="error-banner">{{ error }}</div>

  <div v-if="loading && !result" class="loading">Loading…</div>

  <template v-else-if="result">
    <div v-if="result.content.length === 0" class="empty">
      No books yet.
      <RouterLink to="/books/create">Add the first one.</RouterLink>
    </div>

    <div v-else class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Title</th>
            <th>Author</th>
            <th>ISBN</th>
            <th class="col-price">Price</th>
            <th>Publisher</th>
            <th class="col-actions"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in result.content" :key="book.id">
            <td>
              <RouterLink :to="{ name: 'book-detail', params: { id: book.id } }" class="book-link">
                {{ book.title }}
              </RouterLink>
            </td>
            <td>{{ book.author }}</td>
            <td class="isbn">{{ book.isbn }}</td>
            <td class="col-price">€ {{ book.price.toFixed(2) }}</td>
            <td class="muted">{{ book.publisher ?? '—' }}</td>
            <td class="col-actions">
              <div class="row-actions">
                <RouterLink
                  :to="{ name: 'book-detail', params: { id: book.id } }"
                  class="btn btn-secondary btn-sm"
                >
                  View
                </RouterLink>
                <button
                  v-if="auth.isAdmin"
                  class="btn btn-danger btn-sm"
                  :disabled="deletingId === book.id"
                  @click="handleDelete(book)"
                >
                  {{ deletingId === book.id ? '…' : 'Delete' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <Pagination
      :current-page="page"
      :total-pages="result.totalPages"
      :total-elements="result.totalElements"
      @page-change="onPageChange"
    />
  </template>
</template>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.page-title {
  display: flex;
  align-items: baseline;
  gap: 1rem;
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
  background: #dcfce7;
  color: #166534;
  border: 1px solid #bbf7d0;
  border-radius: 4px;
  padding: 0.1rem 0.4rem;
  vertical-align: middle;
}

.hint {
  font-size: 0.875rem;
  color: var(--color-text-muted);
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

.col-actions {
  width: 1%;
  white-space: nowrap;
}

.row-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}
</style>
