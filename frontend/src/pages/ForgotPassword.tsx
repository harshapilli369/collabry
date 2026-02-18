import { useState } from 'react'
import { Form, Input, Button, Typography, ConfigProvider } from 'antd'
import { MailOutlined } from '@ant-design/icons'
import { Link } from 'react-router-dom'
import { forgotPassword } from '../services/authService'

const { Title, Text } = Typography

export const ForgotPassword = () => {
    const [loading, setLoading] = useState(false)

    const onFinish = async (values: any) => {
        setLoading(true)
        try {
            await forgotPassword(values.email)
            alert("Reset link sent! Check your backend console/email.")
        } catch (error) {
            console.error(error)
            const msg = error instanceof Error ? error.message : 'Failed to send reset link'
            alert(msg)
        } finally {
            setLoading(false)
        }
    }

    // Colors from User Palette (Synced with index.css)
    const primaryColor = '#FFFD82'; // Neon Yellow-Green
    const textColor = '#000000'; // Black (for inside the white card)
    const pageBackgroundColor = '#1E1E1E'; // Primary BG
    const cardBackgroundColor = '#FFFFFF'; // Pure White

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
                    }
                }
            }}
        >
            <div style={{
                display: 'flex',
                justifyContent: 'center',
                alignItems: 'center',
                minHeight: '100vh',
                backgroundColor: pageBackgroundColor
            }}>
                <div style={{
                    width: '100%',
                    maxWidth: 400,
                    padding: 40,
                    backgroundColor: cardBackgroundColor,
                    borderRadius: 16,
                    boxShadow: '0 4px 20px rgba(0,0,0,0.3)'
                }}>
                    <div style={{ textAlign: 'center', marginBottom: 30 }}>
                        <Title level={2} style={{ margin: '0 0 8px', color: textColor }}>Forgot Password</Title>
                        <Text type="secondary">Enter your email to reset your password</Text>
                    </div>

                    <Form
                        name="forgot-password"
                        onFinish={onFinish}
                        layout="vertical"
                        size="large"
                    >
                        <Form.Item
                            name="email"
                            rules={[{ required: true, message: 'Please input your Email!' }, { type: 'email', message: 'Invalid email!' }]}
                        >
                            <Input
                                prefix={<MailOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="Email address"
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
                                    fontWeight: 600,
                                    fontSize: 16,
                                    color: textColor
                                }}
                            >
                                Send Reset Link
                            </Button>
                        </Form.Item>
                    </Form>

                    <div style={{ textAlign: 'center', marginTop: 20 }}>
                        <Link to="/login" style={{ color: '#BD72EB', fontWeight: 500 }}>Back to Login</Link>
                    </div>
                </div>
            </div>
        </ConfigProvider>
    )
}
