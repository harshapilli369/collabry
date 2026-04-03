const API_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090/api/auth';

const getAuthHeaders = () => {
    const headers: Record<string, string> = {
        'Content-Type': 'application/json',
    };
    const token = localStorage.getItem('token');
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    return headers;
};

const getUsersApiUrl = () => API_URL.replace('/auth', '/users');

export interface InfluencerSearchResult {
    id: number;
    email: string;
    displayName: string;
}

export const userService = {
    linkSocialAccount: async (platform: string, handle: string): Promise<void> => {
        const response = await fetch(`${getUsersApiUrl()}/me/link-social`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify({ platform, handle }),
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || 'Failed to link social account');
        }
    },

    /** List influencers with id, email, displayName (for finding user IDs). */
    listInfluencers: async (): Promise<InfluencerSearchResult[]> => {
        const response = await fetch(`${getUsersApiUrl()}/influencers`, { method: 'GET', headers: getAuthHeaders() });
        if (!response.ok) {
            const data = await response.json().catch(() => ({}));
            throw new Error(data.message || 'Failed to load influencers');
        }
        return response.json();
    },

    requestVerification: async (): Promise<void> => {
        const response = await fetch(`${getUsersApiUrl()}/me/verification/request`, {
            method: 'POST',
            headers: getAuthHeaders(),
        });
        if (!response.ok) {
            const data = await response.json().catch(() => ({}));
            throw new Error(data.message || 'Failed to request verification');
        }
    },

    getVerificationStatus: async (): Promise<any> => {
        const response = await fetch(`${getUsersApiUrl()}/me/verification/status`, {
            method: 'GET',
            headers: getAuthHeaders(),
        });
        if (response.status === 204) return null;
        if (!response.ok) {
            const data = await response.json().catch(() => ({}));
            throw new Error(data.message || 'Failed to check verification status');
        }
        return response.json();
    },
};
