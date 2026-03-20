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
import { InfluencerSearch } from './InfluencerSearch'
import * as influencerProfileService from '../services/influencerProfileService'
import * as campaignService from '../services/campaignService'
import * as invitationService from '../services/invitationService'

vi.mock('../services/influencerProfileService')
vi.mock('../services/campaignService')
vi.mock('../services/invitationService')

describe('InfluencerSearch', () => {
  const mockSearchInfluencers = vi.mocked(influencerProfileService.searchInfluencers)
  const mockGetMyCampaigns = vi.mocked(campaignService.getMyCampaigns)
  const mockCreateInvitation = vi.mocked(invitationService.createInvitation)

  beforeEach(() => {
    vi.clearAllMocks()
    mockSearchInfluencers.mockResolvedValue([])
    mockGetMyCampaigns.mockResolvedValue([])
    mockCreateInvitation.mockResolvedValue({} as invitationService.InvitationResponse)
  })

  const renderWithRouter = () => {
    return render(
      <MemoryRouter>
        <InfluencerSearch />
      </MemoryRouter>
    )
  }

  it('calls searchInfluencers with form values when Search is clicked', async () => {
    const user = userEvent.setup()
    mockSearchInfluencers.mockResolvedValue([])
    renderWithRouter()

    const nicheInput = screen.getByPlaceholderText(/e.g. Fashion/i)
    await user.type(nicheInput, 'Fashion')
    const searchButton = screen.getByRole('button', { name: /search/i })
    await user.click(searchButton)

    expect(mockSearchInfluencers).toHaveBeenCalledWith(
      expect.objectContaining({
        niche: 'Fashion',
      })
    )
  })

  it('displays search results and Invite button for each influencer', async () => {
    const mockInfluencers = [
      {
        id: 1,
        userId: 10,
        name: 'Jane Doe',
        age: 25,
        location: 'NYC',
        niche: 'Fashion',
        isComplete: true,
        followerCount: 50000,
        engagementRate: 3.5,
      } as influencerProfileService.InfluencerProfileResponse,
    ]
    mockSearchInfluencers.mockResolvedValue(mockInfluencers)
    renderWithRouter()

    const searchButton = screen.getByRole('button', { name: /search/i })
    await userEvent.click(searchButton)

    expect(await screen.findByText('Jane Doe')).toBeInTheDocument()
    const inviteButtons = screen.getAllByRole('button', { name: /invite/i })
    expect(inviteButtons.length).toBeGreaterThanOrEqual(1)
  })

  it('opens invite modal with campaign select when Invite is clicked', async () => {
    const mockInfluencers = [
      {
        id: 1,
        userId: 10,
        name: 'Jane',
        age: 25,
        location: 'NYC',
        niche: 'Fashion',
        isComplete: true,
      } as influencerProfileService.InfluencerProfileResponse,
    ]
    mockSearchInfluencers.mockResolvedValue(mockInfluencers)
    mockGetMyCampaigns.mockResolvedValue([
      { id: 1, name: 'Campaign One', status: 'ACTIVE', userId: 1, budgetRange: 'ONE_K_5K' },
    ])
    renderWithRouter()

    await userEvent.click(screen.getByRole('button', { name: /search/i }))
    await screen.findByText('Jane')
    await userEvent.click(screen.getByRole('button', { name: /invite/i }))

    expect(await screen.findByText(/invite jane/i)).toBeInTheDocument()
    expect(mockGetMyCampaigns).toHaveBeenCalled()
  })
})
