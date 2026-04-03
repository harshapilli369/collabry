import { useState, useEffect } from 'react'
import { Typography, Button, Card, Row, Col, Avatar, Progress } from 'antd'
import { MailOutlined, UserOutlined, ArrowRightOutlined, SyncOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import {
    getMyInvitations,
    getMyCollaborations,
    DELIVERABLE_STATUS_LABELS,
    type InvitationResponse,
    type DeliverableStatus,
} from '../services/invitationService'
import { getMyInfluencerProfile } from '../services/influencerProfileService'
import { getMyPayments, type PaymentResponse } from '../services/paymentService'
import { InfluencerPortalLayout, INFLUENCER_PORTAL_PRIMARY } from '../components/InfluencerPortalLayout'
import { InfluencerPerformanceCharts } from '../components/InfluencerPerformanceCharts'
import { InfluencerCampaignCharts } from '../components/InfluencerCampaignCharts'

const { Title, Text } = Typography

function getProgressPercent(status?: string): number {
    switch (status) {
        case 'IN_PROGRESS': return 40
        case 'SUBMITTED': return 75
        case 'APPROVED': return 100
        default: return 5
    }
}

export const InfluencerDashboard = () => {
    const navigate = useNavigate()
    const [invitations, setInvitations] = useState<InvitationResponse[]>([])
    const [collaborations, setCollaborations] = useState<InvitationResponse[]>([])
    const [profile, setProfile] = useState<any>(null)
    const [payments, setPayments] = useState<PaymentResponse[]>([])
    const [loading, setLoading] = useState(true)

    const loadData = () => {
        setLoading(true)
        Promise.all([
            getMyInvitations().catch(() => []),
            getMyCollaborations().catch(() => []),
            getMyInfluencerProfile().catch(() => null),
            getMyPayments().catch(() => []),
        ]).then(([invs, collabs, prof, pays]) => {
            setInvitations(invs)
            setCollaborations(collabs)
            setProfile(prof)
            setPayments(pays)
        }).finally(() => setLoading(false))
    }

    useEffect(() => {
        loadData()
    }, [])

    const pendingInvitations = invitations.filter((i) => i.status === 'PENDING' || i.status === 'NEGOTIATING')
    
    // Show top 3 most recent active/ongoing collaborations first
    const activeCollabs = collaborations
        .filter(c => c.deliverableStatus !== 'APPROVED')
        .slice(0, 3)

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
                        extra={activeCollabs.length > 0 && <Button type="link" size="small" onClick={() => navigate('/influencer/collaborations')}>Track all</Button>}
                    >
                        {loading ? (
                            <Text type="secondary">Loading...</Text>
                        ) : activeCollabs.length === 0 ? (
                            <div style={{ textAlign: 'center', padding: '40px 0' }}>
                                <SyncOutlined style={{ fontSize: 32, color: INFLUENCER_PORTAL_PRIMARY, opacity: 0.2, marginBottom: 12, display: 'block' }} />
                                <Text type="secondary">No active campaigns at the moment.</Text>
                                <br />
                                <Button type="link" onClick={() => navigate('/influencer/invitations')} style={{ marginTop: 8 }}>Check invitations</Button>
                            </div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                                {activeCollabs.map((collab) => {
                                    const progress = getProgressPercent(collab.deliverableStatus)
                                    const isSubmitted = collab.deliverableStatus === 'SUBMITTED'

                                    return (
                                        <div
                                            key={collab.id}
                                            style={{
                                                display: 'flex',
                                                justifyContent: 'space-between',
                                                alignItems: 'center',
                                                padding: '16px 20px',
                                                background: '#141414',
                                                borderRadius: 12,
                                                border: '1px solid #1a1a1a',
                                                transition: 'all 0.3s ease',
                                                cursor: 'pointer'
                                            }}
                                            className="influencer-campaign-item"
                                            onClick={() => navigate('/influencer/collaborations')}
                                        >
                                            <div style={{ flex: 1 }}>
                                                <Title level={5} style={{ margin: 0, color: '#fff', fontSize: 15 }}>
                                                    {collab.campaignName || `Campaign #${collab.campaignId}`}
                                                </Title>
                                                <div style={{ marginTop: 4, display: 'flex', alignItems: 'center', gap: 6 }}>
                                                    <Text type="secondary" style={{ fontSize: 13 }}>
                                                        {collab.brandName || 'Brand'} &bull; 
                                                    </Text>
                                                    <Text style={{ 
                                                        fontSize: 12, 
                                                        color: isSubmitted ? '#faad14' : INFLUENCER_PORTAL_PRIMARY,
                                                        fontWeight: 500
                                                    }}>
                                                        {DELIVERABLE_STATUS_LABELS[collab.deliverableStatus as DeliverableStatus] || 'Not Started'}
                                                    </Text>
                                                </div>
                                                <Progress
                                                    percent={progress}
                                                    size="small"
                                                    showInfo={false}
                                                    strokeColor={isSubmitted ? '#faad14' : INFLUENCER_PORTAL_PRIMARY}
                                                    trailColor="#1a1a1a"
                                                    style={{ marginTop: 10, maxWidth: 240 }}
                                                />
                                            </div>
                                            <div style={{ textAlign: 'right' }}>
                                                <Button 
                                                    type="default" 
                                                    ghost 
                                                    size="small" 
                                                    icon={<ArrowRightOutlined />}
                                                    style={{ color: INFLUENCER_PORTAL_PRIMARY, border: `1px solid ${INFLUENCER_PORTAL_PRIMARY}40` }}
                                                >
                                                    Manage
                                                </Button>
                                            </div>
                                        </div>
                                    )
                                })}
                            </div>
                        )}
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
                        {loading ? (
                             <Text type="secondary">Loading...</Text>
                        ) : pendingInvitations.length === 0 ? (
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
                                        <Text strong style={{ color: '#fff', fontSize: 14 }}>{inv.campaignName || `Campaign #${inv.campaignId}`}</Text>
                                        <Text type="secondary" style={{ display: 'block', margin: '6px 0 10px', fontSize: 12 }}>
                                            from {inv.brandName || 'Brand'}
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
