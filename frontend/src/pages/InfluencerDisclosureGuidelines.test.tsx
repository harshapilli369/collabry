import '@testing-library/jest-dom'
import { describe, it, expect, vi } from 'vitest'

Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation((query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { InfluencerDisclosureGuidelines } from './InfluencerDisclosureGuidelines'
import { DISCLOSURE_GUIDELINES_TITLE } from '../content/disclosureGuidelines'

describe('InfluencerDisclosureGuidelines', () => {
  it('renders standalone disclosure documentation for influencers', () => {
    render(
      <MemoryRouter initialEntries={['/influencer/disclosure-guidelines']}>
        <Routes>
          <Route path="/influencer/disclosure-guidelines" element={<InfluencerDisclosureGuidelines />} />
        </Routes>
      </MemoryRouter>
    )
    expect(screen.getByRole('heading', { name: /disclosure documentation/i })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: DISCLOSURE_GUIDELINES_TITLE })).toBeInTheDocument()
  })
})
