import { useState, useEffect } from 'react'
import { Typography, Button, ConfigProvider, Layout, Menu, Card, Row, Col, Avatar } from 'antd'
import { UserOutlined, LogoutOutlined, PlusCircleOutlined, TeamOutlined, AppstoreOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyBrandProfile } from '../services/brandService'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography

export const BrandDashboard = () => {
    const navigate = useNavigate()
    const [profileCheckDone, setProfileCheckDone] = useState(false)
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    const email = user?.email ?? 'Brand'

    useEffect(() => {
        if (user?.role !== 'BRAND') {
            setProfileCheckDone(true)
            return
        }
        getMyBrandProfile()
            .then((profile) => {
                if (profile == null) {
                    navigate('/brand/profile', { replace: true })
                    return
                }
                setProfileCheckDone(true)
            })
            .catch(() => {
                setProfileCheckDone(true)
            })
    }, [user?.role, navigate])

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    const primaryColor = '#FFFD82'; // Neon Yellow-Green
    const textColor = '#000000';
    const pageBackgroundColor = '#1E1E1E';


    if (!profileCheckDone && user?.role === 'BRAND') {
        return null
    }

    return (
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: primaryColor,
                    colorTextBase: textColor,
                    fontFamily: 'Inter, sans-serif',
                },
                components: {
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
                        <Text style={{ color: primaryColor }}>Brand Portal</Text>
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
                                icon: <PlusCircleOutlined />,
                                label: 'Create Campaign',
                                onClick: () => navigate('/brand/campaigns/create'),
                            },
                            {
                                key: '3',
                                icon: <UserOutlined />,
                                label: 'Profile',
                                onClick: () => navigate('/brand/profile'),
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

                            <Avatar size="large" icon={<UserOutlined />} style={{ backgroundColor: primaryColor, color: '#000' }} />
                        </div>
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
                        <div style={{ marginBottom: 30 }}>
                            <Title level={1} style={{ color: primaryColor, margin: 0, fontSize: '3rem' }}>Welcome!</Title>
                            <Text style={{ color: '#aaa', fontSize: '1.2rem' }}>Overview of your improved brand performance.</Text>
                        </div>

                        <Row gutter={[24, 24]}>
                            {/* Stats Row */}
                            <Col span={6}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Active Campaigns</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>3</Title>
                                </Card>
                            </Col>
                            <Col span={6}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Total Reach</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>1.2M</Title>
                                    <Text type="success">+5% this month</Text>
                                </Card>
                            </Col>
                            <Col span={6}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Budget Spent</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>$15k</Title>
                                    <Text type="secondary">of $50k</Text>
                                </Card>
                            </Col>
                            <Col span={6}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">ROI</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>3.4x</Title>
                                    <Text type="success">Excellent</Text>
                                </Card>
                            </Col>

                            <Col span={12}>
                                <Card title="Active Campaigns" bordered={false} style={{ borderRadius: 12, height: '100%' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                                        <Text>Total Running: 2</Text>
                                        <Button type="link">View All</Button>
                                    </div>
                                    <div style={{ padding: 15, background: '#f5f5f5', borderRadius: 8, marginBottom: 10 }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                            <Text strong>Spring Collection Launch</Text>
                                            <Text type="success" strong>Active</Text>
                                        </div>
                                        <Text type="secondary">Budget: $5,000 | Influencers: 3</Text>
                                        <div style={{ marginTop: 8, width: '100%', height: 6, background: '#ddd', borderRadius: 3 }}>
                                            <div style={{ width: '60%', height: '100%', background: '#52c41a', borderRadius: 3 }}></div>
                                        </div>
                                    </div>
                                    <div style={{ padding: 15, background: '#f5f5f5', borderRadius: 8 }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                            <Text strong>Holiday Special</Text>
                                            <Text type="warning" strong>Ending Soon</Text>
                                        </div>
                                        <Text type="secondary">Budget: $10,000 | Influencers: 8</Text>
                                        <div style={{ marginTop: 8, width: '100%', height: 6, background: '#ddd', borderRadius: 3 }}>
                                            <div style={{ width: '90%', height: '100%', background: '#faad14', borderRadius: 3 }}></div>
                                        </div>
                                    </div>
                                </Card>
                            </Col>
                            <Col span={12}>
                                <Card title="Invited Influencers" bordered={false} style={{ borderRadius: 12, height: '100%' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                                        <Text>Pending Responses: 5</Text>
                                        <Button type="link">Manage</Button>
                                    </div>
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                        <div style={{ padding: 15, background: '#f5f5f5', borderRadius: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                                                <Avatar icon={<TeamOutlined />} style={{ backgroundColor: '#87d068' }} />
                                                <div>
                                                    <Text strong>Jane Doe</Text><br />
                                                    <Text type="secondary" style={{ fontSize: 12 }}>Fashion & Lifestyle</Text>
                                                </div>
                                            </div>
                                            <Button size="small">Remind</Button>
                                        </div>
                                        <div style={{ padding: 15, background: '#f5f5f5', borderRadius: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
                                                <Avatar icon={<TeamOutlined />} style={{ backgroundColor: '#1890ff' }} />
                                                <div>
                                                    <Text strong>Tech Reviewer X</Text><br />
                                                    <Text type="secondary" style={{ fontSize: 12 }}>Technology</Text>
                                                </div>
                                            </div>
                                            <Button size="small">Remind</Button>
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
