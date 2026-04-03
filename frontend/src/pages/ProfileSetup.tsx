import { useState, useEffect } from 'react'
import { Form, Input, InputNumber, Button, Typography, ConfigProvider, Steps, theme, Modal, Upload, Avatar, notification, Select } from 'antd'
import { UserOutlined, LinkOutlined, DollarOutlined, ArrowLeftOutlined, CheckCircleFilled, InstagramOutlined, YoutubeOutlined, RobotOutlined, CameraOutlined, LoadingOutlined } from '@ant-design/icons'
import type { UploadFile, RcFile } from 'antd/es/upload/interface'
import { useNavigate } from 'react-router-dom'
import {
    getMyInfluencerProfile,
    updateMyInfluencerProfile,
    enhanceBio,
    type InfluencerProfileRequest,
    type InfluencerProfileResponse,
} from '../services/influencerProfileService'
import { uploadProfileImage } from '../services/imageUploadService'
import { INDUSTRY_NICHE_OPTIONS, LOCATION_OPTIONS } from '../constants/profileOptions'

const { Title, Text } = Typography
const { TextArea } = Input

const PRIMARY = '#BD72EB'

const STEPS = [
    { key: 'personal', title: 'Personal Info', icon: <UserOutlined /> },
    { key: 'social', title: 'Social Media', icon: <LinkOutlined /> },
    { key: 'pricing', title: 'Pricing', icon: <DollarOutlined /> },
]

export const ProfileSetup = () => {
    const [current, setCurrent] = useState(0)
    const [loading, setLoading] = useState(false)
    const [fetching, setFetching] = useState(true)
    const [isEdit, setIsEdit] = useState(false)
    const [form] = Form.useForm<InfluencerProfileRequest & { saveAsDraft?: boolean }>()
    const navigate = useNavigate()
    const [enhancingBio, setEnhancingBio] = useState(false)
    const [enhancedBio, setEnhancedBio] = useState<string | null>(null)
    const [avatarUrl, setAvatarUrl] = useState<string | undefined>(undefined)
    const [uploadingAvatar, setUploadingAvatar] = useState(false)
    const [photoSaved, setPhotoSaved] = useState(false)

    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null
    const isInfluencer = user?.role === 'INFLUENCER'

    useEffect(() => {
        if (!isInfluencer) {
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
                    setIsEdit(true)
                    setAvatarUrl(profile.profilePictureUrl ?? undefined)
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
                        followerCount: profile.followerCount ?? undefined,
                        engagementRate: profile.engagementRate ?? undefined,
                        audienceInfo: profile.audienceInfo ?? undefined,
                    })
                }
            })
            .catch(() => notification.error({ message: 'Failed to load profile', placement: 'topRight' }))
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
                setCurrent(1)
                notification.error({ message: 'At least one social media handle is required to complete your profile', placement: 'topRight' })
            } else {
                setCurrent(2)
                notification.error({ message: 'Rate is required to complete your profile', placement: 'topRight' })
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
                followerCount: values.followerCount ?? undefined,
                engagementRate: values.engagementRate ?? undefined,
                audienceInfo: values.audienceInfo || undefined,
            }
            await updateMyInfluencerProfile(payload, saveAsDraft)
            setPhotoSaved(false)
            notification.success({
                message: saveAsDraft ? 'Profile saved as draft' : 'Profile saved!',
                placement: 'topRight',
                duration: 4,
            })
            navigate('/influencer/profile', { replace: true })
        } catch (e) {
            const msg = e instanceof Error ? e.message : 'Failed to save profile'
            notification.error({ message: msg, placement: 'topRight' })
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
            const step1Fields = ['name', 'age', 'location', 'niche']
            const values = form.getFieldsValue()
            const step1Invalid = step1Fields.some(f => !values[f as keyof typeof values])
            if (step1Invalid) {
                setCurrent(0)
                notification.error({ message: 'Please complete your personal info first', placement: 'topRight' })
            } else if (!values.rate) {
                setCurrent(2)
                notification.error({ message: 'Rate is required to complete your profile', placement: 'topRight' })
            } else {
                notification.error({ message: 'Please fill in all required fields', placement: 'topRight' })
            }
            return
        }
        const values = form.getFieldsValue()
        await onFinish(values, false)
    }


    const TiktokSvg = () => (
        <svg viewBox="0 0 448 512" width="14px" height="14px" fill="currentColor" style={{ verticalAlign: '-0.125em' }}>
            <path d="M448,209.91a210.06,210.06,0,0,1-122.77-39.25V349.38A162.55,162.55,0,1,1,185,188.31V278.2a74.62,74.62,0,1,0,52.23,71.18V0l88,0a121.18,121.18,0,0,0,1.86,22.17h0A122.18,122.18,0,0,0,381,102.39a121.43,121.43,0,0,0,67,20.14Z"/>
        </svg>
    )

    if (!isInfluencer && !fetching) return null

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: {
                    colorPrimary: PRIMARY,
                    colorText: '#ffffff',
                    borderRadius: 8,
                    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
                },
                components: {
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
                    backgroundColor: '#000',
                    padding: '40px 20px',
                }}
            >
                <div
                    style={{
                        width: '100%',
                        maxWidth: 560,
                        padding: '40px 44px',
                        backgroundColor: '#0d0d0d',
                        borderRadius: 20,
                        border: `1px solid ${PRIMARY}15`,
                        boxShadow: `0 8px 40px rgba(189, 114, 235, 0.06)`,
                    }}
                >
                    {/* Back button */}
                    {isEdit && (
                        <Button
                            type="text"
                            icon={<ArrowLeftOutlined />}
                            onClick={() => navigate('/influencer/profile')}
                            style={{ color: '#888', marginBottom: 16, padding: 0 }}
                        >
                            Back to Profile
                        </Button>
                    )}

                    {/* Header */}
                    <div style={{ textAlign: 'center', marginBottom: 36 }}>
                        <div
                            style={{
                                width: 56,
                                height: 56,
                                borderRadius: 16,
                                background: `linear-gradient(135deg, ${PRIMARY}, #9b59b6)`,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                margin: '0 auto 16px',
                            }}
                        >
                            <UserOutlined style={{ fontSize: 24, color: '#fff' }} />
                        </div>
                        <Title level={2} style={{ margin: 0, color: '#fff' }}>
                            {isEdit ? 'Edit your profile' : 'Complete your profile'}
                        </Title>
                        <Text style={{ color: '#666', fontSize: 14, marginTop: 8, display: 'block' }}>
                            Add details so brands can find and evaluate you.
                        </Text>
                    </div>

                    {/* Steps */}
                    <Steps
                        current={current}
                        onChange={setCurrent}
                        items={STEPS.map((s) => ({ key: s.key, title: s.title, icon: s.icon }))}
                        style={{ marginBottom: 36 }}
                    />

                    <Form
                        form={form}
                        layout="vertical"
                        size="large"
                        initialValues={{ age: undefined, rate: undefined }}
                    >
                        {/* Step 1: Personal Info */}
                        <div style={{ display: current === 0 ? 'block' : 'none' }}>
                            <div style={{ padding: '20px 24px', background: '#111', borderRadius: 12, border: '1px solid #1a1a1a', marginBottom: 8 }}>
                                <Text style={{ color: PRIMARY, fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 16 }}>
                                    Basic Information
                                </Text>
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
                                        { type: 'number', min: 13, max: 120, message: 'Age must be 13-120' },
                                    ]}
                                >
                                    <InputNumber placeholder="Your age" style={{ width: '100%' }} min={13} max={120} />
                                </Form.Item>
                                <Form.Item
                                    name="location"
                                    label="Location"
                                    rules={[{ required: true, message: 'Location is required' }]}
                                >
                                    <Select
                                        showSearch
                                        placeholder="Search city, country"
                                        optionFilterProp="label"
                                        options={LOCATION_OPTIONS}
                                        allowClear
                                    />
                                </Form.Item>
                                <Form.Item
                                    name="niche"
                                    label="Niche"
                                    rules={[{ required: true, message: 'Niche is required' }]}
                                >
                                    <Select
                                        showSearch
                                        placeholder="Search or select your niche"
                                        optionFilterProp="label"
                                        options={INDUSTRY_NICHE_OPTIONS}
                                        allowClear
                                    />
                                </Form.Item>
                            </div>
                            <div style={{ padding: '20px 24px', background: '#111', borderRadius: 12, border: '1px solid #1a1a1a', marginTop: 16 }}>
                                <Text style={{ color: '#666', fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 16 }}>
                                    Optional
                                </Text>
                                <Form.Item name="bio" label={
                                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%' }}>
                                        <span>Bio</span>
                                        <Button
                                            type="link"
                                            size="small"
                                            icon={<RobotOutlined />}
                                            loading={enhancingBio}
                                            onClick={async () => {
                                                const currentBio = form.getFieldValue('bio' as any) as string | undefined
                                                if (!currentBio?.trim()) {
                                                    Modal.warning({ title: 'Bio Required', content: 'Please write a bio first, then click Enhance with AI to polish it.', centered: true, okText: 'Got it' })
                                                    return
                                                }
                                                setEnhancingBio(true)
                                                try {
                                                    const result = await enhanceBio(currentBio.trim())
                                                    setEnhancedBio(result)
                                                } catch (e) {
                                                    Modal.error({ title: 'Enhancement Failed', content: e instanceof Error ? e.message : 'Could not enhance bio. Try again.', centered: true })
                                                } finally {
                                                    setEnhancingBio(false)
                                                }
                                            }}
                                            style={{ color: PRIMARY, padding: 0, fontSize: 12 }}
                                        >
                                            Enhance with AI
                                        </Button>
                                    </div>
                                }>
                                    <TextArea rows={4} placeholder="Tell brands about yourself..." />
                                </Form.Item>

                                {/* Enhanced Bio Preview */}
                                {enhancedBio && (
                                    <div style={{ marginBottom: 16, padding: 16, background: `${PRIMARY}08`, borderRadius: 10, border: `1px solid ${PRIMARY}25` }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 8 }}>
                                            <RobotOutlined style={{ color: PRIMARY }} />
                                            <Text style={{ color: PRIMARY, fontSize: 12, fontWeight: 600 }}>AI-Enhanced Version</Text>
                                        </div>
                                        <Text style={{ color: '#ccc', fontSize: 14, lineHeight: 1.6, display: 'block', marginBottom: 12 }}>{enhancedBio}</Text>
                                        <div style={{ display: 'flex', gap: 8 }}>
                                            <Button
                                                type="primary"
                                                size="small"
                                                onClick={() => {
                                                    form.setFieldsValue({ bio: enhancedBio } as any)
                                                    setEnhancedBio(null)
                                                    Modal.success({ title: 'Bio Updated!', content: 'The enhanced bio has been applied. Remember to save your profile.', centered: true, okText: 'Got it' })
                                                }}
                                                style={{ borderRadius: 8 }}
                                            >
                                                Accept
                                            </Button>
                                            <Button
                                                size="small"
                                                onClick={() => setEnhancedBio(null)}
                                                style={{ borderRadius: 8 }}
                                            >
                                                Discard
                                            </Button>
                                        </div>
                                    </div>
                                )}
                                <Form.Item name="profilePictureUrl" label="Profile picture">
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                                        <div style={{ position: 'relative', flexShrink: 0 }}>
                                            <Avatar
                                                size={72}
                                                src={avatarUrl}
                                                icon={<UserOutlined />}
                                                style={{ background: '#222', border: `2px solid ${PRIMARY}30` }}
                                            />
                                            {uploadingAvatar && (
                                                <div style={{
                                                    position: 'absolute', inset: 0, borderRadius: '50%',
                                                    background: 'rgba(0,0,0,0.5)', display: 'flex',
                                                    alignItems: 'center', justifyContent: 'center',
                                                }}>
                                                    <LoadingOutlined style={{ color: '#fff' }} />
                                                </div>
                                            )}
                                        </div>
                                        <Upload
                                            accept="image/jpeg,image/png,image/gif,image/webp"
                                            showUploadList={false}
                                            beforeUpload={async (file: RcFile) => {
                                                if (file.size > 10 * 1024 * 1024) {
                                                    notification.error({ message: 'Image must be smaller than 10 MB', placement: 'topRight' })
                                                    return Upload.LIST_IGNORE
                                                }
                                                setUploadingAvatar(true)
                                                setPhotoSaved(false)
                                                try {
                                                    const url = await uploadProfileImage(file, 'profile-pictures')
                                                    setAvatarUrl(url)
                                                    form.setFieldValue('profilePictureUrl', url)
                                                    setPhotoSaved(true)
                                                    notification.success({
                                                        message: 'Photo uploaded!',
                                                        description: 'Click "Save Changes" below to apply it to your profile.',
                                                        placement: 'topRight',
                                                        duration: 6,
                                                    })
                                                } catch (e) {
                                                    notification.error({
                                                        message: 'Upload failed',
                                                        description: e instanceof Error ? e.message : 'Could not upload photo. Please try again.',
                                                        placement: 'topRight',
                                                        duration: 8,
                                                    })
                                                } finally {
                                                    setUploadingAvatar(false)
                                                }
                                                return false
                                            }}
                                            fileList={[] as UploadFile[]}
                                        >
                                            <Button
                                                icon={<CameraOutlined />}
                                                loading={uploadingAvatar}
                                                style={{ borderRadius: 8 }}
                                            >
                                                {avatarUrl ? 'Change photo' : 'Upload photo'}
                                            </Button>
                                        </Upload>
                                    </div>
                                    {photoSaved && (
                                        <div style={{
                                            marginTop: 10,
                                            padding: '8px 12px',
                                            background: '#162312',
                                            border: '1px solid #274916',
                                            borderRadius: 8,
                                            color: '#95de64',
                                            fontSize: 12,
                                        }}>
                                            ✓ Photo ready — click <strong>Save Changes</strong> below to apply it.
                                        </div>
                                    )}
                                </Form.Item>
                            </div>
                        </div>

                        {/* Step 2: Social Media */}
                        <div style={{ display: current === 1 ? 'block' : 'none' }}>
                            <div style={{ padding: '20px 24px', background: '#111', borderRadius: 12, border: '1px solid #1a1a1a' }}>
                                <Text style={{ color: PRIMARY, fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 8 }}>
                                    Connect Your Accounts
                                </Text>
                                <Text style={{ color: '#555', fontSize: 12, display: 'block', marginBottom: 20 }}>
                                    At least one handle is required to showcase your presence.
                                </Text>

                                <div style={{ marginBottom: 20 }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                                        <InstagramOutlined style={{ color: '#E1306C', fontSize: 18 }} />
                                        <Text style={{ color: '#fff', fontWeight: 500 }}>Instagram</Text>
                                    </div>
                                    <Form.Item name="instagramHandle" style={{ marginBottom: 0 }}>
                                        <Input addonBefore="@" placeholder="your_handle" />
                                    </Form.Item>
                                </div>

                                <div style={{ marginBottom: 20 }}>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                                        <YoutubeOutlined style={{ color: '#FF0000', fontSize: 18 }} />
                                        <Text style={{ color: '#fff', fontWeight: 500 }}>YouTube</Text>
                                    </div>
                                    <Form.Item name="youtubeHandle" style={{ marginBottom: 0 }}>
                                        <Input addonBefore="@" placeholder="your_channel" />
                                    </Form.Item>
                                </div>

                                <div>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                                        <TiktokSvg />
                                        <Text style={{ color: '#fff', fontWeight: 500 }}>TikTok</Text>
                                    </div>
                                    <Form.Item name="tiktokHandle" style={{ marginBottom: 0 }}>
                                        <Input addonBefore="@" placeholder="your_handle" />
                                    </Form.Item>
                                </div>
                            </div>

                            <div style={{ marginTop: 16, padding: '12px 16px', background: `${PRIMARY}10`, borderRadius: 8, border: `1px solid ${PRIMARY}20` }}>
                                <CheckCircleFilled style={{ color: PRIMARY, marginRight: 8 }} />
                                <Text style={{ color: '#aaa', fontSize: 12 }}>Adding a social account makes you visible and builds trust with brands.</Text>
                            </div>
                        </div>

                        {/* Step 3: Pricing */}
                        <div style={{ display: current === 2 ? 'block' : 'none' }}>
                            <div style={{ padding: '20px 24px', background: '#111', borderRadius: 12, border: '1px solid #1a1a1a' }}>
                                <Text style={{ color: PRIMARY, fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 16 }}>
                                    Your Rates
                                </Text>
                                <Form.Item
                                    name="rate"
                                    label="Rate (per post/collab)"
                                    rules={[
                                        { required: true, message: 'Rate is required to complete profile' },
                                        { type: 'number', min: 0, message: 'Rate must be 0 or greater' },
                                    ]}
                                >
                                    <InputNumber
                                        prefix="$"
                                        placeholder="0"
                                        style={{ width: '100%' }}
                                        min={0}
                                        precision={2}
                                    />
                                </Form.Item>
                            </div>

                            <div style={{ padding: '20px 24px', background: '#111', borderRadius: 12, border: '1px solid #1a1a1a', marginTop: 16 }}>
                                <Text style={{ color: '#666', fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 16 }}>
                                    Audience Details (Optional)
                                </Text>
                                <Form.Item name="followerCount" label="Follower count">
                                    <InputNumber placeholder="e.g. 50000" style={{ width: '100%' }} min={0} />
                                </Form.Item>
                                <Form.Item name="engagementRate" label="Engagement rate %">
                                    <InputNumber placeholder="e.g. 3.5" style={{ width: '100%' }} min={0} max={100} step={0.1} />
                                </Form.Item>
                                <Form.Item name="audienceInfo" label="Audience info">
                                    <TextArea rows={4} placeholder="e.g. Demographics, engagement metrics, reach..." />
                                </Form.Item>
                            </div>
                        </div>

                        {/* Action Buttons */}
                        <div style={{ display: 'flex', gap: 12, marginTop: 28 }}>
                            {current > 0 && (
                                <Button size="large" onClick={() => setCurrent(current - 1)} style={{ borderRadius: 10 }}>
                                    Back
                                </Button>
                            )}
                            <div style={{ flex: 1 }} />
                            <Button size="large" loading={loading} onClick={handleSaveDraft} style={{ borderRadius: 10 }}>
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
                                            if (current === 1 && !hasSocialHandle(form.getFieldsValue())) {
                                                notification.error({ message: 'At least one social media handle is required', placement: 'topRight' })
                                                return
                                            }
                                            setCurrent(current + 1)
                                        } catch {
                                            /* validation failed */
                                        }
                                    }}
                                    style={{ color: '#000', fontWeight: 600, borderRadius: 10 }}
                                >
                                    Next
                                </Button>
                            ) : (
                                <Button
                                    type="primary"
                                    size="large"
                                    loading={loading}
                                    onClick={handleComplete}
                                    style={{ color: '#000', fontWeight: 600, borderRadius: 10 }}
                                >
                                    {isEdit ? 'Save Changes' : 'Complete profile'}
                                </Button>
                            )}
                        </div>
                    </Form>
                </div>
            </div>
        </ConfigProvider>
    )
}
