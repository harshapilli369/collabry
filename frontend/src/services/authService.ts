export const loginUser = async (credentials: any) => {
    const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials),
    });
    if (!response.ok) {
        throw new Error('Login failed');
    }
    return response.json();
};

export const googleLoginUser = async (token: string) => {
    const response = await fetch('http://localhost:8080/api/auth/google', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token }),
    });
    if (!response.ok) {
        throw new Error('Google login failed');
    }
    return response.json();
};
