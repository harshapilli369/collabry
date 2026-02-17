import { useState } from 'react'
import { Form, Input, Button, Typography, ConfigProvider, Layout, Menu, Select, message, InputNumber } from 'antd'
import { UserOutlined, LogoutOutlined, PlusCircleOutlined, AppstoreOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import {
    createCampaign,
    BUDGET_RANGE_OPTIONS,
    CAMPAIGN_GOAL_OPTIONS,
    PREFERRED_CONTENT_OPTIONS,
    type CampaignRequest,
} from '../services/campaignService'

const { Content, Sider } = Layout
const { Title, Text } = Typography
const { TextArea } = Input

const primaryColor = '#FFFD82'
const textColor = '#000000'
const cardBackgroundColor = '#FFFFFF'

export const CreateCampaign = () => {
    const [form] = Form.useForm<CampaignRequest & { preferredContentTypesList?: string[] }>()
    const [loading, setLoading] = useState(false)
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
            await createCampaign(payload)
            message.success('Campaign created successfully')
            setTimeout(() => {
                navigate('/brand/dashboard', { replace: true })
            }, 300)
        } catch (e) {
            const msg = e instanceof Error ? e.message : 'Failed to create campaign'
            message.error(msg)
            setLoading(false)
        }
    }

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    return (
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: primaryColor,
                    colorTextBase: textColor,
                    fontFamily: 'Inter, sans-serif',
                },
                components: {
                    Layout: {
                        bodyBg: cardBackgroundColor,
                        headerBg: cardBackgroundColor,
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
                <Layout style={{ backgroundColor: cardBackgroundColor }}>
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
                                    style={{ minWidth: 160, fontWeight: 600, color: textColor }}
                                >
                                    Create campaign
                                </Button>
                            </Form.Item>
                        </Form>
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
