import { useState, useEffect } from 'react'
import { Typography, Button, ConfigProvider, Layout, Menu, Card, Row, Col, Avatar, theme } from 'antd'
import { UserOutlined, LogoutOutlined, MailOutlined, AppstoreOutlined, DollarOutlined, TeamOutlined, CheckCircleFilled } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyInvitations } from '../services/invitationService'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography

export const InfluencerDashboard = () => {
    const navigate = useNavigate()
    const [invitations, setInvitations] = useState<Awaited<ReturnType<typeof getMyInvitations>>>([])

    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    useEffect(() => {
        getMyInvitations()
            .then(setInvitations)
            .catch(() => setInvitations([]))
    }, [])

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    const primaryColor = '#EFEE96'; // Neon Yellow-Green
    const secondaryColor = '#BD72EB'; // Soft Purple
    const textColor = '#ffffff';
    const pageBackgroundColor = '#000000';

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: {
                    colorPrimary: primaryColor,
                    colorTextBase: textColor,
                    fontFamily: 'Inter, sans-serif',
                },
                components: {
                    Button: {},
                    Layout: {
                        bodyBg: '#000000',
                        headerBg: '#000000',
                        siderBg: '#000000',
                    },
                    Menu: {
                        darkItemBg: '#000000',
                        darkItemSelectedBg: '#333333',
                    }
                }
            }}
        >
            <Layout style={{ minHeight: '100vh' }}>
                <Sider width={250} theme="dark">
                    <div style={{ padding: '20px', textAlign: 'center' }}>
                        <Title level={4} style={{ color: '#fff', margin: 0 }}>Collabry</Title>
                        <Text style={{ color: secondaryColor }}>Influencer</Text>
                    </div>
                    <Menu
                        theme="dark"
                        mode="inline"
                        defaultSelectedKeys={['1']}
                        items={[
                            {
                                key: '1',
                                icon: <AppstoreOutlined />,
                                label: 'Dashboard',
                            },
                            {
                                key: '2',
                                icon: <UserOutlined />,
                                label: 'Profile',
                                onClick: () => navigate('/influencer/profile-setup'),
                            },
                            {
                                key: '3',
                                icon: <MailOutlined />,
                                label: 'Invitations',
                                onClick: () => navigate('/influencer/invitations'),
                            },
                            {
                                key: 'collaborations',
                                icon: <TeamOutlined />,
                                label: 'Collaborations',
                                onClick: () => navigate('/influencer/collaborations'),
                            },
                            {
                                key: 'payments',
                                icon: <DollarOutlined />,
                                label: 'Payments',
                                onClick: () => navigate('/influencer/payments'),
                            },
                            {
                                key: '4',
                                icon: <LogoutOutlined />,
                                label: 'Logout',
                                onClick: handleLogout,
                                danger: true
                            },
                        ]}
                    />
                </Sider>
                <Layout>
                    <Header style={{ padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'flex-end', background: pageBackgroundColor }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            {/*<Text style={{ color: '#fff' }}>Welcome!</Text>*/}
                            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                                <Text style={{ color: '#aaa', fontSize: '0.9rem' }}>ID: {user?.id}</Text>
                                <Avatar size="large" icon={<UserOutlined />} style={{ backgroundColor: secondaryColor }} />
                            </div>
                        </div>
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
                        <div style={{ marginBottom: 30 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                                <Title level={1} style={{ color: secondaryColor, margin: 0, fontSize: '3rem' }}>Welcome!</Title>
                                {user?.isVerified && <CheckCircleFilled style={{ color: '#1890ff', fontSize: '2.5rem' }} title="Verified Profile" />}
                            </div>
                            <Text style={{ color: '#aaa', fontSize: '1.2rem' }}>Here's what's happening with your campaigns today.</Text>
                        </div>

                        <Row gutter={[24, 24]}>
                            {/* Stats Row */}
                            <Col span={8}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Total Earnings</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>$12,450</Title>
                                    <Text type="success">+15% from last month</Text>
                                </Card>
                            </Col>
                            <Col span={8}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Active Collabs</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>4</Title>
                                    <Text style={{ color: secondaryColor }}>2 pending approval</Text>
                                </Card>
                            </Col>
                            <Col span={8}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Engagement Rate</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>5.8%</Title>
                                    <Text type="success">+0.4% this week</Text>
                                </Card>
                            </Col>

                            {/* Main Content */}
                            <Col span={16}>
                                <Card title="Active Campaigns" bordered={false} style={{ borderRadius: 12, height: '100%' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20, padding: 20, background: '#1c1c1c', borderRadius: 8, border: '1px solid #333' }}>
                                        <div>
                                            <Title level={5} style={{ margin: 0, color: '#fff' }}>Summer Fashion 2026</Title>
                                            <Text type="secondary">Nike • Due in 2 days</Text>
                                        </div>
                                        <Button type="primary" size="small" style={{ color: '#000000' }}>Submit Content</Button>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: 20, background: '#1c1c1c', borderRadius: 8, border: '1px solid #333' }}>
                                        <div>
                                            <Title level={5} style={{ margin: 0, color: '#fff' }}>Eco-Friendly Water Bottle</Title>
                                            <Text type="secondary">HydroFlask • In Review</Text>
                                        </div>
                                        <Button size="small">View Feedback</Button>
                                    </div>
                                </Card>
                            </Col>
                            <Col span={8}>
                                <Card
                                    title="Pending Invitations"
                                    bordered={false}
                                    style={{ borderRadius: 12, height: '100%' }}
                                    extra={invitations.length > 0 ? <Button type="link" size="small" onClick={() => navigate('/influencer/invitations')}>View all</Button> : null}
                                >
                                    {invitations.length === 0 ? (
                                        <Text type="secondary">No pending invitations.</Text>
                                    ) : (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 15 }}>
                                            {invitations.filter((i) => i.status === 'PENDING' || i.status === 'NEGOTIATING').slice(0, 3).map((inv) => (
                                                <div key={inv.id} style={{ padding: 15, background: '#1c1c1c', borderRadius: 8, border: '1px solid #333' }}>
                                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                                                        <Text strong style={{ color: '#fff' }}>Campaign #{inv.campaignId}</Text>
                                                    </div>
                                                    <Text type="secondary" style={{ display: 'block', marginBottom: 10 }}>
                                                        {inv.brandMessage ? inv.brandMessage.slice(0, 60) + (inv.brandMessage.length > 60 ? '…' : '') : 'No message'}
                                                    </Text>
                                                    <Button type="primary" size="small" block onClick={() => navigate(`/influencer/invitations/${inv.id}`)} style={{ color: '#000000' }}>View & respond</Button>
                                                </div>
                                            ))}
                                            {invitations.filter((i) => i.status === 'PENDING' || i.status === 'NEGOTIATING').length > 3 && (
                                                <Button size="small" block onClick={() => navigate('/influencer/invitations')}>View all invitations</Button>
                                            )}
                                        </div>
                                    )}
                                </Card>
                            </Col>
                        </Row>
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
