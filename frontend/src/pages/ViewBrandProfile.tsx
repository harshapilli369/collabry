import { useState, useEffect } from 'react'
import { Card, Typography, Button, ConfigProvider, Layout, Menu, Descriptions, theme, Avatar, Spin, Space } from 'antd'
import { UserOutlined, LogoutOutlined, AppstoreOutlined, ArrowLeftOutlined, EditOutlined, GlobalOutlined, InstagramOutlined, LinkedinOutlined, TwitterOutlined, CheckCircleFilled, FundProjectionScreenOutlined, DollarOutlined, PlusCircleOutlined, UnorderedListOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyBrandProfile, type BrandProfileResponse, BUDGET_RANGE_OPTIONS } from '../services/brandService'

const { Content, Sider } = Layout
const { Title, Text, Paragraph } = Typography

export const ViewBrandProfile = () => {
    const [profile, setProfile] = useState<BrandProfileResponse | null>(null)
    const [loading, setLoading] = useState(true)
    const navigate = useNavigate()
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    useEffect(() => {
        getMyBrandProfile()
            .then((data) => {
                if (!data) {
                    navigate('/brand/profile/edit', { replace: true })
                    return
                }
                setProfile(data)
            })
            .catch(() => {})
            .finally(() => setLoading(false))
    }, [navigate])

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    const primaryColor = '#FFFD82'
    const textColor = '#ffffff'
    const pageBackgroundColor = '#000000'
    const cardBackgroundColor = '#141414'

    const formatSocialHandle = (url: string | undefined) => {
        if (!url) return '';
        try {
            const cleanUrl = url.endsWith('/') ? url.slice(0, -1) : url;
            const segments = cleanUrl.split('/');
            const handle = segments[segments.length - 1];
            return handle.startsWith('@') ? handle : `@${handle}`;
        } catch {
            return url;
        }
    }

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: pageBackgroundColor }}>
                <Spin size="large" />
            </div>
        )
    }

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
                    Layout: {
                        bodyBg: '#000000',
                        headerBg: '#000000',
                        siderBg: '#000000',
                    },
                    Menu: {
                        darkItemBg: '#000000',
                        darkItemSelectedBg: '#333333',
                    },
                    Descriptions: {
                        colorTextSecondary: '#8c8c8c',
                    }
                },
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
                        selectedKeys={['profile']}
                        items={[
                            {
                                key: 'dashboard',
                                icon: <AppstoreOutlined />,
                                label: 'Dashboard',
                                onClick: () => navigate('/brand/dashboard'),
                            },
                            {
                                key: 'campaign',
                                icon: <FundProjectionScreenOutlined />,
                                label: 'Campaign',
                                children: [
                                    {
                                        key: 'campaign-create',
                                        icon: <PlusCircleOutlined />,
                                        label: 'Create campaign',
                                        onClick: () => navigate('/brand/campaigns/create'),
                                    },
                                    {
                                        key: 'campaign-view',
                                        icon: <UnorderedListOutlined />,
                                        label: 'View my campaigns',
                                    },
                                ],
                            },
                            {
                                key: 'payments',
                                icon: <DollarOutlined />,
                                label: 'Payments',
                                onClick: () => navigate('/brand/payments'),
                            },
                            {
                                key: 'profile',
                                icon: <UserOutlined />,
                                label: 'Profile',
                                onClick: () => navigate('/brand/profile'),
                            },
                            {
                                key: 'logout',
                                icon: <LogoutOutlined />,
                                label: 'Logout',
                                onClick: handleLogout,
                                danger: true,
                            },
                        ]}
                    />
                </Sider>
                <Layout style={{ backgroundColor: pageBackgroundColor }}>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
                        <Button
                            type="link"
                            icon={<ArrowLeftOutlined />}
                            onClick={() => navigate('/brand/dashboard')}
                            style={{ color: primaryColor, paddingLeft: 0, marginBottom: 16 }}
                        >
                            Back to Dashboard
                        </Button>
                        
                        <Card 
                            bordered={false} 
                            style={{ backgroundColor: cardBackgroundColor, borderRadius: 12 }}
                            title={
                                <div style={{ display: 'flex', alignItems: 'center', gap: 16, paddingTop: 8 }}>
                                    {profile?.logoUrl ? (
                                        <Avatar size={64} src={profile.logoUrl} style={{ border: `2px solid ${primaryColor}` }} />
                                    ) : (
                                        <Avatar size={64} style={{ backgroundColor: primaryColor, color: '#000', fontSize: '2rem' }}>
                                            {profile?.name?.charAt(0)?.toUpperCase()}
                                        </Avatar>
                                    )}
                                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                            <Title level={2} style={{ margin: 0, color: textColor }}>{profile?.name}</Title>
                                            {user?.isVerified && <CheckCircleFilled style={{ color: '#1890ff', fontSize: '1.5rem' }} />}
                                        </div>
                                        <Text type="secondary">{profile?.industry}</Text>
                                    </div>
                                </div>
                            }
                            extra={
                                <Button 
                                    type="primary" 
                                    icon={<EditOutlined />} 
                                    style={{ color: '#000', fontWeight: 600 }}
                                    onClick={() => navigate('/brand/profile/edit')}
                                >
                                    Edit Profile
                                </Button>
                            }
                        >
                            <Space direction="vertical" size="large" style={{ width: '100%', marginTop: 24 }}>
                                {profile?.description && (
                                    <div>
                                        <Title level={5} style={{ color: primaryColor }}>About Us</Title>
                                        <Paragraph style={{ color: '#d9d9d9', fontSize: '1.1rem' }}>
                                            {profile.description}
                                        </Paragraph>
                                    </div>
                                )}

                                <div>
                                    <Title level={5} style={{ color: primaryColor, marginBottom: 16 }}>Details</Title>
                                    <Descriptions column={{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }} bordered size="middle">
                                        <Descriptions.Item label="Contact Email">{profile?.email}</Descriptions.Item>
                                        <Descriptions.Item label="Typical Budget">
                                            {BUDGET_RANGE_OPTIONS.find(o => o.value === profile?.budgetRange)?.label || profile?.budgetRange || 'Not specified'}
                                        </Descriptions.Item>
                                    </Descriptions>
                                </div>

                                <div>
                                    <Title level={5} style={{ color: primaryColor, marginBottom: 16 }}>Links & Socials</Title>
                                    <Space size="middle" wrap>
                                        {profile?.website && (
                                            <Button type="default" icon={<GlobalOutlined />} href={profile.website} target="_blank">Website</Button>
                                        )}
                                        {profile?.instagramUrl && (
                                            <Button type="default" icon={<InstagramOutlined />} href={profile.instagramUrl} target="_blank" style={{ color: '#E1306C', borderColor: '#E1306C' }}>
                                                {formatSocialHandle(profile.instagramUrl)}
                                            </Button>
                                        )}
                                        {profile?.linkedInUrl && (
                                            <Button type="default" icon={<LinkedinOutlined />} href={profile.linkedInUrl} target="_blank" style={{ color: '#0077B5', borderColor: '#0077B5' }}>
                                                {formatSocialHandle(profile.linkedInUrl)}
                                            </Button>
                                        )}
                                        {profile?.twitterUrl && (
                                            <Button type="default" icon={<TwitterOutlined />} href={profile.twitterUrl} target="_blank" style={{ color: '#1DA1F2', borderColor: '#1DA1F2' }}>
                                                {formatSocialHandle(profile.twitterUrl)}
                                            </Button>
                                        )}
                                    </Space>
                                    {(!profile?.website && !profile?.instagramUrl && !profile?.linkedInUrl && !profile?.twitterUrl) && (
                                        <Text type="secondary">No links provided.</Text>
                                    )}
                                </div>
                            </Space>
                        </Card>
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
