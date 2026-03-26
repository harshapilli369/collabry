import { describe, it, expect } from 'vitest'
import {
  DISCLOSURE_ACKNOWLEDGMENT_LABEL,
  DISCLOSURE_GUIDELINES_TITLE,
  DISCLOSURE_POINTS,
} from './disclosureGuidelines'

describe('disclosureGuidelines', () => {
  it('exports title and mandatory acknowledgment copy for compliance UX', () => {
    expect(DISCLOSURE_GUIDELINES_TITLE.length).toBeGreaterThan(10)
    expect(DISCLOSURE_ACKNOWLEDGMENT_LABEL).toMatch(/read|understand/i)
    expect(DISCLOSURE_ACKNOWLEDGMENT_LABEL).toMatch(/disclos/i)
  })

  it('includes actionable disclosure points for influencers', () => {
    expect(DISCLOSURE_POINTS.length).toBeGreaterThanOrEqual(4)
    expect(DISCLOSURE_POINTS.some((p) => /sponsored|paid|partner/i.test(p))).toBe(true)
  })
})
