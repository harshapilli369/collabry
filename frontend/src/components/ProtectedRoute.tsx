import { Navigate, Outlet } from 'react-router-dom';

interface ProtectedRouteProps {
    allowedRole?: 'INFLUENCER' | 'BRAND' | 'ADMIN';
}

export const ProtectedRoute = ({ allowedRole }: ProtectedRouteProps) => {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');
    const user = userStr ? JSON.parse(userStr) : null;

    if (!token || !user) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRole && user.role !== allowedRole) {
        if (user.role === 'INFLUENCER') {
            return <Navigate to="/influencer/dashboard" replace />;
        } else if (user.role === 'BRAND') {
            return <Navigate to="/brand/dashboard" replace />;
        } else if (user.role === 'ADMIN') {
            return <Navigate to="/admin/dashboard" replace />;
        }
        return <Navigate to="/" replace />;
    }

    return <Outlet />;
};
