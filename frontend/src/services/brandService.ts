const AUTH_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth';
const API_BASE = AUTH_API.replace(/\/api\/auth\/?$/, '') || 'http://localhost:8080';
const BRANDS_URL = `${API_BASE}/api/brands`;

function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

export type BudgetRange =
    | 'UNDER_1K'
    | 'ONE_K_5K'
    | 'FIVE_K_10K'
    | 'TEN_K_50K'
    | 'FIFTY_K_PLUS';

export const BUDGET_RANGE_OPTIONS: { value: BudgetRange; label: string }[] = [
    { value: 'UNDER_1K', label: 'Under $1,000' },
    { value: 'ONE_K_5K', label: '$1,000 – $5,000' },
    { value: 'FIVE_K_10K', label: '$5,000 – $10,000' },
    { value: 'TEN_K_50K', label: '$10,000 – $50,000' },
    { value: 'FIFTY_K_PLUS', label: '$50,000+' },
];

export interface BrandProfileRequest {
    name: string;
    industry: string;
    website: string;
    email: string;
    logoUrl?: string;
    description?: string;
    instagramUrl?: string;
    linkedInUrl?: string;
    twitterUrl?: string;
    budgetRange?: BudgetRange;
}

export interface BrandProfileResponse {
    id: number;
    userId: number;
    name: string;
    industry: string;
    website: string;
    email: string;
    logoUrl?: string;
    description?: string;
    instagramUrl?: string;
    linkedInUrl?: string;
    twitterUrl?: string;
    budgetRange?: BudgetRange;
    verified?: boolean;
    createdAt?: string;
    updatedAt?: string;
}

export async function getMyBrandProfile(): Promise<BrandProfileResponse | null> {
    const response = await fetch(`${BRANDS_URL}/me`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (response.status === 404) return null;
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load brand profile');
    }
    return response.json();
}

export async function updateMyBrandProfile(payload: BrandProfileRequest): Promise<BrandProfileResponse> {
    const response = await fetch(`${BRANDS_URL}/me`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to save brand profile');
    }
    return response.json();
}

export async function getBrandProfile(brandId: number): Promise<BrandProfileResponse | null> {
    const response = await fetch(`${BRANDS_URL}/${brandId}/profile`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (response.status === 404) return null;
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load brand profile');
    }
    return response.json();
}
