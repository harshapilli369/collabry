import { useState, useEffect } from 'react'
import { Card, Typography, Button, Descriptions, Avatar, Spin, Space } from 'antd'
import { ArrowLeftOutlined, EditOutlined, GlobalOutlined, InstagramOutlined, LinkedinOutlined, TwitterOutlined, CheckCircleFilled } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyBrandProfile, type BrandProfileResponse, BUDGET_RANGE_OPTIONS } from '../services/brandService'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'

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

    const primaryColor = BRAND_PORTAL_PRIMARY
    const textColor = '#ffffff'
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
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#000000' }}>
                <Spin size="large" />
            </div>
        )
    }

    if (!profile) {
        return null
    }

    return (
        <BrandPortalLayout activeMenuKey="profile" brandProfileForHeader={profile}>
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
        </BrandPortalLayout>
    )
}
