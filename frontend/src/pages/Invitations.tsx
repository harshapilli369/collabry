import { useState, useEffect } from 'react'
import { Typography, Button, Card, Row, Col, Tag, message, Select } from 'antd'
import { EyeOutlined, FilterOutlined, MailOutlined, TeamOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import {
    getMyInvitations,
    INVITATION_STATUS_LABELS,
    type InvitationResponse,
    type InvitationStatus,
} from '../services/invitationService'
import { InfluencerPortalLayout, INFLUENCER_PORTAL_PRIMARY } from '../components/InfluencerPortalLayout'

const { Title, Text } = Typography

function formatDate(s: string | undefined) {
    if (!s) return '—'
    try {
        const d = new Date(s)
        return d.toLocaleDateString(undefined, { dateStyle: 'short' })
    } catch {
        return s
    }
}

const statusBorderColor: Record<string, string> = {
    PENDING: '#faad14',
    NEGOTIATING: '#faad14',
    ACCEPTED: '#52c41a',
    CONFIRMED: '#52c41a',
    REJECTED: '#ff4d4f',
    WITHDRAWN: '#888',
}

export const Invitations = () => {
    const navigate = useNavigate()
    const [invitations, setInvitations] = useState<InvitationResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [statusFilter, setStatusFilter] = useState<string | null>(null)

    useEffect(() => {
        getMyInvitations()
            .then(setInvitations)
            .catch(() => {
                message.error('Failed to load invitations')
                setInvitations([])
            })
            .finally(() => setLoading(false))
    }, [])

    const pendingCount = invitations.filter((i) => i.status === 'PENDING' || i.status === 'NEGOTIATING').length
    const filtered = statusFilter ? invitations.filter((i) => i.status === statusFilter) : invitations

    return (
        <InfluencerPortalLayout activeMenuKey="invitations">
            <div style={{ marginBottom: 30 }}>
                <Title level={1} style={{ color: INFLUENCER_PORTAL_PRIMARY, margin: 0, fontSize: '2.5rem' }}>My Invitations</Title>
                <Text style={{ color: '#aaa', fontSize: '1.1rem' }}>Review and respond to collaboration invites from brands.</Text>
            </div>

            {loading ? (
                <Text type="secondary">Loading invitations...</Text>
            ) : invitations.length === 0 ? (
                <Card style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a', textAlign: 'center', padding: '40px 0' }}>
                    <MailOutlined style={{ fontSize: 40, opacity: 0.2, color: INFLUENCER_PORTAL_PRIMARY, marginBottom: 16, display: 'block' }} />
                    <Text type="secondary" style={{ fontSize: 16 }}>You have no invitations yet.</Text>
                    <br />
                    <Text type="secondary" style={{ fontSize: 13 }}>When brands invite you, they'll show up here.</Text>
                </Card>
            ) : (
                <>
                    <Row gutter={[20, 20]} style={{ marginBottom: 24 }}>
                        <Col span={8}>
                            <Card style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: `1px solid ${INFLUENCER_PORTAL_PRIMARY}20` }} className="influencer-stat-card">
                                <MailOutlined style={{ fontSize: 20, color: '#faad14', marginBottom: 8 }} />
                                <Text type="secondary" style={{ display: 'block' }}>Pending / Negotiating</Text>
                                <Title level={2} style={{ margin: '8px 0 0', color: '#faad14' }}>{pendingCount}</Title>
                            </Card>
                        </Col>
                        <Col span={8}>
                            <Card style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: '1px solid #1a1a1a' }} className="influencer-stat-card">
                                <TeamOutlined style={{ fontSize: 20, color: INFLUENCER_PORTAL_PRIMARY, marginBottom: 8 }} />
                                <Text type="secondary" style={{ display: 'block' }}>Total invitations</Text>
                                <Title level={2} style={{ margin: '8px 0 0' }}>{invitations.length}</Title>
                            </Card>
                        </Col>
                        <Col span={8}>
                            <Card style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                    <FilterOutlined style={{ color: '#888' }} />
                                    <Select
                                        placeholder="Filter by status"
                                        allowClear
                                        value={statusFilter}
                                        onChange={(val) => setStatusFilter(val || null)}
                                        style={{ width: 180 }}
                                        options={[
                                            { label: 'Pending', value: 'PENDING' },
                                            { label: 'Negotiating', value: 'NEGOTIATING' },
                                            { label: 'Accepted', value: 'ACCEPTED' },
                                            { label: 'Confirmed', value: 'CONFIRMED' },
                                            { label: 'Rejected', value: 'REJECTED' },
                                            { label: 'Withdrawn', value: 'WITHDRAWN' },
                                        ]}
                                    />
                                </div>
                            </Card>
                        </Col>
                    </Row>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                        {filtered.map((inv) => (
                            <Card
                                key={inv.id}
                                size="small"
                                className="influencer-invitation-item"
                                style={{
                                    background: '#0d0d0d',
                                    borderRadius: 12,
                                    borderColor: '#1a1a1a',
                                    borderLeft: `3px solid ${statusBorderColor[inv.status] || '#333'}`,
                                    transition: 'all 0.3s ease',
                                }}
                            >
                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12 }}>
                                    <div>
                                        <Text strong style={{ color: '#fff', fontSize: 16 }}>Campaign #{inv.campaignId}</Text>
                                        {inv.brandMessage && (
                                            <div style={{ marginTop: 6 }}>
                                                <Text type="secondary" style={{ fontSize: 13 }}>{inv.brandMessage.slice(0, 120)}{inv.brandMessage.length > 120 ? '...' : ''}</Text>
                                            </div>
                                        )}
                                        <div style={{ marginTop: 6 }}>
                                            <Text type="secondary" style={{ fontSize: 12 }}>
                                                Received {formatDate(inv.createdAt)}
                                                {(inv.status === 'PENDING' || inv.status === 'NEGOTIATING') && (
                                                    <> &middot; You can accept, decline, or propose terms</>
                                                )}
                                            </Text>
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                        <Tag color={inv.status === 'PENDING' || inv.status === 'NEGOTIATING' ? 'gold' : inv.status === 'ACCEPTED' || inv.status === 'CONFIRMED' ? 'green' : 'default'}>
                                            {INVITATION_STATUS_LABELS[inv.status as InvitationStatus]}
                                        </Tag>
                                        <Button type="primary" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/influencer/invitations/${inv.id}`)} style={{ color: '#000', fontWeight: 600, borderRadius: 8 }}>
                                            View
                                        </Button>
                                    </div>
                                </div>
                            </Card>
                        ))}
                        {filtered.length === 0 && (
                            <div style={{ textAlign: 'center', padding: '30px 0', color: '#555' }}>
                                <Text type="secondary">No invitations match this filter.</Text>
                            </div>
                        )}
                    </div>
                </>
            )}
        </InfluencerPortalLayout>
    )
}
