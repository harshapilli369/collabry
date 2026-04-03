import { useState } from 'react'
import { Form, Input, Button, Typography, Select, message, InputNumber, Card, Alert, Row, Col, Avatar, Rate } from 'antd'
import {
    ArrowLeftOutlined,
    MailOutlined,
    FundProjectionScreenOutlined,
    CheckCircleFilled,
    ThunderboltOutlined,
    SearchOutlined,
    UserOutlined,
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
    CAMPAIGNS_URL,
} from '../services/campaignService'
import { createInvitation } from '../services/invitationService'
import { searchInfluencers, type InfluencerProfileResponse, type InfluencerSearchParams } from '../services/influencerProfileService'

const { Title, Text } = Typography
const { TextArea } = Input

const PRIMARY = BRAND_PORTAL_PRIMARY

export const CreateCampaign = () => {
    const [form] = Form.useForm<CampaignRequest & { preferredContentTypesList?: string[] }>()
    const [inviteForm] = Form.useForm<{ message?: string }>()
    const [searchForm] = Form.useForm<InfluencerSearchParams>()
    const [loading, setLoading] = useState(false)
    const [createdCampaign, setCreatedCampaign] = useState<CampaignResponse | null>(null)
    const [inviteSubmitting, setInviteSubmitting] = useState(false)
    const [submitError, setSubmitError] = useState<string | null>(null)
    const [searchLoading, setSearchLoading] = useState(false)
    const [searchResults, setSearchResults] = useState<InfluencerProfileResponse[]>([])
    const [inviteSearchDone, setInviteSearchDone] = useState(false)
    const [selectedInfluencer, setSelectedInfluencer] = useState<InfluencerProfileResponse | null>(null)
    const [aiDescLoading, setAiDescLoading] = useState(false)
    const navigate = useNavigate()

    const today = new Date().toISOString().split('T')[0]

    const generateDescription = async () => {
        const name = form.getFieldValue('name')
        if (!name?.trim()) {
            message.warning('Please enter a campaign name first')
            return
        }
        setAiDescLoading(true)
        try {
            const token = localStorage.getItem('token')
            const res = await fetch(`${CAMPAIGNS_URL}/generate-description`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
                body: JSON.stringify({
                    name: name.trim(),
                    goal: form.getFieldValue('campaignGoal') || '',
                    budget: form.getFieldValue('budgetRange') || '',
                }),
            })
            const data = await res.json()
            if (res.ok && data.description) {
                form.setFieldsValue({ description: data.description })
                message.success('AI description generated! You can edit it before saving.')
            } else {
                message.error(data.message || 'Failed to generate description')
            }
        } catch {
            message.error('Failed to connect to AI service')
        } finally {
            setAiDescLoading(false)
        }
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
            setInviteSearchDone(true)
            setSearchResults(list)
            if (list.length === 0) message.info('No influencers match your filters.')
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Search failed')
            setSearchResults([])
            setInviteSearchDone(true)
        } finally {
            setSearchLoading(false)
        }
    }

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
                searchForm.resetFields()
                setSearchResults([])
                setInviteSearchDone(false)
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
        if (!createdCampaign) return
        if (!selectedInfluencer) {
            message.warning('Search for influencers, then select one to invite.')
            return
        }
        setInviteSubmitting(true)
        try {
            await createInvitation(createdCampaign.id, {
                influencerId: selectedInfluencer.userId,
                message: values.message?.trim() || undefined,
            })
            message.success('Invitation sent to influencer')
            inviteForm.resetFields()
            setSelectedInfluencer(null)
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

            <div style={{ maxWidth: createdCampaign ? 820 : 600, margin: '0 auto' }}>
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
                            <Form.Item
                                name="description"
                                label={
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                                        <span>Description</span>
                                        <Button
                                            type="link"
                                            icon={<ThunderboltOutlined />}
                                            loading={aiDescLoading}
                                            onClick={generateDescription}
                                            style={{ color: PRIMARY, padding: 0, fontSize: 13 }}
                                        >
                                            Generate with AI
                                        </Button>
                                    </div>
                                }
                            >
                                <TextArea rows={4} placeholder="Describe the campaign, deliverables, and key messages — or click 'Generate with AI'" />
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
                                <Input type="date" min={today} />
                            </Form.Item>
                            <Form.Item
                                name="endDate"
                                label="End date"
                                dependencies={['startDate']}
                                rules={[
                                    ({ getFieldValue }) => ({
                                        validator(_, value) {
                                            if (!value) return Promise.resolve()
                                            const start = getFieldValue('startDate')
                                            if (start && value < start) {
                                                return Promise.reject(new Error('End date must be after the start date'))
                                            }
                                            return Promise.resolve()
                                        },
                                    }),
                                ]}
                            >
                                <Input type="date" min={today} />
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
                        <div style={{ textAlign: 'center', marginBottom: 24 }}>
                            <CheckCircleFilled style={{ fontSize: 48, color: PRIMARY, marginBottom: 12 }} />
                            <Title level={3} style={{ color: PRIMARY, margin: 0 }}>
                                "{createdCampaign.name}" created!
                            </Title>
                            <Text style={{ color: '#888', display: 'block', marginTop: 8 }}>
                                Invite an influencer now, or do it later from the Dashboard.
                            </Text>
                        </div>

                        <Text style={{ color: '#aaa', display: 'block', marginBottom: 16 }}>
                            Search for influencers below, select one, then send the invitation for this campaign.
                        </Text>

                        <div
                            style={{
                                padding: '16px 20px',
                                background: '#141414',
                                borderRadius: 12,
                                border: '1px solid #1a1a1a',
                                marginBottom: 20,
                            }}
                        >
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                                <SearchOutlined style={{ color: PRIMARY }} />
                                <Text style={{ color: '#fff', fontWeight: 600, fontSize: 14 }}>Find influencer</Text>
                            </div>
                            <Form form={searchForm} layout="vertical" onFinish={onSearchInfluencers}>
                                <Row gutter={[12, 0]}>
                                    <Col xs={24} sm={12}>
                                        <Form.Item name="niche" label="Niche" style={{ marginBottom: 12 }}>
                                            <Input placeholder="e.g. Fashion" />
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} sm={12}>
                                        <Form.Item name="location" label="Location" style={{ marginBottom: 12 }}>
                                            <Input placeholder="e.g. New York" />
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} sm={8}>
                                        <Form.Item name="minFollowers" label="Min followers" style={{ marginBottom: 12 }}>
                                            <InputNumber min={0} placeholder="0" style={{ width: '100%' }} />
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} sm={8}>
                                        <Form.Item name="maxFollowers" label="Max followers" style={{ marginBottom: 12 }}>
                                            <InputNumber min={0} placeholder="Any" style={{ width: '100%' }} />
                                        </Form.Item>
                                    </Col>
                                    <Col xs={24} sm={8}>
                                        <Form.Item name="minEngagementRate" label="Min engagement %" style={{ marginBottom: 12 }}>
                                            <InputNumber min={0} max={100} step={0.1} placeholder="0" style={{ width: '100%' }} />
                                        </Form.Item>
                                    </Col>
                                </Row>
                                <Form.Item style={{ marginBottom: 0 }}>
                                    <Button
                                        type="default"
                                        htmlType="submit"
                                        icon={<SearchOutlined />}
                                        loading={searchLoading}
                                        style={{ borderRadius: 10, borderColor: PRIMARY, color: PRIMARY }}
                                    >
                                        Search
                                    </Button>
                                </Form.Item>
                            </Form>
                        </div>

                        {searchResults.length > 0 && (
                            <>
                                <Text style={{ color: '#888', fontWeight: 600, fontSize: 13, display: 'block', marginBottom: 12 }}>
                                    {searchResults.length} result{searchResults.length !== 1 ? 's' : ''} — select an influencer
                                </Text>
                                <Row gutter={[12, 12]} style={{ marginBottom: 20 }}>
                                    {searchResults.map((inf) => {
                                        const engColor =
                                            inf.engagementRate != null
                                                ? Number(inf.engagementRate) >= 5
                                                    ? '#52c41a'
                                                    : Number(inf.engagementRate) >= 2
                                                      ? '#faad14'
                                                      : '#ff4d4f'
                                                : '#888'
                                        const isSelected = selectedInfluencer?.userId === inf.userId
                                        return (
                                            <Col key={inf.id} xs={24} sm={12}>
                                                <Card
                                                    size="small"
                                                    style={{
                                                        background: '#141414',
                                                        borderRadius: 12,
                                                        borderColor: isSelected ? PRIMARY : '#1a1a1a',
                                                        boxShadow: isSelected ? `0 0 0 1px ${PRIMARY}60` : undefined,
                                                    }}
                                                >
                                                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                                        <Avatar
                                                            size={44}
                                                            icon={<UserOutlined />}
                                                            src={inf.profilePictureUrl}
                                                            style={{ backgroundColor: PRIMARY, color: '#000', flexShrink: 0 }}
                                                        />
                                                        <div style={{ flex: 1, minWidth: 0 }}>
                                                            <Text strong style={{ color: '#fff', fontSize: 14 }}>
                                                                {inf.name}
                                                            </Text>
                                                            <div style={{ marginTop: 4 }}>
                                                                <Text type="secondary" style={{ fontSize: 12 }}>
                                                                    {inf.niche} · {inf.location}
                                                                </Text>
                                                            </div>
                                                            <div style={{ marginTop: 8, display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                                                                {inf.followerCount != null && (
                                                                    <span
                                                                        style={{
                                                                            fontSize: 11,
                                                                            color: '#aaa',
                                                                            background: '#1a1a1a',
                                                                            padding: '2px 8px',
                                                                            borderRadius: 6,
                                                                        }}
                                                                    >
                                                                        {inf.followerCount >= 1000
                                                                            ? `${(inf.followerCount / 1000).toFixed(1)}K`
                                                                            : inf.followerCount}{' '}
                                                                        followers
                                                                    </span>
                                                                )}
                                                                {inf.engagementRate != null && (
                                                                    <span
                                                                        style={{
                                                                            fontSize: 11,
                                                                            color: engColor,
                                                                            background: '#1a1a1a',
                                                                            padding: '2px 8px',
                                                                            borderRadius: 6,
                                                                        }}
                                                                    >
                                                                        {Number(inf.engagementRate).toFixed(1)}% eng.
                                                                    </span>
                                                                )}
                                                            </div>
                                                            {inf.totalRatings != null && inf.totalRatings > 0 && (
                                                                <div style={{ marginTop: 6, display: 'flex', alignItems: 'center', gap: 6 }}>
                                                                    <Rate disabled allowHalf value={inf.averageRating ?? 0} style={{ fontSize: 11, color: '#FFFD82' }} />
                                                                    <span style={{ fontSize: 11, color: '#888' }}>({inf.totalRatings})</span>
                                                                </div>
                                                            )}
                                                            <Button
                                                                type={isSelected ? 'default' : 'primary'}
                                                                size="small"
                                                                style={{ marginTop: 10, borderRadius: 8, ...(isSelected ? { borderColor: PRIMARY, color: PRIMARY } : { color: '#000' }) }}
                                                                onClick={() => setSelectedInfluencer(isSelected ? null : inf)}
                                                            >
                                                                {isSelected ? 'Deselect' : 'Select'}
                                                            </Button>
                                                        </div>
                                                    </div>
                                                </Card>
                                            </Col>
                                        )
                                    })}
                                </Row>
                            </>
                        )}

                        {searchResults.length === 0 && !searchLoading && !inviteSearchDone && (
                            <div style={{ textAlign: 'center', padding: '16px 0 8px' }}>
                                <Text type="secondary" style={{ fontSize: 13 }}>
                                    Set filters and click Search to see influencers.
                                </Text>
                            </div>
                        )}
                        {inviteSearchDone && searchResults.length === 0 && !searchLoading && (
                            <div style={{ textAlign: 'center', padding: '16px 0 8px' }}>
                                <Text type="secondary" style={{ fontSize: 13 }}>
                                    No influencers match these filters. Try broadening your search.
                                </Text>
                            </div>
                        )}

                        {selectedInfluencer && (
                            <Alert
                                type="info"
                                showIcon
                                message={
                                    <span style={{ color: '#fff' }}>
                                        Inviting <strong style={{ color: PRIMARY }}>{selectedInfluencer.name}</strong> to &quot;{createdCampaign.name}&quot;
                                    </span>
                                }
                                style={{ marginBottom: 16, borderRadius: 10, background: '#1a1a1a', borderColor: '#333' }}
                            />
                        )}

                        <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit} size="large">
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
                                    style={{ color: '#000', fontWeight: 600, borderRadius: 10, flex: 1, minWidth: 200 }}
                                >
                                    Send Invitation
                                </Button>
                                <Button onClick={() => navigate('/brand/dashboard', { replace: true })} style={{ borderRadius: 10 }}>
                                    Skip to Dashboard
                                </Button>
                            </div>
                        </Form>
                    </Card>
                )}
            </div>
        </BrandPortalLayout>
    )
}
