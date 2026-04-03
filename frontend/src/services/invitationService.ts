const AUTH_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth';
const API_BASE = AUTH_API.replace(/\/api\/auth\/?$/, '') || 'http://localhost:8080';
const INVITATIONS_URL = `${API_BASE}/api/invitations`;
const COLLABORATIONS_URL = `${API_BASE}/api/collaborations`;
const CAMPAIGNS_URL = `${API_BASE}/api/campaigns`;

function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

export type InvitationStatus =
    | 'PENDING'
    | 'NEGOTIATING'
    | 'ACCEPTED'
    | 'REJECTED'
    | 'CONFIRMED'
    | 'EXPIRED'
    | 'WITHDRAWN';

export const INVITATION_STATUS_LABELS: Record<InvitationStatus, string> = {
    PENDING: 'Sent',
    NEGOTIATING: 'Negotiating',
    ACCEPTED: 'Accepted',
    REJECTED: 'Rejected',
    CONFIRMED: 'Confirmed',
    EXPIRED: 'Expired',
    WITHDRAWN: 'Withdrawn',
};

export interface InvitationResponse {
    id: number;
    campaignId: number;
    influencerId: number;
    brandId: number;
    status: InvitationStatus;
    brandMessage?: string;
    proposedAmount?: number;
    proposedTimeline?: string;
    proposedDeliverables?: string;
    platform?: string;
    expiresAt?: string;
    createdAt?: string;
    updatedAt?: string;
    respondedAt?: string;
    /** True if the brand has already submitted a rating for this invitation. */
    rated?: boolean;

    // Brand profile fields
    brandName?: string;
    brandLogo?: string;
    brandNiche?: string;

    // Influencer profile fields
    influencerName?: string;
    influencerProfilePicture?: string;
    influencerNiche?: string;
    influencerRate?: string;

    // Campaign name
    campaignName?: string;

    // Deliverable tracking fields
    deliverableStatus?: string;
    contentLink?: string;
    deliverableNotes?: string;
}

export interface CampaignResponse {
    id: number;
    userId: number;
    name: string;
    description?: string;
    budgetRange: string;
    status: string;
    campaignGoal?: string;
    preferredContentTypes?: string;
    startDate?: string;
    endDate?: string;
    numberOfInfluencers?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface InvitationDetailResponse extends InvitationResponse {
    campaign?: CampaignResponse;
}

export interface InvitationRequest {
    influencerId: number;
    message?: string;
    proposedAmount?: number;
    proposedTimeline?: string;
    proposedDeliverables?: string;
    platform?: string;
    expiresInDays?: number;
}

export interface RespondRequest {
    action: 'ACCEPT' | 'REJECT';
}

export interface NegotiationRequest {
    proposedAmount?: number;
    proposedTimeline?: string;
    proposedDeliverables?: string;
}

export async function getMyInvitations(): Promise<InvitationResponse[]> {
    const response = await fetch(`${INVITATIONS_URL}/me`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load invitations');
    }
    return response.json();
}

export async function getInvitationById(id: number): Promise<InvitationDetailResponse> {
    const response = await fetch(`${INVITATIONS_URL}/${id}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load invitation');
    }
    return response.json();
}

export async function respondToInvitation(id: number, request: RespondRequest): Promise<InvitationResponse> {
    const response = await fetch(`${INVITATIONS_URL}/${id}/respond`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to respond');
    }
    return response.json();
}

export async function negotiateInvitation(id: number, request: NegotiationRequest): Promise<InvitationResponse> {
    const response = await fetch(`${INVITATIONS_URL}/${id}/negotiate`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to submit negotiation');
    }
    return response.json();
}

export async function confirmTerms(id: number): Promise<InvitationResponse> {
    const response = await fetch(`${INVITATIONS_URL}/${id}/confirm-terms`, {
        method: 'POST',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to confirm terms');
    }
    return response.json();
}

export async function getMyCollaborations(): Promise<InvitationResponse[]> {
    const response = await fetch(`${COLLABORATIONS_URL}/me`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load collaborations');
    }
    return response.json();
}

export async function getMyInvitationsAsBrand(): Promise<InvitationResponse[]> {
    const response = await fetch(`${INVITATIONS_URL}/brand/me`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load invitations');
    }
    return response.json();
}

export async function getSentInvitations(): Promise<InvitationResponse[]> {
    const response = await fetch(`${INVITATIONS_URL}/sent`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load sent invitations');
    }
    return response.json();
}

export interface UpdateInvitationRequest {
    message?: string;
    proposedAmount?: number;
    proposedTimeline?: string;
    proposedDeliverables?: string;
    platform?: string;
}

export async function withdrawInvitation(id: number): Promise<void> {
    const response = await fetch(`${INVITATIONS_URL}/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to withdraw invitation');
    }
}

export async function updateInvitation(id: number, request: UpdateInvitationRequest): Promise<InvitationResponse> {
    const response = await fetch(`${INVITATIONS_URL}/${id}`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to update invitation');
    }
    return response.json();
}

export async function createInvitation(campaignId: number, request: InvitationRequest): Promise<InvitationResponse> {
    const response = await fetch(`${CAMPAIGNS_URL}/${campaignId}/invitations`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({})) as { message?: string; error?: string };
        throw new Error(data.message || data.error || 'Failed to send invitation');
    }
    return response.json();
}

export type DeliverableStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'SUBMITTED' | 'APPROVED';

export const DELIVERABLE_STATUS_LABELS: Record<DeliverableStatus, string> = {
    NOT_STARTED: 'Not Started',
    IN_PROGRESS: 'In Progress',
    SUBMITTED: 'Submitted',
    APPROVED: 'Approved',
};

export interface DeliverableUpdateRequest {
    deliverableStatus?: string;
    contentLink?: string;
    deliverableNotes?: string;
}

export async function updateDeliverable(invitationId: number, request: DeliverableUpdateRequest): Promise<InvitationResponse> {
    const response = await fetch(`${COLLABORATIONS_URL}/${invitationId}/deliverable`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'Failed to update deliverable');
    }
    return response.json();
}

export async function approveDeliverable(invitationId: number): Promise<InvitationResponse> {
    const response = await fetch(`${COLLABORATIONS_URL}/${invitationId}/approve`, {
        method: 'PUT',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.error || 'Failed to approve deliverable');
    }
    return response.json();
}
