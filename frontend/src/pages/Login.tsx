import { useState } from 'react'
import { Form, Input, Button, Checkbox, Typography, Divider, ConfigProvider, message, theme, Radio } from 'antd'
import { MailOutlined, LockOutlined, GoogleOutlined, RocketOutlined, UserOutlined } from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { useGoogleLogin } from '@react-oauth/google'
import { loginUser, googleLoginUser } from '../services/authService'
import { getMyInfluencerProfile } from '../services/influencerProfileService'

const { Title, Text } = Typography

const PRIMARY_COLOR = '#FFFD82'
const SECONDARY_COLOR = '#BD72EB'
const TEXT_COLOR = '#ffffff'
const PAGE_BG = '#000000'
const CARD_BG = '#141414'

export const Login = () => {
    const [loading, setLoading] = useState(false)
    const [googleRole, setGoogleRole] = useState<'BRAND' | 'INFLUENCER'>('BRAND')
    const [form] = Form.useForm()
    const navigate = useNavigate()

    const navigateAfterLogin = async (role: string) => {
        if (role === 'INFLUENCER') {
            let profile = null
            try {
                profile = await getMyInfluencerProfile()
            } catch {
                // Profile load failed — direct to profile setup
            }
            const dest = profile?.complete ? '/influencer/dashboard' : '/influencer/profile/edit'
            navigate(dest, { replace: true })
        } else if (role === 'BRAND') {
            navigate('/brand/dashboard', { replace: true })
        } else if (role === 'ADMIN') {
            navigate('/admin/dashboard', { replace: true })
        } else {
            navigate('/', { replace: true })
        }
    }

    const saveUserToStorage = (data: { token: string; id: unknown; email: string; role: string; isVerified: boolean }) => {
        localStorage.setItem('token', data.token)
        localStorage.setItem('user', JSON.stringify({ id: data.id, email: data.email, role: data.role, isVerified: data.isVerified }))
    }

    const submitLogin = async (values: { email?: string; password?: string; rememberMe?: boolean }) => {
        setLoading(true)
        try {
            const payload = { email: values.email ?? '', password: values.password ?? '', rememberMe: values.rememberMe ?? false }
            const data = await loginUser(payload)
            if (!data?.token) {
                message.error('Invalid response from server')
                alert('Invalid response from server')
                return
            }
            saveUserToStorage(data)
            message.success('Login successful!')
            alert('Login successful!')
            await navigateAfterLogin(data.role)
        } catch (error) {
            const msg = error instanceof Error ? error.message : 'Login failed'
            message.error(msg)
            alert(msg)
        } finally {
            setLoading(false)
        }
    }

    const googleLogin = useGoogleLogin({
        onSuccess: async (tokenResponse) => {
            try {
                const data = await googleLoginUser(tokenResponse.access_token, googleRole)
                saveUserToStorage(data)
                alert('Google Login Successful! Redirecting...')
                await navigateAfterLogin(data.role)
            } catch (err) {
                console.error('Google Backend Error', err)
                alert('Google Login Failed on Backend')
            }
        },
        onError: () => console.log('Google Login Failed'),
    })

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: {
                    colorPrimary: PRIMARY_COLOR,
                    colorText: TEXT_COLOR,
                    borderRadius: 8,
                    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
                },
                components: { Input: { paddingBlock: 10 } }
            }}
        >
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: PAGE_BG }}>
                <div style={{ width: '100%', maxWidth: 400, padding: 40, backgroundColor: CARD_BG, borderRadius: 16, boxShadow: '0 4px 20px rgba(0,0,0,0.3)' }}>

                    <div style={{ textAlign: 'center', marginBottom: 30 }}>
                        <div style={{ marginBottom: 20, cursor: 'pointer' }} onClick={() => navigate('/')}>
                            <div style={{ width: 64, height: 64, borderRadius: 16, background: 'linear-gradient(135deg, #FFFD82, #BD72EB)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto', fontWeight: 900, fontSize: 28, color: '#000' }}>
                                C
                            </div>
                        </div>
                        <Title level={2} style={{ margin: '0 0 8px', color: TEXT_COLOR, cursor: 'pointer' }} onClick={() => navigate('/')}>Collabry</Title>
                        <Text type="secondary">Log in to your account to continue</Text>
                    </div>

                    <Form form={form} name="login" initialValues={{ rememberMe: true }} onFinish={submitLogin} layout="vertical" size="large">
                        <Form.Item name="email" rules={[{ required: true, message: 'Please input your Email!' }]}>
                            <Input prefix={<MailOutlined style={{ color: 'rgba(0,0,0,.25)' }} />} placeholder="Email address" />
                        </Form.Item>

                        <Form.Item name="password" rules={[{ required: true, message: 'Please input your Password!' }]}>
                            <Input.Password prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />} placeholder="Password" />
                        </Form.Item>

                        <Form.Item>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Form.Item name="rememberMe" valuePropName="checked" noStyle>
                                    <Checkbox className="custom-checkbox">Remember me</Checkbox>
                                </Form.Item>
                                <Link to="/forgot-password" style={{ color: SECONDARY_COLOR, fontWeight: 500 }}>Forgot password?</Link>
                            </div>
                        </Form.Item>

                        <Form.Item>
                            <Button type="primary" htmlType="submit" block loading={loading} style={{ height: 50, fontWeight: 600, fontSize: 16, color: '#000000' }}>
                                Log In
                            </Button>
                        </Form.Item>
                    </Form>

                    <Divider style={{ color: 'rgba(0,0,0,0.4)', fontSize: 12 }}>OR</Divider>

                    <div style={{ marginBottom: 12 }}>
                        <Text style={{ color: '#888', fontSize: 12, display: 'block', marginBottom: 8 }}>Signing up with Google? Select your role:</Text>
                        <Radio.Group value={googleRole} onChange={e => setGoogleRole(e.target.value)} style={{ width: '100%', display: 'flex', gap: 8 }}>
                            <Radio.Button value="BRAND" style={{ flex: 1, textAlign: 'center', height: 36, lineHeight: '36px', borderRadius: 8 }}>
                                <RocketOutlined /> Brand
                            </Radio.Button>
                            <Radio.Button value="INFLUENCER" style={{ flex: 1, textAlign: 'center', height: 36, lineHeight: '36px', borderRadius: 8 }}>
                                <UserOutlined /> Influencer
                            </Radio.Button>
                        </Radio.Group>
                    </div>

                    <Button block size="large" icon={<GoogleOutlined style={{ color: '#000' }} />} onClick={() => googleLogin()} style={{ height: 50, fontWeight: 500, color: '#000000', borderColor: '#eee', backgroundColor: '#fff' }}>
                        Continue with Google
                    </Button>

                    <div style={{ textAlign: 'center', marginTop: 30 }}>
                        <Text style={{ color: TEXT_COLOR }}>Don't have an account? </Text>
                        <Link to="/signup" style={{ color: SECONDARY_COLOR, fontWeight: 500 }}>Sign up</Link>
                    </div>
                </div>
            </div>
        </ConfigProvider>
    )
}
