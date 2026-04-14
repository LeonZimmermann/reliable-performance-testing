<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { booksApi, authorsApi } from '../api/index'
import BookForm from '../components/BookForm.vue'
import type { NewBook } from '../types/book'
import type { AuthorSummary } from '../types/author'

const router = useRouter()

const form = ref<NewBook>({ title: '', author: '', isbn: '', price: 0, publisher: '', authorIds: [] })
const availableAuthors = ref<AuthorSummary[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    const page = await authorsApi.getAuthors({ page: 0, size: 200 })
    availableAuthors.value = page.content.map((a) => ({ id: a.id, name: a.name }))
  } catch {
    // author list is optional — silently ignore
  }
})

async function handleSubmit() {
  loading.value = true
  error.value = null
  try {
    const book = await booksApi.createBook({
      newBook: { ...form.value, publisher: form.value.publisher || undefined },
    })
    router.push({ name: 'book-detail', params: { id: book.id } })
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to create book'
    loading.value = false
  }
}
</script>

<template>
  <div class="back-link">
    <RouterLink to="/books">← Back to books</RouterLink>
  </div>

  <div class="create-card">
    <h1>New Book</h1>

    <div v-if="error" class="error-banner">{{ error }}</div>

    <BookForm
      v-model="form"
      :loading="loading"
      :available-authors="availableAuthors"
      submit-label="Create Book"
      @submit="handleSubmit"
    >
      <template #actions>
        <RouterLink to="/books" class="btn btn-secondary">Cancel</RouterLink>
      </template>
    </BookForm>
  </div>
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

.create-card {
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  box-shadow: var(--shadow-sm);
  padding: 2rem;
  max-width: 560px;
}

h1 {
  margin: 0 0 1.5rem;
  font-size: 1.375rem;
  font-weight: 700;
}
</style>
