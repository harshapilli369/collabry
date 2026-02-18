import { Typography, Button, ConfigProvider } from 'antd'
import { useNavigate } from 'react-router-dom'

const { Title, Text } = Typography

export const Dashboard = () => {
    const navigate = useNavigate()
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    const email = user?.email ?? ''

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    const primaryColor = '#FFFD82'
    const textColor = '#000000'
    const pageBackgroundColor = '#1E1E1E'
    const cardBackgroundColor = '#FFFFFF'

    return (
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: primaryColor,
                    colorText: textColor,
                    borderRadius: 8,
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
                    <Title level={2} style={{ color: textColor }}>Welcome to Collabry</Title>
                    <Text style={{ display: 'block', marginBottom: 24, color: 'rgba(0,0,0,0.7)' }}>
                        You are logged in as {email || 'user'}
                    </Text>
                    <Button type="primary" onClick={handleLogout}>
                        Log out
                    </Button>
                </div>
            </div>
        </ConfigProvider>
    )
}
