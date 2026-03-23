import { useState, useEffect } from 'react'
import { Typography, Card, Row, Col, Tag, message } from 'antd'
import { TeamOutlined } from '@ant-design/icons'
import { getMyCollaborations, INVITATION_STATUS_LABELS, type InvitationResponse, type InvitationStatus } from '../services/invitationService'
import { InfluencerPortalLayout, INFLUENCER_PORTAL_PRIMARY } from '../components/InfluencerPortalLayout'

const { Title, Text } = Typography

function formatDate(s: string | undefined) {
    if (!s) return '—'
    try {
        return new Date(s).toLocaleDateString(undefined, { dateStyle: 'medium' })
    } catch {
        return s
    }
}

export const Collaborations = () => {
    const [collaborations, setCollaborations] = useState<InvitationResponse[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        getMyCollaborations()
            .then(setCollaborations)
            .catch(() => {
                message.error('Failed to load collaborations')
                setCollaborations([])
            })
            .finally(() => setLoading(false))
    }, [])

    return (
        <InfluencerPortalLayout activeMenuKey="collaborations">
            <div style={{ marginBottom: 30 }}>
                <Title level={1} style={{ color: INFLUENCER_PORTAL_PRIMARY, margin: 0, fontSize: '2.5rem' }}>My Collaborations</Title>
                <Text style={{ color: '#aaa', fontSize: '1.1rem' }}>Campaigns you've accepted or confirmed.</Text>
            </div>

            {loading ? (
                <Text type="secondary">Loading...</Text>
            ) : collaborations.length === 0 ? (
                <Card style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a', textAlign: 'center', padding: '40px 0' }}>
                    <TeamOutlined style={{ fontSize: 40, opacity: 0.2, color: INFLUENCER_PORTAL_PRIMARY, marginBottom: 16, display: 'block' }} />
                    <Text type="secondary" style={{ fontSize: 16 }}>You have no collaborations yet.</Text>
                    <br />
                    <Text type="secondary" style={{ fontSize: 13 }}>Accept an invitation to get started.</Text>
                </Card>
            ) : (
                <>
                    <Row gutter={[20, 20]} style={{ marginBottom: 24 }}>
                        <Col span={8}>
                            <Card className="influencer-stat-card" style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: `1px solid ${INFLUENCER_PORTAL_PRIMARY}20` }}>
                                <TeamOutlined style={{ fontSize: 20, color: INFLUENCER_PORTAL_PRIMARY, marginBottom: 8 }} />
                                <Text type="secondary" style={{ display: 'block' }}>Active collaborations</Text>
                                <Title level={2} style={{ margin: '8px 0 0', color: INFLUENCER_PORTAL_PRIMARY }}>{collaborations.length}</Title>
                            </Card>
                        </Col>
                    </Row>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                        {collaborations.map((inv) => (
                            <Card
                                key={inv.id}
                                size="small"
                                className="influencer-invitation-item"
                                style={{
                                    background: '#0d0d0d',
                                    borderRadius: 12,
                                    borderColor: '#1a1a1a',
                                    borderLeft: '3px solid #52c41a',
                                    transition: 'all 0.3s ease',
                                }}
                            >
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12 }}>
                                    <div>
                                        <Text strong style={{ color: '#fff', fontSize: 16 }}>Campaign #{inv.campaignId}</Text>
                                        {inv.brandMessage && (
                                            <div style={{ marginTop: 6 }}>
                                                <Text type="secondary" style={{ fontSize: 13 }}>{inv.brandMessage.slice(0, 100)}{inv.brandMessage.length > 100 ? '...' : ''}</Text>
                                            </div>
                                        )}
                                        <div style={{ marginTop: 6 }}>
                                            <Text type="secondary" style={{ fontSize: 12 }}>
                                                {inv.proposedAmount != null && `$${Number(inv.proposedAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}`}
                                                {inv.respondedAt && ` · Responded ${formatDate(inv.respondedAt)}`}
                                            </Text>
                                        </div>
                                    </div>
                                    <Tag color="green">{INVITATION_STATUS_LABELS[inv.status as InvitationStatus]}</Tag>
                                </div>
                            </Card>
                        ))}
                    </div>
                </>
            )}
        </InfluencerPortalLayout>
    )
}
