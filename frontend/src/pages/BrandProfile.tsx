import { useState, useEffect } from 'react'
import { Form, Input, Button, Typography, ConfigProvider, Layout, Menu, Select, message } from 'antd'
import { UserOutlined, LogoutOutlined, PlusCircleOutlined, AppstoreOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import {
    getMyBrandProfile,
    updateMyBrandProfile,
    BUDGET_RANGE_OPTIONS,
    type BrandProfileRequest,
    type BrandProfileResponse,
} from '../services/brandService'

const { Content, Sider } = Layout
const { Title, Text } = Typography
const { TextArea } = Input

const primaryColor = '#FFFD82'
const textColor = '#000000'
const cardBackgroundColor = '#FFFFFF'

export const BrandProfile = () => {
    const [form] = Form.useForm<BrandProfileRequest>()
    const [loading, setLoading] = useState(false)
    const [fetching, setFetching] = useState(true)
    const navigate = useNavigate()

    useEffect(() => {
        getMyBrandProfile()
            .then((profile: BrandProfileResponse | null) => {
                if (profile) {
                    form.setFieldsValue({
                        name: profile.name,
                        industry: profile.industry,
                        website: profile.website,
                        email: profile.email,
                        logoUrl: profile.logoUrl ?? undefined,
                        description: profile.description ?? undefined,
                        instagramUrl: profile.instagramUrl ?? undefined,
                        linkedInUrl: profile.linkedInUrl ?? undefined,
                        twitterUrl: profile.twitterUrl ?? undefined,
                        budgetRange: profile.budgetRange ?? undefined,
                    })
                }
            })
            .catch(() => message.error('Failed to load profile'))
            .finally(() => setFetching(false))
    }, [form])

    const onFinish = async (values: BrandProfileRequest) => {
        setLoading(true)
        try {
            await updateMyBrandProfile(values)
            message.success('Profile saved successfully')
            // Short delay so success message is visible and backend commit is ready before dashboard loads
            setTimeout(() => {
                navigate('/brand/dashboard', { replace: true })
            }, 300)
        } catch (e) {
            const msg = e instanceof Error ? e.message : 'Failed to save profile'
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
                        selectedKeys={['profile']}
                        items={[
                            {
                                key: 'dashboard',
                                icon: <AppstoreOutlined />,
                                label: 'Dashboard',
                                onClick: () => navigate('/brand/dashboard'),
                            },
                            {
                                key: 'profile',
                                icon: <UserOutlined />,
                                label: 'Profile',
                            },
                            {
                                key: 'campaign',
                                icon: <PlusCircleOutlined />,
                                label: 'Create Campaign',
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
                                Company Profile
                            </Title>
                            <Text style={{ color: '#aaa' }}>
                                Manage your brand profile. This is visible to influencers you contact.
                            </Text>
                        </div>

                        <Form
                            form={form}
                            layout="vertical"
                            onFinish={onFinish}
                            style={{ maxWidth: 640 }}
                            disabled={fetching}
                        >
                            <Title level={5} style={{ color: '#ccc', marginTop: 24 }}>Required</Title>
                            <Form.Item
                                name="name"
                                label="Company name"
                                rules={[{ required: true, message: 'Company name is required' }]}
                            >
                                <Input placeholder="Your company or brand name" />
                            </Form.Item>
                            <Form.Item
                                name="industry"
                                label="Industry"
                                rules={[{ required: true, message: 'Industry is required' }]}
                            >
                                <Input placeholder="e.g. Fashion, Technology, Food & Beverage" />
                            </Form.Item>
                            <Form.Item
                                name="website"
                                label="Website"
                                rules={[
                                    { required: true, message: 'Website is required' },
                                    { type: 'url', message: 'Enter a valid URL (e.g. https://example.com)' },
                                ]}
                            >
                                <Input placeholder="https://www.example.com" />
                            </Form.Item>
                            <Form.Item
                                name="email"
                                label="Email"
                                rules={[
                                    { required: true, message: 'Email is required' },
                                    { type: 'email', message: 'Enter a valid email' },
                                ]}
                            >
                                <Input placeholder="contact@company.com" />
                            </Form.Item>

                            <Title level={5} style={{ color: '#ccc', marginTop: 24 }}>Optional</Title>
                            <Form.Item name="logoUrl" label="Logo URL">
                                <Input placeholder="https://example.com/logo.png" />
                            </Form.Item>
                            <Form.Item name="description" label="Description">
                                <TextArea rows={4} placeholder="Tell influencers about your brand and campaigns" />
                            </Form.Item>
                            <Form.Item name="instagramUrl" label="Instagram">
                                <Input placeholder="https://instagram.com/yourbrand" />
                            </Form.Item>
                            <Form.Item name="linkedInUrl" label="LinkedIn">
                                <Input placeholder="https://linkedin.com/company/yourbrand" />
                            </Form.Item>
                            <Form.Item name="twitterUrl" label="Twitter / X">
                                <Input placeholder="https://twitter.com/yourbrand" />
                            </Form.Item>

                            <Form.Item name="budgetRange" label="Budget range">
                                <Select
                                    placeholder="Select your typical campaign budget range"
                                    allowClear
                                    options={BUDGET_RANGE_OPTIONS}
                                />
                            </Form.Item>

                            <Form.Item style={{ marginTop: 32 }}>
                                <Button
                                    type="primary"
                                    htmlType="submit"
                                    loading={loading}
                                    style={{ minWidth: 140, fontWeight: 600, color: textColor }}
                                >
                                    Save profile
                                </Button>
                            </Form.Item>
                        </Form>
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
