import { useState } from 'react'
import { Form, Input, Button, Radio, Typography, ConfigProvider, theme, message } from 'antd'
import { MailOutlined, LockOutlined, UserOutlined, RocketOutlined } from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { registerUser } from '../services/authService'

const { Title, Text } = Typography

const PRIMARY = '#FFFD82'
const PURPLE = '#BD72EB'

const PASSWORD_RULES = [
    { required: true, message: 'Please input your password!' },
    { min: 8, message: 'Password must be at least 8 characters' },
    {
        pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/,
        message: 'Password must contain at least one letter and one digit',
    },
]

const getSignupErrorMessage = (err: unknown) => {
    const msg = err instanceof Error ? err.message : 'Registration failed'
    return /SMTP|send confirmation email/i.test(msg)
        ? 'Could not send confirmation email. Check backend SMTP config.'
        : msg
}

const makeConfirmPasswordRule = (getFieldValue: (name: string) => string) => ({
    validator(_: unknown, value: string) {
        if (!value || getFieldValue('password') === value) return Promise.resolve()
        return Promise.reject(new Error('Passwords do not match'))
    },
})

export const Signup = () => {
    const [loading, setLoading] = useState(false)
    const [form] = Form.useForm()
    const navigate = useNavigate()

    const onFinish = async (values: { email: string; password: string; role: 'BRAND' | 'INFLUENCER' }) => {
        setLoading(true)
        try {
            const data = await registerUser({ email: values.email, password: values.password, role: values.role })
            message.success(data.message || 'Account created! Check your email to confirm.')
            navigate('/login')
        } catch (err) {
            const errorMessage = getSignupErrorMessage(err)
            const isDuplicateEmail = /already exists/i.test(errorMessage)
            if (isDuplicateEmail) {
                form.setFields([{ name: 'email', errors: [errorMessage] }])
            } else {
                message.error(errorMessage)
            }
        } finally {
            setLoading(false)
        }
    }

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
                    alignItems: 'center',
                    minHeight: '100vh',
                    backgroundColor: '#000',
                    position: 'relative',
                    overflow: 'hidden',
                }}
            >
                {/* Background glows */}
                <div style={{ position: 'absolute', top: '30%', right: '20%', width: 400, height: 400, borderRadius: '50%', background: `radial-gradient(circle, ${PRIMARY}08 0%, transparent 70%)`, pointerEvents: 'none' }} />
                <div style={{ position: 'absolute', bottom: '10%', left: '15%', width: 300, height: 300, borderRadius: '50%', background: `radial-gradient(circle, ${PURPLE}08 0%, transparent 70%)`, pointerEvents: 'none' }} />

                <div
                    style={{
                        width: '100%',
                        maxWidth: 420,
                        padding: '44px 40px',
                        backgroundColor: '#0d0d0d',
                        borderRadius: 20,
                        border: `1px solid ${PURPLE}10`,
                        boxShadow: '0 8px 40px rgba(0,0,0,0.4)',
                        position: 'relative',
                        zIndex: 1,
                    }}
                >
                    {/* Logo */}
                    <div style={{ textAlign: 'center', marginBottom: 32 }}>
                        <div
                            onClick={() => navigate('/')}
                            style={{
                                width: 52,
                                height: 52,
                                borderRadius: 14,
                                background: `linear-gradient(135deg, ${PRIMARY}, ${PURPLE})`,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                margin: '0 auto 16px',
                                fontWeight: 900,
                                fontSize: 24,
                                color: '#000',
                                cursor: 'pointer',
                            }}
                        >
                            C
                        </div>
                        <Title level={2} style={{ margin: '0 0 8px', color: '#fff' }}>Create your account</Title>
                        <Text style={{ color: '#666' }}>Join Collabry as a Brand or Influencer</Text>
                    </div>

                    <Form form={form} name="signup" onFinish={onFinish} layout="vertical" size="large" initialValues={{ role: 'BRAND' }}>
                        <Form.Item name="role" label="I am a" rules={[{ required: true, message: 'Please choose your role' }]}>
                            <Radio.Group style={{ width: '100%', display: 'flex', gap: 12 }}>
                                <Radio.Button value="BRAND" style={{ flex: 1, textAlign: 'center', height: 44, lineHeight: '44px', borderRadius: 10 }}>
                                    <RocketOutlined /> Brand
                                </Radio.Button>
                                <Radio.Button value="INFLUENCER" style={{ flex: 1, textAlign: 'center', height: 44, lineHeight: '44px', borderRadius: 10 }}>
                                    <UserOutlined /> Influencer
                                </Radio.Button>
                            </Radio.Group>
                        </Form.Item>

                        <Form.Item name="email" label="Email" rules={[{ required: true, message: 'Please input your email!' }, { type: 'email', message: 'Please enter a valid email' }]}>
                            <Input prefix={<MailOutlined style={{ color: '#555' }} />} placeholder="Email address" />
                        </Form.Item>

                        <Form.Item name="password" label="Password" rules={PASSWORD_RULES}>
                            <Input.Password prefix={<LockOutlined style={{ color: '#555' }} />} placeholder="At least 8 characters, one letter and one digit" />
                        </Form.Item>

                        <Form.Item
                            name="confirmPassword"
                            label="Confirm password"
                            dependencies={['password']}
                            rules={[
                                { required: true, message: 'Please confirm your password!' },
                                ({ getFieldValue }) => makeConfirmPasswordRule(getFieldValue),
                            ]}
                        >
                            <Input.Password prefix={<LockOutlined style={{ color: '#555' }} />} placeholder="Confirm password" />
                        </Form.Item>

                        <Form.Item>
                            <Button type="primary" htmlType="submit" block loading={loading} style={{ height: 50, fontWeight: 700, fontSize: 16, color: '#000', borderRadius: 12 }}>
                                Sign Up
                            </Button>
                        </Form.Item>
                    </Form>

                    <div style={{ textAlign: 'center', marginTop: 24 }}>
                        <Text style={{ color: '#555' }}>Already have an account? </Text>
                        <Link to="/login" style={{ color: PURPLE, fontWeight: 600 }}>Log in</Link>
                    </div>
                </div>
            </div>
        </ConfigProvider>
    )
}
