import { useState, useEffect } from 'react'
import { Card, Typography, Button, ConfigProvider, Layout, Menu, Descriptions, theme, Avatar, Spin, Tag, Space, Row, Col, Rate } from 'antd'
import { UserOutlined, LogoutOutlined, AppstoreOutlined, ArrowLeftOutlined, EditOutlined, InstagramOutlined, YoutubeOutlined, CheckCircleFilled, DollarOutlined, MailOutlined, TeamOutlined, StarFilled } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyInfluencerProfile, type InfluencerProfileResponse, type RatingResponse } from '../services/influencerProfileService'

const { Content, Header, Sider } = Layout
const { Title, Text, Paragraph } = Typography

export const ViewInfluencerProfile = () => {
    const [profile, setProfile] = useState<InfluencerProfileResponse | null>(null)
    const [loading, setLoading] = useState(true)
    const navigate = useNavigate()
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    useEffect(() => {
        getMyInfluencerProfile()
            .then((data) => {
                if (!data) {
                    navigate('/influencer/profile/edit', { replace: true })
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

    const primaryColor = '#EFEE96'
    const secondaryColor = '#BD72EB'
    const textColor = '#ffffff'
    const pageBackgroundColor = '#000000'
    const cardBackgroundColor = '#141414'

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: pageBackgroundColor }}>
                <Spin size="large" />
            </div>
        )
    }

    const TiktokSvg = () => (
        <svg viewBox="0 0 448 512" width="14px" height="14px" fill="currentColor" style={{ verticalAlign: '-0.125em', marginRight: 8 }}>
            <path d="M448,209.91a210.06,210.06,0,0,1-122.77-39.25V349.38A162.55,162.55,0,1,1,185,188.31V278.2a74.62,74.62,0,1,0,52.23,71.18V0l88,0a121.18,121.18,0,0,0,1.86,22.17h0A122.18,122.18,0,0,0,381,102.39a121.43,121.43,0,0,0,67,20.14Z"/>
        </svg>
    )

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
                        <Text style={{ color: secondaryColor }}>Influencer</Text>
                    </div>
                    <Menu
                        theme="dark"
                        mode="inline"
                        selectedKeys={['2']}
                        items={[
                            {
                                key: '1',
                                icon: <AppstoreOutlined />,
                                label: 'Dashboard',
                                onClick: () => navigate('/influencer/dashboard'),
                            },
                            {
                                key: '2',
                                icon: <UserOutlined />,
                                label: 'Profile',
                                onClick: () => navigate('/influencer/profile'),
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
                <Layout style={{ backgroundColor: pageBackgroundColor }}>
                    <Header style={{ padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'flex-end', background: pageBackgroundColor }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <Text style={{ color: '#fff', fontSize: '1.1rem', fontWeight: 500 }}>
                                {(() => {
                                    let handle = profile?.instagramHandle || profile?.tiktokHandle || profile?.youtubeHandle || profile?.name || user?.email;
                                    if (handle && !handle.startsWith('@') && !handle.includes('@')) {
                                        handle = `@${handle}`;
                                    }
                                    return handle;
                                })()}
                            </Text>
                            {user?.isVerified && <CheckCircleFilled style={{ color: '#1890ff', fontSize: '1.2rem' }} title="Verified Influencer" />}
                        </div>
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
                        <Button
                            type="link"
                            icon={<ArrowLeftOutlined />}
                            onClick={() => navigate('/influencer/dashboard')}
                            style={{ color: secondaryColor, paddingLeft: 0, marginBottom: 16 }}
                        >
                            Back to Dashboard
                        </Button>
                        
                        <Row gutter={[24, 24]}>
                            <Col xs={24} md={16}>
                                <Card 
                                    bordered={false} 
                                    style={{ backgroundColor: cardBackgroundColor, borderRadius: 12, height: '100%' }}
                                    title={
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 16, paddingTop: 8 }}>
                                            {profile?.profilePictureUrl ? (
                                                <Avatar size={80} src={profile.profilePictureUrl} style={{ border: `2px solid ${secondaryColor}` }} />
                                            ) : (
                                                <Avatar size={80} icon={<UserOutlined />} style={{ backgroundColor: secondaryColor, color: '#000' }} />
                                            )}
                                            <div style={{ display: 'flex', flexDirection: 'column' }}>
                                                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                                    <Title level={2} style={{ margin: 0, color: textColor }}>{profile?.name}</Title>
                                                    {user?.isVerified && <CheckCircleFilled style={{ color: '#1890ff', fontSize: '1.5rem' }} />}
                                                </div>
                                                <Text type="secondary" style={{ fontSize: '1.1rem' }}>{profile?.niche} • {profile?.location}</Text>
                                                <div style={{ marginTop: 8 }}>
                                                    {profile?.openToCollaborations !== false ? (
                                                        <Tag color="green">Open to collaborations</Tag>
                                                    ) : (
                                                        <Tag>Not accepting new collabs</Tag>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                    }
                                    extra={
                                        <Button 
                                            type="primary" 
                                            icon={<EditOutlined />} 
                                            style={{ color: '#000', fontWeight: 600, backgroundColor: secondaryColor, borderColor: secondaryColor }}
                                            onClick={() => navigate('/influencer/profile/edit')}
                                        >
                                            Edit Profile
                                        </Button>
                                    }
                                >
                                    <Space direction="vertical" size="large" style={{ width: '100%', marginTop: 16 }}>
                                        {profile?.bio && (
                                            <div>
                                                <Title level={5} style={{ color: secondaryColor }}>Bio</Title>
                                                <Paragraph style={{ color: '#d9d9d9', fontSize: '1.1rem' }}>
                                                    {profile.bio}
                                                </Paragraph>
                                            </div>
                                        )}

                                        <div>
                                            <Title level={5} style={{ color: secondaryColor, marginBottom: 16 }}>Social Reach</Title>
                                            <Space size="large" wrap>
                                                {profile?.instagramHandle && (
                                                    <Tag icon={<InstagramOutlined />} style={{ background: 'linear-gradient(45deg, #f09433 0%, #e6683c 25%, #dc2743 50%, #cc2366 75%, #bc1888 100%)', color: '#fff', padding: '6px 12px', fontSize: '1rem', borderRadius: 8, border: 'none' }}>
                                                        @{profile.instagramHandle}
                                                    </Tag>
                                                )}
                                                {profile?.tiktokHandle && (
                                                    <Tag color="#000000" style={{ color: '#ffffff', padding: '6px 12px', fontSize: '1rem', borderRadius: 8, border: '1px solid #333' }}>
                                                        <TiktokSvg /> @{profile.tiktokHandle}
                                                    </Tag>
                                                )}
                                                {profile?.youtubeHandle && (
                                                    <Tag icon={<YoutubeOutlined />} color="#FF0000" style={{ padding: '6px 12px', fontSize: '1rem', borderRadius: 8 }}>
                                                        @{profile.youtubeHandle}
                                                    </Tag>
                                                )}
                                                {(!profile?.instagramHandle && !profile?.tiktokHandle && !profile?.youtubeHandle) && (
                                                    <Text type="secondary">No social accounts connected.</Text>
                                                )}
                                            </Space>
                                        </div>

                                        {profile?.audienceInfo && (
                                            <div>
                                                <Title level={5} style={{ color: secondaryColor }}>Audience Demographics</Title>
                                                <Paragraph style={{ color: '#d9d9d9' }}>
                                                    {profile.audienceInfo}
                                                </Paragraph>
                                            </div>
                                        )}
                                    </Space>
                                </Card>
                            </Col>

                            <Col xs={24} md={8}>
                                <Card bordered={false} style={{ backgroundColor: cardBackgroundColor, borderRadius: 12 }}>
                                    <Title level={5} style={{ color: secondaryColor }}>Pricing & Details</Title>
                                    <div style={{ margin: '24px 0', textAlign: 'center', padding: '24px', background: '#111', borderRadius: 8, border: '1px solid #333' }}>
                                        <Text type="secondary" style={{ fontSize: '1rem' }}>Standard Rate</Text>
                                        <Title level={2} style={{ margin: '8px 0 0', color: primaryColor }}>
                                            ${profile?.rate?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                                        </Title>
                                        <Text type="secondary" style={{ fontSize: '0.8rem' }}>per post / collab</Text>
                                    </div>
                                    <Descriptions column={1} bordered size="small" style={{ marginTop: 24 }}>
                                        <Descriptions.Item label="Age">{profile?.age}</Descriptions.Item>
                                        <Descriptions.Item label="Profile Status">
                                            {user?.isVerified ? <Text type="success">Verified</Text> : <Text type="warning">Unverified</Text>}
                                        </Descriptions.Item>
                                    </Descriptions>
                                </Card>

                                {(profile?.totalRatings != null && profile.totalRatings > 0) && (
                                    <Card bordered={false} style={{ backgroundColor: cardBackgroundColor, borderRadius: 12, marginTop: 24 }}>
                                        <Title level={5} style={{ color: secondaryColor, marginBottom: 12 }}>
                                            <StarFilled style={{ marginRight: 8, color: primaryColor }} />
                                            Ratings & Reviews
                                        </Title>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 16 }}>
                                            <Rate disabled allowHalf value={profile.averageRating ?? 0} style={{ color: primaryColor }} />
                                            <Text style={{ color: '#d9d9d9' }}>
                                                {typeof profile.averageRating === 'number' ? profile.averageRating.toFixed(1) : '0'} ({profile.totalRatings} {profile.totalRatings === 1 ? 'review' : 'reviews'})
                                            </Text>
                                        </div>
                                        {profile.recentReviews && profile.recentReviews.length > 0 && (
                                            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                                                {profile.recentReviews.map((r: RatingResponse) => (
                                                    <div key={r.id} style={{ padding: '12px 0', borderBottom: '1px solid #333' }}>
                                                        <Rate disabled value={r.rating} count={5} style={{ fontSize: 12, color: primaryColor }} />
                                                        {r.review && (
                                                            <Paragraph style={{ color: '#d9d9d9', margin: '6px 0 0', fontSize: '0.9rem' }}>{r.review}</Paragraph>
                                                        )}
                                                    </div>
                                                ))}
                                            </Space>
                                        )}
                                    </Card>
                                )}
                            </Col>
                        </Row>
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
