import { Typography, ConfigProvider, Layout, Menu, Card, Avatar, Button, theme } from 'antd'
import {
    UserOutlined,
    LogoutOutlined,
    MailOutlined,
    AppstoreOutlined,
    DollarOutlined,
    TeamOutlined,
    FileTextOutlined,
    ArrowLeftOutlined,
} from '@ant-design/icons'
import { useNavigate, Link } from 'react-router-dom'
import { DisclosureGuidelinesContent } from '../components/DisclosureGuidelinesContent'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography

const primaryColor = '#EFEE96'
const secondaryColor = '#BD72EB'
const pageBackgroundColor = '#000000'

const buildSidebarItems = (navigate: ReturnType<typeof useNavigate>, onLogout: () => void) => [
    { key: 'dashboard', icon: <AppstoreOutlined />, label: 'Dashboard', onClick: () => navigate('/influencer/dashboard') },
    { key: 'profile', icon: <UserOutlined />, label: 'Profile', onClick: () => navigate('/influencer/profile') },
    { key: 'invitations', icon: <MailOutlined />, label: 'Invitations', onClick: () => navigate('/influencer/invitations') },
    { key: 'collaborations', icon: <TeamOutlined />, label: 'Collaborations', onClick: () => navigate('/influencer/collaborations') },
    { key: 'disclosure', icon: <FileTextOutlined />, label: 'Disclosure guidelines' },
    { key: 'payments', icon: <DollarOutlined />, label: 'Payments', onClick: () => navigate('/influencer/payments') },
    { key: 'logout', icon: <LogoutOutlined />, label: 'Logout', onClick: onLogout, danger: true },
]

const GuidelinesContent = ({ onBack }: { onBack: () => void }) => (
    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
        <Button type="text" icon={<ArrowLeftOutlined />} style={{ marginBottom: 16, color: '#aaa' }} onClick={onBack}>
            Back to dashboard
        </Button>
        <Title level={1} style={{ color: secondaryColor, margin: '0 0 8px', fontSize: '2rem' }}>Disclosure documentation</Title>
        <Text type="secondary" style={{ display: 'block', marginBottom: 24 }}>
            Review these guidelines anytime. You must acknowledge them when accepting a campaign invitation.
        </Text>
        <Card bordered={false} style={{ background: '#1c1c1c', borderRadius: 12, border: '1px solid #333', maxWidth: 900 }}>
            <DisclosureGuidelinesContent />
            <Text type="secondary" style={{ display: 'block', marginTop: 24 }}>
                This is general guidance only and not legal advice. Consult a lawyer for your jurisdiction and contracts.
            </Text>
            <Link to="/influencer/invitations" style={{ color: primaryColor, display: 'inline-block', marginTop: 16 }}>
                Go to invitations
            </Link>
        </Card>
    </Content>
)

export const InfluencerDisclosureGuidelines = () => {
    const navigate = useNavigate()

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: { colorPrimary: primaryColor, colorTextBase: '#ffffff', fontFamily: 'Inter, sans-serif' },
                components: {
                    Layout: { bodyBg: '#000000', headerBg: '#000000', siderBg: '#000000' },
                    Menu: { darkItemBg: '#000000', darkItemSelectedBg: '#333333' },
                },
            }}
        >
            <Layout style={{ minHeight: '100vh' }}>
                <Sider width={250} theme="dark">
                    <div style={{ padding: '20px', textAlign: 'center' }}>
                        <Title level={4} style={{ color: '#fff', margin: 0 }}>Collabry</Title>
                        <Text style={{ color: secondaryColor }}>Influencer</Text>
                    </div>
                    <Menu theme="dark" mode="inline" defaultSelectedKeys={['disclosure']} items={buildSidebarItems(navigate, handleLogout)} />
                </Sider>
                <Layout>
                    <Header style={{ padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'flex-end', background: pageBackgroundColor }}>
                        <Text style={{ color: '#aaa', fontSize: '0.9rem', marginRight: 10 }}>ID: {user?.id}</Text>
                        <Avatar size="large" icon={<UserOutlined />} style={{ backgroundColor: secondaryColor }} />
                    </Header>
                    <GuidelinesContent onBack={() => navigate('/influencer/dashboard')} />
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
