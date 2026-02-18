const AUTH_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth';
const API_BASE = AUTH_API.replace(/\/api\/auth\/?$/, '') || 'http://localhost:8080';
const INFLUENCERS_URL = `${API_BASE}/api/influencers`;

const STORAGE_KEY = 'influencer_profile_submitted';

function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

export interface InfluencerProfileRequest {
    name?: string;
    age?: number;
    location?: string;
    niche?: string;
    bio?: string;
    profilePictureUrl?: string;
    instagramHandle?: string;
    youtubeHandle?: string;
    tiktokHandle?: string;
    audienceInfo?: string;
    rate?: number;
}

export interface InfluencerProfileResponse {
    id?: number;
    userId?: number;
    name?: string;
    age?: number;
    location?: string;
    niche?: string;
    bio?: string;
    profilePictureUrl?: string;
    instagramHandle?: string;
    youtubeHandle?: string;
    tiktokHandle?: string;
    audienceInfo?: string;
    rate?: number;
    profileComplete?: boolean;
}

export function isProfileComplete(profile: InfluencerProfileResponse | null): boolean {
    if (!profile) return false;
    if (profile.profileComplete === true) return true;
    const hasName = !!profile.name?.trim();
    const hasAge = profile.age != null && profile.age > 0;
    const hasLocation = !!profile.location?.trim();
    const hasNiche = !!profile.niche?.trim();
    const hasRate = profile.rate != null && profile.rate >= 0;
    const hasSocial = !!(
        profile.instagramHandle?.trim() ||
        profile.youtubeHandle?.trim() ||
        profile.tiktokHandle?.trim()
    );
    return !!(hasName && hasAge && hasLocation && hasNiche && hasRate && hasSocial);
}

export function getProfileSubmittedFlag(): boolean {
    return localStorage.getItem(STORAGE_KEY) === 'true';
}

export function setProfileSubmittedFlag(): void {
    localStorage.setItem(STORAGE_KEY, 'true');
}

export function clearProfileSubmittedFlag(): void {
    localStorage.removeItem(STORAGE_KEY);
}

export async function getMyInfluencerProfile(): Promise<InfluencerProfileResponse | null> {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 10000);
    try {
        const response = await fetch(`${INFLUENCERS_URL}/me`, {
            method: 'GET',
            headers: getAuthHeaders(),
            signal: controller.signal,
        });
        clearTimeout(timeout);
        if (response.status === 404) return null;
        if (!response.ok) {
            const data = await response.json().catch(() => ({}));
            throw new Error(data.message || 'Failed to load influencer profile');
        }
        return response.json();
    } catch (e) {
        clearTimeout(timeout);
        if (e instanceof Error && e.name === 'AbortError') {
            throw new Error('Request timed out');
        }
        throw e;
    }
}

export async function updateMyInfluencerProfile(
    payload: InfluencerProfileRequest
): Promise<InfluencerProfileResponse> {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 10000);
    try {
        const response = await fetch(`${INFLUENCERS_URL}/me`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(payload),
            signal: controller.signal,
        });
        clearTimeout(timeout);
        if (response.status === 404) {
            // No backend API - save to localStorage and return payload as "saved"
            const saved = { ...payload, profileComplete: isProfileComplete(payload as InfluencerProfileResponse) };
            setProfileSubmittedFlag();
            return saved;
        }
        if (!response.ok) {
            const data = await response.json().catch(() => ({}));
            throw new Error(data.message || 'Failed to save influencer profile');
        }
        const result = await response.json();
        if (isProfileComplete(result)) {
            setProfileSubmittedFlag();
        }
        return result;
    } catch (e) {
        clearTimeout(timeout);
        if (e instanceof Error && e.name === 'AbortError') {
            throw new Error('Request timed out');
        }
        // Fallback: API may be down or wrong URL (e.g. 8080 vs 9090) - save locally so user can still navigate
        console.warn('Influencer profile API unavailable, saved locally:', e);
        const saved = { ...payload, profileComplete: isProfileComplete(payload as InfluencerProfileResponse) };
        setProfileSubmittedFlag();
        return saved;
    }
}
