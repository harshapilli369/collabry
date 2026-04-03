import { useEffect, useState } from 'react'
import { Typography, Button, ConfigProvider, Spin, theme } from 'antd'
import { CheckCircleFilled, CloseCircleFilled } from '@ant-design/icons'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { confirmEmail } from '../services/authService'

const { Title, Text } = Typography
const PRIMARY = '#FFFD82'
const PURPLE = '#BD72EB'

const ROLE_DESTINATIONS: Record<string, string> = {
    INFLUENCER: '/influencer/profile/edit',
    BRAND: '/brand/dashboard',
}

const handleConfirmSuccess = (
    data: { token: string; email: string; role: string; id: number },
    setStatus: (s: 'success') => void,
    navigate: (path: string, opts: { replace: boolean }) => void
) => {
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({ id: data.id, email: data.email, role: data.role }))
    setStatus('success')
    setTimeout(() => {
        navigate(ROLE_DESTINATIONS[data.role] ?? '/', { replace: true })
    }, 2000)
}

export const ConfirmEmail = () => {
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const token = searchParams.get('token')
    const [status, setStatus] = useState<'loading' | 'success' | 'error'>(token ? 'loading' : 'error')
    const [errorMessage, setErrorMessage] = useState<string>(token ? '' : 'Missing confirmation link.')

    useEffect(() => {
        if (!token) return
        confirmEmail(token)
            .then((data: { token: string; email: string; role: string; id: number }) => {
                handleConfirmSuccess(data, setStatus, navigate)
            })
            .catch((err) => {
                setStatus('error')
                setErrorMessage(err instanceof Error ? err.message : 'Invalid or expired link.')
            })
    }, [token])

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: { colorPrimary: PRIMARY, colorText: '#ffffff', borderRadius: 8, fontFamily: 'Inter, sans-serif' },
            }}
        >
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: '#000', position: 'relative', overflow: 'hidden' }}>
                <div style={{ position: 'absolute', top: '20%', left: '30%', width: 400, height: 400, borderRadius: '50%', background: `radial-gradient(circle, ${PRIMARY}06 0%, transparent 70%)`, pointerEvents: 'none' }} />
                <div style={{ position: 'absolute', bottom: '25%', right: '25%', width: 300, height: 300, borderRadius: '50%', background: `radial-gradient(circle, ${PURPLE}06 0%, transparent 70%)`, pointerEvents: 'none' }} />

                <div style={{ width: '100%', maxWidth: 420, padding: '48px 40px', backgroundColor: '#0d0d0d', borderRadius: 20, border: `1px solid ${PRIMARY}10`, boxShadow: '0 8px 40px rgba(0,0,0,0.4)', textAlign: 'center', position: 'relative', zIndex: 1 }}>
                    <div style={{ width: 52, height: 52, borderRadius: 14, background: `linear-gradient(135deg, ${PRIMARY}, ${PURPLE})`, display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 24px', fontWeight: 900, fontSize: 24, color: '#000' }}>
                        C
                    </div>

                    {status === 'loading' && (
                        <>
                            <Spin size="large" style={{ marginBottom: 20 }} />
                            <Title level={3} style={{ color: '#fff', margin: 0 }}>Confirming your email...</Title>
                            <Text style={{ color: '#666', display: 'block', marginTop: 8 }}>Please wait a moment.</Text>
                        </>
                    )}

                    {status === 'success' && (
                        <>
                            <CheckCircleFilled style={{ fontSize: 48, color: '#52c41a', marginBottom: 16 }} />
                            <Title level={3} style={{ color: '#fff', margin: 0 }}>Email Confirmed!</Title>
                            <Text style={{ color: '#666', display: 'block', marginTop: 8 }}>Redirecting you to your dashboard...</Text>
                        </>
                    )}

                    {status === 'error' && (
                        <>
                            <CloseCircleFilled style={{ fontSize: 48, color: '#ff4d4f', marginBottom: 16 }} />
                            <Title level={3} style={{ color: '#fff', margin: 0 }}>Confirmation Failed</Title>
                            <Text style={{ color: '#666', display: 'block', marginTop: 8, marginBottom: 24 }}>{errorMessage}</Text>
                            <Button type="primary" size="large" onClick={() => navigate('/signup')} style={{ color: '#000', fontWeight: 600, borderRadius: 12 }}>
                                Sign up again
                            </Button>
                        </>
                    )}
                </div>
            </div>
        </ConfigProvider>
    )
}
