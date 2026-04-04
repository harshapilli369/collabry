import { useState, useEffect } from 'react'
import { Typography, Button, Card, Tabs, Modal, Form, Input, InputNumber, Select, App, Row, Col, Avatar, Rate } from 'antd'
import { PlusCircleOutlined, FundProjectionScreenOutlined, MailOutlined, ArrowLeftOutlined, EditOutlined, CheckCircleOutlined, RocketOutlined, StopOutlined, DownloadOutlined, SearchOutlined, UserOutlined } from '@ant-design/icons'
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
import { searchInfluencers, type InfluencerProfileResponse, type InfluencerSearchParams } from '../services/influencerProfileService'

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
    const [searchForm] = Form.useForm<InfluencerSearchParams>()
    const [searchLoading, setSearchLoading] = useState(false)
    const [searchResults, setSearchResults] = useState<InfluencerProfileResponse[]>([])
    const [searchDone, setSearchDone] = useState(false)
    const [selectedInfluencer, setSelectedInfluencer] = useState<InfluencerProfileResponse | null>(null)
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
        searchForm.resetFields()
        setSearchResults([])
        setSearchDone(false)
        setSelectedInfluencer(null)
        setInviteModalOpen(true)
    }

    const closeInviteModal = () => {
        setInviteModalOpen(false)
        setInviteCampaignId(null)
    }

    const onSearchInfluencers = async () => {
        const values = searchForm.getFieldsValue()
        setSearchLoading(true)
        try {
            const list = await searchInfluencers({
                niche: values.niche,
                location: values.location,
                minFollowers: values.minFollowers,
                maxFollowers: values.maxFollowers,
                minEngagementRate: values.minEngagementRate,
            })
            setSearchDone(true)
            setSearchResults(list)
            if (list.length === 0) messageApi.info('No influencers match your filters.')
        } catch (e) {
            messageApi.error(e instanceof Error ? e.message : 'Search failed')
            setSearchResults([])
            setSearchDone(true)
        } finally {
            setSearchLoading(false)
        }
    }

    const onInviteSubmit = async (values: InvitationRequest & { expiresInDays?: number }) => {
        if (inviteCampaignId == null) return
        if (!selectedInfluencer) {
            messageApi.warning('Search and select an influencer first.')
            return
        }
        setInviteSubmitting(true)
        try {
            await createInvitation(inviteCampaignId, {
                influencerId: selectedInfluencer.userId,
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

            <Modal title="Invite influencer" open={inviteModalOpen} onCancel={closeInviteModal} footer={null} destroyOnClose width={640}>
                {/* Search section */}
                <div style={{ padding: ‘12px 0 16px’ }}>
                    <div style={{ display: ‘flex’, alignItems: ‘center’, gap: 8, marginBottom: 12 }}>
                        <SearchOutlined style={{ color: BRAND_PORTAL_PRIMARY }} />
                        <Text style={{ fontWeight: 600, fontSize: 14 }}>Find influencer</Text>
                    </div>
                    <Form form={searchForm} layout="vertical" onFinish={onSearchInfluencers}>
                        <Row gutter={[12, 0]}>
                            <Col xs={24} sm={12}>
                                <Form.Item name="niche" label="Niche" style={{ marginBottom: 10 }}>
                                    <Input placeholder="e.g. Fashion" />
                                </Form.Item>
                            </Col>
                            <Col xs={24} sm={12}>
                                <Form.Item name="location" label="Location" style={{ marginBottom: 10 }}>
                                    <Input placeholder="e.g. New York" />
                                </Form.Item>
                            </Col>
                            <Col xs={24} sm={8}>
                                <Form.Item name="minFollowers" label="Min followers" style={{ marginBottom: 10 }}>
                                    <InputNumber min={0} placeholder="0" style={{ width: ‘100%’ }} />
                                </Form.Item>
                            </Col>
                            <Col xs={24} sm={8}>
                                <Form.Item name="maxFollowers" label="Max followers" style={{ marginBottom: 10 }}>
                                    <InputNumber min={0} placeholder="Any" style={{ width: ‘100%’ }} />
                                </Form.Item>
                            </Col>
                            <Col xs={24} sm={8}>
                                <Form.Item name="minEngagementRate" label="Min eng. %" style={{ marginBottom: 10 }}>
                                    <InputNumber min={0} max={100} step={0.1} placeholder="0" style={{ width: ‘100%’ }} />
                                </Form.Item>
                            </Col>
                        </Row>
                        <Button type="default" htmlType="submit" icon={<SearchOutlined />} loading={searchLoading} style={{ borderRadius: 8, borderColor: BRAND_PORTAL_PRIMARY, color: BRAND_PORTAL_PRIMARY }}>
                            Search
                        </Button>
                    </Form>
                </div>

                {/* Search results */}
                {searchResults.length > 0 && (
                    <>
                        <Text type="secondary" style={{ fontSize: 13, display: ‘block’, marginBottom: 10 }}>
                            {searchResults.length} result{searchResults.length !== 1 ? ‘s’ : ‘’} — select an influencer
                        </Text>
                        <div style={{ maxHeight: 260, overflowY: ‘auto’, marginBottom: 16 }}>
                            <Row gutter={[10, 10]}>
                                {searchResults.map((inf) => {
                                    const engColor = inf.engagementRate != null
                                        ? Number(inf.engagementRate) >= 5 ? ‘#52c41a’ : Number(inf.engagementRate) >= 2 ? ‘#faad14’ : ‘#ff4d4f’
                                        : ‘#888’
                                    const isSelected = selectedInfluencer?.userId === inf.userId
                                    return (
                                        <Col key={inf.id} xs={24} sm={12}>
                                            <Card
                                                size="small"
                                                style={{ borderRadius: 10, borderColor: isSelected ? BRAND_PORTAL_PRIMARY : undefined, boxShadow: isSelected ? `0 0 0 1px ${BRAND_PORTAL_PRIMARY}60` : undefined }}
                                                hoverable
                                                onClick={() => setSelectedInfluencer(isSelected ? null : inf)}
                                            >
                                                <div style={{ display: ‘flex’, alignItems: ‘center’, gap: 10 }}>
                                                    <Avatar size={36} icon={<UserOutlined />} src={inf.profilePictureUrl} style={{ backgroundColor: BRAND_PORTAL_PRIMARY, color: ‘#000’, flexShrink: 0 }} />
                                                    <div style={{ flex: 1, minWidth: 0 }}>
                                                        <Text strong style={{ fontSize: 13 }}>{inf.name}</Text>
                                                        <div><Text type="secondary" style={{ fontSize: 11 }}>{inf.niche} · {inf.location}</Text></div>
                                                        <div style={{ display: ‘flex’, gap: 6, marginTop: 4, flexWrap: ‘wrap’ }}>
                                                            {inf.followerCount != null && (
                                                                <span style={{ fontSize: 11, color: ‘#888’ }}>
                                                                    {inf.followerCount >= 1000 ? `${(inf.followerCount / 1000).toFixed(1)}K` : inf.followerCount} followers
                                                                </span>
                                                            )}
                                                            {inf.engagementRate != null && (
                                                                <span style={{ fontSize: 11, color: engColor }}>{Number(inf.engagementRate).toFixed(1)}% eng.</span>
                                                            )}
                                                        </div>
                                                        {inf.totalRatings != null && inf.totalRatings > 0 && (
                                                            <Rate disabled allowHalf value={inf.averageRating ?? 0} style={{ fontSize: 10, marginTop: 2 }} />
                                                        )}
                                                    </div>
                                                    {isSelected && <CheckCircleOutlined style={{ color: BRAND_PORTAL_PRIMARY, fontSize: 18 }} />}
                                                </div>
                                            </Card>
                                        </Col>
                                    )
                                })}
                            </Row>
                        </div>
                    </>
                )}
                {searchDone && searchResults.length === 0 && !searchLoading && (
                    <Text type="secondary" style={{ display: ‘block’, textAlign: ‘center’, padding: ‘12px 0’ }}>No influencers match these filters.</Text>
                )}

                {/* Invitation form */}
                {selectedInfluencer && (
                    <>
                        <div style={{ padding: ‘8px 12px’, borderRadius: 8, background: ‘#f6ffed’, border: ‘1px solid #b7eb8f’, marginBottom: 12 }}>
                            <Text>Inviting <strong>{selectedInfluencer.name}</strong> (ID: {selectedInfluencer.userId})</Text>
                        </div>
                        <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit}>
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
                                <InputNumber min={0} step={100} style={{ width: ‘100%’ }} placeholder="Amount" addonBefore="$" />
                            </Form.Item>
                            <Form.Item name="platform" label="Platform">
                                <Select placeholder="Select platform" allowClear options={PREFERRED_CONTENT_OPTIONS} />
                            </Form.Item>
                            <Form.Item name="expiresInDays" label="Invitation valid for (days)" initialValue={14}>
                                <InputNumber min={1} max={90} style={{ width: ‘100%’ }} />
                            </Form.Item>
                            <Form.Item>
                                <Button type="primary" htmlType="submit" loading={inviteSubmitting} style={{ color: ‘#000000’ }}>
                                    Send invitation
                                </Button>
                                <Button style={{ marginLeft: 8 }} onClick={closeInviteModal}>
                                    Cancel
                                </Button>
                            </Form.Item>
                        </Form>
                    </>
                )}
            </Modal>

        </BrandPortalLayout>
    )
}
