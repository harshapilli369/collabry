import { useState, useEffect } from 'react'
import { Card, Typography, Button, Descriptions, Avatar, Spin, Space, Row, Col, Alert, message } from 'antd'
import { ArrowLeftOutlined, EditOutlined, GlobalOutlined, InstagramOutlined, LinkedinOutlined, TwitterOutlined, CheckCircleFilled, SafetyCertificateOutlined, ReloadOutlined, ClockCircleOutlined, CloseCircleOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyBrandProfile, type BrandProfileResponse, BUDGET_RANGE_OPTIONS } from '../services/brandService'
import { userService } from '../services/userService'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'

const { Title, Text, Paragraph } = Typography

export const ViewBrandProfile = () => {
    const [profile, setProfile] = useState<BrandProfileResponse | null>(null)
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
                const profileData = await getMyBrandProfile()
                if (!profileData) {
                    navigate('/brand/profile/edit', { replace: true })
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

    const PRIMARY = BRAND_PORTAL_PRIMARY

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
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#000' }}>
                <Spin size="large" />
            </div>
        )
    }

    if (!profile) return null

    return (
        <BrandPortalLayout activeMenuKey="profile" brandProfileForHeader={profile}>
            <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => navigate('/brand/dashboard')} style={{ color: PRIMARY, paddingLeft: 0, marginBottom: 16 }}>
                Back to Dashboard
            </Button>

            <Row gutter={[24, 24]}>
                <Col xs={24} md={16}>
                    {/* Profile Header Card */}
                    <Card
                        style={{ backgroundColor: '#0d0d0d', borderRadius: 16, border: '1px solid #1a1a1a' }}
                        title={
                            <div style={{ display: 'flex', alignItems: 'center', gap: 14, paddingTop: 4 }}>
                                {profile.logoUrl ? (
                                    <Avatar size={50} src={profile.logoUrl} style={{ border: `2px solid ${PRIMARY}40` }} />
                                ) : (
                                    <Avatar size={50} style={{ backgroundColor: PRIMARY, color: '#000', fontSize: '1.4rem', fontWeight: 700 }}>
                                        {profile.name?.charAt(0)?.toUpperCase()}
                                    </Avatar>
                                )}
                                <div>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                        <Title level={4} style={{ margin: 0, color: '#fff' }}>{profile.name}</Title>
                                        {isVerified && <CheckCircleFilled style={{ color: PRIMARY, fontSize: '1.1rem' }} />}
                                    </div>
                                    <Text type="secondary" style={{ fontSize: '0.9rem' }}>{profile.industry}</Text>
                                </div>
                            </div>
                        }
                        extra={
                            <Button type="primary" icon={<EditOutlined />} onClick={() => navigate('/brand/profile/edit')} style={{ borderRadius: 10 }}>
                                Edit Profile
                            </Button>
                        }
                    >
                        <Space direction="vertical" size="large" style={{ width: '100%', marginTop: 8 }}>
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
                                                        ? 'An admin is currently reviewing your profile. You will be notified once a decision is made.'
                                                        : verification?.status === 'REJECTED'
                                                        ? `Reason: ${verification.adminReason || 'No reason provided.'}. Please update your profile and try again.`
                                                        : 'Verified brands get a blue badge and can create unlimited campaigns, invite influencers, and use AI recommendations.'}
                                                </Paragraph>
                                            </div>
                                            {(!verification || verification.status === 'REJECTED') && (
                                                <Button 
                                                    type="primary" 
                                                    size="small" 
                                                    icon={verification?.status === 'REJECTED' ? <ReloadOutlined /> : <SafetyCertificateOutlined />}
                                                    loading={requesting}
                                                    onClick={handleRequestVerification}
                                                    style={{ width: 'fit-content' }}
                                                >
                                                    {verification?.status === 'REJECTED' ? 'Request Again' : 'Request Verification'}
                                                </Button>
                                            )}
                                        </div>
                                    }
                                    type={verification?.status === 'REJECTED' ? 'error' : 'info'}
                                    showIcon={false}
                                    style={{ 
                                        backgroundColor: verification?.status === 'REJECTED' ? '#2a1215' : '#111b26', 
                                        border: `1px solid ${verification?.status === 'REJECTED' ? '#5c2223' : '#153450'}`,
                                        borderRadius: 12
                                    }}
                                />
                            )}

                            {profile.description && (
                                <div>
                                    <Title level={5} style={{ color: PRIMARY }}>About Us</Title>
                                    <Paragraph style={{ color: '#d9d9d9', fontSize: '1rem', lineHeight: 1.7 }}>
                                        {profile.description}
                                    </Paragraph>
                                </div>
                            )}

                            <div>
                                <Title level={5} style={{ color: PRIMARY, marginBottom: 16 }}>Details</Title>
                                <Descriptions column={{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }} bordered size="middle">
                                    <Descriptions.Item label="Contact Email">{profile.email}</Descriptions.Item>
                                    <Descriptions.Item label="Typical Budget">
                                        {BUDGET_RANGE_OPTIONS.find(o => o.value === profile.budgetRange)?.label || profile.budgetRange || 'Not specified'}
                                    </Descriptions.Item>
                                </Descriptions>
                            </div>
                        </Space>
                    </Card>
                </Col>

                <Col xs={24} md={8}>
                    {/* Links & Socials Card */}
                    <Card style={{ backgroundColor: '#0d0d0d', borderRadius: 16, border: '1px solid #1a1a1a' }}>
                        <Title level={5} style={{ color: PRIMARY, marginBottom: 16 }}>Links & Socials</Title>
                        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
                            {profile.website && (
                                <Button
                                    type="default"
                                    icon={<GlobalOutlined />}
                                    href={profile.website}
                                    target="_blank"
                                    block
                                    className="brand-campaign-card"
                                    style={{ textAlign: 'left', borderRadius: 10, height: 44, borderColor: '#333' }}
                                >
                                    Website
                                </Button>
                            )}
                            {profile.instagramUrl && (
                                <Button
                                    type="default"
                                    icon={<InstagramOutlined />}
                                    href={profile.instagramUrl}
                                    target="_blank"
                                    block
                                    className="brand-campaign-card"
                                    style={{ textAlign: 'left', borderRadius: 10, height: 44, color: '#E1306C', borderColor: '#E1306C40' }}
                                >
                                    {formatSocialHandle(profile.instagramUrl)}
                                </Button>
                            )}
                            {profile.linkedInUrl && (
                                <Button
                                    type="default"
                                    icon={<LinkedinOutlined />}
                                    href={profile.linkedInUrl}
                                    target="_blank"
                                    block
                                    className="brand-campaign-card"
                                    style={{ textAlign: 'left', borderRadius: 10, height: 44, color: '#0077B5', borderColor: '#0077B540' }}
                                >
                                    {formatSocialHandle(profile.linkedInUrl)}
                                </Button>
                            )}
                            {profile.twitterUrl && (
                                <Button
                                    type="default"
                                    icon={<TwitterOutlined />}
                                    href={profile.twitterUrl}
                                    target="_blank"
                                    block
                                    className="brand-campaign-card"
                                    style={{ textAlign: 'left', borderRadius: 10, height: 44, color: '#1DA1F2', borderColor: '#1DA1F240' }}
                                >
                                    {formatSocialHandle(profile.twitterUrl)}
                                </Button>
                            )}
                            {(!profile.website && !profile.instagramUrl && !profile.linkedInUrl && !profile.twitterUrl) && (
                                <Text type="secondary">No links provided yet.</Text>
                            )}
                        </Space>
                    </Card>
                </Col>
            </Row>
        </BrandPortalLayout>
    )
}
