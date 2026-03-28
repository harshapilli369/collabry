const AUTH_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth';
const API_BASE = AUTH_API.replace(/\/api\/auth\/?$/, '') || 'http://localhost:8080';
const INFLUENCERS_URL = `${API_BASE}/api/influencers`;

function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

export interface InfluencerProfileRequest {
    name: string;
    age: number;
    location: string;
    niche: string;
    bio?: string;
    profilePictureUrl?: string;
    instagramHandle?: string;
    youtubeHandle?: string;
    tiktokHandle?: string;
    rate?: number;
    followerCount?: number;
    engagementRate?: number;
    audienceInfo?: string;
    saveAsDraft?: boolean;
}

export interface RatingResponse {
    id: number;
    invitationId: number;
    brandId: number;
    influencerId: number;
    rating: number;
    review?: string;
    createdAt?: string;
}

export interface InfluencerProfileResponse {
    id: number;
    userId: number;
    name: string;
    age: number;
    location: string;
    niche: string;
    bio?: string;
    profilePictureUrl?: string;
    instagramHandle?: string;
    youtubeHandle?: string;
    tiktokHandle?: string;
    rate?: number;
    followerCount?: number;
    engagementRate?: number;
    audienceInfo?: string;
    complete: boolean;
    createdAt?: string;
    updatedAt?: string;
    averageRating?: number;
    totalRatings?: number;
    recentReviews?: RatingResponse[];
}

export interface InfluencerSearchParams {
    niche?: string;
    location?: string;
    minFollowers?: number;
    maxFollowers?: number;
    minEngagementRate?: number;
}

export async function getMyInfluencerProfile(): Promise<InfluencerProfileResponse | null> {
    const response = await fetch(`${INFLUENCERS_URL}/me`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (response.status === 404) return null;
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load influencer profile');
    }
    return response.json();
}

export async function updateMyInfluencerProfile(
    payload: InfluencerProfileRequest,
    saveAsDraft: boolean
): Promise<InfluencerProfileResponse> {
    const response = await fetch(`${INFLUENCERS_URL}/me`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify({ ...payload, saveAsDraft }),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to save influencer profile');
    }
    return response.json();
}

export async function enhanceBio(bio: string): Promise<string> {
    const response = await fetch(`${INFLUENCERS_URL}/enhance-bio`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify({ bio }),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to enhance bio');
    }
    const data = await response.json();
    return data.enhancedBio;
}

/** Search influencers by niche, location, followers, engagement. Brands only. */
export async function searchInfluencers(params: InfluencerSearchParams): Promise<InfluencerProfileResponse[]> {
    const sp = new URLSearchParams();
    if (params.niche != null && params.niche.trim() !== '') sp.set('niche', params.niche.trim());
    if (params.location != null && params.location.trim() !== '') sp.set('location', params.location.trim());
    if (params.minFollowers != null) sp.set('minFollowers', String(params.minFollowers));
    if (params.maxFollowers != null) sp.set('maxFollowers', String(params.maxFollowers));
    if (params.minEngagementRate != null) sp.set('minEngagementRate', String(params.minEngagementRate));
    const qs = sp.toString();
    const url = `${INFLUENCERS_URL}/search${qs ? `?${qs}` : ''}`;
    const response = await fetch(url, { method: 'GET', headers: getAuthHeaders() });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to search influencers');
    }
    return response.json();
}
