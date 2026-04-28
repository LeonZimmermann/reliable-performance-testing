<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { booksApi, authorsApi, ResponseError } from '../api/index'
import { auth } from '../main'
import BookForm from '../components/BookForm.vue'
import type { Book, NewBook } from '../types/book'
import type { AuthorSummary } from '../types/author'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)

const book = ref<Book | null>(null)
const editing = ref(false)
const form = ref<NewBook>({ title: '', author: '', isbn: '', price: 0 })
const availableAuthors = ref<AuthorSummary[]>([])
const loading = ref(false)
const saving = ref(false)
const deleting = ref(false)
const error = ref<string | null>(null)
const notFound = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const [loadedBook, authorPage] = await Promise.all([
      booksApi.getBookById({ id }),
      authorsApi.getAuthors({ page: 0, size: 200 }),
    ])
    book.value = loadedBook
    availableAuthors.value = authorPage.content.map((a) => ({ id: a.id, name: a.name }))
  } catch (e) {
    if (e instanceof ResponseError && e.response.status === 404) {
      notFound.value = true
    } else {
      error.value = e instanceof Error ? e.message : 'Failed to load book'
    }
  } finally {
    loading.value = false
  }
})

function startEdit() {
  if (!book.value) return
  form.value = {
    title: book.value.title,
    author: book.value.author,
    isbn: book.value.isbn,
    price: book.value.price,
    publisher: book.value.publisher,
    authorIds: book.value.authors.map((a) => a.id),
  }
  editing.value = true
  error.value = null
}

async function handleUpdate() {
  if (!book.value) return
  saving.value = true
  error.value = null
  try {
    book.value = await booksApi.updateBook({
      id,
      newBook: { ...form.value, publisher: form.value.publisher || undefined },
    })
    editing.value = false
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to update book'
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  if (!book.value) return
  if (!confirm(`Delete "${book.value.title}"?`)) return
  deleting.value = true
  try {
    await booksApi.deleteBook({ id })
    router.push({ name: 'book-list' })
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to delete book'
    deleting.value = false
  }
}
</script>

<template>
  <div class="back-link">
    <RouterLink to="/books">← Back to books</RouterLink>
  </div>

  <div v-if="loading" class="loading">Loading…</div>

  <div v-else-if="notFound" class="not-found">
    <h2>Book not found</h2>
    <p>This book may have been deleted.</p>
    <RouterLink to="/books" class="btn btn-secondary">Back to list</RouterLink>
  </div>

  <template v-else-if="book">
    <div class="detail-card">
      <div class="card-header">
        <div>
          <h1>{{ book.title }}</h1>
          <p class="subtitle">by {{ book.author }}</p>
        </div>
        <div class="header-actions" v-if="!editing && auth.isAdmin">
          <button class="btn btn-secondary" @click="startEdit">Edit</button>
          <button class="btn btn-danger" :disabled="deleting" @click="handleDelete">
            {{ deleting ? 'Deleting…' : 'Delete' }}
          </button>
        </div>
      </div>

      <div v-if="error" class="error-banner">{{ error }}</div>

      <template v-if="!editing">
        <dl class="fields">
          <div class="field-row">
            <dt>ISBN</dt>
            <dd class="mono">{{ book.isbn }}</dd>
          </div>
          <div class="field-row">
            <dt>Price</dt>
            <dd>€ {{ book.price.toFixed(2) }}</dd>
          </div>
          <div class="field-row">
            <dt>Publisher</dt>
            <dd :class="{ muted: !book.publisher }">{{ book.publisher ?? '—' }}</dd>
          </div>
          <div class="field-row">
            <dt>Authors</dt>
            <dd>
              <span v-if="book.authors.length === 0" class="muted">—</span>
              <ul v-else class="author-links">
                <li v-for="a in book.authors" :key="a.id">
                  <RouterLink :to="{ name: 'author-detail', params: { id: a.id } }">
                    {{ a.name }}
                  </RouterLink>
                </li>
              </ul>
            </dd>
          </div>
          <div class="field-row">
            <dt>ID</dt>
            <dd class="muted mono">{{ book.id }}</dd>
          </div>
        </dl>
      </template>

      <template v-else>
        <BookForm
          v-model="form"
          :loading="saving"
          :available-authors="availableAuthors"
          submit-label="Save Changes"
          @submit="handleUpdate"
        >
          <template #actions>
            <button type="button" class="btn btn-secondary" @click="editing = false">
              Cancel
            </button>
          </template>
        </BookForm>
      </template>
    </div>
  </template>
</template>

<style scoped>
.back-link {
  margin-bottom: 1.25rem;
}

.back-link a {
  color: var(--color-text-muted);
  text-decoration: none;
  font-size: 0.875rem;
}

.back-link a:hover {
  color: var(--color-primary);
}

.loading,
.not-found {
  text-align: center;
  padding: 3rem 0;
  color: var(--color-text-muted);
}

.not-found h2 {
  color: var(--color-text);
  margin-bottom: 0.5rem;
}

.detail-card {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  padding: 2rem;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  margin-bottom: 1.75rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

h1 {
  margin: 0 0 0.25rem;
  font-size: 1.5rem;
  font-weight: 700;
  line-height: 1.2;
}

.subtitle {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 1rem;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
  flex-shrink: 0;
}

.fields {
  display: grid;
  gap: 0;
  margin: 0;
}

.field-row {
  display: flex;
  align-items: baseline;
  gap: 1rem;
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--color-border);
}

.field-row:last-child {
  border-bottom: none;
}

dt {
  width: 100px;
  flex-shrink: 0;
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

dd {
  margin: 0;
  font-size: 0.9375rem;
}

.muted {
  color: var(--color-text-muted);
}

.mono {
  font-family: monospace;
  font-size: 0.875rem;
}

.author-links {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.375rem;
}

.author-links a {
  color: var(--color-primary);
  text-decoration: none;
  font-size: 0.9rem;
}

.author-links a:hover {
  text-decoration: underline;
}
</style>
