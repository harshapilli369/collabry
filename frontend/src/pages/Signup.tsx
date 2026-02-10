import { useState } from 'react'
import { Form, Input, Button, Radio, Typography, ConfigProvider } from 'antd'
import { MailOutlined, LockOutlined } from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { registerUser } from '../services/authService'

const { Title, Text } = Typography

const PASSWORD_RULES = [
    { required: true, message: 'Please input your password!' },
    { min: 8, message: 'Password must be at least 8 characters' },
    {
        pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/,
        message: 'Password must contain at least one letter and one digit',
    },
]

export const Signup = () => {
    const [loading, setLoading] = useState(false)
    const navigate = useNavigate()

    const onFinish = async (values: { email: string; password: string; role: 'BRAND' | 'INFLUENCER' }) => {
        setLoading(true)
        try {
            const data = await registerUser({
                email: values.email,
                password: values.password,
                role: values.role,
            })
            localStorage.setItem('token', data.token)
            localStorage.setItem('user', JSON.stringify({ email: data.email, role: data.role }))
            navigate('/profile-setup')
        } catch (err) {
            const message = err instanceof Error ? err.message : 'Registration failed'
            alert(message)
        } finally {
            setLoading(false)
        }
    }

    const primaryColor = '#FFFD82'
    const secondaryColor = '#BD72EB'
    const textColor = '#000000'
    const pageBackgroundColor = '#1E1E1E'
    const cardBackgroundColor = '#FFFFFF'

    return (
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: primaryColor,
                    colorText: textColor,
                    borderRadius: 8,
                    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
                },
                components: {
                    Button: {
                        colorPrimary: primaryColor,
                        algorithm: true,
                        primaryShadow: 'none',
                        colorTextLightSolid: textColor,
                    },
                    Input: {
                        paddingBlock: 10,
                    },
                },
            }}
        >
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '100vh',
                backgroundColor: pageBackgroundColor,
            }}>
                <div style={{
                    width: '100%',
                    maxWidth: 400,
                    padding: 40,
                    backgroundColor: cardBackgroundColor,
                    borderRadius: 16,
                    boxShadow: '0 4px 20px rgba(0,0,0,0.3)',
                }}>
                    <div style={{ textAlign: 'center', marginBottom: 30 }}>
                        <div style={{ marginBottom: 20 }}>
                            <img src="/logo.png" alt="Collabry Logo" style={{ height: 60 }} />
                        </div>
                        <Title level={2} style={{ margin: '0 0 8px', color: textColor }}>Collabry</Title>
                        <Text type="secondary">Create your account</Text>
                    </div>

                    <Form
                        name="signup"
                        onFinish={onFinish}
                        layout="vertical"
                        size="large"
                        initialValues={{ role: 'BRAND' }}
                    >
                        <Form.Item
                            name="role"
                            label="I am a"
                            rules={[{ required: true, message: 'Please choose your role' }]}
                        >
                            <Radio.Group>
                                <Radio value="BRAND">Brand</Radio>
                                <Radio value="INFLUENCER">Influencer</Radio>
                            </Radio.Group>
                        </Form.Item>

                        <Form.Item
                            name="email"
                            label="Email"
                            rules={[
                                { required: true, message: 'Please input your email!' },
                                { type: 'email', message: 'Please enter a valid email' },
                            ]}
                        >
                            <Input
                                prefix={<MailOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="Email address"
                            />
                        </Form.Item>

                        <Form.Item
                            name="password"
                            label="Password"
                            rules={PASSWORD_RULES}
                        >
                            <Input.Password
                                prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="At least 8 characters, one letter and one digit"
                            />
                        </Form.Item>

                        <Form.Item
                            name="confirmPassword"
                            label="Confirm password"
                            dependencies={['password']}
                            rules={[
                                { required: true, message: 'Please confirm your password!' },
                                ({ getFieldValue }) => ({
                                    validator(_, value) {
                                        if (!value || getFieldValue('password') === value) {
                                            return Promise.resolve()
                                        }
                                        return Promise.reject(new Error('Passwords do not match'))
                                    },
                                }),
                            ]}
                        >
                            <Input.Password
                                prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="Confirm password"
                            />
                        </Form.Item>

                        <Form.Item>
                            <Button
                                type="primary"
                                htmlType="submit"
                                block
                                loading={loading}
                                style={{ height: 50, fontWeight: 600, fontSize: 16, color: textColor }}
                            >
                                Sign up
                            </Button>
                        </Form.Item>
                    </Form>

                    <div style={{ textAlign: 'center', marginTop: 24 }}>
                        <Text style={{ color: 'rgba(0,0,0,0.5)' }}>Already have an account? </Text>
                        <Link to="/login" style={{ color: secondaryColor, fontWeight: 500 }}>Log in</Link>
                    </div>
                </div>
            </div>
        </ConfigProvider>
    )
}
