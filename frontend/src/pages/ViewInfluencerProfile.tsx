import { useState, useEffect } from 'react'
import { Card, Typography, Button, ConfigProvider, Descriptions, theme, Avatar, Spin, Tag, Space, Row, Col, Rate, Alert, message } from 'antd'
import { UserOutlined, ArrowLeftOutlined, EditOutlined, InstagramOutlined, YoutubeOutlined, CheckCircleFilled, SafetyCertificateOutlined, ReloadOutlined, ClockCircleOutlined, CloseCircleOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyInfluencerProfile, type InfluencerProfileResponse } from '../services/influencerProfileService'
import { userService } from '../services/userService'
import { InfluencerPortalLayout, INFLUENCER_PORTAL_PRIMARY } from '../components/InfluencerPortalLayout'

const { Title, Text, Paragraph } = Typography

export const ViewInfluencerProfile = () => {
    const [profile, setProfile] = useState<InfluencerProfileResponse | null>(null)
    const [verification, setVerification] = useState<any>(null)
    const [isVerified, setIsVerified] = useState(false)
    const [loading, setLoading] = useState(true)
    const [requesting, setRequesting] = useState(false)
    const navigate = useNavigate()
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    useEffect(() => {
        const loadAll = async () => {
            try {
                const profileData = await getMyInfluencerProfile()
                if (!profileData) {
                    navigate('/influencer/profile/edit', { replace: true })
                    return
                }
                setProfile(profileData)
                // Use profile data as the source of truth for verification status
                const verifiedStatus = !!profileData.verified
                setIsVerified(verifiedStatus)

                // If not already verified, check latest request status for the alert box
                if (!verifiedStatus) {
                    const vData = await userService.getVerificationStatus()
                    setVerification(vData)
                    if (vData?.status === 'APPROVED') {
                        setIsVerified(true)
                    }
                }
            } catch (e) {
                console.error('Failed to load profile/verification data', e)
            } finally {
                setLoading(false)
            }
        }
        void loadAll()
    }, [navigate, user?.isVerified])

    const handleRequestVerification = async () => {
        setRequesting(true)
        try {
            await userService.requestVerification()
            message.success('Verification request submitted successfully!')
            const vData = await userService.getVerificationStatus()
            setVerification(vData)
            if (vData?.status === 'APPROVED') {
                setIsVerified(true)
            }
        } catch (e: any) {
            message.error(e.message || 'Failed to request verification')
        } finally {
            setRequesting(false)
        }
    }

    const cardBackgroundColor = '#141414'

    if (loading) {
        return (
            <ConfigProvider theme={{ algorithm: theme.darkAlgorithm }}>
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#000' }}>
                    <Spin size="large" />
                </div>
            </ConfigProvider>
        )
    }

    const TiktokSvg = () => (
        <svg viewBox="0 0 448 512" width="14px" height="14px" fill="currentColor" style={{ verticalAlign: '-0.125em', marginRight: 8 }}>
            <path d="M448,209.91a210.06,210.06,0,0,1-122.77-39.25V349.38A162.55,162.55,0,1,1,185,188.31V278.2a74.62,74.62,0,1,0,52.23,71.18V0l88,0a121.18,121.18,0,0,0,1.86,22.17h0A122.18,122.18,0,0,0,381,102.39a121.43,121.43,0,0,0,67,20.14Z"/>
        </svg>
    )

    return (
        <InfluencerPortalLayout activeMenuKey="profile" influencerProfileForHeader={profile}>
            <Button
                type="link"
                icon={<ArrowLeftOutlined />}
                onClick={() => navigate('/influencer/dashboard')}
                style={{ color: INFLUENCER_PORTAL_PRIMARY, paddingLeft: 0, marginBottom: 16 }}
            >
                Back to Dashboard
            </Button>

            <Row gutter={[24, 24]}>
                <Col xs={24} md={16}>
                    <Card
                        style={{ backgroundColor: cardBackgroundColor, borderRadius: 12 }}
                        title={
                            <div style={{ display: 'flex', alignItems: 'center', gap: 12, paddingTop: 4 }}>
                                {profile?.profilePictureUrl ? (
                                    <Avatar size={48} src={profile.profilePictureUrl} style={{ border: `2px solid ${INFLUENCER_PORTAL_PRIMARY}` }} />
                                ) : (
                                    <Avatar size={48} icon={<UserOutlined />} style={{ backgroundColor: INFLUENCER_PORTAL_PRIMARY, color: '#000' }} />
                                )}
                                <div style={{ display: 'flex', flexDirection: 'column' }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                        <Title level={4} style={{ margin: 0, color: '#fff' }}>{profile?.name}</Title>
                                        {isVerified && <CheckCircleFilled style={{ color: INFLUENCER_PORTAL_PRIMARY, fontSize: '1rem' }} />}
                                    </div>
                                    <Text type="secondary" style={{ fontSize: '0.9rem' }}>{profile?.niche} &bull; {profile?.location}</Text>
                                </div>
                            </div>
                        }
                        extra={
                            <Button
                                type="primary"
                                icon={<EditOutlined />}
                                style={{ color: '#000', fontWeight: 600 }}
                                onClick={() => navigate('/influencer/profile/edit')}
                            >
                                Edit Profile
                            </Button>
                        }
                    >
                        <Space direction="vertical" size="large" style={{ width: '100%', marginTop: 16 }}>
                            {!isVerified && (
                                <Alert
                                    message={
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                            <div>
                                                <Text strong style={{ color: '#fff' }}>
                                                    {verification?.status === 'PENDING' ? (
                                                        <><ClockCircleOutlined /> Verification Pending</>
                                                    ) : verification?.status === 'REJECTED' ? (
                                                        <><CloseCircleOutlined /> Verification Rejected</>
                                                    ) : (
                                                        <><SafetyCertificateOutlined /> Get Verified!</>
                                                    )}
                                                </Text>
                                                <Paragraph style={{ margin: '4px 0 0', color: 'rgba(255,255,255,0.7)', fontSize: '0.9rem' }}>
                                                    {verification?.status === 'PENDING' 
                                                        ? 'An admin is currently reviewing your profile to unlock premium collaboration tools.'
                                                        : verification?.status === 'REJECTED'
                                                        ? `Reason: ${verification.adminReason || 'No reason provided.'}. Please fix any profile gaps and re-apply.`
                                                        : 'Verified influencers get a gold badge and priority matching for high-budget brand campaigns.'}
                                                </Paragraph>
                                            </div>
                                            {(!verification || verification.status === 'REJECTED') && (
                                                <Button 
                                                    type="primary" 
                                                    size="small" 
                                                    icon={verification?.status === 'REJECTED' ? <ReloadOutlined /> : <SafetyCertificateOutlined />}
                                                    loading={requesting}
                                                    onClick={handleRequestVerification}
                                                    style={{ width: 'fit-content', color: '#000', fontWeight: 600 }}
                                                >
                                                    {verification?.status === 'REJECTED' ? 'Request Again' : 'Request Verification'}
                                                </Button>
                                            )}
                                        </div>
                                    }
                                    type={verification?.status === 'REJECTED' ? 'error' : 'info'}
                                    showIcon={false}
                                    style={{ 
                                        backgroundColor: verification?.status === 'REJECTED' ? '#2a1215' : '#1a1a2e', 
                                        border: `1px solid ${verification?.status === 'REJECTED' ? '#5c2223' : '#333'}`,
                                        borderRadius: 12
                                    }}
                                />
                            )}
                            
                            {profile?.bio && (
                                <div>
                                    <Title level={5} style={{ color: INFLUENCER_PORTAL_PRIMARY }}>Bio</Title>
                                    <Paragraph style={{ color: '#d9d9d9', fontSize: '1.1rem' }}>
                                        {profile.bio}
                                    </Paragraph>
                                </div>
                            )}

                            <div>
                                <Title level={5} style={{ color: INFLUENCER_PORTAL_PRIMARY, marginBottom: 16 }}>Social Reach</Title>
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
                                    <Title level={5} style={{ color: INFLUENCER_PORTAL_PRIMARY }}>Audience Demographics</Title>
                                    <Paragraph style={{ color: '#d9d9d9' }}>
                                        {profile.audienceInfo}
                                    </Paragraph>
                                </div>
                            )}
                        </Space>
                    </Card>
                </Col>

                <Col xs={24} md={8}>
                    <Card style={{ backgroundColor: cardBackgroundColor, borderRadius: 12 }}>
                        <Title level={5} style={{ color: INFLUENCER_PORTAL_PRIMARY }}>Pricing & Details</Title>
                        <div style={{ margin: '24px 0', textAlign: 'center', padding: '24px', background: '#111', borderRadius: 8, border: '1px solid #333' }}>
                            <Text type="secondary" style={{ fontSize: '1rem' }}>Standard Rate</Text>
                            <Title level={2} style={{ margin: '8px 0 0', color: INFLUENCER_PORTAL_PRIMARY }}>
                                ${profile?.rate?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                            </Title>
                            <Text type="secondary" style={{ fontSize: '0.8rem' }}>per post / collab</Text>
                        </div>
                        <Descriptions column={1} bordered size="small" style={{ marginTop: 24 }}>
                            <Descriptions.Item label="Age">{profile?.age}</Descriptions.Item>
                            <Descriptions.Item label="Profile Status">
                                {isVerified ? <Text type="success">Verified</Text> : <Text type="warning">Unverified</Text>}
                            </Descriptions.Item>
                        </Descriptions>
                    </Card>

                    {(profile?.totalRatings != null && profile.totalRatings > 0) && (
                        <Card style={{ backgroundColor: cardBackgroundColor, borderRadius: 12, marginTop: 16 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 16 }}>
                                <Title level={5} style={{ color: INFLUENCER_PORTAL_PRIMARY, margin: 0 }}>Reviews</Title>
                                <Rate disabled allowHalf value={profile.averageRating ?? 0} style={{ fontSize: 14, color: '#FFFD82' }} />
                                <Text style={{ color: '#aaa', fontSize: '0.85rem' }}>
                                    {typeof profile.averageRating === 'number' ? profile.averageRating.toFixed(1) : '0'}/5 ({profile.totalRatings})
                                </Text>
                            </div>
                            {profile.recentReviews && profile.recentReviews.length > 0 ? (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                    {profile.recentReviews.map((rev) => (
                                        <div key={rev.id} style={{ padding: '12px 16px', background: '#111', borderRadius: 8, border: '1px solid #222' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
                                                <Rate disabled value={rev.rating} style={{ fontSize: 12, color: '#FFFD82' }} />
                                                <Text type="secondary" style={{ fontSize: 12 }}>
                                                    Brand #{rev.brandId}
                                                    {rev.createdAt && ` · ${new Date(rev.createdAt).toLocaleDateString(undefined, { dateStyle: 'medium' })}`}
                                                </Text>
                                            </div>
                                            {rev.review && (
                                                <Paragraph style={{ color: '#d9d9d9', margin: 0, fontSize: '0.9rem' }}>
                                                    {rev.review}
                                                </Paragraph>
                                            )}
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <Text type="secondary">No written reviews yet.</Text>
                            )}
                        </Card>
                    )}
                </Col>
            </Row>
        </InfluencerPortalLayout>
    )
}
