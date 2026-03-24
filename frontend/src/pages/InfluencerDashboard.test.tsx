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

import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { InfluencerDashboard } from './InfluencerDashboard'
import * as invitationService from '../services/invitationService'
import * as influencerProfileService from '../services/influencerProfileService'

vi.mock('../services/invitationService')
vi.mock('../services/influencerProfileService')

describe('InfluencerDashboard', () => {
  const mockGetInvitations = vi.mocked(invitationService.getMyInvitations)
  const mockGetProfile = vi.mocked(influencerProfileService.getMyInfluencerProfile)
  const mockUpdateAvailability = vi.mocked(influencerProfileService.updateCollaborationAvailability)

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.setItem('user', JSON.stringify({ email: 'inf@test.com', isVerified: true }))
    localStorage.setItem('token', 't')
    mockGetInvitations.mockResolvedValue([])
    mockGetProfile.mockResolvedValue({
      id: 1,
      userId: 5,
      name: 'Test Inf',
      age: 22,
      location: 'Halifax',
      niche: 'Lifestyle',
      complete: true,
      openToCollaborations: true,
    })
    mockUpdateAvailability.mockResolvedValue({
      id: 1,
      userId: 5,
      name: 'Test Inf',
      age: 22,
      location: 'Halifax',
      niche: 'Lifestyle',
      complete: true,
      openToCollaborations: false,
    })
  })

  it('renders collaboration availability switch and loads profile', async () => {
    render(
      <MemoryRouter>
        <InfluencerDashboard />
      </MemoryRouter>
    )

    expect(await screen.findByText(/open to collaborations/i)).toBeInTheDocument()
    const sw = screen.getByRole('switch')
    expect(sw).toBeChecked()
  })

  it('calls updateCollaborationAvailability when switch is toggled', async () => {
    const user = userEvent.setup()
    render(
      <MemoryRouter>
        <InfluencerDashboard />
      </MemoryRouter>
    )

    await waitFor(() => expect(mockGetProfile).toHaveBeenCalled())
    const sw = screen.getByRole('switch')
    await user.click(sw)

    await waitFor(() => {
      expect(mockUpdateAvailability).toHaveBeenCalledWith(false)
    })
  })
})
