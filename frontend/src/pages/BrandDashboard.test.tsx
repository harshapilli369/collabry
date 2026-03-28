import '@testing-library/jest-dom'
import { describe, it, expect, vi, beforeEach } from 'vitest'

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
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { BrandDashboard } from './BrandDashboard'
import * as brandService from '../services/brandService'
import * as campaignService from '../services/campaignService'
import * as invitationService from '../services/invitationService'
import * as paymentService from '../services/paymentService'

vi.mock('../services/brandService')
vi.mock('../services/campaignService')
vi.mock('../services/invitationService')
vi.mock('../services/paymentService')

describe('BrandDashboard', () => {
  const mockGetMyBrandProfile = vi.mocked(brandService.getMyBrandProfile)
  const mockGetMyCampaigns = vi.mocked(campaignService.getMyCampaigns)
  const mockGetSentInvitations = vi.mocked(invitationService.getSentInvitations)
  const mockWithdrawInvitation = vi.mocked(invitationService.withdrawInvitation)
  const mockUpdateInvitation = vi.mocked(invitationService.updateInvitation)
  const mockGetMyPayments = vi.mocked(paymentService.getMyPayments)

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.setItem('user', JSON.stringify({ role: 'BRAND', id: 1, email: 'brand@test.com' }))
    mockGetMyBrandProfile.mockResolvedValue({
      id: 1,
      userId: 1,
      name: 'Test Co',
      industry: 'Technology',
      website: 'https://test.example',
      email: 'brand@test.com',
    })
    mockGetMyCampaigns.mockResolvedValue([])
    mockGetSentInvitations.mockResolvedValue([])
    mockWithdrawInvitation.mockResolvedValue()
    mockUpdateInvitation.mockResolvedValue({} as invitationService.InvitationResponse)
    mockGetMyPayments.mockResolvedValue([])
  })

  const renderWithRouter = () => {
    return render(
      <MemoryRouter>
        <BrandDashboard />
      </MemoryRouter>
    )
  }

  it('loads sent invitations and shows status', async () => {
    mockGetSentInvitations.mockResolvedValue([
      {
        id: 1,
        campaignId: 5,
        influencerId: 20,
        brandId: 1,
        status: 'PENDING',
        proposedAmount: 500,
      } as invitationService.InvitationResponse,
    ])
    renderWithRouter()

    expect(await screen.findByText('Sent Invitations')).toBeInTheDocument()
    expect(mockGetSentInvitations).toHaveBeenCalled()
  })

  it('Withdraw button calls withdrawInvitation for PENDING invitation', async () => {
    mockGetSentInvitations.mockResolvedValue([
      {
        id: 100,
        campaignId: 1,
        influencerId: 20,
        brandId: 1,
        status: 'PENDING',
      } as invitationService.InvitationResponse,
    ])
    mockGetMyCampaigns.mockResolvedValue([{ id: 1, name: 'My Campaign', status: 'ACTIVE', userId: 1, budgetRange: 'ONE_K_5K' }])
    renderWithRouter()

    await screen.findByText('Sent Invitations')
    const withdrawButtons = screen.getAllByRole('button', { name: /withdraw/i })
    expect(withdrawButtons.length).toBeGreaterThanOrEqual(1)
    await userEvent.click(withdrawButtons[0])

    expect(mockWithdrawInvitation).toHaveBeenCalledWith(100)
  })

  it('Edit button opens modal with invitation details', async () => {
    mockGetSentInvitations.mockResolvedValue([
      {
        id: 100,
        campaignId: 1,
        influencerId: 20,
        brandId: 1,
        status: 'PENDING',
        brandMessage: 'Hello',
        proposedAmount: 500,
      } as invitationService.InvitationResponse,
    ])
    mockGetMyCampaigns.mockResolvedValue([{ id: 1, name: 'My Campaign', status: 'ACTIVE', userId: 1, budgetRange: 'ONE_K_5K' }])
    renderWithRouter()

    await screen.findByText('Sent Invitations')
    const editButtons = screen.getAllByRole('button', { name: /edit/i })
    expect(editButtons.length).toBeGreaterThanOrEqual(1)
    await userEvent.click(editButtons[0])

    expect(await screen.findByText('Edit invitation')).toBeInTheDocument()
  })
})
