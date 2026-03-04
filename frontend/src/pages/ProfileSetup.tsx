import { useState, useEffect } from 'react'
import { Form, Input, InputNumber, Button, Typography, ConfigProvider, Steps, message, theme } from 'antd'
import { UserOutlined, LinkOutlined, DollarOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import {
    getMyInfluencerProfile,
    updateMyInfluencerProfile,
    type InfluencerProfileRequest,
    type InfluencerProfileResponse,
} from '../services/influencerProfileService'

const { Title, Text } = Typography
const { TextArea } = Input

const STEPS = [
    { key: 'personal', title: 'Personal Info', icon: <UserOutlined /> },
    { key: 'social', title: 'Social Media', icon: <LinkOutlined /> },
    { key: 'pricing', title: 'Pricing', icon: <DollarOutlined /> },
]

export const ProfileSetup = () => {
    const [current, setCurrent] = useState(0)
    const [loading, setLoading] = useState(false)
    const [fetching, setFetching] = useState(true)
    const [form] = Form.useForm<InfluencerProfileRequest & { saveAsDraft?: boolean }>()
    const navigate = useNavigate()

    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    const isInfluencer = user?.role === 'INFLUENCER'

    useEffect(() => {
        if (!isInfluencer) {
            // Non-influencers should not be here; redirect to appropriate dashboard
            if (user?.role === 'BRAND') {
                navigate('/brand/dashboard', { replace: true })
            } else {
                navigate('/login', { replace: true })
            }
            return
        }
        getMyInfluencerProfile()
            .then((profile: InfluencerProfileResponse | null) => {
                if (profile) {
                    form.setFieldsValue({
                        name: profile.name,
                        age: profile.age,
                        location: profile.location,
                        niche: profile.niche,
                        bio: profile.bio ?? undefined,
                        profilePictureUrl: profile.profilePictureUrl ?? undefined,
                        instagramHandle: profile.instagramHandle ?? undefined,
                        youtubeHandle: profile.youtubeHandle ?? undefined,
                        tiktokHandle: profile.tiktokHandle ?? undefined,
                        rate: profile.rate ?? undefined,
                        audienceInfo: profile.audienceInfo ?? undefined,
                    })
                }
            })
            .catch(() => message.error('Failed to load profile'))
            .finally(() => setFetching(false))
    }, [form, isInfluencer, user?.role, navigate])

    const hasSocialHandle = (values: InfluencerProfileRequest) => {
        const ig = (values.instagramHandle ?? '').trim()
        const yt = (values.youtubeHandle ?? '').trim()
        const tt = (values.tiktokHandle ?? '').trim()
        return !!ig || !!yt || !!tt
    }

    const onFinish = async (values: InfluencerProfileRequest & { saveAsDraft?: boolean }, saveAsDraft: boolean) => {
        if (!isInfluencer) return
        if (!saveAsDraft && (!hasSocialHandle(values) || values.rate == null)) {
            if (!hasSocialHandle(values)) {
                message.error('At least one social media handle is required to complete your profile')
            } else {
                message.error('Rate is required to complete your profile')
            }
            return
        }
        setLoading(true)
        try {
            const payload: InfluencerProfileRequest = {
                name: values.name ?? '',
                age: values.age ?? 0,
                location: values.location ?? '',
                niche: values.niche ?? '',
                bio: values.bio || undefined,
                profilePictureUrl: values.profilePictureUrl || undefined,
                instagramHandle: values.instagramHandle || undefined,
                youtubeHandle: values.youtubeHandle || undefined,
                tiktokHandle: values.tiktokHandle || undefined,
                rate: values.rate ?? undefined,
                audienceInfo: values.audienceInfo || undefined,
            }
            await updateMyInfluencerProfile(payload, saveAsDraft)
            message.success(saveAsDraft ? 'Profile saved as draft' : 'Profile completed!')
            navigate('/influencer/dashboard', { replace: true })
        } catch (e) {
            const msg = e instanceof Error ? e.message : 'Failed to save profile'
            message.error(msg)
        } finally {
            setLoading(false)
        }
    }

    const handleSaveDraft = async () => {
        try {
            await form.validateFields(['name', 'age', 'location', 'niche'])
        } catch {
            return
        }
        const values = form.getFieldsValue()
        await onFinish(values, true)
    }

    const handleComplete = async () => {
        try {
            await form.validateFields()
        } catch {
            return
        }
        const values = form.getFieldsValue()
        await onFinish(values, false)
    }

    const primaryColor = '#FFFD82'
    const textColor = '#ffffff'
    const pageBackgroundColor = '#000000'
    const cardBackgroundColor = '#141414'

    if (!isInfluencer && !fetching) return null

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: {
                    colorPrimary: primaryColor,
                    colorText: textColor,
                    borderRadius: 8,
                    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
                },
                components: {
                    Button: {},
                    Input: { paddingBlock: 10 },
                },
            }}
        >
            <div
                style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'flex-start',
                    minHeight: '100vh',
                    backgroundColor: pageBackgroundColor,
                    padding: '40px 20px',
                }}
            >
                <div
                    style={{
                        width: '100%',
                        maxWidth: 520,
                        padding: 40,
                        backgroundColor: cardBackgroundColor,
                        borderRadius: 16,
                        boxShadow: '0 4px 20px rgba(0,0,0,0.3)',
                    }}
                >
                    <div style={{ textAlign: 'center', marginBottom: 32 }}>
                        <img src="/logo.png" alt="Collabry Logo" style={{ height: 60, marginBottom: 16, borderRadius: 8 }} />
                        <Title level={2} style={{ margin: '0 0 8px', color: textColor }}>
                            Complete your profile
                        </Title>
                        <Text type="secondary">
                            Add details so brands can find and evaluate you. Complete all steps to appear in search.
                        </Text>
                    </div>

                    <Steps
                        current={current}
                        onChange={setCurrent}
                        items={STEPS.map((s) => ({ key: s.key, title: s.title, icon: s.icon }))}
                        style={{ marginBottom: 32 }}
                    />

                    <Form
                        form={form}
                        layout="vertical"
                        size="large"
                        initialValues={{ age: undefined, rate: undefined }}
                    >
                        <div style={{ display: current === 0 ? 'block' : 'none' }}>
                            <Form.Item
                                name="name"
                                label="Name"
                                rules={[{ required: true, message: 'Name is required' }]}
                            >
                                <Input placeholder="Your full name" />
                            </Form.Item>
                            <Form.Item
                                name="age"
                                label="Age"
                                rules={[
                                    { required: true, message: 'Age is required' },
                                    { type: 'number', min: 13, max: 120, message: 'Age must be 13–120' },
                                ]}
                            >
                                <InputNumber placeholder="Your age" style={{ width: '100%' }} min={13} max={120} />
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
                            <Form.Item name="bio" label="Bio (optional)">
                                <TextArea rows={4} placeholder="Tell brands about yourself..." />
                            </Form.Item>
                            <Form.Item name="profilePictureUrl" label="Profile picture URL (optional)">
                                <Input placeholder="https://..." />
                            </Form.Item>
                        </div>

                        <div style={{ display: current === 1 ? 'block' : 'none' }}>
                            <Form.Item
                                name="instagramHandle"
                                label="Instagram handle"
                                help="At least one social handle is required to complete your profile"
                            >
                                <Input addonBefore="@" placeholder="username" />
                            </Form.Item>
                            <Form.Item name="youtubeHandle" label="YouTube channel/handle">
                                <Input addonBefore="@" placeholder="channel or username" />
                            </Form.Item>
                            <Form.Item name="tiktokHandle" label="TikTok handle">
                                <Input addonBefore="@" placeholder="username" />
                            </Form.Item>
                        </div>

                        <div style={{ display: current === 2 ? 'block' : 'none' }}>
                            <Form.Item
                                name="rate"
                                label="Rate (per post/collab)"
                                rules={[
                                    { required: true, message: 'Rate is required to complete profile' },
                                    { type: 'number', min: 0, message: 'Rate must be 0 or greater' },
                                ]}
                                help="Required to appear in brand search"
                            >
                                <InputNumber
                                    prefix="$"
                                    placeholder="0"
                                    style={{ width: '100%' }}
                                    min={0}
                                    precision={2}
                                />
                            </Form.Item>
                            <Form.Item name="audienceInfo" label="Audience info (optional)">
                                <TextArea rows={4} placeholder="e.g. Demographics, engagement metrics, reach..." />
                            </Form.Item>
                        </div>

                        <div style={{ display: 'flex', gap: 12, marginTop: 24 }}>
                            {current > 0 ? (
                                <Button size="large" onClick={() => setCurrent(current - 1)}>
                                    Back
                                </Button>
                            ) : null}
                            <div style={{ flex: 1 }} />
                            <Button size="large" loading={loading} onClick={handleSaveDraft}>
                                Save as draft
                            </Button>
                            {current < STEPS.length - 1 ? (
                                <Button
                                    type="primary"
                                    size="large"
                                    onClick={async () => {
                                        try {
                                            const fields = current === 0 ? ['name', 'age', 'location', 'niche'] : []
                                            if (fields.length) await form.validateFields(fields)
                                            setCurrent(current + 1)
                                        } catch {
                                            /* validation failed */
                                        }
                                    }}
                                    style={{ color: textColor, fontWeight: 600 }}
                                >
                                    Next
                                </Button>
                            ) : (
                                <Button
                                    type="primary"
                                    size="large"
                                    loading={loading}
                                    onClick={handleComplete}
                                    style={{ color: textColor, fontWeight: 600 }}
                                >
                                    Complete profile
                                </Button>
                            )}
                        </div>
                    </Form>
                </div>
            </div>
        </ConfigProvider>
    )
}
