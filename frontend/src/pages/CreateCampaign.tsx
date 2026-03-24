import { useState } from 'react'
import { Form, Input, Button, Typography, Select, message, InputNumber, Card, Alert, Drawer, Avatar, Empty, Tag } from 'antd'
import {
    ArrowLeftOutlined,
    MailOutlined,
    UserOutlined,
    SearchOutlined,
    CloseCircleOutlined,
    FundProjectionScreenOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'
import {
    createCampaign,
    BUDGET_RANGE_OPTIONS,
    CAMPAIGN_GOAL_OPTIONS,
    PREFERRED_CONTENT_OPTIONS,
    type CampaignRequest,
    type CampaignResponse,
} from '../services/campaignService'
import { createInvitation } from '../services/invitationService'
import { userService, type InfluencerSearchResult } from '../services/userService'

const { Title, Text } = Typography
const { TextArea } = Input

const PRIMARY = BRAND_PORTAL_PRIMARY

export const CreateCampaign = () => {
    const [form] = Form.useForm<CampaignRequest & { preferredContentTypesList?: string[] }>()
    const [inviteForm] = Form.useForm<{ message?: string }>()
    const [loading, setLoading] = useState(false)
    const [createdCampaign, setCreatedCampaign] = useState<CampaignResponse | null>(null)
    const [inviteSubmitting, setInviteSubmitting] = useState(false)
    const [submitError, setSubmitError] = useState<string | null>(null)
    const [drawerOpen, setDrawerOpen] = useState(false)
    const [influencerList, setInfluencerList] = useState<InfluencerSearchResult[]>([])
    const [influencerListLoading, setInfluencerListLoading] = useState(false)
    const [influencerSearch, setInfluencerSearch] = useState('')
    const [selectedInfluencer, setSelectedInfluencer] = useState<InfluencerSearchResult | null>(null)
    const navigate = useNavigate()

    const openDrawer = () => {
        setDrawerOpen(true)
        setInfluencerSearch('')
        if (influencerList.length === 0) {
            setInfluencerListLoading(true)
            userService
                .listInfluencers()
                .then(setInfluencerList)
                .catch(() => {
                    message.error('Failed to load influencers')
                    setInfluencerList([])
                })
                .finally(() => setInfluencerListLoading(false))
        }
    }

    const handleSelectInfluencer = (inf: InfluencerSearchResult) => {
        setSelectedInfluencer(inf)
        setDrawerOpen(false)
    }

    const clearSelectedInfluencer = () => {
        setSelectedInfluencer(null)
    }

    const filteredInfluencers = influencerList.filter((inf) => {
        const q = influencerSearch.trim().toLowerCase()
        if (!q) return true
        return (
            inf.displayName?.toLowerCase().includes(q) ||
            inf.email?.toLowerCase().includes(q) ||
            String(inf.id).includes(q)
        )
    })

    const onFinish = async (values: CampaignRequest & { preferredContentTypesList?: string[] }) => {
        setLoading(true)
        try {
            const payload: CampaignRequest = {
                name: values.name,
                description: values.description,
                budgetRange: values.budgetRange,
                campaignGoal: values.campaignGoal,
                preferredContentTypes: values.preferredContentTypesList?.length
                    ? values.preferredContentTypesList.join(',')
                    : undefined,
                startDate: values.startDate || undefined,
                endDate: values.endDate || undefined,
                numberOfInfluencers: values.numberOfInfluencers,
            }
            setSubmitError(null)
            const campaign = await createCampaign(payload)
            if (campaign?.id != null) {
                setCreatedCampaign(campaign)
                message.success('Campaign created successfully')
                inviteForm.resetFields()
                setSelectedInfluencer(null)
            } else {
                const err = 'Invalid response from server. Please try again.'
                setSubmitError(err)
                message.error(err)
            }
        } catch (e) {
            const msg = e instanceof Error ? e.message : 'Failed to create campaign'
            setSubmitError(msg)
            message.error(msg)
        } finally {
            setLoading(false)
        }
    }

    const onInviteSubmit = async (values: { message?: string }) => {
        if (!createdCampaign || !selectedInfluencer) return
        setInviteSubmitting(true)
        try {
            await createInvitation(createdCampaign.id, {
                influencerId: selectedInfluencer.id,
                message: values.message?.trim() || undefined,
            })
            message.success(`Invitation sent to ${selectedInfluencer.displayName || 'influencer'}`)
            inviteForm.resetFields()
            navigate('/brand/collaborations', { replace: true })
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to send invitation')
        } finally {
            setInviteSubmitting(false)
        }
    }

    return (
        <BrandPortalLayout activeMenuKey="campaign-create">
            <Button
                type="link"
                icon={<ArrowLeftOutlined />}
                onClick={() => navigate('/brand/dashboard')}
                style={{ color: PRIMARY, paddingLeft: 0, marginBottom: 16 }}
            >
                Back to Dashboard
            </Button>

            <div style={{ maxWidth: 600, margin: '0 auto' }}>
                {/* Header */}
                <div style={{ textAlign: 'center', marginBottom: 36 }}>
                    <div
                        style={{
                            width: 56,
                            height: 56,
                            borderRadius: 16,
                            background: `linear-gradient(135deg, ${PRIMARY}, #e6d800)`,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            margin: '0 auto 16px',
                        }}
                    >
                        <FundProjectionScreenOutlined style={{ fontSize: 24, color: '#000' }} />
                    </div>
                    <Title level={2} style={{ margin: 0, color: '#fff' }}>Create Campaign</Title>
                    <Text style={{ color: '#666', fontSize: 14, marginTop: 8, display: 'block' }}>
                        Set up a new influencer campaign with budget, goals, and content preferences.
                    </Text>
                </div>

                {!createdCampaign ? (
                    <Form form={form} layout="vertical" onFinish={onFinish} size="large">
                        {submitError && (
                            <Alert type="error" message={submitError} showIcon closable onClose={() => setSubmitError(null)} style={{ marginBottom: 16, borderRadius: 10 }} />
                        )}

                        {/* Required */}
                        <div style={{ padding: '20px 24px', background: '#0d0d0d', borderRadius: 12, border: '1px solid #1a1a1a', marginBottom: 16 }}>
                            <Text style={{ color: PRIMARY, fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 16 }}>
                                Campaign Basics
                            </Text>
                            <Form.Item name="name" label="Campaign name" rules={[{ required: true, message: 'Campaign name is required' }]}>
                                <Input placeholder="e.g. Spring Collection Launch 2025" />
                            </Form.Item>
                            <Form.Item name="budgetRange" label="Budget range" rules={[{ required: true, message: 'Budget range is required' }]}>
                                <Select placeholder="Select campaign budget range" options={BUDGET_RANGE_OPTIONS} />
                            </Form.Item>
                        </div>

                        {/* Optional */}
                        <div style={{ padding: '20px 24px', background: '#0d0d0d', borderRadius: 12, border: '1px solid #1a1a1a', marginBottom: 16 }}>
                            <Text style={{ color: '#666', fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 16 }}>
                                Campaign Details
                            </Text>
                            <Form.Item name="description" label="Description">
                                <TextArea rows={4} placeholder="Describe the campaign, deliverables, and key messages" />
                            </Form.Item>
                            <Form.Item name="campaignGoal" label="Campaign goal">
                                <Select placeholder="Select primary goal" allowClear options={CAMPAIGN_GOAL_OPTIONS} />
                            </Form.Item>
                            <Form.Item name="preferredContentTypesList" label="Preferred content types">
                                <Select mode="multiple" placeholder="Select content types" allowClear options={PREFERRED_CONTENT_OPTIONS} />
                            </Form.Item>
                        </div>

                        {/* Timeline */}
                        <div style={{ padding: '20px 24px', background: '#0d0d0d', borderRadius: 12, border: '1px solid #1a1a1a', marginBottom: 24 }}>
                            <Text style={{ color: '#666', fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 16 }}>
                                Timeline & Scale
                            </Text>
                            <Form.Item name="startDate" label="Start date">
                                <Input type="date" />
                            </Form.Item>
                            <Form.Item name="endDate" label="End date">
                                <Input type="date" />
                            </Form.Item>
                            <Form.Item name="numberOfInfluencers" label="Number of influencers">
                                <InputNumber min={1} placeholder="e.g. 5" style={{ width: '100%' }} />
                            </Form.Item>
                        </div>

                        <Button type="primary" htmlType="submit" loading={loading} size="large" block style={{ fontWeight: 600, color: '#000', borderRadius: 10 }}>
                            Create Campaign
                        </Button>
                    </Form>
                ) : (
                    <Card
                        style={{
                            borderRadius: 16,
                            background: '#0d0d0d',
                            border: `1px solid ${PRIMARY}30`,
                            boxShadow: `0 4px 20px ${PRIMARY}08`,
                        }}
                    >
                        <Title level={5} style={{ color: PRIMARY, marginTop: 0 }}>
                            Campaign &quot;{createdCampaign.name}&quot; created
                        </Title>
                        <Text style={{ color: '#aaa', display: 'block', marginBottom: 24 }}>
                            Search for an influencer to invite, or skip and invite later from the Dashboard.
                        </Text>

                        <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit} size="large">
                            {/* Influencer selector */}
                            <Form.Item label="Invite influencer" required style={{ marginBottom: 16 }}>
                                {selectedInfluencer ? (
                                    <Card
                                        size="small"
                                        style={{ borderColor: PRIMARY, borderWidth: 1.5 }}
                                        styles={{ body: { padding: 12 } }}
                                    >
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                                            <Avatar icon={<UserOutlined />} style={{ background: '#333' }} />
                                            <div style={{ flex: 1, minWidth: 0 }}>
                                                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                                    <Text strong>{selectedInfluencer.displayName || 'Influencer'}</Text>
                                                    <Tag color="gold" style={{ fontSize: 11 }}>Selected</Tag>
                                                </div>
                                                <Text type="secondary" style={{ fontSize: 12 }}>{selectedInfluencer.email}</Text>
                                            </div>
                                            <div style={{ display: 'flex', gap: 8, flexShrink: 0 }}>
                                                <Button
                                                    size="small"
                                                    icon={<SearchOutlined />}
                                                    onClick={openDrawer}
                                                >
                                                    Change
                                                </Button>
                                                <Button
                                                    size="small"
                                                    danger
                                                    icon={<CloseCircleOutlined />}
                                                    onClick={clearSelectedInfluencer}
                                                />
                                            </div>
                                        </div>
                                    </Card>
                                ) : (
                                    <Button
                                        icon={<SearchOutlined />}
                                        onClick={openDrawer}
                                        style={{ width: '100%', height: 48, borderStyle: 'dashed', color: '#aaa' }}
                                    >
                                        Search and select an influencer
                                    </Button>
                                )}
                                {!selectedInfluencer && (
                                    <Text type="secondary" style={{ fontSize: 12, display: 'block', marginTop: 4 }}>
                                        Required to send an invitation
                                    </Text>
                                )}
                            </Form.Item>

                            <Form.Item name="message" label="Message (optional)">
                                <TextArea rows={3} placeholder="Personal message to the influencer" />
                            </Form.Item>

                            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
                                <Button
                                    type="primary"
                                    htmlType="submit"
                                    loading={inviteSubmitting}
                                    disabled={!selectedInfluencer}
                                    icon={<MailOutlined />}
                                    style={{ color: '#000', fontWeight: 600, borderRadius: 10, flex: 1, minWidth: 140 }}
                                >
                                    Send invitation
                                </Button>
                                <Button onClick={() => navigate('/brand/dashboard', { replace: true })} style={{ borderRadius: 10 }}>
                                    Skip to Dashboard
                                </Button>
                            </div>
                        </Form>
                    </Card>
                )}
            </div>

            <Drawer
                title={
                    <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                        <SearchOutlined />
                        Search users
                    </span>
                }
                placement="right"
                open={drawerOpen}
                onClose={() => setDrawerOpen(false)}
                width={440}
            >
                <Text type="secondary" style={{ display: 'block', marginBottom: 12 }}>
                    Click a card to select the influencer and invite them to your campaign.
                </Text>
                <Input
                    value={influencerSearch}
                    onChange={(e) => setInfluencerSearch(e.target.value)}
                    prefix={<SearchOutlined style={{ color: '#666' }} />}
                    placeholder="Search by name, email, or ID"
                    allowClear
                    style={{ marginBottom: 16 }}
                />
                {influencerListLoading ? (
                    <Text type="secondary">Loading influencers...</Text>
                ) : filteredInfluencers.length === 0 ? (
                    <Empty description={influencerSearch ? `No results for "${influencerSearch}"` : 'No influencers found'} />
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                        {filteredInfluencers.map((inf) => {
                            const isSelected = selectedInfluencer?.id === inf.id
                            return (
                                <Card
                                    key={inf.id}
                                    hoverable
                                    size="small"
                                    onClick={() => handleSelectInfluencer(inf)}
                                    style={{
                                        borderColor: isSelected ? PRIMARY : '#2a2a2a',
                                        borderWidth: isSelected ? 1.5 : 1,
                                        cursor: 'pointer',
                                    }}
                                    styles={{ body: { padding: 12 } }}
                                >
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                                        <Avatar
                                            icon={<UserOutlined />}
                                            style={{ background: isSelected ? PRIMARY : '#333', color: isSelected ? '#000' : '#fff', flexShrink: 0 }}
                                        />
                                        <div style={{ flex: 1, minWidth: 0 }}>
                                            <div>
                                                <Text strong ellipsis style={{ maxWidth: 240 }}>
                                                    {inf.displayName || 'Influencer'}
                                                </Text>
                                            </div>
                                            <Text type="secondary" style={{ fontSize: 12 }} ellipsis>
                                                {inf.email}
                                            </Text>
                                        </div>
                                        {isSelected && (
                                            <Tag color="gold" style={{ flexShrink: 0, fontSize: 11 }}>Selected</Tag>
                                        )}
                                    </div>
                                </Card>
                            )
                        })}
                    </div>
                )}
            </Drawer>
        </BrandPortalLayout>
    )
}
