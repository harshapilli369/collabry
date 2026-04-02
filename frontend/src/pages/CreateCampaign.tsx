import { useState } from 'react'
import { Form, Input, Button, Typography, Select, message, InputNumber, Card, Alert, Table, Modal } from 'antd'
import { ArrowLeftOutlined, MailOutlined, FundProjectionScreenOutlined, CheckCircleFilled, ThunderboltOutlined } from '@ant-design/icons'
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
import { userService, type InfluencerSearchResult } from '../services/userService'

const { Title, Text } = Typography
const { TextArea } = Input

const PRIMARY = BRAND_PORTAL_PRIMARY

export const CreateCampaign = () => {
    const [form] = Form.useForm<CampaignRequest & { preferredContentTypesList?: string[] }>()
    const [inviteForm] = Form.useForm<{ influencerId: number; message?: string }>()
    const [loading, setLoading] = useState(false)
    const [createdCampaign, setCreatedCampaign] = useState<CampaignResponse | null>(null)
    const [inviteSubmitting, setInviteSubmitting] = useState(false)
    const [submitError, setSubmitError] = useState<string | null>(null)
    const [findIdModalOpen, setFindIdModalOpen] = useState(false)
    const [influencerList, setInfluencerList] = useState<InfluencerSearchResult[]>([])
    const [influencerListLoading, setInfluencerListLoading] = useState(false)
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

    const openFindIdModal = () => {
        setFindIdModalOpen(true)
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

                        <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit} size="large">
                            <Form.Item
                                name="influencerId"
                                label={
                                    <span>
                                        Influencer user ID{' '}
                                        <Button type="link" size="small" onClick={openFindIdModal} style={{ paddingLeft: 8 }}>
                                            Find user ID
                                        </Button>
                                    </span>
                                }
                                rules={[{ required: true, message: 'Enter the influencer user ID' }]}
                            >
                                <InputNumber min={1} step={1} placeholder="e.g. 2" style={{ width: '100%' }} />
                            </Form.Item>
                            <Form.Item name="message" label="Message (optional)">
                                <TextArea rows={3} placeholder="Personal message to the influencer" />
                            </Form.Item>
                            <div style={{ display: 'flex', gap: 12 }}>
                                <Button type="primary" htmlType="submit" loading={inviteSubmitting} icon={<MailOutlined />} style={{ color: '#000', fontWeight: 600, borderRadius: 10, flex: 1 }}>
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
