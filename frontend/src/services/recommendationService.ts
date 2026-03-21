const AUTH_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth';
const API_BASE = AUTH_API.replace(/\/api\/auth\/?$/, '') || 'http://localhost:8080';
const CAMPAIGNS_URL = `${API_BASE}/api/campaigns`;

function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

export interface InfluencerRecommendationDTO {
    influencerId: number;
    matchScore: number;
    reason: string;
    name?: string;
    niche?: string;
    profilePictureUrl?: string;
}

export async function getCampaignRecommendations(campaignId: number): Promise<InfluencerRecommendationDTO[]> {
    const response = await fetch(`${CAMPAIGNS_URL}/${campaignId}/recommendations`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load recommendations');
    }
    return response.json();
}
