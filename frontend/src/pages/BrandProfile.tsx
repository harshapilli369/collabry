import { useState, useEffect } from 'react'
import { Form, Input, Button, Typography, Select, Modal, Upload, Avatar, notification } from 'antd'
import { ArrowLeftOutlined, InstagramOutlined, LinkedinOutlined, TwitterOutlined, CheckCircleFilled, GlobalOutlined, CameraOutlined, LoadingOutlined } from '@ant-design/icons'
import type { UploadFile, RcFile } from 'antd/es/upload/interface'
import { useNavigate } from 'react-router-dom'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'
import {
    getMyBrandProfile,
    updateMyBrandProfile,
    BUDGET_RANGE_OPTIONS,
    type BrandProfileRequest,
    type BrandProfileResponse,
} from '../services/brandService'
import { INDUSTRY_NICHE_OPTIONS } from '../constants/profileOptions'
import { uploadProfileImage } from '../services/imageUploadService'
import { userService } from '../services/userService'

const { Title, Text } = Typography
const { TextArea } = Input

const PRIMARY = BRAND_PORTAL_PRIMARY


export const BrandProfile = () => {
    const [form] = Form.useForm<BrandProfileRequest>()
    const [loading, setLoading] = useState(false)
    const [fetching, setFetching] = useState(true)
    const [isEdit, setIsEdit] = useState(false)
    const [logoUrl, setLogoUrl] = useState<string | undefined>(undefined)
    const [uploadingLogo, setUploadingLogo] = useState(false)
    const [logoSaved, setLogoSaved] = useState(false)
    const navigate = useNavigate()

    useEffect(() => {
        getMyBrandProfile()
            .then((profile: BrandProfileResponse | null) => {
                if (profile) {
                    setIsEdit(true)
                    setLogoUrl(profile.logoUrl ?? undefined)
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
            .catch(() => notification.error({ message: 'Failed to load profile', placement: 'topRight' }))
            .finally(() => setFetching(false))
    }, [form])

    const onFinish = async (values: BrandProfileRequest) => {
        setLoading(true)
        try {
            await updateMyBrandProfile(values)
            setLogoSaved(false)
            notification.success({ message: 'Profile saved successfully', placement: 'topRight', duration: 4 })
            setTimeout(() => {
                navigate('/brand/profile', { replace: true })
            }, 300)
        } catch (e) {
            const msg = e instanceof Error ? e.message : 'Failed to save profile'
            notification.error({ message: msg, placement: 'topRight' })
            setLoading(false)
        }
    }

    const platformLabels: Record<string, string> = { INSTAGRAM: 'Instagram', LINKEDIN: 'LinkedIn', TWITTER: 'Twitter / X' }

    const handleConnect = async (platform: string) => {
        const fieldMap: Record<string, string> = { INSTAGRAM: 'instagramUrl', LINKEDIN: 'linkedInUrl', TWITTER: 'twitterUrl' }
        const handle = (form.getFieldValue(fieldMap[platform] as any) as string | undefined)?.trim()
        const label = platformLabels[platform] || platform

        if (!handle) {
            Modal.warning({
                title: 'URL Required',
                content: `Please enter your ${label} URL before connecting.`,
                okText: 'Got it',
                centered: true,
            })
            return
        }
        try {
            await userService.linkSocialAccount(platform, handle)
            Modal.success({
                title: `${label} Connected!`,
                content: `Your ${label} account has been linked successfully. Remember to click "Save Changes" to keep your links.`,
                okText: 'Awesome',
                centered: true,
            })
        } catch {
            Modal.error({
                title: 'Connection Failed',
                content: `We couldn't connect your ${label} account. Please try again.`,
                okText: 'OK',
                centered: true,
            })
        }
    }

    return (
        <BrandPortalLayout activeMenuKey="profile">
            <Button
                type="link"
                icon={<ArrowLeftOutlined />}
                onClick={() => navigate('/brand/profile')}
                style={{ color: PRIMARY, paddingLeft: 0, marginBottom: 16 }}
            >
                Back to Profile
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
                        <GlobalOutlined style={{ fontSize: 24, color: '#000' }} />
                    </div>
                    <Title level={2} style={{ margin: 0, color: '#fff' }}>
                        {isEdit ? 'Edit your brand profile' : 'Complete your brand profile'}
                    </Title>
                    <Text style={{ color: '#666', fontSize: 14, marginTop: 8, display: 'block' }}>
                        This is visible to influencers you contact.
                    </Text>
                </div>

                <Form form={form} layout="vertical" onFinish={onFinish} disabled={fetching} size="large">
                    {/* Required Section */}
                    <div style={{ padding: '20px 24px', background: '#0d0d0d', borderRadius: 12, border: '1px solid #1a1a1a', marginBottom: 16 }}>
                        <Text style={{ color: PRIMARY, fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 16 }}>
                            Company Information
                        </Text>
                        <Form.Item name="name" label="Company name" rules={[{ required: true, message: 'Company name is required' }]}>
                            <Input placeholder="Your company or brand name" />
                        </Form.Item>
                        <Form.Item name="industry" label="Industry" rules={[{ required: true, message: 'Industry is required' }]}>
                            <Select
                                showSearch
                                placeholder="Search or select your industry"
                                optionFilterProp="label"
                                options={INDUSTRY_NICHE_OPTIONS}
                                allowClear
                            />
                        </Form.Item>
                        <Form.Item name="website" label="Website" rules={[{ required: true, message: 'Website is required' }, { type: 'url', message: 'Enter a valid URL' }]}>
                            <Input placeholder="https://www.example.com" />
                        </Form.Item>
                        <Form.Item name="email" label="Email" rules={[{ required: true, message: 'Email is required' }, { type: 'email', message: 'Enter a valid email' }]}>
                            <Input placeholder="contact@company.com" />
                        </Form.Item>
                    </div>

                    {/* Optional Section */}
                    <div style={{ padding: '20px 24px', background: '#0d0d0d', borderRadius: 12, border: '1px solid #1a1a1a', marginBottom: 16 }}>
                        <Text style={{ color: '#666', fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 16 }}>
                            Brand Details
                        </Text>
                        <Form.Item name="logoUrl" label="Company logo">
                            <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                                <div style={{ position: 'relative', flexShrink: 0 }}>
                                    <Avatar
                                        size={72}
                                        src={logoUrl}
                                        icon={<GlobalOutlined />}
                                        style={{ background: '#1a1a1a', border: `2px solid ${PRIMARY}30`, borderRadius: 12 }}
                                    />
                                    {uploadingLogo && (
                                        <div style={{
                                            position: 'absolute', inset: 0, borderRadius: 12,
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
                                            notification.error({ message: 'Logo must be smaller than 10 MB', placement: 'topRight' })
                                            return Upload.LIST_IGNORE
                                        }
                                        setUploadingLogo(true)
                                        setLogoSaved(false)
                                        try {
                                            const url = await uploadProfileImage(file, 'brand-logos')
                                            setLogoUrl(url)
                                            form.setFieldValue('logoUrl', url)
                                            setLogoSaved(true)
                                            notification.success({
                                                message: 'Logo uploaded!',
                                                description: 'Click "Save Changes" below to apply it to your profile.',
                                                placement: 'topRight',
                                                duration: 6,
                                            })
                                        } catch (e) {
                                            notification.error({
                                                message: 'Upload failed',
                                                description: e instanceof Error ? e.message : 'Could not upload logo. Please try again.',
                                                placement: 'topRight',
                                                duration: 8,
                                            })
                                        } finally {
                                            setUploadingLogo(false)
                                        }
                                        return false
                                    }}
                                    fileList={[] as UploadFile[]}
                                >
                                    <Button
                                        icon={<CameraOutlined />}
                                        loading={uploadingLogo}
                                        style={{ borderRadius: 8 }}
                                    >
                                        {logoUrl ? 'Change logo' : 'Upload logo'}
                                    </Button>
                                </Upload>
                            </div>
                            {logoSaved && (
                                <div style={{
                                    marginTop: 10,
                                    padding: '8px 12px',
                                    background: '#162312',
                                    border: '1px solid #274916',
                                    borderRadius: 8,
                                    color: '#95de64',
                                    fontSize: 12,
                                }}>
                                    ✓ Logo ready — click <strong>Save Changes</strong> below to apply it.
                                </div>
                            )}
                        </Form.Item>
                        <Form.Item name="description" label="Description">
                            <TextArea rows={4} placeholder="Tell influencers about your brand and campaigns" />
                        </Form.Item>
                        <Form.Item name="budgetRange" label="Budget range">
                            <Select placeholder="Select your typical campaign budget range" allowClear options={BUDGET_RANGE_OPTIONS} />
                        </Form.Item>
                    </div>

                    {/* Social Section */}
                    <div style={{ padding: '20px 24px', background: '#0d0d0d', borderRadius: 12, border: '1px solid #1a1a1a', marginBottom: 16 }}>
                        <Text style={{ color: PRIMARY, fontSize: 13, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1, display: 'block', marginBottom: 8 }}>
                            Social Accounts
                        </Text>
                        <Text style={{ color: '#555', fontSize: 12, display: 'block', marginBottom: 20 }}>
                            Connect your brand's official social media pages.
                        </Text>

                        <div style={{ marginBottom: 16 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                                <InstagramOutlined style={{ color: '#E1306C', fontSize: 18 }} />
                                <Text style={{ color: '#fff', fontWeight: 500 }}>Instagram</Text>
                            </div>
                            <Form.Item name="instagramUrl" style={{ marginBottom: 0 }}>
                                <Input.Search
                                    placeholder="https://instagram.com/yourbrand"
                                    enterButton={<Button type="primary" style={{ color: '#000', fontWeight: 600 }}>Connect</Button>}
                                    onSearch={() => handleConnect('INSTAGRAM')}
                                />
                            </Form.Item>
                        </div>

                        <div style={{ marginBottom: 16 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                                <LinkedinOutlined style={{ color: '#0077B5', fontSize: 18 }} />
                                <Text style={{ color: '#fff', fontWeight: 500 }}>LinkedIn</Text>
                            </div>
                            <Form.Item name="linkedInUrl" style={{ marginBottom: 0 }}>
                                <Input.Search
                                    placeholder="https://linkedin.com/company/yourbrand"
                                    enterButton={<Button type="primary" style={{ color: '#000', fontWeight: 600 }}>Connect</Button>}
                                    onSearch={() => handleConnect('LINKEDIN')}
                                />
                            </Form.Item>
                        </div>

                        <div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
                                <TwitterOutlined style={{ color: '#1DA1F2', fontSize: 18 }} />
                                <Text style={{ color: '#fff', fontWeight: 500 }}>Twitter / X</Text>
                            </div>
                            <Form.Item name="twitterUrl" style={{ marginBottom: 0 }}>
                                <Input.Search
                                    placeholder="https://twitter.com/yourbrand"
                                    enterButton={<Button type="primary" style={{ color: '#000', fontWeight: 600 }}>Connect</Button>}
                                    onSearch={() => handleConnect('TWITTER')}
                                />
                            </Form.Item>
                        </div>
                    </div>

                    <div style={{ marginTop: 16, padding: '12px 16px', background: `${PRIMARY}10`, borderRadius: 8, border: `1px solid ${PRIMARY}20`, marginBottom: 24 }}>
                        <CheckCircleFilled style={{ color: PRIMARY, marginRight: 8 }} />
                        <Text style={{ color: '#aaa', fontSize: 12 }}>Connecting a social account builds trust with influencers.</Text>
                    </div>

                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={loading} size="large" style={{ color: '#000', fontWeight: 600, borderRadius: 10, width: '100%' }}>
                            {isEdit ? 'Save Changes' : 'Complete profile'}
                        </Button>
                    </Form.Item>
                </Form>
            </div>
        </BrandPortalLayout>
    )
}
