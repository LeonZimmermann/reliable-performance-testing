<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { authorsApi, ResponseError } from '../api/index'
import { auth } from '../main'
import AuthorForm from '../components/AuthorForm.vue'
import type { Author, NewAuthor } from '../types/author'

const route = useRoute()
const router = useRouter()
const id = Number(route.params.id)

const author = ref<Author | null>(null)
const editing = ref(false)
const form = ref<NewAuthor>({ name: '' })
const loading = ref(false)
const saving = ref(false)
const deleting = ref(false)
const error = ref<string | null>(null)
const notFound = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    author.value = await authorsApi.getAuthorById({ id })
  } catch (e) {
    if (e instanceof ResponseError && e.response.status === 404) {
      notFound.value = true
    } else {
      error.value = e instanceof Error ? e.message : 'Failed to load author'
    }
  } finally {
    loading.value = false
  }
})

function startEdit() {
  if (!author.value) return
  form.value = {
    name: author.value.name,
    birthdate: author.value.birthdate,
    origin: author.value.origin,
    biography: author.value.biography,
  }
  editing.value = true
  error.value = null
}

async function handleUpdate() {
  saving.value = true
  error.value = null
  try {
    author.value = await authorsApi.updateAuthor({ id, newAuthor: form.value })
    editing.value = false
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to update author'
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  if (!author.value || !confirm(`Delete "${author.value.name}"?`)) return
  deleting.value = true
  try {
    await authorsApi.deleteAuthor({ id })
    router.push({ name: 'author-list' })
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Failed to delete author'
    deleting.value = false
  }
}
</script>

<template>
  <div class="back-link">
    <RouterLink to="/authors">← Back to authors</RouterLink>
  </div>

  <div v-if="loading" class="loading">Loading…</div>

  <div v-else-if="notFound" class="not-found">
    <h2>Author not found</h2>
    <p>This author may have been deleted.</p>
    <RouterLink to="/authors" class="btn btn-secondary">Back to list</RouterLink>
  </div>

  <template v-else-if="author">
    <div class="detail-card">
      <div class="card-header">
        <div>
          <h1>{{ author.name }}</h1>
          <p v-if="author.origin" class="subtitle">{{ author.origin }}</p>
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
          <div class="field-row" v-if="author.birthdate">
            <dt>Born</dt>
            <dd>{{ author.birthdate }}</dd>
          </div>
          <div class="field-row" v-if="author.origin">
            <dt>Origin</dt>
            <dd>{{ author.origin }}</dd>
          </div>
          <div class="field-row" v-if="author.biography">
            <dt>Biography</dt>
            <dd class="biography">{{ author.biography }}</dd>
          </div>
          <div class="field-row">
            <dt>ID</dt>
            <dd class="muted mono">{{ author.id }}</dd>
          </div>
        </dl>
      </template>

      <template v-else>
        <AuthorForm
          v-model="form"
          :loading="saving"
          submit-label="Save Changes"
          @submit="handleUpdate"
        >
          <template #actions>
            <button type="button" class="btn btn-secondary" @click="editing = false">
              Cancel
            </button>
          </template>
        </AuthorForm>
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

.biography {
  white-space: pre-wrap;
  line-height: 1.6;
}

.muted {
  color: var(--color-text-muted);
}

.mono {
  font-family: monospace;
  font-size: 0.875rem;
}
</style>
