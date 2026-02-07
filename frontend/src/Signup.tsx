import { useState } from 'react'
import { Form, Input, Button, Typography, Radio, ConfigProvider } from 'antd'
import { MailOutlined, LockOutlined, UserOutlined, ShopOutlined } from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'

const { Title, Text } = Typography

type RoleType = 'INFLUENCER' | 'BRAND'

export const Signup = () => {
    const [loading, setLoading] = useState(false)
    const [role, setRole] = useState<RoleType>('INFLUENCER')
    const navigate = useNavigate()

    const onFinish = async (values: Record<string, string>) => {
        setLoading(true)
        try {
            const payload = {
                email: values.email,
                password: values.password,
                role,
                displayName: role === 'INFLUENCER' ? values.displayName : undefined,
                companyName: role === 'BRAND' ? values.companyName : undefined,
            }

            const response = await fetch('http://localhost:8080/api/auth/signup', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload),
            })

            const data = await response.json()

            if (response.ok) {
                localStorage.setItem('token', data.token)
                localStorage.setItem(
                    'user',
                    JSON.stringify({
                        email: data.email,
                        role: data.role,
                        displayName: data.displayName,
                        companyName: data.companyName,
                    })
                )
                alert('Sign up successful! Redirecting to login...')
                navigate('/login')
            } else {
                alert(data.message || data.error || 'Sign up failed. Please try again.')
            }
        } catch (error) {
            console.error('Signup error:', error)
            alert('Network error. Is the backend running?')
        } finally {
            setLoading(false)
        }
    }

    const primaryColor = '#FFF066'

    return (
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: primaryColor,
                    borderRadius: 8,
                    fontFamily:
                        'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
                },
                components: {
                    Button: {
                        colorPrimary: primaryColor,
                        algorithm: true,
                        primaryShadow: 'none',
                    },
                    Input: {
                        paddingBlock: 10,
                    },
                },
            }}
        >
            <div
                style={{
                    display: 'flex',
                    justifyContent: 'center',
                    alignItems: 'center',
                    minHeight: '100vh',
                    backgroundColor: '#1A1A1A',
                }}
            >
                <div style={{ width: '100%', maxWidth: 400, padding: 32, backgroundColor: '#252525', borderRadius: 16 }}>
                    <div style={{ textAlign: 'center', marginBottom: 40 }}>
                        <div style={{ marginBottom: 20 }}>
                            <img src="/logo.png" alt="Collabry Logo" style={{ height: 60 }} />
                        </div>
                        <Title level={2} style={{ margin: '0 0 8px', color: '#fff' }}>
                            Create Account
                        </Title>
                        <Text style={{ color: 'rgba(255,255,255,0.7)' }}>Sign up to get started with Collabry</Text>
                    </div>

                    <Form
                        name="signup"
                        onFinish={onFinish}
                        layout="vertical"
                        size="large"
                    >
                        <Form.Item label="I am a">
                            <Radio.Group
                                value={role}
                                onChange={(e) => setRole(e.target.value)}
                                style={{ width: '100%' }}
                            >
                                <Radio.Button value="INFLUENCER" style={{ flex: 1 }}>
                                    Influencer
                                </Radio.Button>
                                <Radio.Button value="BRAND" style={{ flex: 1 }}>
                                    Brand
                                </Radio.Button>
                            </Radio.Group>
                        </Form.Item>

                        {role === 'INFLUENCER' && (
                            <Form.Item
                                name="displayName"
                                label="Display Name"
                                rules={[{ required: true, message: 'Please input your display name!' }]}
                            >
                                <Input
                                    prefix={<UserOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                    placeholder="Display name"
                                />
                            </Form.Item>
                        )}

                        {role === 'BRAND' && (
                            <Form.Item
                                name="companyName"
                                label="Company Name"
                                rules={[{ required: true, message: 'Please input your company name!' }]}
                            >
                                <Input
                                    prefix={<ShopOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                    placeholder="Company name"
                                />
                            </Form.Item>
                        )}

                        <Form.Item
                            name="email"
                            label="Email"
                            rules={[
                                { required: true, message: 'Please input your email!' },
                                { type: 'email', message: 'Please enter a valid email!' },
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
                            rules={[
                                { required: true, message: 'Please input your password!' },
                                { min: 6, message: 'Password must be at least 6 characters!' },
                            ]}
                        >
                            <Input.Password
                                prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="Password (min 6 characters)"
                            />
                        </Form.Item>

                        <Form.Item
                            name="confirmPassword"
                            label="Confirm Password"
                            dependencies={['password']}
                            rules={[
                                { required: true, message: 'Please confirm your password!' },
                                ({ getFieldValue }) => ({
                                    validator(_, value) {
                                        if (!value || getFieldValue('password') === value) {
                                            return Promise.resolve()
                                        }
                                        return Promise.reject(new Error('Passwords do not match!'))
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
                                style={{
                                    height: 50,
                                    fontWeight: 500,
                                    fontSize: 16,
                                    backgroundColor: '#FFF066',
                                    borderColor: '#FFF066',
                                    color: '#1A1A1A',
                                }}
                            >
                                Sign Up
                            </Button>
                        </Form.Item>
                    </Form>

                    <div style={{ textAlign: 'center', marginTop: 30 }}>
                        <Text style={{ color: 'rgba(255,255,255,0.7)' }}>Already have an account? </Text>
                        <Link to="/login" style={{ color: '#FFF066', fontWeight: 500 }}>
                            Log in
                        </Link>
                    </div>
                </div>
            </div>
        </ConfigProvider>
    )
}
