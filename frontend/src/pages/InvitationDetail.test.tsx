import '@testing-library/jest-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { InvitationDetail } from './InvitationDetail'
import * as invitationService from '../services/invitationService'
import { DISCLOSURE_GUIDELINES_TITLE } from '../content/disclosureGuidelines'

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

vi.mock('../services/invitationService', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../services/invitationService')>()
  return {
    ...actual,
    getInvitationById: vi.fn(),
    respondToInvitation: vi.fn(),
    negotiateInvitation: vi.fn(),
  }
})

const mockInvitation: invitationService.InvitationDetailResponse = {
  id: 1,
  campaignId: 10,
  influencerId: 5,
  brandId: 2,
  status: 'PENDING',
  brandMessage: 'Hi!',
  createdAt: '2026-01-01T00:00:00Z',
  campaign: {
    id: 10,
    userId: 2,
    name: 'Summer Drop',
    description: 'Promo',
    budgetRange: 'MID',
    status: 'ACTIVE',
  },
}

function renderDetail() {
  return render(
    <MemoryRouter initialEntries={['/influencer/invitations/1']}>
      <Routes>
        <Route path="/influencer/invitations/:id" element={<InvitationDetail />} />
      </Routes>
    </MemoryRouter>
  )
}

describe('InvitationDetail disclosure before accept', () => {
  const getInvitationById = vi.mocked(invitationService.getInvitationById)
  const respondToInvitation = vi.mocked(invitationService.respondToInvitation)

  beforeEach(() => {
    vi.clearAllMocks()
    getInvitationById.mockResolvedValue(mockInvitation)
    respondToInvitation.mockResolvedValue({ ...mockInvitation, status: 'ACCEPTED' })
  })

  it('opens a modal with disclosure guidelines when Accept is clicked', async () => {
    const user = userEvent.setup()
    renderDetail()

    await screen.findByTestId('invitation-accept-open-disclosure')

    await user.click(screen.getByTestId('invitation-accept-open-disclosure'))

    const dialog = await screen.findByRole('dialog')
    expect(within(dialog).getByText(DISCLOSURE_GUIDELINES_TITLE)).toBeInTheDocument()
    expect(within(dialog).getByTestId('disclosure-guidelines-content')).toBeInTheDocument()
  })

  it('does not call accept API until acknowledgment is checked and confirmed', async () => {
    const user = userEvent.setup()
    renderDetail()

    await screen.findByTestId('invitation-accept-open-disclosure')
    await user.click(screen.getByTestId('invitation-accept-open-disclosure'))

    const dialog = await screen.findByRole('dialog')
    let confirmBtn = within(dialog).getByRole('button', { name: /confirm acceptance/i })
    expect(confirmBtn).toBeDisabled()

    await user.click(confirmBtn)
    expect(respondToInvitation).not.toHaveBeenCalled()

    await user.click(within(dialog).getByRole('checkbox'))

    await waitFor(() => {
      confirmBtn = within(screen.getByRole('dialog')).getByRole('button', { name: /confirm acceptance/i })
      expect(confirmBtn).not.toBeDisabled()
    })
    await user.click(within(screen.getByRole('dialog')).getByRole('button', { name: /confirm acceptance/i }))

    await waitFor(() =>
      expect(respondToInvitation).toHaveBeenCalledWith(1, { action: 'ACCEPT' })
    )
  })

  it('shows a link to full disclosure documentation on the invitation page', async () => {
    renderDetail()
    await screen.findByTestId('invitation-accept-open-disclosure')
    const link = screen.getByRole('link', { name: /disclosure guidelines/i })
    expect(link).toHaveAttribute('href', '/influencer/disclosure-guidelines')
  })
})
