import { useState, useEffect } from 'react'
import { Typography, Button, Card, Row, Col, Tag, message, Tabs, Select } from 'antd'
import { EyeOutlined, MailOutlined, TeamOutlined, CheckCircleOutlined, ClockCircleOutlined, CloseCircleOutlined, FilterOutlined } from '@ant-design/icons'
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
    if (!s) return ''
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

const InvitationCard = ({ inv, navigate }: { inv: InvitationResponse; navigate: ReturnType<typeof useNavigate> }) => (
    <Card
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
                <Text strong style={{ color: '#fff', fontSize: 16 }}>{inv.campaignName || `Campaign #${inv.campaignId}`}</Text>
                {inv.brandName && (
                    <span style={{ marginLeft: 8 }}>
                        <Text type="secondary" style={{ fontSize: 13 }}>by {inv.brandName}</Text>
                        {inv.brandNiche && <Tag style={{ marginLeft: 6, fontSize: 11 }}>{inv.brandNiche}</Tag>}
                    </span>
                )}
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
                <Button type="primary" size="small" icon={<EyeOutlined />} onClick={() => navigate(`/influencer/invitations/${inv.id}`)} style={{ borderRadius: 8 }}>
                    View
                </Button>
            </div>
        </div>
    </Card>
)

export const Invitations = () => {
    const navigate = useNavigate()
    const [invitations, setInvitations] = useState<InvitationResponse[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        getMyInvitations()
            .then(setInvitations)
            .catch(() => {
                message.error('Failed to load invitations')
                setInvitations([])
            })
            .finally(() => setLoading(false))
    }, [])

    const [statusFilter, setStatusFilter] = useState<string | null>(null)

    const pending = invitations.filter((i) => i.status === 'PENDING' || i.status === 'NEGOTIATING')
    const accepted = invitations.filter((i) => i.status === 'ACCEPTED' || i.status === 'CONFIRMED')
    const rejected = invitations.filter((i) => i.status === 'REJECTED' || i.status === 'WITHDRAWN')

    // Unique brand/campaign count
    const uniqueCampaigns = new Set(invitations.map((i) => i.campaignId)).size

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
                    <Text type="secondary" style={{ fontSize: 13 }}>When brands invite you, they will show up here.</Text>
                </Card>
            ) : (
                <>
                    {/* Stats Row */}
                    <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
                        {[
                            { icon: <ClockCircleOutlined style={{ fontSize: 20, color: '#faad14' }} />, label: 'Pending', value: pending.length, color: '#faad14', borderColor: '#faad1420' },
                            { icon: <CheckCircleOutlined style={{ fontSize: 20, color: '#52c41a' }} />, label: 'Accepted', value: accepted.length, color: '#52c41a', borderColor: '#52c41a20' },
                            { icon: <CloseCircleOutlined style={{ fontSize: 20, color: '#ff4d4f' }} />, label: 'Declined', value: rejected.length, color: '#ff4d4f', borderColor: '#ff4d4f20' },
                            { icon: <TeamOutlined style={{ fontSize: 20, color: INFLUENCER_PORTAL_PRIMARY }} />, label: 'Campaigns', value: uniqueCampaigns, color: INFLUENCER_PORTAL_PRIMARY, borderColor: `${INFLUENCER_PORTAL_PRIMARY}20` },
                            { icon: <MailOutlined style={{ fontSize: 20, color: '#1890ff' }} />, label: 'Total', value: invitations.length, color: '#1890ff', borderColor: '#1890ff20' },
                        ].map((stat, idx) => (
                            <Col span={4} key={idx} style={{ minWidth: 120 }}>
                                <Card className="influencer-stat-card" style={{ borderRadius: 14, textAlign: 'center', background: '#0d0d0d', border: `1px solid ${stat.borderColor}`, padding: 0 }}>
                                    <div style={{ marginBottom: 4 }}>{stat.icon}</div>
                                    <Text type="secondary" style={{ fontSize: 11 }}>{stat.label}</Text>
                                    <Title level={3} style={{ margin: '4px 0 0', color: stat.color }}>{stat.value}</Title>
                                </Card>
                            </Col>
                        ))}
                    </Row>

                    {/* Tabbed Invitations */}
                    <Card
                        style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a' }}
                        extra={
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                <FilterOutlined style={{ color: '#555', fontSize: 13 }} />
                                <Select
                                    placeholder="Filter status"
                                    allowClear
                                    value={statusFilter}
                                    onChange={(val) => setStatusFilter(val || null)}
                                    style={{ width: 150 }}
                                    size="small"
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
                        }
                    >
                        <Tabs
                            defaultActiveKey="all"
                            items={[
                                {
                                    key: 'all',
                                    label: (
                                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                            <MailOutlined /> All ({invitations.length})
                                        </span>
                                    ),
                                    children: (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                            {(statusFilter ? invitations.filter((i) => i.status === statusFilter) : invitations).map((inv) => (
                                                <InvitationCard key={inv.id} inv={inv} navigate={navigate} />
                                            ))}
                                        </div>
                                    ),
                                },
                                {
                                    key: 'pending',
                                    label: (
                                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                            <ClockCircleOutlined style={{ color: '#faad14' }} /> Pending ({pending.length})
                                        </span>
                                    ),
                                    children: pending.length === 0 ? (
                                        <div style={{ textAlign: 'center', padding: '30px 0' }}>
                                            <Text type="secondary">No pending invitations.</Text>
                                        </div>
                                    ) : (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                            {pending.map((inv) => (
                                                <InvitationCard key={inv.id} inv={inv} navigate={navigate} />
                                            ))}
                                        </div>
                                    ),
                                },
                                {
                                    key: 'accepted',
                                    label: (
                                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                            <CheckCircleOutlined style={{ color: '#52c41a' }} /> Accepted ({accepted.length})
                                        </span>
                                    ),
                                    children: accepted.length === 0 ? (
                                        <div style={{ textAlign: 'center', padding: '30px 0' }}>
                                            <Text type="secondary">No accepted invitations yet.</Text>
                                        </div>
                                    ) : (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                            {accepted.map((inv) => (
                                                <InvitationCard key={inv.id} inv={inv} navigate={navigate} />
                                            ))}
                                        </div>
                                    ),
                                },
                                {
                                    key: 'rejected',
                                    label: (
                                        <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                            <CloseCircleOutlined style={{ color: '#ff4d4f' }} /> Declined ({rejected.length})
                                        </span>
                                    ),
                                    children: rejected.length === 0 ? (
                                        <div style={{ textAlign: 'center', padding: '30px 0' }}>
                                            <Text type="secondary">No declined invitations.</Text>
                                        </div>
                                    ) : (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                            {rejected.map((inv) => (
                                                <InvitationCard key={inv.id} inv={inv} navigate={navigate} />
                                            ))}
                                        </div>
                                    ),
                                },
                            ]}
                        />
                    </Card>
                </>
            )}
        </InfluencerPortalLayout>
    )
}
