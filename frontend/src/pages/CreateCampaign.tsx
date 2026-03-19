import { useState } from 'react'
import { Form, Input, Button, Typography, ConfigProvider, Layout, Menu, Select, message, InputNumber, theme, Card } from 'antd'
import { UserOutlined, LogoutOutlined, PlusCircleOutlined, AppstoreOutlined, ArrowLeftOutlined, MailOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import {
    createCampaign,
    BUDGET_RANGE_OPTIONS,
    CAMPAIGN_GOAL_OPTIONS,
    PREFERRED_CONTENT_OPTIONS,
    type CampaignRequest,
    type CampaignResponse,
} from '../services/campaignService'
import { createInvitation } from '../services/invitationService'

const { Content, Sider } = Layout
const { Title, Text } = Typography
const { TextArea } = Input

const primaryColor = '#fffd82ff'
const textColor = '#ffffff'
const cardBackgroundColor = '#141414'

export const CreateCampaign = () => {
    const [form] = Form.useForm<CampaignRequest & { preferredContentTypesList?: string[] }>()
    const [inviteForm] = Form.useForm<{ influencerId: number; message?: string }>()
    const [loading, setLoading] = useState(false)
    const [createdCampaign, setCreatedCampaign] = useState<CampaignResponse | null>(null)
    const [inviteSubmitting, setInviteSubmitting] = useState(false)
    const navigate = useNavigate()

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
            const campaign = await createCampaign(payload)
            setCreatedCampaign(campaign)
            message.success('Campaign created successfully')
            inviteForm.resetFields()
        } catch (e) {
            const msg = e instanceof Error ? e.message : 'Failed to create campaign'
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
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to send invitation')
        } finally {
            setInviteSubmitting(false)
        }
    }

    const goToDashboard = () => {
        navigate('/brand/dashboard', { replace: true })
    }

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: {
                    colorPrimary: primaryColor,
                    colorTextBase: textColor,
                    fontFamily: 'Inter, sans-serif',
                },
                components: {
                    Layout: {
                        bodyBg: '#000000',
                        headerBg: '#000000',
                        siderBg: '#000000',
                    },
                    Menu: {
                        darkItemBg: '#000000',
                        darkItemSelectedBg: '#333333',
                    },
                },
            }}
        >
            <Layout style={{ minHeight: '100vh' }}>
                <Sider width={250} theme="dark">
                    <div style={{ padding: '20px', textAlign: 'center' }}>
                        <Title level={4} style={{ color: '#fff', margin: 0 }}>Collabry</Title>
                        <Text style={{ color: primaryColor }}>Brand Portal</Text>
                    </div>
                    <Menu
                        theme="dark"
                        mode="inline"
                        selectedKeys={['campaign']}
                        items={[
                            {
                                key: 'dashboard',
                                icon: <AppstoreOutlined />,
                                label: 'Dashboard',
                                onClick: () => navigate('/brand/dashboard'),
                            },
                            {
                                key: 'campaign',
                                icon: <PlusCircleOutlined />,
                                label: 'Create Campaign',
                            },
                            {
                                key: 'profile',
                                icon: <UserOutlined />,
                                label: 'Profile',
                                onClick: () => navigate('/brand/profile'),
                            },
                            {
                                key: 'logout',
                                icon: <LogoutOutlined />,
                                label: 'Logout',
                                onClick: handleLogout,
                                danger: true,
                            },
                        ]}
                    />
                </Sider>
                <Layout style={{ backgroundColor: '#000000' }}>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280, backgroundColor: cardBackgroundColor }}>
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
                            <Form
                                form={form}
                                layout="vertical"
                                onFinish={onFinish}
                                style={{ maxWidth: 640 }}
                            >
                                <Title level={5} style={{ color: '#ccc', marginTop: 0 }}>Required</Title>
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
                                    <Select
                                        placeholder="Select campaign budget range"
                                        options={BUDGET_RANGE_OPTIONS}
                                    />
                                </Form.Item>

                                <Title level={5} style={{ color: '#ccc', marginTop: 24 }}>Optional</Title>
                                <Form.Item name="description" label="Description">
                                    <TextArea rows={4} placeholder="Describe the campaign, deliverables, and key messages" />
                                </Form.Item>
                                <Form.Item name="campaignGoal" label="Campaign goal">
                                    <Select
                                        placeholder="Select primary goal"
                                        allowClear
                                        options={CAMPAIGN_GOAL_OPTIONS}
                                    />
                                </Form.Item>
                                <Form.Item
                                    name="preferredContentTypesList"
                                    label="Preferred content types"
                                >
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
                                <Form.Item
                                    name="numberOfInfluencers"
                                    label="Number of influencers"
                                >
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
                                        You can invite an influencer now, or go to the Dashboard and use the Invite button on your campaign card anytime.
                                    </Text>

                                    <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit}>
                                        <Form.Item
                                            name="influencerId"
                                            label="Influencer user ID"
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
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
