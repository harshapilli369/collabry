const AUTH_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth';
const API_BASE = AUTH_API.replace(/\/api\/auth\/?$/, '') || 'http://localhost:8080';
const RATINGS_URL = `${API_BASE}/api/ratings`;

function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

export interface RatingRequest {
    invitationId: number;
    rating: number;
    review?: string;
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

export async function submitRating(request: RatingRequest): Promise<RatingResponse> {
    const response = await fetch(RATINGS_URL, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(request),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to submit rating');
    }
    return response.json();
}
