import '@testing-library/jest-dom'
import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { DisclosureGuidelinesContent } from './DisclosureGuidelinesContent'
import { DISCLOSURE_GUIDELINES_TITLE } from '../content/disclosureGuidelines'

describe('DisclosureGuidelinesContent', () => {
  it('renders the guidelines title and bullet topics', () => {
    render(<DisclosureGuidelinesContent />)
    expect(screen.getByTestId('disclosure-guidelines-content')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: DISCLOSURE_GUIDELINES_TITLE })).toBeInTheDocument()
    expect(screen.getAllByText(/paid partnership/i).length).toBeGreaterThanOrEqual(1)
  })
})
