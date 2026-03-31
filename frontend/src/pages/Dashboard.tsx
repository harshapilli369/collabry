import { Typography, Button, ConfigProvider, theme } from 'antd'
import { useNavigate } from 'react-router-dom'

const { Title, Text } = Typography

const PRIMARY_COLOR = '#FFFD82'
const TEXT_COLOR = '#ffffff'
const PAGE_BG = '#000000'
const CARD_BG = '#141414'

const WelcomeCard = ({ email, onLogout }: { email: string; onLogout: () => void }) => (
    <div style={{ width: '100%', maxWidth: 400, padding: 40, backgroundColor: CARD_BG, borderRadius: 16, boxShadow: '0 4px 20px rgba(0,0,0,0.3)', textAlign: 'center' }}>
        <Title level={2} style={{ color: TEXT_COLOR }}>Welcome to Collabry</Title>
        <Text style={{ display: 'block', marginBottom: 24, color: 'rgba(0,0,0,0.7)' }}>
            You are logged in as {email || 'user'}
        </Text>
        <Button type="primary" onClick={onLogout}>Log out</Button>
    </div>
)

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

    return (
        <ConfigProvider theme={{ algorithm: theme.darkAlgorithm, token: { colorPrimary: PRIMARY_COLOR, colorText: TEXT_COLOR, borderRadius: 8 } }}>
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: PAGE_BG }}>
                <WelcomeCard email={email} onLogout={handleLogout} />
            </div>
        </ConfigProvider>
    )
}
