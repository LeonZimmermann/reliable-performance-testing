import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import BookForm from './BookForm.vue'
import type { NewBook } from '../types/book'

describe('BookForm', () => {
  const baseModel: NewBook = { title: '', author: '', isbn: '', price: 0 }

  it('renders all required fields', () => {
    const wrapper = mount(BookForm, {
      props: { modelValue: baseModel, loading: false, submitLabel: 'Save' },
    })
    expect(wrapper.find('#title').exists()).toBe(true)
    expect(wrapper.find('#author').exists()).toBe(true)
    expect(wrapper.find('#isbn').exists()).toBe(true)
    expect(wrapper.find('#price').exists()).toBe(true)
    expect(wrapper.find('#publisher').exists()).toBe(true)
  })

  it('emits update:modelValue with updated title when title changes', async () => {
    const wrapper = mount(BookForm, {
      props: { modelValue: baseModel, loading: false, submitLabel: 'Save' },
    })
    await wrapper.find('#title').setValue('Clean Code')
    const emitted = wrapper.emitted('update:modelValue') as [NewBook][]
    expect(emitted[0][0].title).toBe('Clean Code')
  })

  it('emits price as a number', async () => {
    const wrapper = mount(BookForm, {
      props: { modelValue: baseModel, loading: false, submitLabel: 'Save' },
    })
    await wrapper.find('#price').setValue('29.99')
    const emitted = wrapper.emitted('update:modelValue') as [NewBook][]
    expect(typeof emitted[0][0].price).toBe('number')
    expect(emitted[0][0].price).toBe(29.99)
  })

  it('adds an author id when its checkbox is checked', async () => {
    const wrapper = mount(BookForm, {
      props: {
        modelValue: { ...baseModel, authorIds: [] },
        loading: false,
        submitLabel: 'Save',
        availableAuthors: [
          { id: 1, name: 'Jane Austen' },
          { id: 2, name: 'Tolkien' },
        ],
      },
    })
    await wrapper.findAll('input[type="checkbox"]')[0].trigger('change')
    const emitted = wrapper.emitted('update:modelValue') as [NewBook][]
    expect(emitted[0][0].authorIds).toContain(1)
    expect(emitted[0][0].authorIds).not.toContain(2)
  })

  it('removes an author id when its checkbox is unchecked', async () => {
    const wrapper = mount(BookForm, {
      props: {
        modelValue: { ...baseModel, authorIds: [1] },
        loading: false,
        submitLabel: 'Save',
        availableAuthors: [{ id: 1, name: 'Jane Austen' }],
      },
    })
    await wrapper.find('input[type="checkbox"]').trigger('change')
    const emitted = wrapper.emitted('update:modelValue') as [NewBook][]
    expect(emitted[0][0].authorIds).not.toContain(1)
  })

  it('does not render author checkboxes when no authors are available', () => {
    const wrapper = mount(BookForm, {
      props: { modelValue: baseModel, loading: false, submitLabel: 'Save' },
    })
    expect(wrapper.find('input[type="checkbox"]').exists()).toBe(false)
  })

  it('emits submit when the form is submitted', async () => {
    const wrapper = mount(BookForm, {
      props: { modelValue: baseModel, loading: false, submitLabel: 'Save' },
    })
    await wrapper.find('form').trigger('submit')
    expect(wrapper.emitted('submit')).toHaveLength(1)
  })

  it('disables the submit button while loading', () => {
    const wrapper = mount(BookForm, {
      props: { modelValue: baseModel, loading: true, submitLabel: 'Save' },
    })
    const button = wrapper.find('button[type="submit"]').element as HTMLButtonElement
    expect(button.disabled).toBe(true)
  })

  it('shows the submitLabel on the submit button', () => {
    const wrapper = mount(BookForm, {
      props: { modelValue: baseModel, loading: false, submitLabel: 'Create Book' },
    })
    expect(wrapper.find('button[type="submit"]').text()).toBe('Create Book')
  })
})
