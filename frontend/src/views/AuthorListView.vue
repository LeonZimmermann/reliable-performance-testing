<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { authorsApi } from '../api/index'
import { auth } from '../main'
import Pagination from '../components/Pagination.vue'
import type { Author, AuthorPage } from '../types/author'

const router = useRouter()
const route = useRoute()

const page = ref(Number(route.query.page) || 0)
const pageSize = 10
const result = ref<AuthorPage | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)
const deletingId = ref<number | null>(null)

async function load() {
  loading.value = true
  error.value = null
  try {
    result.value = await authorsApi.getAuthors({ page: page.value, size: pageSize })
    if (result.value.content.length === 0 && page.value > 0) {
      page.value--
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to load authors'
  } finally {
    loading.value = false
  }
}

async function handleDelete(author: Author) {
  if (!confirm(`Delete "${author.name}"?`)) return
  deletingId.value = author.id
  try {
    await authorsApi.deleteAuthor({ id: author.id })
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to delete author'
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
    <h1>Authors</h1>
  </div>

  <div v-if="error" class="error-banner">{{ error }}</div>

  <div v-if="loading && !result" class="loading">Loading…</div>

  <template v-else-if="result">
    <div v-if="result.content.length === 0" class="empty">
      No authors yet.
      <RouterLink to="/authors/create">Add the first one.</RouterLink>
    </div>

    <div v-else class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Birthdate</th>
            <th>Origin</th>
            <th class="col-actions"></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="author in result.content" :key="author.id">
            <td>
              <RouterLink
                :to="{ name: 'author-detail', params: { id: author.id } }"
                class="author-link"
              >
                {{ author.name }}
              </RouterLink>
            </td>
            <td class="muted">{{ author.birthdate ?? '—' }}</td>
            <td class="muted">{{ author.origin ?? '—' }}</td>
            <td class="col-actions">
              <div class="row-actions">
                <RouterLink
                  :to="{ name: 'author-detail', params: { id: author.id } }"
                  class="btn btn-secondary btn-sm"
                >
                  View
                </RouterLink>
                <button
                  v-if="auth.isAdmin"
                  class="btn btn-danger btn-sm"
                  :disabled="deletingId === author.id"
                  @click="handleDelete(author)"
                >
                  {{ deletingId === author.id ? '…' : 'Delete' }}
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

h1 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
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

.author-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.author-link:hover {
  text-decoration: underline;
}

.muted {
  color: var(--color-text-muted);
  font-size: 0.875rem;
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
