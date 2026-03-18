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

export const userService = {
    linkSocialAccount: async (platform: string, handle: string): Promise<void> => {
        // VITE_API_BASE_URL usually points to /api/auth.
        const usersApiUrl = API_URL.replace('/auth', '/users');
        const response = await fetch(`${usersApiUrl}/me/link-social`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify({ platform, handle }),
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || 'Failed to link social account');
        }
    },
};
