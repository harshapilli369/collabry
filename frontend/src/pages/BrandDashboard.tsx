import { useState, useEffect } from 'react'
import {
    Typography,
    Button,
    Card,
    Row,
    Col,
    Modal,
    Form,
    Input,
    InputNumber,
    Select,
    Drawer,
    Spin,
    Avatar,
    Progress,
    Tag,
    Divider,
    Slider,
    App,
} from 'antd'
import {
    PlusCircleOutlined,
    FundProjectionScreenOutlined,
    UnorderedListOutlined,
    EditOutlined,
    DeleteOutlined,
    RobotOutlined,
    UserOutlined,
    MailOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'
import { CampaignPerformanceCharts } from '../components/CampaignPerformanceCharts'
import { getMyBrandProfile } from '../services/brandService'
import { getMyCampaigns, PREFERRED_CONTENT_OPTIONS, type CampaignResponse } from '../services/campaignService'
import {
    getSentInvitations,
    withdrawInvitation,
    updateInvitation,
    createInvitation,
    INVITATION_STATUS_LABELS,
    type InvitationResponse,
    type UpdateInvitationRequest,
} from '../services/invitationService'
import { getMyPayments, type PaymentResponse } from '../services/paymentService'
import { getCampaignRecommendations, type InfluencerRecommendationDTO } from '../services/recommendationService'

const { Title, Text } = Typography

export const BrandDashboard = () => {
    const navigate = useNavigate()
    const { message } = App.useApp()
    const [profileCheckDone, setProfileCheckDone] = useState(false)
    const [brandProfile, setBrandProfile] = useState<any>(null)
    const [campaigns, setCampaigns] = useState<CampaignResponse[]>([])
    const [sentInvitations, setSentInvitations] = useState<InvitationResponse[]>([])
    const [sentInvitationsLoading, setSentInvitationsLoading] = useState(false)
    const [payments, setPayments] = useState<PaymentResponse[]>([])
    const [editModalOpen, setEditModalOpen] = useState(false)
    const [editingInvitation, setEditingInvitation] = useState<InvitationResponse | null>(null)
    const [editForm] = Form.useForm<UpdateInvitationRequest>()
    const [editSubmitting, setEditSubmitting] = useState(false)

    const [inviteModalOpen, setInviteModalOpen] = useState(false)
    const [inviteCampaignId, setInviteCampaignId] = useState<number | null>(null)
    const [inviteSubmitting, setInviteSubmitting] = useState(false)
    const [inviteForm] = Form.useForm()

    const [aiPickCampaignId, setAiPickCampaignId] = useState<number | null>(null)
    const [aiDrawerOpen, setAiDrawerOpen] = useState(false)
    const [aiDrawerCampaignId, setAiDrawerCampaignId] = useState<number | null>(null)
    const [aiRecommendations, setAiRecommendations] = useState<InfluencerRecommendationDTO[]>([])
    const [aiAllRecommendations, setAiAllRecommendations] = useState<InfluencerRecommendationDTO[]>([])
    const [aiLoading, setAiLoading] = useState(false)
    const [aiFilterNiche, setAiFilterNiche] = useState<string | null>(null)
    const [aiFilterMinScore, setAiFilterMinScore] = useState<number>(0)

    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    useEffect(() => {
        if (user?.role !== 'BRAND') {
            setProfileCheckDone(true)
            return
        }
        getMyBrandProfile()
            .then((profile) => {
                if (profile == null) {
                    navigate('/brand/profile/edit', { replace: true })
                    return
                }
                setBrandProfile(profile)
                setProfileCheckDone(true)
            })
            .catch(() => {
                setProfileCheckDone(true)
            })
    }, [user?.role, navigate])

    useEffect(() => {
        if (!profileCheckDone || user?.role !== 'BRAND') return
        getMyCampaigns()
            .then(setCampaigns)
            .catch(() => setCampaigns([]))
    }, [profileCheckDone, user?.role])

    useEffect(() => {
        if (!profileCheckDone || user?.role !== 'BRAND') return
        setSentInvitationsLoading(true)
        getSentInvitations()
            .then(setSentInvitations)
            .catch(() => setSentInvitations([]))
            .finally(() => setSentInvitationsLoading(false))
    }, [profileCheckDone, user?.role])

    useEffect(() => {
        if (!profileCheckDone || user?.role !== 'BRAND') return
        getMyPayments()
            .then(setPayments)
            .catch(() => setPayments([]))
    }, [profileCheckDone, user?.role])

    const handleWithdraw = async (inv: InvitationResponse) => {
        try {
            await withdrawInvitation(inv.id)
            message.success('Invitation withdrawn')
            setSentInvitations((prev) => prev.filter((i) => i.id !== inv.id))
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to withdraw')
        }
    }

    const openEditModal = (inv: InvitationResponse) => {
        setEditingInvitation(inv)
        editForm.setFieldsValue({
            message: inv.brandMessage,
            proposedAmount: inv.proposedAmount,
            proposedTimeline: inv.proposedTimeline,
            proposedDeliverables: inv.proposedDeliverables,
            platform: inv.platform,
        })
        setEditModalOpen(true)
    }
    const closeEditModal = () => {
        setEditModalOpen(false)
        setEditingInvitation(null)
    }
    const onEditSubmit = async (values: UpdateInvitationRequest) => {
        if (!editingInvitation) return
        setEditSubmitting(true)
        try {
            await updateInvitation(editingInvitation.id, values)
            message.success('Invitation updated')
            closeEditModal()
            getSentInvitations().then(setSentInvitations).catch(() => {})
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to update')
        } finally {
            setEditSubmitting(false)
        }
    }

    const openInviteModal = (campaignId: number) => {
        setInviteCampaignId(campaignId)
        inviteForm.resetFields()
        setInviteModalOpen(true)
    }
    const closeInviteModal = () => {
        setInviteModalOpen(false)
        setInviteCampaignId(null)
    }
    const onInviteSubmit = async (values: { influencerId: number; message?: string }) => {
        if (inviteCampaignId == null) return
        setInviteSubmitting(true)
        try {
            await createInvitation(inviteCampaignId, {
                influencerId: values.influencerId,
                message: values.message?.trim() || undefined,
            })
            message.success('Invitation sent')
            closeInviteModal()
            getSentInvitations().then(setSentInvitations).catch(() => {})
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to send invitation')
        } finally {
            setInviteSubmitting(false)
        }
    }

    const normalizeNiche = (niche: string | undefined): string => {
        if (!niche) return ''
        const lower = niche.toLowerCase().trim()
        if (lower === 'tech' || lower === 'technology') return 'Technology'
        if (lower === 'fashion' || lower === 'style') return 'Fashion'
        if (lower === 'gaming' || lower === 'games') return 'Gaming'
        if (lower === 'fitness' || lower === 'health' || lower === 'health & fitness') return 'Fitness'
        if (lower === 'food' || lower === 'cooking' || lower === 'food & cooking') return 'Food'
        return niche.charAt(0).toUpperCase() + niche.slice(1)
    }

    const applyAiFilters = (all: InfluencerRecommendationDTO[], niche: string | null, minScore: number) => {
        let filtered = all
        if (niche) {
            filtered = filtered.filter((r) => normalizeNiche(r.niche) === niche)
        }
        if (minScore > 0) {
            filtered = filtered.filter((r) => r.matchScore >= minScore)
        }
        setAiRecommendations(filtered)
    }

    const openAiDrawer = async (campaignId: number) => {
        setAiDrawerCampaignId(campaignId)
        setAiDrawerOpen(true)
        setAiLoading(true)
        setAiRecommendations([])
        setAiAllRecommendations([])
        setAiFilterNiche(null)
        setAiFilterMinScore(0)
        try {
            const recs = await getCampaignRecommendations(campaignId)
            setAiAllRecommendations(recs)
            setAiRecommendations(recs)
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to load AI recommendations')
        } finally {
            setAiLoading(false)
        }
    }
    const closeAiDrawer = () => {
        setAiDrawerOpen(false)
        setAiDrawerCampaignId(null)
    }
    const handleInviteFromAi = (influencerId: number) => {
        setInviteCampaignId(aiDrawerCampaignId)
        inviteForm.setFieldsValue({ influencerId })
        setInviteModalOpen(true)
    }

    const primaryColor = BRAND_PORTAL_PRIMARY

    if (!profileCheckDone && user?.role === 'BRAND') {
        return null
    }

    return (
        <BrandPortalLayout activeMenuKey="dashboard" brandProfileForHeader={brandProfile}>
            {/* Welcome Banner */}
            <div
                style={{
                    marginBottom: 30,
                    padding: '32px 36px',
                    borderRadius: 16,
                    background: 'linear-gradient(135deg, #1a1a00 0%, #0d0d0d 50%, #1a1a00 100%)',
                    border: `1px solid ${primaryColor}15`,
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
                        background: `radial-gradient(circle, ${primaryColor}12 0%, transparent 70%)`,
                        pointerEvents: 'none',
                    }}
                />
                <div style={{ position: 'relative', zIndex: 1 }}>
                    <Title level={2} style={{ color: '#fff', margin: 0 }}>
                        Welcome back, <span style={{ color: primaryColor }}>{brandProfile?.name || 'Brand'}</span>!
                    </Title>
                    <Text style={{ color: '#888', fontSize: 16 }}>Overview of your campaign performance and collaborations.</Text>
                </div>
            </div>

            <Row gutter={[20, 20]}>
                {/* Stats Cards */}
                {[
                    { label: 'Draft', count: campaigns.filter((c) => c.status === 'DRAFT').length, color: '#888', icon: <EditOutlined style={{ fontSize: 20, color: '#888' }} /> },
                    { label: 'Active', count: campaigns.filter((c) => c.status === 'ACTIVE').length, color: '#52c41a', icon: <FundProjectionScreenOutlined style={{ fontSize: 20, color: '#52c41a' }} /> },
                    { label: 'Completed', count: campaigns.filter((c) => c.status === 'COMPLETED').length, color: '#1890ff', icon: <UnorderedListOutlined style={{ fontSize: 20, color: '#1890ff' }} /> },
                    { label: 'Total', count: campaigns.length, color: primaryColor, icon: <FundProjectionScreenOutlined style={{ fontSize: 20, color: primaryColor }} /> },
                ].map((stat, idx) => (
                    <Col span={6} key={idx}>
                        <Card
                            className="brand-stat-card"
                            style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: `1px solid ${stat.color}20`, transition: 'all 0.3s ease' }}
                        >
                            <div style={{ marginBottom: 8 }}>{stat.icon}</div>
                            <Text type="secondary">{stat.label}</Text>
                            <Title level={2} style={{ margin: '8px 0 0', color: stat.color }}>{stat.count}</Title>
                        </Card>
                    </Col>
                ))}

                {/* Performance Charts */}
                <Col span={24}>
                    <CampaignPerformanceCharts
                        campaigns={campaigns}
                        sentInvitations={sentInvitations}
                        payments={payments}
                    />
                </Col>

                {/* Campaigns Card */}
                <Col span={12}>
                    <Card
                        title={
                            <span style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#fff' }}>
                                <FundProjectionScreenOutlined style={{ color: primaryColor }} />
                                Campaigns
                            </span>
                        }
                        style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a', height: '100%' }}
                    >
                        <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
                            Manage your campaigns, track performance, and send invitations.
                        </Text>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12 }}>
                            <Button
                                type="primary"
                                icon={<UnorderedListOutlined />}
                                onClick={() => navigate('/brand/campaigns')}
                                style={{ color: '#000', fontWeight: 600, borderRadius: 10 }}
                            >
                                View my campaigns
                            </Button>
                            <Button
                                icon={<PlusCircleOutlined />}
                                onClick={() => navigate('/brand/campaigns/create')}
                                style={{ borderRadius: 10 }}
                            >
                                Create campaign
                            </Button>
                        </div>
                    </Card>
                </Col>

                {/* AI Matchmaker Card */}
                <Col span={12}>
                    <Card
                        title={
                            <span style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#fff' }}>
                                <RobotOutlined style={{ color: primaryColor }} />
                                AI Matchmaker
                            </span>
                        }
                        style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a', height: '100%' }}
                    >
                        <Text type="secondary" style={{ display: 'block', marginBottom: 12 }}>
                            Get AI-powered influencer recommendations for your campaigns.
                        </Text>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                            <Select
                                placeholder="Choose a campaign"
                                style={{ width: '100%' }}
                                allowClear
                                options={campaigns.map((c) => ({ value: c.id, label: c.name }))}
                                value={aiPickCampaignId ?? undefined}
                                onChange={(v) => setAiPickCampaignId(v ?? null)}
                            />
                            <div style={{ display: 'flex', gap: 10 }}>
                                <Button
                                    type="primary"
                                    icon={<RobotOutlined />}
                                    disabled={aiPickCampaignId == null}
                                    onClick={() => aiPickCampaignId != null && openAiDrawer(aiPickCampaignId)}
                                    style={{ color: '#000', fontWeight: 600, borderRadius: 10, flex: 1 }}
                                >
                                    AI Match
                                </Button>
                                <Button
                                    icon={<MailOutlined />}
                                    disabled={aiPickCampaignId == null}
                                    onClick={() => aiPickCampaignId != null && openInviteModal(aiPickCampaignId)}
                                    style={{ borderRadius: 10 }}
                                >
                                    Invite
                                </Button>
                            </div>
                        </div>
                    </Card>
                </Col>

                <Col span={24}>
                    <Card
                        title={
                            <span style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#fff' }}>
                                <MailOutlined style={{ color: primaryColor }} />
                                Sent Invitations
                            </span>
                        }
                        style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a' }}
                        extra={
                            <Button
                                type="link"
                                onClick={() => navigate('/brand/influencers')}
                                style={{ color: primaryColor, padding: 0 }}
                            >
                                Find influencers
                            </Button>
                        }
                    >
                        <Text type="secondary" style={{ display: 'block', marginBottom: 12 }}>
                            Track invitations you've sent. Edit or withdraw before acceptance.
                        </Text>
                        {sentInvitationsLoading ? (
                            <Text type="secondary">Loading...</Text>
                        ) : sentInvitations.length === 0 ? (
                            <div style={{ textAlign: 'center', padding: '30px 0' }}>
                                <MailOutlined style={{ fontSize: 32, opacity: 0.2, color: primaryColor, marginBottom: 12, display: 'block' }} />
                                <Text type="secondary">No invitations sent yet. Use "Find influencers" or Invite on a campaign to send one.</Text>
                            </div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                {sentInvitations.map((inv) => {
                                    const canWithdrawOrEdit = inv.status === 'PENDING' || inv.status === 'NEGOTIATING'
                                    const campaign = campaigns.find((c) => c.id === inv.campaignId)
                                    const statusColor = (inv.status === 'PENDING' || inv.status === 'NEGOTIATING') ? '#faad14' : (inv.status === 'ACCEPTED' || inv.status === 'CONFIRMED') ? '#52c41a' : '#888'
                                    return (
                                        <Card
                                            key={inv.id}
                                            size="small"
                                            className="brand-invitation-item"
                                            style={{ background: '#141414', borderRadius: 12, borderColor: '#1a1a1a', borderLeft: `3px solid ${statusColor}` }}
                                        >
                                            <div
                                                style={{
                                                    display: 'flex',
                                                    justifyContent: 'space-between',
                                                    alignItems: 'center',
                                                    flexWrap: 'wrap',
                                                    gap: 8,
                                                }}
                                            >
                                                <div>
                                                    <Text strong style={{ color: '#fff' }}>
                                                        Campaign: {campaign?.name ?? `#${inv.campaignId}`}
                                                    </Text>
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 6 }}>
                                                        {inv.influencerProfilePicture && (
                                                            <img src={inv.influencerProfilePicture} alt={inv.influencerName || 'Influencer'} style={{ width: 28, height: 28, borderRadius: '50%', objectFit: 'cover', border: '1px solid #333' }} />
                                                        )}
                                                        <Text type="secondary">{inv.influencerName || `Influencer ID ${inv.influencerId}`}</Text>
                                                        {inv.influencerNiche && <Tag style={{ fontSize: 11 }}>{inv.influencerNiche}</Tag>}
                                                        {inv.influencerRate && <Text type="secondary" style={{ fontSize: 12 }}>· ${Number(inv.influencerRate).toLocaleString()}/post</Text>}
                                                    </div>
                                                    {inv.proposedAmount != null && (
                                                        <span style={{ marginLeft: 8 }}>
                                                            <Text type="secondary">
                                                                · ${Number(inv.proposedAmount).toLocaleString()}
                                                            </Text>
                                                        </span>
                                                    )}
                                                    <div style={{ marginTop: 4 }}>
                                                        <Text style={{ fontSize: 12, fontWeight: 600, color: primaryColor }}>
                                                            {INVITATION_STATUS_LABELS[inv.status]}
                                                        </Text>
                                                    </div>
                                                </div>
                                                {canWithdrawOrEdit && (
                                                    <div style={{ display: 'flex', gap: 8 }}>
                                                        <Button
                                                            type="default"
                                                            size="small"
                                                            icon={<EditOutlined />}
                                                            onClick={() => openEditModal(inv)}
                                                        >
                                                            Edit
                                                        </Button>
                                                        <Button
                                                            type="default"
                                                            size="small"
                                                            danger
                                                            icon={<DeleteOutlined />}
                                                            onClick={() => handleWithdraw(inv)}
                                                        >
                                                            Withdraw
                                                        </Button>
                                                    </div>
                                                )}
                                            </div>
                                        </Card>
                                    )
                                })}
                            </div>
                        )}
                    </Card>
                </Col>
            </Row>

            <Modal title="Edit invitation" open={editModalOpen} onCancel={closeEditModal} footer={null} destroyOnClose width={520}>
                <Form form={editForm} layout="vertical" onFinish={onEditSubmit}>
                    <Form.Item name="message" label="Message">
                        <Input.TextArea rows={2} placeholder="Message to influencer" />
                    </Form.Item>
                    <Form.Item name="proposedDeliverables" label="Deliverables">
                        <Input.TextArea rows={2} placeholder="e.g. 1 Instagram Reel" />
                    </Form.Item>
                    <Form.Item name="proposedTimeline" label="Timeline">
                        <Input placeholder="e.g. 2 weeks" />
                    </Form.Item>
                    <Form.Item name="proposedAmount" label="Proposed amount">
                        <InputNumber min={0} step={100} style={{ width: '100%' }} addonBefore="$" />
                    </Form.Item>
                    <Form.Item name="platform" label="Platform">
                        <Select placeholder="Select platform" allowClear options={PREFERRED_CONTENT_OPTIONS} />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={editSubmitting} style={{ color: '#000000' }}>
                            Save changes
                        </Button>
                        <Button style={{ marginLeft: 8 }} onClick={closeEditModal}>
                            Cancel
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal title="Invite influencer" open={inviteModalOpen} onCancel={closeInviteModal} footer={null} destroyOnClose>
                <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit}>
                    <Form.Item
                        name="influencerId"
                        label="Influencer user ID"
                        rules={[{ required: true, message: 'Enter the influencer’s user ID' }]}
                    >
                        <InputNumber min={1} step={1} style={{ width: '100%' }} placeholder="e.g. 2" />
                    </Form.Item>
                    <Form.Item name="message" label="Message (optional)">
                        <Input.TextArea rows={3} placeholder="Personal message to the influencer" />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={inviteSubmitting} style={{ color: '#000' }}>
                            Send invitation
                        </Button>
                        <Button style={{ marginLeft: 8 }} onClick={closeInviteModal}>
                            Cancel
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>

            <Drawer
                title={
                    <span
                        style={{
                            color: primaryColor,
                            fontSize: '1.2rem',
                            fontWeight: 600,
                            display: 'flex',
                            alignItems: 'center',
                            gap: 8,
                        }}
                    >
                        <RobotOutlined />
                        AI Matchmaker
                    </span>
                }
                placement="right"
                onClose={closeAiDrawer}
                open={aiDrawerOpen}
                width={550}
                styles={{
                    body: { background: '#0a0a0a', color: '#fff', padding: 24 },
                    header: { background: '#111', borderBottom: '1px solid #333' },
                }}
            >
                {aiLoading ? (
                    <div style={{ textAlign: 'center', padding: '60px 0' }}>
                        <Spin size="large" />
                        <div style={{ marginTop: 24, color: '#aaa', fontSize: 16 }}>Scanning influencer dataset...</div>
                        <div style={{ marginTop: 8, color: '#666', fontSize: 13 }}>
                            Analyzing campaign metrics, niche resonance, and engagement rates
                        </div>
                    </div>
                ) : aiAllRecommendations.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '100px 0', color: '#aaa' }}>
                        <RobotOutlined style={{ fontSize: 40, opacity: 0.2, marginBottom: 16 }} />
                        <div>No optimal matches found for this campaign&apos;s criteria.</div>
                    </div>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
                        <Text style={{ color: '#aaa', fontSize: 14 }}>
                            We analyzed your campaign metrics against available influencers. Here are your top algorithmic matches:
                        </Text>

                        {/* Filter Controls */}
                        <div style={{ background: '#161616', borderRadius: 12, padding: 16, border: '1px solid #2a2a2a' }}>
                            <div style={{ display: 'flex', gap: 12, alignItems: 'flex-end', flexWrap: 'wrap' }}>
                                <div style={{ flex: 1, minWidth: 140 }}>
                                    <Text style={{ color: '#888', fontSize: 12, display: 'block', marginBottom: 4 }}>Niche</Text>
                                    <Select
                                        placeholder="All niches"
                                        value={aiFilterNiche ?? 'all'}
                                        onChange={(val) => {
                                            const selected = val === 'all' ? null : val
                                            setAiFilterNiche(selected)
                                            applyAiFilters(aiAllRecommendations, selected, aiFilterMinScore)
                                        }}
                                        style={{ width: '100%' }}
                                        options={[
                                            { label: 'All', value: 'all' },
                                            ...[...new Set(aiAllRecommendations.map((r) => normalizeNiche(r.niche)).filter(Boolean))].map(
                                                (n) => ({ label: n, value: n })
                                            ),
                                        ]}
                                    />
                                </div>
                                <div style={{ flex: 1, minWidth: 160 }}>
                                    <Text style={{ color: '#888', fontSize: 12, display: 'block', marginBottom: 4 }}>
                                        Min Match Score: {aiFilterMinScore}%
                                    </Text>
                                    <Slider
                                        min={0}
                                        max={100}
                                        value={aiFilterMinScore}
                                        onChange={(val) => {
                                            setAiFilterMinScore(val)
                                            applyAiFilters(aiAllRecommendations, aiFilterNiche, val)
                                        }}
                                        styles={{ track: { background: primaryColor }, rail: { background: '#333' } }}
                                    />
                                </div>
                            </div>
                            <Text style={{ color: '#555', fontSize: 11, marginTop: 4, display: 'block' }}>
                                Showing {aiRecommendations.length} of {aiAllRecommendations.length} matches
                            </Text>
                        </div>

                        {aiRecommendations.length === 0 && aiAllRecommendations.length > 0 ? (
                            <div style={{ textAlign: 'center', padding: '40px 0', color: '#aaa' }}>
                                <RobotOutlined style={{ fontSize: 32, opacity: 0.3, marginBottom: 12 }} />
                                <div>No matches for the selected filters. Try adjusting your criteria.</div>
                            </div>
                        ) : null}

                        {aiRecommendations.map((rec, idx) => (
                            <Card
                                key={rec.influencerId}
                                bordered={false}
                                style={{
                                    background: 'linear-gradient(145deg, #1c1c1c 0%, #151515 100%)',
                                    borderRadius: 16,
                                    border: idx === 0 ? `1px solid ${primaryColor}` : '1px solid #2a2a2a',
                                    boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
                                    overflow: 'hidden',
                                }}
                                styles={{ body: { padding: idx === 0 ? 0 : 20 } }}
                            >
                                {idx === 0 && (
                                    <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                                        <div
                                            style={{
                                                background: primaryColor,
                                                color: '#000',
                                                fontSize: 11,
                                                fontWeight: 'bold',
                                                padding: '4px 12px',
                                                borderBottomLeftRadius: 12,
                                            }}
                                        >
                                            TOP MATCH
                                        </div>
                                    </div>
                                )}
                                <div style={{ display: 'flex', gap: 20, padding: idx === 0 ? '8px 20px 20px' : 0 }}>
                                    <div
                                        style={{
                                            display: 'flex',
                                            flexDirection: 'column',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                        }}
                                    >
                                        <Progress
                                            type="circle"
                                            percent={rec.matchScore}
                                            size={70}
                                            strokeWidth={8}
                                            strokeColor={
                                                rec.matchScore >= 90
                                                    ? { '0%': '#108ee9', '100%': primaryColor }
                                                    : rec.matchScore >= 75
                                                      ? { '0%': '#faad14', '100%': primaryColor }
                                                      : '#faad14'
                                            }
                                            format={(percent) => (
                                                <span style={{ color: '#fff', fontSize: '18px', fontWeight: 800 }}>
                                                    {percent}%
                                                </span>
                                            )}
                                        />
                                        <Text
                                            style={{
                                                color: '#888',
                                                fontSize: 11,
                                                marginTop: 6,
                                                fontWeight: 600,
                                                textTransform: 'uppercase',
                                                letterSpacing: 1,
                                            }}
                                        >
                                            Match
                                        </Text>
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                                                <Avatar
                                                    src={rec.profilePictureUrl || undefined}
                                                    icon={!rec.profilePictureUrl ? <UserOutlined /> : undefined}
                                                    size={50}
                                                    style={{ border: `2px solid ${primaryColor}40` }}
                                                />
                                                <div>
                                                    <div style={{ color: '#fff', fontSize: 18, fontWeight: 700 }}>
                                                        {rec.name || `Influencer #${rec.influencerId}`}
                                                    </div>
                                                    {rec.niche && (
                                                        <Tag
                                                            style={{
                                                                marginTop: 6,
                                                                background: '#112233',
                                                                border: '1px solid #1890ff',
                                                                color: '#40a9ff',
                                                                borderRadius: 12,
                                                                padding: '0 8px',
                                                            }}
                                                        >
                                                            {rec.niche}
                                                        </Tag>
                                                    )}
                                                </div>
                                            </div>
                                            <Button
                                                type="primary"
                                                onClick={() => handleInviteFromAi(rec.influencerId)}
                                                style={{
                                                    background: primaryColor,
                                                    color: '#000',
                                                    fontWeight: 600,
                                                    borderRadius: 20,
                                                    padding: '0 16px',
                                                    border: 'none',
                                                }}
                                            >
                                                Invite
                                            </Button>
                                        </div>
                                        <Divider style={{ margin: '16px 0', borderColor: '#2a2a2a' }} />
                                        <div
                                            style={{
                                                background: '#000000',
                                                padding: '12px 16px',
                                                borderRadius: 8,
                                                borderLeft: `3px solid ${primaryColor}`,
                                            }}
                                        >
                                            <Text style={{ color: '#ccc', fontSize: 13, lineHeight: 1.5 }}>{rec.reason}</Text>
                                        </div>
                                    </div>
                                </div>
                            </Card>
                        ))}
                    </div>
                )}
            </Drawer>
        </BrandPortalLayout>
    )
}
