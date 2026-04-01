import { beforeEach, describe, expect, it, vi } from 'vitest'
import { fetchAdminDashboard, fetchAdminUsers, updateAdminUser } from './adminService'

describe('adminService', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    localStorage.setItem('token', 'jwt-test')
  })

  it('fetchAdminDashboard sends Authorization and returns JSON', async () => {
    const payload = {
      brandCount: 1,
      influencerCount: 2,
      campaignCount: 3,
      recentSignups: [],
      activeCollaborations: [],
      paymentsByStatus: { PENDING: 1 },
    }
    const fetchMock = vi.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => payload,
    } as Response)

    const out = await fetchAdminDashboard()

    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining('/api/admin/dashboard'),
      expect.objectContaining({
        headers: expect.objectContaining({ Authorization: 'Bearer jwt-test' }),
      })
    )
    expect(out.brandCount).toBe(1)
    expect(out.paymentsByStatus.PENDING).toBe(1)
  })

  it('fetchAdminUsers passes page query params', async () => {
    const fetchMock = vi.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        content: [],
        totalElements: 0,
        totalPages: 0,
        number: 0,
        size: 20,
      }),
    } as Response)

    await fetchAdminUsers(2, 15)

    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringMatching(/\/api\/admin\/users\?.*page=2.*size=15/),
      expect.any(Object)
    )
  })

  it('updateAdminUser PUTs active and flagged', async () => {
    const summary = {
      id: 5,
      email: 'u@test.com',
      role: 'BRAND' as const,
      active: false,
      flagged: true,
      createdAt: null,
    }
    const fetchMock = vi.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => summary,
    } as Response)

    const out = await updateAdminUser(5, { active: false, flagged: true })

    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining('/api/admin/users/5'),
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify({ active: false, flagged: true }),
      })
    )
    expect(out.email).toBe('u@test.com')
  })
})
