import { useState, useEffect } from 'react'
import { Typography, Button, ConfigProvider, Layout, Menu, Card, Row, Col, Avatar, Tag, message, theme, Input, InputNumber, Form, Modal, Checkbox } from 'antd'
import { UserOutlined, LogoutOutlined, MailOutlined, AppstoreOutlined, DollarOutlined, TeamOutlined, ArrowLeftOutlined, CheckOutlined, CloseOutlined, FileTextOutlined } from '@ant-design/icons'
import { useNavigate, useParams, Link } from 'react-router-dom'
import { DisclosureGuidelinesContent } from '../components/DisclosureGuidelinesContent'
import { DISCLOSURE_ACKNOWLEDGMENT_LABEL } from '../content/disclosureGuidelines'
import {
    getInvitationById,
    respondToInvitation,
    negotiateInvitation,
    INVITATION_STATUS_LABELS,
    type InvitationDetailResponse,
    type InvitationStatus,
} from '../services/invitationService'
import { BUDGET_RANGE_OPTIONS, CAMPAIGN_STATUS_LABELS } from '../services/campaignService'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography
const { TextArea } = Input

const primaryColor = '#EFEE96'
const secondaryColor = '#BD72EB'
const pageBackgroundColor = '#000000'

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
    const [acceptModalOpen, setAcceptModalOpen] = useState(false)
    const [disclosureAcknowledged, setDisclosureAcknowledged] = useState(false)
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

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    const canRespond = invitation && (invitation.status === 'PENDING' || invitation.status === 'NEGOTIATING')

    const openAcceptDisclosureModal = () => {
        if (!id || !canRespond) return
        setDisclosureAcknowledged(false)
        setAcceptModalOpen(true)
    }

    const closeAcceptModal = () => {
        setAcceptModalOpen(false)
        setDisclosureAcknowledged(false)
    }

    const handleConfirmAcceptAfterDisclosure = () => {
        if (!id || !canRespond || !disclosureAcknowledged) return
        setResponding(true)
        respondToInvitation(Number(id), { action: 'ACCEPT' })
            .then(() => {
                message.success('Invitation accepted')
                closeAcceptModal()
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
                <div style={{ padding: 24, color: '#aaa' }}>Loading…</div>
            </ConfigProvider>
        )
    }

    const campaign = invitation.campaign
    const budgetLabel = campaign?.budgetRange ? BUDGET_RANGE_OPTIONS.find((o) => o.value === campaign.budgetRange)?.label ?? campaign.budgetRange : null

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: { colorPrimary: primaryColor, colorTextBase: '#ffffff', fontFamily: 'Inter, sans-serif' },
                components: {
                    Layout: { bodyBg: '#000000', headerBg: '#000000', siderBg: '#000000' },
                    Menu: { darkItemBg: '#000000', darkItemSelectedBg: '#333333' },
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
                        defaultSelectedKeys={['invitations']}
                        items={[
                            { key: 'dashboard', icon: <AppstoreOutlined />, label: 'Dashboard', onClick: () => navigate('/influencer/dashboard') },
                            { key: 'profile', icon: <UserOutlined />, label: 'Profile', onClick: () => navigate('/influencer/profile') },
                            { key: 'invitations', icon: <MailOutlined />, label: 'Invitations', onClick: () => navigate('/influencer/invitations') },
                            { key: 'collaborations', icon: <TeamOutlined />, label: 'Collaborations', onClick: () => navigate('/influencer/collaborations') },
                            { key: 'disclosure', icon: <FileTextOutlined />, label: 'Disclosure guidelines', onClick: () => navigate('/influencer/disclosure-guidelines') },
                            { key: 'payments', icon: <DollarOutlined />, label: 'Payments', onClick: () => navigate('/influencer/payments') },
                            { key: 'logout', icon: <LogoutOutlined />, label: 'Logout', onClick: handleLogout, danger: true },
                        ]}
                    />
                </Sider>
                <Layout>
                    <Header style={{ padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'flex-end', background: pageBackgroundColor }}>
                        <Text style={{ color: '#aaa', fontSize: '0.9rem', marginRight: 10 }}>ID: {user?.id}</Text>
                        <Avatar size="large" icon={<UserOutlined />} style={{ backgroundColor: secondaryColor }} />
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
                        <Button type="text" icon={<ArrowLeftOutlined />} onClick={() => navigate('/influencer/invitations')} style={{ marginBottom: 16, color: '#aaa' }}>
                            Back to Invitations
                        </Button>

                        <div style={{ marginBottom: 24 }}>
                            <Tag color={invitation.status === 'PENDING' || invitation.status === 'NEGOTIATING' ? 'gold' : invitation.status === 'ACCEPTED' || invitation.status === 'CONFIRMED' ? 'green' : 'default'}>
                                {INVITATION_STATUS_LABELS[invitation.status as InvitationStatus]}
                            </Tag>
                            <Title level={3} style={{ color: '#fff', margin: '8px 0 0' }}>Invitation #{invitation.id}</Title>
                            <Text type="secondary" style={{ display: 'block', marginTop: 4 }}>Received {formatDate(invitation.createdAt)}</Text>
                            <Text type="secondary" style={{ display: 'block', marginTop: 12 }}>
                                <Link to="/influencer/disclosure-guidelines">View disclosure guidelines</Link>
                                {' '}— reference anytime; required acknowledgment when you accept.
                            </Text>
                        </div>

                        {invitation.brandMessage && (
                            <Card title="Message from brand" bordered={false} size="small" style={{ marginBottom: 24, background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                                <Text style={{ color: '#ccc' }}>{invitation.brandMessage}</Text>
                            </Card>
                        )}

                        {campaign && (
                            <Card title="Campaign details" bordered={false} size="small" style={{ marginBottom: 24, background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
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
                            <Card title="Proposed / negotiated terms" bordered={false} size="small" style={{ marginBottom: 24, background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                                {invitation.proposedAmount != null && <div><Text type="secondary">Amount: </Text><Text style={{ color: '#fff' }}>${Number(invitation.proposedAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}</Text></div>}
                                {invitation.proposedTimeline && <div><Text type="secondary">Timeline: </Text><Text style={{ color: '#fff' }}>{invitation.proposedTimeline}</Text></div>}
                                {invitation.proposedDeliverables && <div><Text type="secondary">Deliverables: </Text><Text style={{ color: '#fff' }}>{invitation.proposedDeliverables}</Text></div>}
                            </Card>
                        )}

                        {canRespond && (
                            <Card title="Respond" bordered={false} size="small" style={{ marginBottom: 24, background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                                <div style={{ display: 'flex', gap: 12, marginBottom: 16 }}>
                                    <Button
                                        type="primary"
                                        icon={<CheckOutlined />}
                                        loading={responding && !acceptModalOpen}
                                        onClick={openAcceptDisclosureModal}
                                        style={{ color: '#000000' }}
                                        data-testid="invitation-accept-open-disclosure"
                                    >
                                        Accept
                                    </Button>
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

                        <Modal
                            title="Accept campaign — disclosure required"
                            open={acceptModalOpen}
                            onCancel={closeAcceptModal}
                            footer={[
                                <Button key="cancel" onClick={closeAcceptModal}>
                                    Cancel
                                </Button>,
                                <Button
                                    key="confirm"
                                    type="primary"
                                    disabled={!disclosureAcknowledged}
                                    loading={responding}
                                    onClick={handleConfirmAcceptAfterDisclosure}
                                    style={{ color: '#000000' }}
                                >
                                    Confirm acceptance
                                </Button>,
                            ]}
                            width={640}
                            destroyOnHidden
                        >
                            <div style={{ maxHeight: 'min(52vh, 420px)', overflowY: 'auto', marginBottom: 16 }}>
                                <DisclosureGuidelinesContent compact />
                            </div>
                            <Checkbox
                                checked={disclosureAcknowledged}
                                onChange={(e) => setDisclosureAcknowledged(e.target.checked)}
                            >
                                {DISCLOSURE_ACKNOWLEDGMENT_LABEL}
                            </Checkbox>
                        </Modal>
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
