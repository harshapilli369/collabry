const API_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090/api/auth';

export interface RegisterPayload {
    email: string;
    password: string;
    role: 'BRAND' | 'INFLUENCER';
}

export interface SignupResponse {
    message: string;
}

export const registerUser = async (payload: RegisterPayload): Promise<SignupResponse> => {
    const response = await fetch(`${API_URL}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
    });
    if (response.status === 409) {
        const data = await response.json();
        throw new Error(data.message || 'An account with this email already exists.');
    }
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        const message = data.message || (response.status === 400 ? 'Invalid input. Check email and password rules.' : 'Registration failed');
        throw new Error(message);
    }
    return response.json();
};

/** Confirm email with token from link; returns JWT and user so you can log in and redirect to profile-setup. */
export const confirmEmail = async (token: string) => {
    const response = await fetch(`${API_URL}/confirm-email?token=${encodeURIComponent(token)}`);
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Invalid or expired link. Please sign up again.');
    }
    return response.json();
};

export const loginUser = async (credentials: any) => {
    const response = await fetch(`${API_URL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials),
    });
    if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        const message = (body && typeof body.message === 'string') ? body.message : `Login failed (${response.status})`;
        throw new Error(message);
    }
    return response.json();
};

// ... existing code ...

// ... existing code ...

export const googleLoginUser = async (token: string, role: 'BRAND' | 'INFLUENCER') => {
    const response = await fetch(`${API_URL}/google`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, role }),
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
