import { useState } from 'react'
import { Form, Input, Button, Typography, Select, message, InputNumber, Card, Alert, Drawer, Avatar, Empty } from 'antd'
import { ArrowLeftOutlined, MailOutlined, UserOutlined } from '@ant-design/icons'
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

const primaryColor = BRAND_PORTAL_PRIMARY

export const CreateCampaign = () => {
    const [form] = Form.useForm<CampaignRequest & { preferredContentTypesList?: string[] }>()
    const [inviteForm] = Form.useForm<{ influencerId: number; message?: string }>()
    const [loading, setLoading] = useState(false)
    const [createdCampaign, setCreatedCampaign] = useState<CampaignResponse | null>(null)
    const [inviteSubmitting, setInviteSubmitting] = useState(false)
    const [submitError, setSubmitError] = useState<string | null>(null)
    const [findIdDrawerOpen, setFindIdDrawerOpen] = useState(false)
    const [influencerList, setInfluencerList] = useState<InfluencerSearchResult[]>([])
    const [influencerListLoading, setInfluencerListLoading] = useState(false)
    const [influencerSearch, setInfluencerSearch] = useState('')
    const navigate = useNavigate()

    const openFindIdDrawer = () => {
        setFindIdDrawerOpen(true)
        setInfluencerSearch('')
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

    const handleSelectInfluencer = (id: number) => {
        inviteForm.setFieldValue('influencerId', id)
        setFindIdDrawerOpen(false)
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

    const onInviteSubmit = async (values: { influencerId: number; message?: string }) => {
        if (!createdCampaign) return
        setInviteSubmitting(true)
        try {
            await createInvitation(createdCampaign.id, {
                influencerId: values.influencerId,
                message: values.message?.trim() || undefined,
            })
            message.success('Invitation sent to influencer')
            inviteForm.resetFields()
            navigate('/brand/collaborations', { replace: true })
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to send invitation')
        } finally {
            setInviteSubmitting(false)
        }
    }

    const goToDashboard = () => {
        navigate('/brand/dashboard', { replace: true })
    }

    return (
        <BrandPortalLayout activeMenuKey="campaign-create">
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
                    Create Campaign
                </Title>
                <Text style={{ color: '#aaa' }}>
                    Set up a new influencer campaign with budget, goals, and content preferences.
                </Text>
            </div>

            {!createdCampaign ? (
                <Form form={form} layout="vertical" onFinish={onFinish} style={{ maxWidth: 640 }}>
                    {submitError && (
                        <Alert
                            type="error"
                            message={submitError}
                            showIcon
                            closable
                            onClose={() => setSubmitError(null)}
                            style={{ marginBottom: 16 }}
                        />
                    )}
                    <Title level={5} style={{ color: '#ccc', marginTop: 0 }}>
                        Required
                    </Title>
                    <Form.Item
                        name="name"
                        label="Campaign name"
                        rules={[{ required: true, message: 'Campaign name is required' }]}
                    >
                        <Input placeholder="e.g. Spring Collection Launch 2025" />
                    </Form.Item>
                    <Form.Item
                        name="budgetRange"
                        label="Budget range"
                        rules={[{ required: true, message: 'Budget range is required' }]}
                    >
                        <Select placeholder="Select campaign budget range" options={BUDGET_RANGE_OPTIONS} />
                    </Form.Item>

                    <Title level={5} style={{ color: '#ccc', marginTop: 24 }}>
                        Optional
                    </Title>
                    <Form.Item name="description" label="Description">
                        <TextArea rows={4} placeholder="Describe the campaign, deliverables, and key messages" />
                    </Form.Item>
                    <Form.Item name="campaignGoal" label="Campaign goal">
                        <Select placeholder="Select primary goal" allowClear options={CAMPAIGN_GOAL_OPTIONS} />
                    </Form.Item>
                    <Form.Item name="preferredContentTypesList" label="Preferred content types">
                        <Select
                            mode="multiple"
                            placeholder="Select content types (e.g. Reels, YouTube)"
                            allowClear
                            options={PREFERRED_CONTENT_OPTIONS}
                        />
                    </Form.Item>
                    <Form.Item name="startDate" label="Start date">
                        <Input type="date" />
                    </Form.Item>
                    <Form.Item name="endDate" label="End date">
                        <Input type="date" />
                    </Form.Item>
                    <Form.Item name="numberOfInfluencers" label="Number of influencers">
                        <InputNumber min={1} placeholder="e.g. 5" style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item style={{ marginTop: 32 }}>
                        <Button
                            type="primary"
                            htmlType="submit"
                            loading={loading}
                            style={{ minWidth: 160, fontWeight: 600, color: '#000000' }}
                        >
                            Create campaign
                        </Button>
                    </Form.Item>
                </Form>
            ) : (
                <div style={{ maxWidth: 640 }}>
                    <Card
                        style={{ marginBottom: 24, borderColor: primaryColor, borderWidth: 1 }}
                        styles={{ body: { padding: 24 } }}
                    >
                        <Title level={5} style={{ color: primaryColor, marginTop: 0 }}>
                            Campaign &quot;{createdCampaign.name}&quot; created
                        </Title>
                        <Text style={{ color: '#aaa', display: 'block', marginBottom: 24 }}>
                            You can invite an influencer now, or go to the Dashboard and use the Invite button on your
                            campaign card anytime.
                        </Text>

                        <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit}>
                            <Form.Item
                                name="influencerId"
                                label={
                                    <span>
                                        Influencer user ID
                                        <Button type="link" size="small" onClick={openFindIdDrawer} style={{ paddingLeft: 8 }}>
                                            Find user ID
                                        </Button>
                                    </span>
                                }
                                rules={[{ required: true, message: 'Enter the influencer’s user ID' }]}
                            >
                                <InputNumber min={1} step={1} placeholder="e.g. 2" style={{ width: '100%' }} />
                            </Form.Item>
                            <Form.Item name="message" label="Message (optional)">
                                <TextArea rows={3} placeholder="Personal message to the influencer" />
                            </Form.Item>
                            <Form.Item style={{ marginBottom: 0 }}>
                                <Button
                                    type="primary"
                                    htmlType="submit"
                                    loading={inviteSubmitting}
                                    icon={<MailOutlined />}
                                    style={{ marginRight: 8, color: '#000000' }}
                                >
                                    Send invitation
                                </Button>
                                <Button type="default" onClick={goToDashboard}>
                                    Skip — go to Dashboard
                                </Button>
                            </Form.Item>
                        </Form>
                    </Card>
                </div>
            )}

            <Drawer
                title="Find influencer"
                placement="right"
                open={findIdDrawerOpen}
                onClose={() => setFindIdDrawerOpen(false)}
                width={440}
            >
                <p style={{ color: '#8c8c8c', marginBottom: 12 }}>
                    Pick an influencer card to auto-fill the user ID in your invite form.
                </p>
                <Input
                    value={influencerSearch}
                    onChange={(e) => setInfluencerSearch(e.target.value)}
                    placeholder="Search by name, email, or ID"
                    allowClear
                    style={{ marginBottom: 12 }}
                />
                {influencerListLoading ? (
                    <Text type="secondary">Loading influencers...</Text>
                ) : filteredInfluencers.length === 0 ? (
                    <Empty description="No influencers found" />
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                        {filteredInfluencers.map((inf) => (
                            <Card
                                key={inf.id}
                                hoverable
                                size="small"
                                onClick={() => handleSelectInfluencer(inf.id)}
                                style={{ borderColor: '#2a2a2a' }}
                                styles={{ body: { padding: 12 } }}
                            >
                                <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                                    <Avatar icon={<UserOutlined />} />
                                    <div style={{ flex: 1, minWidth: 0 }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                                            <Text strong ellipsis>{inf.displayName || 'Influencer'}</Text>
                                            <Text style={{ color: primaryColor, fontWeight: 600 }}>ID #{inf.id}</Text>
                                        </div>
                                        <Text type="secondary" ellipsis>{inf.email}</Text>
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
