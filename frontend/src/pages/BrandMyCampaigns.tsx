import { useState, useEffect } from 'react'
import { Typography, Button, Card, Tabs, Modal, Form, Input, InputNumber, Select, Table, App } from 'antd'
import { PlusCircleOutlined, FundProjectionScreenOutlined, MailOutlined, ArrowLeftOutlined, EditOutlined, CheckCircleOutlined, RocketOutlined, StopOutlined, DownloadOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'
import { getMyBrandProfile, type BrandProfileResponse } from '../services/brandService'
import {
    getMyCampaigns,
    downloadCampaignReport,
    updateCampaignStatus,
    CAMPAIGN_STATUS_LABELS,
    BUDGET_RANGE_OPTIONS,
    PREFERRED_CONTENT_OPTIONS,
    type CampaignResponse,
    type CampaignStatus,
} from '../services/campaignService'
import { createInvitation, type InvitationRequest } from '../services/invitationService'
import { userService, type InfluencerSearchResult } from '../services/userService'

const { Title, Text } = Typography
const STATUS_ORDER: CampaignStatus[] = ['DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED']

export const BrandMyCampaigns = () => {
    const navigate = useNavigate()
    const { modal, message: messageApi } = App.useApp()
    const [profileCheckDone, setProfileCheckDone] = useState(false)
    const [brandProfile, setBrandProfile] = useState<BrandProfileResponse | null>(null)
    const [campaigns, setCampaigns] = useState<CampaignResponse[]>([])
    const [campaignsLoading, setCampaignsLoading] = useState(false)
    const [inviteModalOpen, setInviteModalOpen] = useState(false)
    const [inviteCampaignId, setInviteCampaignId] = useState<number | null>(null)
    const [inviteSubmitting, setInviteSubmitting] = useState(false)
    const [inviteForm] = Form.useForm()
    const [findIdModalOpen, setFindIdModalOpen] = useState(false)
    const [influencerList, setInfluencerList] = useState<InfluencerSearchResult[]>([])
    const [influencerListLoading, setInfluencerListLoading] = useState(false)
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
            .catch(() => setProfileCheckDone(true))
    }, [user?.role, navigate])

    const fetchCampaigns = () => {
        if (!profileCheckDone || user?.role !== 'BRAND') return
        setCampaignsLoading(true)
        getMyCampaigns()
            .then(setCampaigns)
            .catch(() => setCampaigns([]))
            .finally(() => setCampaignsLoading(false))
    }

    useEffect(() => {
        fetchCampaigns()
    }, [profileCheckDone, user?.role])

    const campaignsByStatus = STATUS_ORDER.map((status) => ({
        status,
        label: CAMPAIGN_STATUS_LABELS[status],
        list: campaigns.filter((c) => c.status === status),
    }))

    const openInviteModal = (campaignId: number) => {
        setInviteCampaignId(campaignId)
        inviteForm.resetFields()
        setInviteModalOpen(true)
    }

    const openFindIdModal = () => {
        setFindIdModalOpen(true)
        setInfluencerListLoading(true)
        userService
            .listInfluencers()
            .then(setInfluencerList)
            .catch(() => {
                messageApi.error('Failed to load influencers')
                setInfluencerList([])
            })
            .finally(() => setInfluencerListLoading(false))
    }

    const closeInviteModal = () => {
        setInviteModalOpen(false)
        setInviteCampaignId(null)
    }

    const onInviteSubmit = async (values: InvitationRequest & { expiresInDays?: number }) => {
        if (inviteCampaignId == null) return
        setInviteSubmitting(true)
        try {
            await createInvitation(inviteCampaignId, {
                influencerId: values.influencerId,
                message: values.message?.trim() || undefined,
                proposedAmount: values.proposedAmount,
                proposedTimeline: values.proposedTimeline?.trim() || undefined,
                proposedDeliverables: values.proposedDeliverables?.trim() || undefined,
                platform: values.platform || undefined,
                expiresInDays: values.expiresInDays ?? 14,
            })
            messageApi.success('Invitation sent')
            closeInviteModal()
        } catch (e) {
            messageApi.error(e instanceof Error ? e.message : 'Failed to send invitation')
        } finally {
            setInviteSubmitting(false)
        }
    }

    const onDownloadReport = async (campaignId: number) => {
        try {
            await downloadCampaignReport(campaignId)
            messageApi.success('Campaign report downloaded')
        } catch (e) {
            messageApi.error(e instanceof Error ? e.message : 'Failed to download report')
        }
    }

    const onStatusUpdate = (campaignId: number, newStatus: CampaignStatus) => {
        let actionLabel = ''
        if (newStatus === 'ACTIVE') actionLabel = 'publish'
        else if (newStatus === 'CANCELLED') actionLabel = 'cancel'
        else if (newStatus === 'COMPLETED') actionLabel = 'complete'

        modal.confirm({
            title: `Confirm ${actionLabel}`,
            content: `Are you sure you want to ${actionLabel} this campaign?`,
            okText: 'Yes',
            cancelText: 'No',
            onOk: async () => {
                try {
                    await updateCampaignStatus(campaignId, newStatus)
                    messageApi.success(`Campaign ${actionLabel}ed successfully`)
                    fetchCampaigns()
                } catch (e) {
                    messageApi.error(e instanceof Error ? e.message : `Failed to ${actionLabel} campaign`)
                }
            },
        })
    }

    const primaryColor = BRAND_PORTAL_PRIMARY

    const statusColors: Record<string, string> = { DRAFT: '#888', ACTIVE: '#52c41a', COMPLETED: '#1890ff', CANCELLED: '#ff4d4f' }
    const statusIcons: Record<string, React.ReactNode> = { DRAFT: <EditOutlined />, ACTIVE: <RocketOutlined />, COMPLETED: <CheckCircleOutlined />, CANCELLED: <StopOutlined /> }

    if (!profileCheckDone && user?.role === 'BRAND') {
        return null
    }

    return (
        <BrandPortalLayout activeMenuKey="campaign-view" brandProfileForHeader={brandProfile}>
            <div style={{ marginBottom: 24 }}>
                <Button
                    type="link"
                    icon={<ArrowLeftOutlined />}
                    onClick={() => navigate('/brand/dashboard')}
                    style={{ color: primaryColor, paddingLeft: 0, marginBottom: 16 }}
                >
                    Back to Dashboard
                </Button>
                <Title level={1} style={{ color: primaryColor, margin: '0 0 8px', fontSize: '2rem' }}>
                    My campaigns
                </Title>
                <Text style={{ color: '#aaa', fontSize: '1.1rem' }}>
                    Browse campaigns by status and send invitations to influencers.
                </Text>
            </div>

            <Card
                title={
                    <span style={{ display: 'flex', alignItems: 'center', gap: 8, color: '#fff' }}>
                        <FundProjectionScreenOutlined style={{ color: primaryColor }} />
                        Campaigns
                    </span>
                }
                style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a' }}
                extra={
                    <Button
                        type="primary"
                        icon={<PlusCircleOutlined />}
                        onClick={() => navigate('/brand/campaigns/create')}
                        style={{ borderRadius: 10 }}
                    >
                        Create campaign
                    </Button>
                }
            >
                {campaignsLoading ? (
                    <Text type="secondary">Loading campaigns...</Text>
                ) : campaigns.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '40px 0' }}>
                        <FundProjectionScreenOutlined style={{ fontSize: 40, opacity: 0.2, color: primaryColor, marginBottom: 16, display: 'block' }} />
                        <Text type="secondary" style={{ fontSize: 16 }}>No campaigns yet. Create one to get started.</Text>
                    </div>
                ) : (
                    <Tabs
                        defaultActiveKey={STATUS_ORDER.find((s) => campaigns.some((c) => c.status === s)) ?? 'DRAFT'}
                        items={campaignsByStatus.map(({ status, label, list }) => ({
                            key: status,
                            label: (
                                <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                                    <span style={{ color: statusColors[status] }}>{statusIcons[status]}</span>
                                    {label} ({list.length})
                                </span>
                            ),
                            children: (
                                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                    {list.length === 0 ? (
                                        <Text type="secondary">No {label.toLowerCase()} campaigns.</Text>
                                    ) : (
                                        list.map((campaign) => (
                                            <Card
                                                key={campaign.id}
                                                size="small"
                                                className="brand-campaign-card"
                                                style={{
                                                    background: '#141414',
                                                    borderRadius: 12,
                                                    borderColor: '#1a1a1a',
                                                    borderLeft: `3px solid ${statusColors[campaign.status] || '#333'}`,
                                                }}
                                            >
                                                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 8 }}>
                                                    <div>
                                                        <Text strong style={{ color: '#fff', fontSize: 16 }}>
                                                            {campaign.name}
                                                        </Text>
                                                        {campaign.description && (
                                                            <div>
                                                                <Text type="secondary" style={{ fontSize: 13 }}>
                                                                    {campaign.description.slice(0, 100)}
                                                                    {campaign.description.length > 100 ? '...' : ''}
                                                                </Text>
                                                            </div>
                                                        )}
                                                        <div style={{ marginTop: 6 }}>
                                                            <Text type="secondary" style={{ fontSize: 12 }}>
                                                                Budget: {BUDGET_RANGE_OPTIONS.find((o) => o.value === campaign.budgetRange)?.label ?? campaign.budgetRange}
                                                                {campaign.numberOfInfluencers != null && ` \u00b7 ${campaign.numberOfInfluencers} influencer(s)`}
                                                                {campaign.startDate && ` \u00b7 ${campaign.startDate}`}
                                                            </Text>
                                                        </div>
                                                    </div>
                                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                                        <Text style={{ fontSize: 12, fontWeight: 600, color: statusColors[campaign.status] || primaryColor }}>
                                                            {CAMPAIGN_STATUS_LABELS[campaign.status]}
                                                        </Text>
                                                        <Button type="default" size="small" icon={<MailOutlined />} onClick={() => openInviteModal(campaign.id)} style={{ borderRadius: 8 }}>
                                                            Invite
                                                        </Button>
                                                        {campaign.status === 'DRAFT' && (
                                                            <Button
                                                                type="primary"
                                                                size="small"
                                                                icon={<RocketOutlined />}
                                                                onClick={() => onStatusUpdate(campaign.id, 'ACTIVE')}
                                                                style={{ borderRadius: 8, background: statusColors.ACTIVE, borderColor: statusColors.ACTIVE, color: '#000' }}
                                                            >
                                                                Publish
                                                            </Button>
                                                        )}
                                                        {campaign.status === 'ACTIVE' && (
                                                            <Button
                                                                type="primary"
                                                                size="small"
                                                                icon={<CheckCircleOutlined />}
                                                                onClick={() => onStatusUpdate(campaign.id, 'COMPLETED')}
                                                                style={{ borderRadius: 8, background: statusColors.COMPLETED, borderColor: statusColors.COMPLETED, color: '#fff' }}
                                                            >
                                                                Complete
                                                            </Button>
                                                        )}
                                                        {(campaign.status === 'DRAFT' || campaign.status === 'ACTIVE') && (
                                                            <Button
                                                                type="default"
                                                                danger
                                                                size="small"
                                                                icon={<StopOutlined />}
                                                                onClick={() => onStatusUpdate(campaign.id, 'CANCELLED')}
                                                                style={{ borderRadius: 8 }}
                                                            >
                                                                Cancel
                                                            </Button>
                                                        )}
                                                        <Button type="default" size="small" icon={<DownloadOutlined />} onClick={() => onDownloadReport(campaign.id)} style={{ borderRadius: 8 }}>
                                                            Download report
                                                        </Button>
                                                    </div>
                                                </div>
                                            </Card>
                                        ))
                                    )}
                                </div>
                            ),
                        }))}
                    />
                )}
            </Card>

            <Modal title="Invite influencer" open={inviteModalOpen} onCancel={closeInviteModal} footer={null} destroyOnClose width={520}>
                <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit}>
                    <Form.Item
                        name="influencerId"
                        label={
                            <span>
                                Influencer user ID
                                <Button type="link" size="small" onClick={openFindIdModal} style={{ paddingLeft: 8 }}>
                                    Find user ID
                                </Button>
                            </span>
                        }
                        rules={[{ required: true, message: 'Enter the influencer’s user ID' }]}
                    >
                        <InputNumber min={1} step={1} style={{ width: '100%' }} placeholder="e.g. 2" />
                    </Form.Item>
                    <Form.Item name="message" label="Message (optional)">
                        <Input.TextArea rows={2} placeholder="Personal message to the influencer" />
                    </Form.Item>
                    <Form.Item name="proposedDeliverables" label="Deliverables">
                        <Input.TextArea rows={2} placeholder="e.g. 1 Instagram Reel, 3 Stories" />
                    </Form.Item>
                    <Form.Item name="proposedTimeline" label="Timeline">
                        <Input placeholder="e.g. 2 weeks from acceptance" />
                    </Form.Item>
                    <Form.Item name="proposedAmount" label="Budget / proposed amount">
                        <InputNumber min={0} step={100} style={{ width: '100%' }} placeholder="Amount" addonBefore="$" />
                    </Form.Item>
                    <Form.Item name="platform" label="Platform">
                        <Select placeholder="Select platform" allowClear options={PREFERRED_CONTENT_OPTIONS} />
                    </Form.Item>
                    <Form.Item name="expiresInDays" label="Invitation valid for (days)" initialValue={14}>
                        <InputNumber min={1} max={90} style={{ width: '100%' }} />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={inviteSubmitting} style={{ color: '#000000' }}>
                            Send invitation
                        </Button>
                        <Button style={{ marginLeft: 8 }} onClick={closeInviteModal}>
                            Cancel
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="Influencer user IDs"
                open={findIdModalOpen}
                onCancel={() => setFindIdModalOpen(false)}
                footer={<Button onClick={() => setFindIdModalOpen(false)}>Close</Button>}
                width={560}
            >
                <p style={{ color: '#666', marginBottom: 12 }}>Copy the ID and paste it into the invite form.</p>
                <Table
                    size="small"
                    loading={influencerListLoading}
                    dataSource={influencerList}
                    rowKey="id"
                    columns={[
                        { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
                        { title: 'Email', dataIndex: 'email', key: 'email' },
                        { title: 'Name', dataIndex: 'displayName', key: 'displayName' },
                    ]}
                    pagination={influencerList.length <= 10 ? false : { pageSize: 10 }}
                />
            </Modal>
        </BrandPortalLayout>
    )
}
