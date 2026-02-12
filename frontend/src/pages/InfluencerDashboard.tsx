import { Typography, Button, ConfigProvider, Layout, Menu, Card, Row, Col, Avatar } from 'antd'
import { UserOutlined, LogoutOutlined, MailOutlined, AppstoreOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography

export const InfluencerDashboard = () => {
    const navigate = useNavigate()
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    const email = user?.email ?? 'Influencer'

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    const primaryColor = '#EFEE96'; // Neon Yellow-Green
    const secondaryColor = '#BD72EB'; // Soft Purple
    const textColor = '#000000';
    const pageBackgroundColor = '#1E1E1E';

    return (
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: primaryColor,
                    colorTextBase: textColor,
                    fontFamily: 'Inter, sans-serif',
                },
                components: {
                    Button: {
                        colorTextLightSolid: textColor,
                    },
                    Layout: {
                        bodyBg: pageBackgroundColor,
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
                            },
                            {
                                key: '3',
                                icon: <MailOutlined />,
                                label: 'Invitations',
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
                            <Avatar size="large" icon={<UserOutlined />} style={{ backgroundColor: secondaryColor }} />
                        </div>
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
                        <div style={{ marginBottom: 30 }}>
                            <Title level={1} style={{ color: secondaryColor, margin: 0, fontSize: '3rem' }}>Welcome!</Title>
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
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20, padding: 20, background: '#f9f9f9', borderRadius: 8 }}>
                                        <div>
                                            <Title level={5} style={{ margin: 0 }}>Summer Fashion 2026</Title>
                                            <Text type="secondary">Nike • Due in 2 days</Text>
                                        </div>
                                        <Button type="primary" size="small">Submit Content</Button>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: 20, background: '#f9f9f9', borderRadius: 8 }}>
                                        <div>
                                            <Title level={5} style={{ margin: 0 }}>Eco-Friendly Water Bottle</Title>
                                            <Text type="secondary">HydroFlask • In Review</Text>
                                        </div>
                                        <Button size="small">View Feedback</Button>
                                    </div>
                                </Card>
                            </Col>
                            <Col span={8}>
                                <Card title="Pending Invitations" bordered={false} style={{ borderRadius: 12, height: '100%' }}>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 15 }}>
                                        <div style={{ padding: 15, background: '#f9f9f9', borderRadius: 8 }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                                                <Text strong>TechGadgets Inc.</Text>
                                                <Text type="secondary" style={{ fontSize: 12 }}>Today</Text>
                                            </div>
                                            <Text type="secondary" style={{ display: 'block', marginBottom: 10 }}>Review new noise-cancelling headphones.</Text>
                                            <div style={{ display: 'flex', gap: 10 }}>
                                                <Button type="primary" size="small" block>Accept</Button>
                                                <Button size="small" block>Decline</Button>
                                            </div>
                                        </div>
                                        <div style={{ padding: 15, background: '#f9f9f9', borderRadius: 8 }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
                                                <Text strong>BeautyBox</Text>
                                                <Text type="secondary" style={{ fontSize: 12 }}>Yesterday</Text>
                                            </div>
                                            <Text type="secondary" style={{ display: 'block', marginBottom: 10 }}>Monthly subscription unboxing.</Text>
                                            <Button size="small" block>View Details</Button>
                                        </div>
                                    </div>
                                </Card>
                            </Col>
                        </Row>
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
