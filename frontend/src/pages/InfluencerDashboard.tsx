import { useState, useEffect } from 'react'
import { Typography, Button, Card, Row, Col, Avatar, Progress } from 'antd'
import { MailOutlined, UserOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyInvitations } from '../services/invitationService'
import { getMyInfluencerProfile } from '../services/influencerProfileService'
import { getMyPayments, type PaymentResponse } from '../services/paymentService'
import { InfluencerPortalLayout, INFLUENCER_PORTAL_PRIMARY } from '../components/InfluencerPortalLayout'
import { InfluencerPerformanceCharts } from '../components/InfluencerPerformanceCharts'
import { InfluencerCampaignCharts } from '../components/InfluencerCampaignCharts'

const { Title, Text } = Typography

export const InfluencerDashboard = () => {
    const navigate = useNavigate()
    const [invitations, setInvitations] = useState<Awaited<ReturnType<typeof getMyInvitations>>>([])
    const [profile, setProfile] = useState<any>(null)
    const [payments, setPayments] = useState<PaymentResponse[]>([])

    useEffect(() => {
        getMyInvitations()
            .then(setInvitations)
            .catch(() => setInvitations([]))

        getMyInfluencerProfile()
            .then(setProfile)
            .catch(() => {})

        getMyPayments()
            .then(setPayments)
            .catch(() => setPayments([]))
    }, [])

    const pendingInvitations = invitations.filter((i) => i.status === 'PENDING' || i.status === 'NEGOTIATING')

    return (
        <InfluencerPortalLayout activeMenuKey="dashboard" influencerProfileForHeader={profile}>
            {/* Welcome Banner */}
            <div
                style={{
                    marginBottom: 30,
                    padding: '32px 36px',
                    borderRadius: 16,
                    background: 'linear-gradient(135deg, #1a0a2e 0%, #0d0d0d 50%, #1a0a2e 100%)',
                    border: `1px solid ${INFLUENCER_PORTAL_PRIMARY}20`,
                    position: 'relative',
                    overflow: 'hidden',
                }}
            >
                <div
                    style={{
                        position: 'absolute',
                        top: -40,
                        right: -40,
                        width: 200,
                        height: 200,
                        borderRadius: '50%',
                        background: `radial-gradient(circle, ${INFLUENCER_PORTAL_PRIMARY}15 0%, transparent 70%)`,
                        pointerEvents: 'none',
                    }}
                />
                <div style={{ display: 'flex', alignItems: 'center', gap: 20, position: 'relative', zIndex: 1 }}>
                    <Avatar
                        size={64}
                        src={profile?.profilePictureUrl || undefined}
                        icon={!profile?.profilePictureUrl ? <UserOutlined /> : undefined}
                        style={{ border: `3px solid ${INFLUENCER_PORTAL_PRIMARY}60`, flexShrink: 0 }}
                    />
                    <div>
                        <Title level={2} style={{ color: '#fff', margin: 0 }}>
                            Welcome back, <span style={{ color: INFLUENCER_PORTAL_PRIMARY }}>{profile?.name || 'Creator'}</span>!
                        </Title>
                        <Text style={{ color: '#888', fontSize: 16 }}>Here's what's happening with your campaigns today.</Text>
                    </div>
                </div>
            </div>

            <Row gutter={[20, 20]}>
                {/* Performance Charts */}
                <Col span={24}>
                    <InfluencerPerformanceCharts invitations={invitations} payments={payments} />
                </Col>

                {/* Campaign Charts */}
                <Col span={24}>
                    <InfluencerCampaignCharts invitations={invitations} />
                </Col>

                {/* Active Campaigns */}
                <Col span={16}>
                    <Card
                        title={<Text style={{ color: '#fff', fontSize: 16, fontWeight: 600 }}>Active Campaigns</Text>}
                        style={{ borderRadius: 16, height: '100%', background: '#0d0d0d', border: '1px solid #1a1a1a' }}
                    >
                        {[
                            { name: 'Summer Fashion 2026', brand: 'Nike', status: 'Due in 2 days', progress: 75, action: 'Submit Content' },
                            { name: 'Eco-Friendly Water Bottle', brand: 'HydroFlask', status: 'In Review', progress: 90, action: 'View Feedback' },
                        ].map((campaign, idx) => (
                            <div
                                key={idx}
                                style={{
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    padding: 20,
                                    background: '#141414',
                                    borderRadius: 12,
                                    border: '1px solid #1a1a1a',
                                    marginBottom: idx === 0 ? 16 : 0,
                                    transition: 'all 0.3s ease',
                                }}
                                className="influencer-campaign-item"
                            >
                                <div style={{ flex: 1 }}>
                                    <Title level={5} style={{ margin: 0, color: '#fff' }}>{campaign.name}</Title>
                                    <Text type="secondary">{campaign.brand} &bull; {campaign.status}</Text>
                                    <Progress
                                        percent={campaign.progress}
                                        size="small"
                                        strokeColor={INFLUENCER_PORTAL_PRIMARY}
                                        trailColor="#1a1a1a"
                                        style={{ marginTop: 8, maxWidth: 200 }}
                                    />
                                </div>
                                <Button type="primary" size="small" style={{ color: '#000', fontWeight: 600 }}>{campaign.action}</Button>
                            </div>
                        ))}
                    </Card>
                </Col>

                {/* Pending Invitations */}
                <Col span={8}>
                    <Card
                        title={
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <MailOutlined style={{ color: INFLUENCER_PORTAL_PRIMARY }} />
                                <Text style={{ color: '#fff', fontSize: 16, fontWeight: 600 }}>Pending Invitations</Text>
                            </div>
                        }
                        style={{ borderRadius: 16, height: '100%', background: '#0d0d0d', border: '1px solid #1a1a1a' }}
                        extra={pendingInvitations.length > 0 ? <Button type="link" size="small" onClick={() => navigate('/influencer/invitations')}>View all</Button> : null}
                    >
                        {invitations.length === 0 ? (
                            <div style={{ textAlign: 'center', padding: '30px 0', color: '#555' }}>
                                <MailOutlined style={{ fontSize: 32, opacity: 0.3, marginBottom: 12, display: 'block' }} />
                                <Text type="secondary">No pending invitations.</Text>
                            </div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                {pendingInvitations.slice(0, 3).map((inv) => (
                                    <div
                                        key={inv.id}
                                        className="influencer-invitation-item"
                                        style={{
                                            padding: 16,
                                            background: '#141414',
                                            borderRadius: 12,
                                            border: '1px solid #1a1a1a',
                                            borderLeft: `3px solid ${INFLUENCER_PORTAL_PRIMARY}`,
                                            transition: 'all 0.3s ease',
                                        }}
                                    >
                                        <Text strong style={{ color: '#fff', fontSize: 14 }}>Campaign #{inv.campaignId}</Text>
                                        <Text type="secondary" style={{ display: 'block', margin: '6px 0 10px', fontSize: 12 }}>
                                            {inv.brandMessage ? inv.brandMessage.slice(0, 50) + (inv.brandMessage.length > 50 ? '...' : '') : 'No message'}
                                        </Text>
                                        <Button type="primary" size="small" block onClick={() => navigate(`/influencer/invitations/${inv.id}`)} style={{ color: '#000', fontWeight: 600, borderRadius: 8 }}>
                                            View & respond
                                        </Button>
                                    </div>
                                ))}
                                {pendingInvitations.length > 3 && (
                                    <Button size="small" block onClick={() => navigate('/influencer/invitations')} style={{ borderRadius: 8 }}>
                                        View all invitations
                                    </Button>
                                )}
                            </div>
                        )}
                    </Card>
                </Col>
            </Row>
        </InfluencerPortalLayout>
    )
}
