const API_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090/api/auth';

export const loginUser = async (credentials: any) => {
    const response = await fetch(`${API_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials),
    });
    if (!response.ok) {
        throw new Error('Login failed');
    }
    return response.json();
};

// ... existing code ...

// ... existing code ...

export const googleLoginUser = async (token: string) => {
    const response = await fetch(`${API_URL}/google`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token }),
    });
    if (!response.ok) {
        throw new Error('Google login failed');
    }
    return response.json();
};

export const forgotPassword = async (email: string) => {
    const response = await fetch(`${API_URL}/forgot-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
    });
    if (!response.ok) {
        const err = await response.json();
        throw new Error(err.message || 'Failed to send reset email');
    }
    return response.json();
};

export const resetPassword = async (token: string, newPassword: string) => {
    const response = await fetch(`${API_URL}/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword }),
    });
    if (!response.ok) {
        const err = await response.json();
        throw new Error(err.message || 'Failed to reset password');
    }
    return response.json();
};
