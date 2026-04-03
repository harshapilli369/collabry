//admin dashboard
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
import { MemoryRouter } from 'react-router-dom'
import { AdminDashboard } from './AdminDashboard'
import * as adminService from '../services/adminService'

vi.mock('../services/adminService')

describe('AdminDashboard', () => {
  const mockFetchDashboard = vi.mocked(adminService.fetchAdminDashboard)
  const mockFetchUsers = vi.mocked(adminService.fetchAdminUsers)

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.setItem('user', JSON.stringify({ role: 'ADMIN', id: 1, email: 'admin@test.com' }))
    mockFetchDashboard.mockResolvedValue({
      brandCount: 2,
      influencerCount: 3,
      campaignCount: 5,
      recentSignups: [
        {
          id: 10,
          email: 'new@example.com',
          role: 'BRAND',
          createdAt: '2025-01-15T10:00:00Z',
          active: true,
          flagged: false,
        },
      ],
      activeCollaborations: [
        {
          invitationId: 99,
          campaignId: 1,
          brandId: 2,
          influencerId: 3,
          updatedAt: '2025-02-01T12:00:00Z',
        },
      ],
      paymentsByStatus: { PENDING: 4, PAID: 2 },
    })
    mockFetchUsers.mockResolvedValue({
      content: [
        {
          id: 5,
          email: 'u@test.com',
          role: 'INFLUENCER',
          active: true,
          flagged: false,
          createdAt: null,
        },
      ],
      totalElements: 1,
      totalPages: 1,
      number: 0,
      size: 10,
    })
  })

  it('loads dashboard and user list on mount', async () => {
    render(
      <MemoryRouter>
        <AdminDashboard />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(mockFetchDashboard).toHaveBeenCalledTimes(1)
      expect(mockFetchUsers).toHaveBeenCalled()
    })

    expect(await screen.findByText('new@example.com')).toBeInTheDocument()
    expect(screen.getByText('Brands')).toBeInTheDocument()
    expect(screen.getByText('new@example.com')).toBeInTheDocument()
    expect(screen.getByText('PENDING: 4')).toBeInTheDocument()
    expect(screen.getByText('u@test.com')).toBeInTheDocument()
  })
})
