import { useState } from 'react'
import { Button, Card, Col, Form, Input, InputNumber, Row, Select, Typography, Avatar, Modal, message, Switch, Tag, Space } from 'antd'
import { UserOutlined, SearchOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'
import { searchInfluencers, type InfluencerProfileResponse, type InfluencerSearchParams } from '../services/influencerProfileService'
import { getMyCampaigns, type CampaignResponse } from '../services/campaignService'
import { createInvitation, type InvitationRequest } from '../services/invitationService'
import { PREFERRED_CONTENT_OPTIONS } from '../services/campaignService'

const { Title, Text } = Typography

const primaryColor = BRAND_PORTAL_PRIMARY
const cardBg = '#1c1c1c'

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
    const [availableOnlyFilter, setAvailableOnlyFilter] = useState(false)

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
                availableOnly: availableOnlyFilter,
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
        if (!values.campaignId || !selectedInfluencer) return
        setInviteSubmitting(true)
        try {
            await createInvitation(values.campaignId, {
                influencerId: selectedInfluencer.userId,
                message: values.message?.trim() || undefined,
                proposedAmount: values.proposedAmount,
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
                            Search and filter by niche, followers, engagement rate, and location. Results are ranked by relevance to your filters. Send collaboration invitations with clear campaign details.
                        </Text>

                        <Card bordered={false} style={{ background: cardBg, borderRadius: 12, marginBottom: 24 }}>
                            <Title level={5} style={{ color: '#ccc', marginTop: 0 }}>Filters</Title>
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
                                    <Col span={24}>
                                        <div style={{ marginBottom: 8 }}>
                                            <Text type="secondary" style={{ display: 'block', marginBottom: 8 }}>
                                                Availability
                                            </Text>
                                            <Space>
                                                <Switch
                                                    data-testid="influencer-search-available-only"
                                                    checked={availableOnlyFilter}
                                                    onChange={setAvailableOnlyFilter}
                                                />
                                                <Text type="secondary" style={{ fontSize: 13 }}>
                                                    Only show influencers open to new collaborations
                                                </Text>
                                            </Space>
                                        </div>
                                    </Col>
                                </Row>
                                <Form.Item>
                                    <Button type="primary" htmlType="submit" icon={<SearchOutlined />} loading={loading} style={{ color: '#000000' }}>Search</Button>
                                </Form.Item>
                            </Form>
                        </Card>

                        <Title level={5} style={{ color: '#ccc', marginBottom: 12 }}>Results</Title>
                        {results.length === 0 && !loading && (
                            <Text type="secondary">Use filters above and click Search to find influencers.</Text>
                        )}
                        <Row gutter={[16, 16]}>
                            {results.map((inf) => (
                                <Col key={inf.id} xs={24} sm={12} lg={8}>
                                    <Card size="small" style={{ background: cardBg, borderRadius: 8, borderColor: '#333' }}>
                                        <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                                            <Avatar size={48} icon={<UserOutlined />} src={inf.profilePictureUrl} style={{ backgroundColor: primaryColor, color: '#000' }} />
                                            <div style={{ flex: 1, minWidth: 0 }}>
                                                <Text strong style={{ color: '#fff', fontSize: 15 }}>{inf.name}</Text>
                                                <div style={{ marginTop: 4 }}>
                                                    <Text type="secondary" style={{ fontSize: 12 }}>{inf.niche} · {inf.location}</Text>
                                                </div>
                                                <div style={{ marginTop: 6 }}>
                                                    {inf.openToCollaborations !== false ? (
                                                        <Tag color="green">Open to collaborations</Tag>
                                                    ) : (
                                                        <Tag>Not accepting new collabs</Tag>
                                                    )}
                                                </div>
                                                {(inf.followerCount != null || inf.engagementRate != null || inf.rate != null) && (
                                                    <div style={{ marginTop: 4 }}>
                                                        {inf.followerCount != null && <Text type="secondary" style={{ fontSize: 12 }}>{inf.followerCount.toLocaleString()} followers</Text>}
                                                        {inf.engagementRate != null && <Text type="secondary" style={{ fontSize: 12 }}> · {Number(inf.engagementRate).toFixed(1)}% engagement</Text>}
                                                        {inf.rate != null && <Text type="secondary" style={{ fontSize: 12 }}> · ${Number(inf.rate).toLocaleString()} rate</Text>}
                                                    </div>
                                                )}
                                                <Button type="primary" size="small" style={{ marginTop: 8, color: '#000000' }} onClick={() => openInviteModal(inf)}>Invite</Button>
                                            </div>
                                        </div>
                                    </Card>
                                </Col>
                            ))}
                        </Row>

            <Modal
                title={
                    <Space wrap>
                        <span>Invite {selectedInfluencer?.name ?? ''}</span>
                        {selectedInfluencer &&
                            (selectedInfluencer.openToCollaborations !== false ? (
                                <Tag color="green">Open to collaborations</Tag>
                            ) : (
                                <Tag>Not accepting new collabs</Tag>
                            ))}
                    </Space>
                }
                open={inviteModalOpen}
                onCancel={closeInviteModal}
                footer={null}
                destroyOnClose
                width={560}
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
                        <Button type="primary" htmlType="submit" loading={inviteSubmitting} style={{ color: '#000000' }}>Send invitation</Button>
                        <Button style={{ marginLeft: 8 }} onClick={closeInviteModal}>Cancel</Button>
                    </Form.Item>
                </Form>
            </Modal>
        </BrandPortalLayout>
    )
}
