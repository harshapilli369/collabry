import { useState } from 'react'
import { Form, Input, Button, Checkbox, Typography, Divider, ConfigProvider } from 'antd'
import { MailOutlined, LockOutlined, GoogleOutlined } from '@ant-design/icons'

const { Title, Text, Link } = Typography

export const Login = () => {
    const [loading, setLoading] = useState(false)

    const onFinish = (values: any) => {
        setLoading(true)
        console.log('Received values of form: ', values)
        setTimeout(() => setLoading(false), 2000)
    }

    // Custom Mint/Teal color from the design
    const primaryColor = '#8CCAC1'

    return (
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: primaryColor,
                    borderRadius: 8,
                    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
                },
                components: {
                    Button: {
                        colorPrimary: primaryColor,
                        algorithm: true, // Enable derivative colors
                        primaryShadow: 'none',
                    },
                    Input: {
                        paddingBlock: 10, // Taller inputs
                    }
                }
            }}
        >
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '100vh',
                backgroundColor: '#fff'
            }}>
                <div style={{ width: '100%', maxWidth: 400, padding: 20 }}>

                    {/* Logo Section */}
                    <div style={{ textAlign: 'center', marginBottom: 40 }}>
                        <div style={{ marginBottom: 20 }}>
                            <img src="/logo.png" alt="Collabry Logo" style={{ height: 60 }} />
                        </div>

                        <Title level={2} style={{ margin: '0 0 8px' }}>Welcome Back</Title>
                        <Text type="secondary">Log in to your account to continue</Text>
                    </div>

                    {/* Form Section */}
                    <Form
                        name="login"
                        initialValues={{ remember: true }}
                        onFinish={onFinish}
                        layout="vertical"
                        size="large"
                    >
                        <Form.Item
                            name="email"
                            rules={[{ required: true, message: 'Please input your Email!' }]}
                        >
                            <Input
                                prefix={<MailOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="Email address"
                            />
                        </Form.Item>

                        <Form.Item
                            name="password"
                            rules={[{ required: true, message: 'Please input your Password!' }]}
                        >
                            <Input.Password
                                prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="Password"
                            />
                        </Form.Item>

                        <Form.Item>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <Form.Item name="remember" valuePropName="checked" noStyle>
                                    <Checkbox style={{ color: 'rgba(0,0,0,0.5)' }}>Remember me</Checkbox>
                                </Form.Item>
                                <Link style={{ color: primaryColor, fontWeight: 500 }}>
                                    Forgot password?
                                </Link>
                            </div>
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
                                    backgroundColor: '#98D0C9', // Slightly lighter/custom shade for the big button
                                    borderColor: '#98D0C9',
                                    color: 'white'
                                }}
                            >
                                Log In
                            </Button>
                        </Form.Item>
                    </Form>

                    <Divider style={{ color: 'rgba(0,0,0,0.4)', fontSize: 12 }}>OR</Divider>

                    <Button
                        block
                        size="large"
                        icon={<GoogleOutlined style={{ color: '#0F9D58' }} />} // Google Color
                        style={{
                            height: 50,
                            fontWeight: 500,
                            color: '#4a4a4a',
                            borderColor: '#eee',
                            backgroundColor: '#fafafa'
                        }}
                    >
                        Continue with Google
                    </Button>

                    <div style={{ textAlign: 'center', marginTop: 30 }}>
                        <Text style={{ color: 'rgba(0,0,0,0.5)' }}>Don't have an account? </Text>
                        <Link style={{ color: '#2EB5A0', fontWeight: 500 }}>Sign up</Link>
                    </div>
                </div>
            </div>
        </ConfigProvider>
    )
}
