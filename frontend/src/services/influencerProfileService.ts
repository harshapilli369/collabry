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
    audienceInfo?: string;
    saveAsDraft?: boolean;
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
    audienceInfo?: string;
    isComplete: boolean;
    createdAt?: string;
    updatedAt?: string;
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
