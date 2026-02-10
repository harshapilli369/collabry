import { useState } from 'react'
import { Form, Input, Button, Typography, ConfigProvider } from 'antd'
import { LockOutlined } from '@ant-design/icons'
import { resetPassword } from '../services/authService'

const { Title, Text, Link } = Typography

export const ResetPassword = () => {
    const [loading, setLoading] = useState(false)

    // Simple way to get query param without router hooks if not available yet, 
    // but typically we use useSearchParams. 
    // Assuming standard URLSearchParams works:
    const queryParameters = new URLSearchParams(window.location.search)
    const token = queryParameters.get("token")

    const onFinish = async (values: any) => {
        if (!token) {
            alert("Invalid or missing token")
            return
        }
        setLoading(true)
        try {
            await resetPassword(token, values.password)
            alert("Password reset successful! You can now login.")
            window.location.href = '/login' // simple redirect
        } catch (error) {
            console.error(error)
            alert("Failed to reset password")
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
                        <Title level={2} style={{ margin: '0 0 8px', color: textColor }}>Reset Password</Title>
                        <Text type="secondary">Enter your new password</Text>
                    </div>

                    <Form
                        name="reset-password"
                        onFinish={onFinish}
                        layout="vertical"
                        size="large"
                    >
                        <Form.Item
                            name="password"
                            rules={[{ required: true, message: 'Please input your new Password!' }]}
                        >
                            <Input.Password
                                prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="New Password"
                            />
                        </Form.Item>

                        <Form.Item
                            name="confirm"
                            dependencies={['password']}
                            hasFeedback
                            rules={[
                                {
                                    required: true,
                                    message: 'Please confirm your password!',
                                },
                                ({ getFieldValue }) => ({
                                    validator(_, value) {
                                        if (!value || getFieldValue('password') === value) {
                                            return Promise.resolve();
                                        }
                                        return Promise.reject(new Error('The new password that you entered does not match!'));
                                    },
                                }),
                            ]}
                        >
                            <Input.Password
                                prefix={<LockOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="Confirm Password"
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
                                Reset Password
                            </Button>
                        </Form.Item>
                    </Form>
                </div>
            </div>
        </ConfigProvider>
    )
}
