<script setup lang="ts">
import type { NewAuthor } from '../types/author'

const props = defineProps<{
  modelValue: NewAuthor
  loading: boolean
  submitLabel: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: NewAuthor]
  'submit': []
}>()

const countries = [
  'Afghanistan', 'Argentina', 'Australia', 'Austria', 'Belgium', 'Brazil', 'Canada',
  'Chile', 'China', 'Colombia', 'Czech Republic', 'Denmark', 'Egypt', 'Finland',
  'France', 'Germany', 'Greece', 'Hungary', 'India', 'Indonesia', 'Iran', 'Ireland',
  'Israel', 'Italy', 'Japan', 'Kenya', 'Mexico', 'Netherlands', 'New Zealand', 'Nigeria',
  'Norway', 'Pakistan', 'Peru', 'Philippines', 'Poland', 'Portugal', 'Romania', 'Russia',
  'Saudi Arabia', 'South Africa', 'South Korea', 'Spain', 'Sweden', 'Switzerland',
  'Thailand', 'Turkey', 'Ukraine', 'United Kingdom', 'United States', 'Venezuela', 'Vietnam',
]

function update(field: keyof NewAuthor, value: string) {
  if (field === 'birthdate') {
    emit('update:modelValue', { ...props.modelValue, birthdate: value ? new Date(value) : undefined })
  } else {
    emit('update:modelValue', { ...props.modelValue, [field]: value || undefined })
  }
}
</script>

<template>
  <form @submit.prevent="emit('submit')" novalidate>
    <div class="field">
      <label for="name">Name *</label>
      <input
        id="name"
        type="text"
        :value="modelValue.name"
        @input="update('name', ($event.target as HTMLInputElement).value)"
        required
        placeholder="Author name"
      />
    </div>

    <div class="field">
      <label for="birthdate">Birthdate</label>
      <input
        id="birthdate"
        type="date"
        :value="modelValue.birthdate instanceof Date ? modelValue.birthdate.toISOString().substring(0, 10) : ''"
        @input="update('birthdate', ($event.target as HTMLInputElement).value)"
      />
    </div>

    <div class="field">
      <label for="origin">Origin</label>
      <select
        id="origin"
        :value="modelValue.origin ?? ''"
        @change="update('origin', ($event.target as HTMLSelectElement).value)"
      >
        <option value="">— select country —</option>
        <option v-for="country in countries" :key="country" :value="country">{{ country }}</option>
      </select>
    </div>

    <div class="field">
      <label for="biography">Biography</label>
      <textarea
        id="biography"
        rows="5"
        :value="modelValue.biography ?? ''"
        @input="update('biography', ($event.target as HTMLTextAreaElement).value)"
        placeholder="Short biography…"
      />
    </div>

    <div class="form-actions">
      <slot name="actions" />
      <button type="submit" class="btn btn-primary" :disabled="loading">
        {{ loading ? 'Saving…' : submitLabel }}
      </button>
    </div>
  </form>
</template>

<style scoped>
form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 480px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text);
}

input,
select,
textarea {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  font-size: 0.9375rem;
  color: var(--color-text);
  background: var(--color-bg);
  transition: border-color 0.15s, box-shadow 0.15s;
  width: 100%;
  font-family: inherit;
}

textarea {
  resize: vertical;
  line-height: 1.5;
}

input:focus,
select:focus,
textarea:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  padding-top: 0.5rem;
}
</style>
