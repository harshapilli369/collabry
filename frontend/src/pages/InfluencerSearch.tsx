import { useState } from 'react'
import { Button, Card, Col, Form, Input, InputNumber, Row, Select, Typography, Avatar, Modal, message, Rate } from 'antd'
import { UserOutlined, SearchOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'
import { searchInfluencers, type InfluencerProfileResponse, type InfluencerSearchParams } from '../services/influencerProfileService'
import { getMyCampaigns, type CampaignResponse } from '../services/campaignService'
import { createInvitation, type InvitationRequest } from '../services/invitationService'
import { PREFERRED_CONTENT_OPTIONS } from '../services/campaignService'

const { Title, Text } = Typography

const primaryColor = BRAND_PORTAL_PRIMARY

export const InfluencerSearch = () => {
    const navigate = useNavigate()
    const [form] = Form.useForm<InfluencerSearchParams>()
    const [inviteForm] = Form.useForm<InvitationRequest & { campaignId?: number; expiresInDays?: number }>()
    const [loading, setLoading] = useState(false)
    const [results, setResults] = useState<InfluencerProfileResponse[]>([])
    const [inviteModalOpen, setInviteModalOpen] = useState(false)
    const [selectedInfluencer, setSelectedInfluencer] = useState<InfluencerProfileResponse | null>(null)
    const [campaigns, setCampaigns] = useState<CampaignResponse[]>([])
    const [inviteSubmitting, setInviteSubmitting] = useState(false)

    const onSearch = async () => {
        const values = form.getFieldsValue()
        setLoading(true)
        try {
            const list = await searchInfluencers({
                niche: values.niche,
                location: values.location,
                minFollowers: values.minFollowers,
                maxFollowers: values.maxFollowers,
                minEngagementRate: values.minEngagementRate,
            })
            setResults(list)
            if (list.length === 0) message.info('No influencers match your filters.')
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Search failed')
            setResults([])
        } finally {
            setLoading(false)
        }
    }

    const openInviteModal = async (influencer: InfluencerProfileResponse) => {
        setSelectedInfluencer(influencer)
        inviteForm.resetFields()
        try {
            const list = await getMyCampaigns()
            setCampaigns(list.filter((c) => c.status === 'DRAFT' || c.status === 'ACTIVE'))
        } catch {
            setCampaigns([])
        }
        setInviteModalOpen(true)
    }

    const closeInviteModal = () => {
        setInviteModalOpen(false)
        setSelectedInfluencer(null)
    }

    const onInviteSubmit = async (values: InvitationRequest & { campaignId?: number; expiresInDays?: number }) => {
        if (!selectedInfluencer) {
            message.error('No influencer selected.')
            return
        }
        const influencerUserId = selectedInfluencer.userId
        if (influencerUserId == null) {
            message.error('Influencer account id is missing. Run search again and retry.')
            return
        }
        const rawCampaignId = values.campaignId
        if (rawCampaignId === undefined || rawCampaignId === null) {
            message.warning('Select a campaign.')
            return
        }
        const campaignId = typeof rawCampaignId === 'number' ? rawCampaignId : Number(rawCampaignId)
        if (!Number.isFinite(campaignId)) {
            message.error('Invalid campaign.')
            return
        }
        setInviteSubmitting(true)
        try {
            await createInvitation(campaignId, {
                influencerId: influencerUserId,
                message: values.message?.trim() || undefined,
                proposedAmount: values.proposedAmount ?? undefined,
                proposedTimeline: values.proposedTimeline?.trim() || undefined,
                proposedDeliverables: values.proposedDeliverables?.trim() || undefined,
                platform: values.platform || undefined,
                expiresInDays: values.expiresInDays ?? 14,
            })
            message.success('Invitation sent')
            closeInviteModal()
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to send invitation')
        } finally {
            setInviteSubmitting(false)
        }
    }

    return (
        <BrandPortalLayout activeMenuKey="influencers">
                        <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => navigate('/brand/dashboard')} style={{ color: primaryColor, paddingLeft: 0, marginBottom: 16 }}>
                            Back to Dashboard
                        </Button>
                        <Title level={1} style={{ color: primaryColor, margin: '0 0 8px', fontSize: '2rem' }}>Find influencers</Title>
                        <Text style={{ color: '#aaa', display: 'block', marginBottom: 24 }}>
                            Search and filter by niche, followers, engagement rate, and location. Send collaboration invitations with clear campaign details.
                        </Text>

                        <Card style={{ background: '#0d0d0d', borderRadius: 16, border: '1px solid #1a1a1a', marginBottom: 24 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 16 }}>
                                <SearchOutlined style={{ color: primaryColor }} />
                                <Text style={{ color: '#fff', fontWeight: 600, fontSize: 15 }}>Filters</Text>
                            </div>
                            <Form form={form} layout="vertical" onFinish={onSearch}>
                                <Row gutter={16}>
                                    <Col span={6}>
                                        <Form.Item name="niche" label="Niche">
                                            <Input placeholder="e.g. Fashion" />
                                        </Form.Item>
                                    </Col>
                                    <Col span={6}>
                                        <Form.Item name="location" label="Location">
                                            <Input placeholder="e.g. New York" />
                                        </Form.Item>
                                    </Col>
                                    <Col span={4}>
                                        <Form.Item name="minFollowers" label="Min followers">
                                            <InputNumber min={0} placeholder="0" style={{ width: '100%' }} />
                                        </Form.Item>
                                    </Col>
                                    <Col span={4}>
                                        <Form.Item name="maxFollowers" label="Max followers">
                                            <InputNumber min={0} placeholder="Any" style={{ width: '100%' }} />
                                        </Form.Item>
                                    </Col>
                                    <Col span={4}>
                                        <Form.Item name="minEngagementRate" label="Min engagement %">
                                            <InputNumber min={0} max={100} step={0.1} placeholder="0" style={{ width: '100%' }} />
                                        </Form.Item>
                                    </Col>
                                </Row>
                                <Form.Item style={{ marginBottom: 0 }}>
                                    <Button type="primary" htmlType="submit" icon={<SearchOutlined />} loading={loading} style={{ borderRadius: 10 }}>Search</Button>
                                </Form.Item>
                            </Form>
                        </Card>

                        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
                            <Text style={{ color: '#888', fontWeight: 600, fontSize: 15 }}>Results</Text>
                            {results.length > 0 && <Text style={{ color: '#555', fontSize: 13 }}>{results.length} influencer{results.length !== 1 ? 's' : ''} found</Text>}
                        </div>
                        {results.length === 0 && !loading && (
                            <div style={{ textAlign: 'center', padding: '40px 0' }}>
                                <SearchOutlined style={{ fontSize: 40, opacity: 0.2, color: primaryColor, marginBottom: 16, display: 'block' }} />
                                <Text type="secondary" style={{ fontSize: 15 }}>Use filters above and click Search to find influencers.</Text>
                            </div>
                        )}
                        <Row gutter={[16, 16]}>
                            {results.map((inf) => {
                                const engColor = inf.engagementRate != null ? (Number(inf.engagementRate) >= 5 ? '#52c41a' : Number(inf.engagementRate) >= 2 ? '#faad14' : '#ff4d4f') : '#888'
                                return (
                                    <Col key={inf.id} xs={24} sm={12} lg={8}>
                                        <Card
                                            size="small"
                                            className="brand-campaign-card"
                                            style={{ background: '#141414', borderRadius: 12, borderColor: '#1a1a1a' }}
                                        >
                                            <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                                <Avatar size={48} icon={<UserOutlined />} src={inf.profilePictureUrl} style={{ backgroundColor: primaryColor, color: '#000', flexShrink: 0 }} />
                                                <div style={{ flex: 1, minWidth: 0 }}>
                                                    <Text strong style={{ color: '#fff', fontSize: 15 }}>{inf.name}</Text>
                                                    <div style={{ marginTop: 4 }}>
                                                        <Text type="secondary" style={{ fontSize: 12 }}>{inf.niche} &middot; {inf.location}</Text>
                                                    </div>
                                                    <div style={{ marginTop: 8, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                                                        {inf.followerCount != null && (
                                                            <span style={{ fontSize: 11, color: '#aaa', background: '#1a1a1a', padding: '2px 8px', borderRadius: 6 }}>
                                                                {inf.followerCount >= 1000 ? `${(inf.followerCount / 1000).toFixed(1)}K` : inf.followerCount} followers
                                                            </span>
                                                        )}
                                                        {inf.engagementRate != null && (
                                                            <span style={{ fontSize: 11, color: engColor, background: '#1a1a1a', padding: '2px 8px', borderRadius: 6 }}>
                                                                {Number(inf.engagementRate).toFixed(1)}% eng.
                                                            </span>
                                                        )}
                                                        {inf.rate != null && (
                                                            <span style={{ fontSize: 11, color: '#aaa', background: '#1a1a1a', padding: '2px 8px', borderRadius: 6 }}>
                                                                ${Number(inf.rate).toLocaleString()}
                                                            </span>
                                                        )}
                                                    </div>
                                                    {inf.totalRatings != null && inf.totalRatings > 0 && (
                                                        <div style={{ marginTop: 6, display: 'flex', alignItems: 'center', gap: 6 }}>
                                                            <Rate disabled allowHalf value={inf.averageRating ?? 0} style={{ fontSize: 11, color: '#FFFD82' }} />
                                                            <span style={{ fontSize: 11, color: '#888' }}>({inf.totalRatings})</span>
                                                        </div>
                                                    )}
                                                    <Button type="primary" size="small" style={{ marginTop: 10, borderRadius: 8 }} onClick={() => openInviteModal(inf)}>Invite</Button>
                                                </div>
                                            </div>
                                        </Card>
                                    </Col>
                                )
                            })}
                        </Row>

            <Modal
                title={`Invite ${selectedInfluencer?.name ?? ''}`}
                open={inviteModalOpen}
                onCancel={closeInviteModal}
                footer={null}
                destroyOnClose
                width={560}
                key={selectedInfluencer?.userId ?? 'invite-modal'}
            >
                <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit}>
                    <Form.Item name="campaignId" label="Campaign" rules={[{ required: true, message: 'Select a campaign' }]}>
                        <Select
                            placeholder="Select campaign"
                            options={campaigns.map((c) => ({ value: c.id, label: c.name }))}
                        />
                    </Form.Item>
                    <Form.Item name="message" label="Message to influencer">
                        <Input.TextArea rows={2} placeholder="Personal message" />
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
                        <Button
                            type="primary"
                            htmlType="button"
                            loading={inviteSubmitting}
                            style={{ color: '#000000' }}
                            onClick={() => inviteForm.submit()}
                        >
                            Send invitation
                        </Button>
                        <Button style={{ marginLeft: 8 }} onClick={closeInviteModal}>Cancel</Button>
                    </Form.Item>
                </Form>
            </Modal>
        </BrandPortalLayout>
    )
}
