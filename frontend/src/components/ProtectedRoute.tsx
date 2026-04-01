import { Navigate, Outlet } from 'react-router-dom';

interface ProtectedRouteProps {
    allowedRole?: 'INFLUENCER' | 'BRAND' | 'ADMIN';
}

const ROLE_DASHBOARDS: Record<string, string> = {
    INFLUENCER: '/influencer/dashboard',
    BRAND: '/brand/dashboard',
    ADMIN: '/admin/dashboard',
};

export const ProtectedRoute = ({ allowedRole }: ProtectedRouteProps) => {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;

    if (!token || !user) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRole && user.role !== allowedRole) {
        const redirect = ROLE_DASHBOARDS[user.role] ?? '/';
        return <Navigate to={redirect} replace />;
    }

    return <Outlet />;
};
