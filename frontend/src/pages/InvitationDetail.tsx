import { useState, useEffect } from 'react'
import { Typography, Button, Card, Row, Col, Tag, message, Input, InputNumber, Form, ConfigProvider, theme, Spin } from 'antd'
import { ArrowLeftOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons'
import { useNavigate, useParams } from 'react-router-dom'
import {
    getInvitationById,
    respondToInvitation,
    negotiateInvitation,
    INVITATION_STATUS_LABELS,
    type InvitationDetailResponse,
    type InvitationStatus,
} from '../services/invitationService'
import { BUDGET_RANGE_OPTIONS, CAMPAIGN_STATUS_LABELS } from '../services/campaignService'
import { InfluencerPortalLayout } from '../components/InfluencerPortalLayout'

const { Title, Text } = Typography
const { TextArea } = Input

function formatDate(s: string | undefined) {
    if (!s) return '—'
    try {
        return new Date(s).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
    } catch {
        return s
    }
}

export const InvitationDetail = () => {
    const navigate = useNavigate()
    const { id } = useParams<{ id: string }>()
    const [invitation, setInvitation] = useState<InvitationDetailResponse | null>(null)
    const [loading, setLoading] = useState(true)
    const [responding, setResponding] = useState(false)
    const [negotiateForm] = Form.useForm()

    const load = () => {
        if (!id) return
        setLoading(true)
        getInvitationById(Number(id))
            .then(setInvitation)
            .catch(() => {
                message.error('Failed to load invitation')
                navigate('/influencer/invitations', { replace: true })
            })
            .finally(() => setLoading(false))
    }

    useEffect(() => {
        load()
    }, [id])

    const canRespond = invitation && (invitation.status === 'PENDING' || invitation.status === 'NEGOTIATING')
    const handleAccept = () => {
        if (!id || !canRespond) return
        setResponding(true)
        respondToInvitation(Number(id), { action: 'ACCEPT' })
            .then(() => {
                message.success('Invitation accepted')
                load()
            })
            .catch((e) => message.error(e.message || 'Failed to accept'))
            .finally(() => setResponding(false))
    }
    const handleDecline = () => {
        if (!id || !canRespond) return
        setResponding(true)
        respondToInvitation(Number(id), { action: 'REJECT' })
            .then(() => {
                message.success('Invitation declined')
                load()
            })
            .catch((e) => message.error(e.message || 'Failed to decline'))
            .finally(() => setResponding(false))
    }

    const onNegotiate = (values: { proposedAmount?: number; proposedTimeline?: string; proposedDeliverables?: string }) => {
        if (!id || !canRespond) return
        setResponding(true)
        negotiateInvitation(Number(id), {
            proposedAmount: values.proposedAmount,
            proposedTimeline: values.proposedTimeline || undefined,
            proposedDeliverables: values.proposedDeliverables || undefined,
        })
            .then(() => {
                message.success('Negotiation sent')
                negotiateForm.resetFields()
                load()
            })
            .catch((e) => message.error(e.message || 'Failed to submit'))
            .finally(() => setResponding(false))
    }

    if (loading || !invitation) {
        return (
            <ConfigProvider theme={{ algorithm: theme.darkAlgorithm }}>
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', background: '#000' }}>
                    <Spin size="large" />
                </div>
            </ConfigProvider>
        )
    }

    const campaign = invitation.campaign
    const budgetLabel = campaign?.budgetRange ? BUDGET_RANGE_OPTIONS.find((o) => o.value === campaign.budgetRange)?.label ?? campaign.budgetRange : null

    return (
        <InfluencerPortalLayout activeMenuKey="invitations">
            <Button type="text" icon={<ArrowLeftOutlined />} onClick={() => navigate('/influencer/invitations')} style={{ marginBottom: 16, color: '#aaa' }}>
                Back to Invitations
            </Button>

            <div style={{ marginBottom: 24 }}>
                <Tag color={invitation.status === 'PENDING' || invitation.status === 'NEGOTIATING' ? 'gold' : invitation.status === 'ACCEPTED' || invitation.status === 'CONFIRMED' ? 'green' : 'default'}>
                    {INVITATION_STATUS_LABELS[invitation.status as InvitationStatus]}
                </Tag>
                <Title level={3} style={{ color: '#fff', margin: '8px 0 0' }}>
                    {invitation.campaignName || `Invitation #${invitation.id}`}
                </Title>
                <Text type="secondary" style={{ display: 'block', marginTop: 4 }}>Received {formatDate(invitation.createdAt)}</Text>
            </div>

            {invitation.brandMessage && (
                <Card title="Message from brand" size="small" style={{ marginBottom: 24, background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                    <Text style={{ color: '#ccc' }}>{invitation.brandMessage}</Text>
                </Card>
            )}

            {/* Brand Details Card */}
            {invitation.brandName && (
                <Card title="Brand details" size="small" style={{ marginBottom: 24, background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                        {invitation.brandLogo && (
                            <img src={invitation.brandLogo} alt={invitation.brandName} style={{ width: 56, height: 56, borderRadius: 12, objectFit: 'cover', border: '1px solid #333' }} />
                        )}
                        <div>
                            <Text strong style={{ color: '#fff', fontSize: 16 }}>{invitation.brandName}</Text>
                            {invitation.brandNiche && (
                                <div style={{ marginTop: 4 }}>
                                    <Tag>{invitation.brandNiche}</Tag>
                                </div>
                            )}
                        </div>
                    </div>
                </Card>
            )}

            {campaign && (
                <Card title="Campaign details" size="small" style={{ marginBottom: 24, background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                    <Row gutter={[16, 8]}>
                        <Col span={24}><Text strong style={{ color: '#fff' }}>{campaign.name}</Text></Col>
                        {campaign.description && <Col span={24}><Text type="secondary">{campaign.description}</Text></Col>}
                        <Col span={24}>
                            <Text type="secondary" style={{ fontSize: 12 }}>
                                Budget: {budgetLabel ?? campaign.budgetRange}
                                {campaign.numberOfInfluencers != null && ` · ${campaign.numberOfInfluencers} influencer(s)`}
                                {campaign.startDate && ` · ${campaign.startDate}`}
                                {campaign.endDate && ` – ${campaign.endDate}`}
                            </Text>
                        </Col>
                        <Col span={24}><Tag>{CAMPAIGN_STATUS_LABELS[campaign.status as keyof typeof CAMPAIGN_STATUS_LABELS] ?? campaign.status}</Tag></Col>
                    </Row>
                </Card>
            )}

            {(invitation.proposedAmount != null || invitation.proposedTimeline || invitation.proposedDeliverables) && (
                <Card title="Proposed / negotiated terms" size="small" style={{ marginBottom: 24, background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                    {invitation.proposedAmount != null && <div><Text type="secondary">Amount: </Text><Text style={{ color: '#fff' }}>${Number(invitation.proposedAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</Text></div>}
                    {invitation.proposedTimeline && <div><Text type="secondary">Timeline: </Text><Text style={{ color: '#fff' }}>{invitation.proposedTimeline}</Text></div>}
                    {invitation.proposedDeliverables && <div><Text type="secondary">Deliverables: </Text><Text style={{ color: '#fff' }}>{invitation.proposedDeliverables}</Text></div>}
                </Card>
            )}

            {canRespond && (
                <Card title="Respond" size="small" style={{ marginBottom: 24, background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                    <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
                        <Button type="primary" icon={<CheckOutlined />} loading={responding} onClick={handleAccept} style={{ color: '#000000' }}>Accept</Button>
                        <Button danger icon={<CloseOutlined />} loading={responding} onClick={handleDecline}>Decline</Button>
                    </div>
                    <Title level={5} style={{ color: '#aaa', marginTop: 16 }}>Or propose terms (negotiate)</Title>
                    <Form form={negotiateForm} layout="vertical" onFinish={onNegotiate} style={{ maxWidth: 480 }}>
                        <Form.Item name="proposedAmount" label="Proposed amount ($)">
                            <InputNumber min={0} step={100} style={{ width: '100%' }} placeholder="e.g. 1500" />
                        </Form.Item>
                        <Form.Item name="proposedTimeline" label="Proposed timeline">
                            <Input placeholder="e.g. 2 weeks" />
                        </Form.Item>
                        <Form.Item name="proposedDeliverables" label="Proposed deliverables">
                            <TextArea rows={3} placeholder="Describe what you will deliver" />
                        </Form.Item>
                        <Form.Item>
                            <Button type="default" htmlType="submit" loading={responding}>Submit negotiation</Button>
                        </Form.Item>
                    </Form>
                </Card>
            )}
        </InfluencerPortalLayout>
    )
}
