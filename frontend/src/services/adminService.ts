const AUTH_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth'
const API_BASE = AUTH_API.replace(/\/api\/auth\/?$/, '') || 'http://localhost:8080'
const ADMIN_URL = `${API_BASE}/api/admin`

function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('token')
  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  }
}

export type AdminRole = 'USER' | 'ADMIN' | 'BRAND' | 'INFLUENCER'

export interface AdminRecentSignup {
  id: number
  email: string
  role: AdminRole
  createdAt: string | null
  active: boolean
  flagged: boolean
}

export interface AdminActiveCollaboration {
  invitationId: number
  campaignId: number
  brandId: number
  influencerId: number
  updatedAt: string | null
}

export interface AdminDashboardData {
  brandCount: number
  influencerCount: number
  campaignCount: number
  recentSignups: AdminRecentSignup[]
  activeCollaborations: AdminActiveCollaboration[]
  paymentsByStatus: Record<string, number>
}

export interface AdminUserSummary {
  id: number
  email: string
  role: AdminRole
  active: boolean
  flagged: boolean
  createdAt: string | null
}

export interface AdminUserPage {
  content: AdminUserSummary[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export async function fetchAdminDashboard(): Promise<AdminDashboardData> {
  const res = await fetch(`${ADMIN_URL}/dashboard`, { headers: getAuthHeaders() })
  if (res.status === 403) {
    throw new Error('Admin access required')
  }
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error((data as { message?: string }).message || 'Failed to load dashboard')
  }
  return res.json()
}

export async function fetchAdminUsers(page = 0, size = 20): Promise<AdminUserPage> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  const res = await fetch(`${ADMIN_URL}/users?${params}`, { headers: getAuthHeaders() })
  if (res.status === 403) {
    throw new Error('Admin access required')
  }
  if (!res.ok) {
    const data = await res.json().catch(() => ({}))
    throw new Error((data as { message?: string }).message || 'Failed to load users')
  }
  return res.json()
}

export async function updateAdminUser(
  userId: number,
  body: { active: boolean; flagged: boolean }
): Promise<AdminUserSummary> {
  const res = await fetch(`${ADMIN_URL}/users/${userId}`, {
    method: 'PUT',
    headers: getAuthHeaders(),
    body: JSON.stringify(body),
  })
  const data = await res.json().catch(() => ({}))
  if (!res.ok) {
    throw new Error((data as { message?: string }).message || 'Update failed')
  }
  return data as AdminUserSummary
}
