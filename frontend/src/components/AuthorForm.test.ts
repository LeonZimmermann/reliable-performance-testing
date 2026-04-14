import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import AuthorForm from './AuthorForm.vue'
import type { NewAuthor } from '../types/author'

describe('AuthorForm', () => {
  const baseModel: NewAuthor = { name: '' }

  it('renders all fields', () => {
    const wrapper = mount(AuthorForm, {
      props: { modelValue: baseModel, loading: false, submitLabel: 'Save' },
    })
    expect(wrapper.find('#name').exists()).toBe(true)
    expect(wrapper.find('#birthdate').exists()).toBe(true)
    expect(wrapper.find('#origin').exists()).toBe(true)
    expect(wrapper.find('#biography').exists()).toBe(true)
  })

  it('emits update:modelValue with updated name when name input changes', async () => {
    const wrapper = mount(AuthorForm, {
      props: { modelValue: baseModel, loading: false, submitLabel: 'Save' },
    })
    await wrapper.find('#name').setValue('Jane Austen')
    const emitted = wrapper.emitted('update:modelValue') as [NewAuthor][]
    expect(emitted).toHaveLength(1)
    expect(emitted[0][0].name).toBe('Jane Austen')
  })

  it('emits a Date object when birthdate is entered', async () => {
    const wrapper = mount(AuthorForm, {
      props: { modelValue: baseModel, loading: false, submitLabel: 'Save' },
    })
    await wrapper.find('#birthdate').setValue('1775-12-16')
    const emitted = wrapper.emitted('update:modelValue') as [NewAuthor][]
    expect(emitted[0][0].birthdate).toBeInstanceOf(Date)
    expect((emitted[0][0].birthdate as Date).toISOString().substring(0, 10)).toBe('1775-12-16')
  })

  it('emits undefined birthdate when the date field is cleared', async () => {
    const wrapper = mount(AuthorForm, {
      props: {
        modelValue: { ...baseModel, birthdate: new Date('2000-01-01') },
        loading: false,
        submitLabel: 'Save',
      },
    })
    await wrapper.find('#birthdate').setValue('')
    const emitted = wrapper.emitted('update:modelValue') as [NewAuthor][]
    expect(emitted[0][0].birthdate).toBeUndefined()
  })

  it('displays an existing Date as YYYY-MM-DD in the date input', () => {
    const wrapper = mount(AuthorForm, {
      props: {
        modelValue: { ...baseModel, birthdate: new Date('1775-12-16') },
        loading: false,
        submitLabel: 'Save',
      },
    })
    const input = wrapper.find('#birthdate').element as HTMLInputElement
    expect(input.value).toBe('1775-12-16')
  })

  it('emits submit when the form is submitted', async () => {
    const wrapper = mount(AuthorForm, {
      props: { modelValue: { name: 'Jane Austen' }, loading: false, submitLabel: 'Save' },
    })
    await wrapper.find('form').trigger('submit')
    expect(wrapper.emitted('submit')).toHaveLength(1)
  })

  it('disables the submit button while loading', () => {
    const wrapper = mount(AuthorForm, {
      props: { modelValue: baseModel, loading: true, submitLabel: 'Save' },
    })
    const button = wrapper.find('button[type="submit"]').element as HTMLButtonElement
    expect(button.disabled).toBe(true)
  })

  it('shows the submitLabel on the submit button', () => {
    const wrapper = mount(AuthorForm, {
      props: { modelValue: baseModel, loading: false, submitLabel: 'Create Author' },
    })
    expect(wrapper.find('button[type="submit"]').text()).toBe('Create Author')
  })
})
