import { useEffect, useState } from 'react'
import { Typography, Button, ConfigProvider, Spin, theme } from 'antd'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { confirmEmail } from '../services/authService'

const { Title, Text } = Typography

export const ConfirmEmail = () => {
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const token = searchParams.get('token')
    const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading')
    const [errorMessage, setErrorMessage] = useState<string>('')

    useEffect(() => {
        if (!token) {
            setStatus('error')
            setErrorMessage('Missing confirmation link.')
            return
        }
        confirmEmail(token)
            .then((data: { token: string; email: string; role: string; id: number }) => {
                localStorage.setItem('token', data.token)
                localStorage.setItem('user', JSON.stringify({ id: data.id, email: data.email, role: data.role }))
                setStatus('success')
                if (data.role === 'INFLUENCER') {
                    navigate('/influencer/profile/edit', { replace: true })
                } else {
                    navigate('/brand/dashboard', { replace: true })
                }
            })
            .catch((err) => {
                setStatus('error')
                setErrorMessage(err instanceof Error ? err.message : 'Invalid or expired link.')
            })
    }, [token])

    const primaryColor = '#FFFD82'
    const textColor = '#ffffff'
    const pageBackgroundColor = '#000000'
    const cardBackgroundColor = '#141414'

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: {
                    colorPrimary: primaryColor,
                    colorText: textColor,
                    borderRadius: 8,
                    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
                },
                components: {
                    Button: {},
                },
            }}
        >
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '100vh',
                backgroundColor: pageBackgroundColor,
            }}>
                <div style={{
                    width: '100%',
                    maxWidth: 400,
                    padding: 40,
                    backgroundColor: cardBackgroundColor,
                    borderRadius: 16,
                    boxShadow: '0 4px 20px rgba(0,0,0,0.3)',
                    textAlign: 'center',
                }}>
                    <div style={{ marginBottom: 24 }}>
                        <img src="/logo.png" alt="Collabry Logo" style={{ height: 60, borderRadius: 8 }} />
                    </div>
                    {status === 'loading' && (
                        <>
                            <Spin size="large" style={{ marginBottom: 16 }} />
                            <Title level={4} style={{ color: textColor }}>Confirming your email...</Title>
                            <Text type="secondary">Please wait.</Text>
                        </>
                    )}
                    {status === 'success' && (
                        <>
                            <Title level={4} style={{ color: textColor }}>Email confirmed</Title>
                            <Text type="secondary">Redirecting...</Text>
                        </>
                    )}
                    {status === 'error' && (
                        <>
                            <Title level={4} style={{ color: textColor }}>Confirmation failed</Title>
                            <Text type="secondary" style={{ display: 'block', marginBottom: 24 }}>
                                {errorMessage}
                            </Text>
                            <Button type="primary" size="large" style={{ color: '#000000' }} onClick={() => navigate('/signup')}>
                                Sign up again
                            </Button>
                        </>
                    )}
                </div>
            </div>
        </ConfigProvider>
    )
}
