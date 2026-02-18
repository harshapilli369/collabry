import { useState, useEffect } from 'react'
import { Form, Input, Button, Typography, ConfigProvider, Layout, Menu, Steps, message } from 'antd'
import { UserOutlined, LogoutOutlined, MailOutlined, AppstoreOutlined, ArrowLeftOutlined, DollarOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import {
    getMyInfluencerProfile,
    updateMyInfluencerProfile,
    clearProfileSubmittedFlag,
    type InfluencerProfileRequest,
    type InfluencerProfileResponse,
} from '../services/influencerProfileService'

const { Content, Sider } = Layout
const { Title, Text } = Typography
const { TextArea } = Input

const primaryColor = '#EFEE96'
const secondaryColor = '#BD72EB'
const textColor = '#000000'
const cardBackgroundColor = '#FFFFFF'

const STEP_ITEMS = [
    { title: 'Personal Info', icon: <UserOutlined /> },
    { title: 'Social Media', icon: <UserOutlined /> },
    { title: 'Pricing', icon: <DollarOutlined /> },
]

export const InfluencerProfileForm = () => {
    const [form] = Form.useForm<InfluencerProfileRequest>()
    const [currentStep, setCurrentStep] = useState(0)
    const [loading, setLoading] = useState(false)
    const [fetching, setFetching] = useState(true)
    const navigate = useNavigate()

    useEffect(() => {
        getMyInfluencerProfile()
            .then((profile: InfluencerProfileResponse | null) => {
                if (profile) {
                    form.setFieldsValue({
                        name: profile.name,
                        age: profile.age,
                        location: profile.location,
                        niche: profile.niche,
                        bio: profile.bio,
                        profilePictureUrl: profile.profilePictureUrl,
                        instagramHandle: profile.instagramHandle,
                        youtubeHandle: profile.youtubeHandle,
                        tiktokHandle: profile.tiktokHandle,
                        audienceInfo: profile.audienceInfo,
                        rate: profile.rate,
                    })
                }
            })
            .catch(() => message.error('Failed to load profile'))
            .finally(() => setFetching(false))
    }, [form])

    const handleNext = async () => {
        try {
            if (currentStep === 1) {
                const inst = form.getFieldValue('instagramHandle')
                const yt = form.getFieldValue('youtubeHandle')
                const tiktok = form.getFieldValue('tiktokHandle')
                if (!inst?.trim() && !yt?.trim() && !tiktok?.trim()) {
                    message.error('Add at least one social media handle')
                    return
                }
            }
            await form.validateFields()
            setCurrentStep((s) => s + 1)
        } catch {
            message.error('Please fill required fields')
        }
    }

    const handlePrev = () => {
        setCurrentStep((s) => s - 1)
    }

    const handleFinish = async () => {
        try {
            const values = await form.validateFields()
            setLoading(true)
            const payload: InfluencerProfileRequest = {
                name: values.name,
                age: values.age ? Number(values.age) : undefined,
                location: values.location,
                niche: values.niche,
                bio: values.bio,
                profilePictureUrl: values.profilePictureUrl,
                instagramHandle: values.instagramHandle,
                youtubeHandle: values.youtubeHandle,
                tiktokHandle: values.tiktokHandle,
                audienceInfo: values.audienceInfo,
                rate: values.rate != null ? Number(values.rate) : undefined,
            }
            await updateMyInfluencerProfile(payload)
            message.success('Profile saved successfully')
            // Navigate immediately - use replace to avoid back-button returning to form
            navigate('/influencer/dashboard', { replace: true, state: { profileJustCompleted: true } })
            // Fallback: if navigate doesn't trigger (e.g. React batching), force navigation
            setTimeout(() => {
                if (window.location.pathname.includes('/influencer/profile')) {
                    window.location.href = '/influencer/dashboard'
                }
            }, 150)
        } catch (e) {
            const msg = e instanceof Error ? e.message : 'Failed to save profile'
            message.error(msg)
        } finally {
            setLoading(false)
        }
    }

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        clearProfileSubmittedFlag()
        navigate('/login', { replace: true })
    }

    const renderStepContent = () => {
        if (currentStep === 0) {
            return (
                <>
                    <Form.Item
                        name="name"
                        label="Name"
                        rules={[{ required: true, message: 'Name is required' }]}
                    >
                        <Input placeholder="Your name" />
                    </Form.Item>
                    <Form.Item
                        name="age"
                        label="Age"
                        rules={[{ required: true, message: 'Age is required' }]}
                    >
                        <Input type="number" min={13} max={120} placeholder="Your age" />
                    </Form.Item>
                    <Form.Item
                        name="location"
                        label="Location"
                        rules={[{ required: true, message: 'Location is required' }]}
                    >
                        <Input placeholder="City, Country" />
                    </Form.Item>
                    <Form.Item
                        name="niche"
                        label="Niche"
                        rules={[{ required: true, message: 'Niche is required' }]}
                    >
                        <Input placeholder="e.g. Fashion, Tech, Fitness" />
                    </Form.Item>
                    <Form.Item name="bio" label="Bio">
                        <TextArea rows={4} placeholder="Tell brands about yourself" />
                    </Form.Item>
                </>
            )
        }
        if (currentStep === 1) {
            return (
                <>
                    <Form.Item name="instagramHandle" label="Instagram">
                        <Input placeholder="@yourhandle" addonBefore="@" />
                    </Form.Item>
                    <Form.Item name="youtubeHandle" label="YouTube">
                        <Input placeholder="Channel name or handle" />
                    </Form.Item>
                    <Form.Item name="tiktokHandle" label="TikTok">
                        <Input placeholder="@yourhandle" addonBefore="@" />
                    </Form.Item>
                    <Form.Item name="audienceInfo" label="Audience info">
                        <TextArea rows={3} placeholder="Describe your audience (e.g. age, interests)" />
                    </Form.Item>
                </>
            )
        }
        return (
            <>
                <Form.Item
                    name="rate"
                    label="Rate"
                    rules={[{ required: true, message: 'Rate is required' }]}
                >
                    <Input
                        type="number"
                        min={0}
                        prefix="$"
                        placeholder="Your rate per post/campaign"
                    />
                </Form.Item>
            </>
        )
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
                    Button: {
                        colorTextLightSolid: textColor,
                    },
                },
            }}
        >
            <Layout style={{ minHeight: '100vh' }}>
                <Sider width={250} theme="dark">
                    <div style={{ padding: '20px', textAlign: 'center' }}>
                        <Title level={4} style={{ color: '#fff', margin: 0 }}>Collabry</Title>
                        <Text style={{ color: secondaryColor }}>Influencer</Text>
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
                                onClick: () => navigate('/influencer/dashboard'),
                            },
                            {
                                key: 'profile',
                                icon: <UserOutlined />,
                                label: 'Profile',
                            },
                            {
                                key: 'invitations',
                                icon: <MailOutlined />,
                                label: 'Invitations',
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
                                onClick={() => navigate('/influencer/dashboard')}
                                style={{ color: secondaryColor, paddingLeft: 0, marginBottom: 16 }}
                            >
                                Back to Dashboard
                            </Button>
                            <Title level={1} style={{ color: secondaryColor, margin: '0 0 8px', fontSize: '2rem' }}>
                                Your Profile
                            </Title>
                            <Text style={{ color: '#666' }}>
                                Complete your profile so brands can find and evaluate you.
                            </Text>
                        </div>

                        <Steps
                            current={currentStep}
                            items={STEP_ITEMS}
                            style={{ marginBottom: 32 }}
                        />

                        <Form
                            form={form}
                            layout="vertical"
                            style={{ maxWidth: 480 }}
                            disabled={fetching}
                        >
                            {renderStepContent()}

                            <Form.Item style={{ marginTop: 32 }}>
                                <div style={{ display: 'flex', gap: 12 }}>
                                    {currentStep > 0 && (
                                        <Button onClick={handlePrev} disabled={loading}>
                                            Previous
                                        </Button>
                                    )}
                                    {currentStep < 2 ? (
                                        <Button type="primary" onClick={handleNext} disabled={loading}>
                                            Next
                                        </Button>
                                    ) : (
                                        <Button
                                            type="primary"
                                            loading={loading}
                                            onClick={handleFinish}
                                            style={{ color: textColor }}
                                        >
                                            Finish
                                        </Button>
                                    )}
                                </div>
                            </Form.Item>
                        </Form>
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
