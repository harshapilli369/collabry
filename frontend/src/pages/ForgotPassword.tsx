import { useState } from 'react'
import { Form, Input, Button, Typography, ConfigProvider, theme, message } from 'antd'
import { MailOutlined } from '@ant-design/icons'
import { Link } from 'react-router-dom'
import { forgotPassword } from '../services/authService'

const { Title, Text } = Typography
const PRIMARY = '#FFFD82'
const PURPLE = '#BD72EB'

export const ForgotPassword = () => {
    const [loading, setLoading] = useState(false)

    const onFinish = async (values: any) => {
        setLoading(true)
        try {
            await forgotPassword(values.email)
            message.success('Reset link sent! Check your email.')
        } catch (error) {
            const msg = error instanceof Error ? error.message : 'Failed to send reset link'
            message.error(msg)
        } finally {
            setLoading(false)
        }
    }

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: { colorPrimary: PRIMARY, colorText: '#ffffff', borderRadius: 8, fontFamily: 'Inter, sans-serif' },
                components: { Input: { paddingBlock: 10 } },
            }}
        >
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', backgroundColor: '#000', position: 'relative', overflow: 'hidden' }}>
                <div style={{ position: 'absolute', top: '30%', left: '15%', width: 350, height: 350, borderRadius: '50%', background: `radial-gradient(circle, ${PRIMARY}06 0%, transparent 70%)`, pointerEvents: 'none' }} />
                <div style={{ position: 'absolute', bottom: '20%', right: '20%', width: 300, height: 300, borderRadius: '50%', background: `radial-gradient(circle, ${PURPLE}06 0%, transparent 70%)`, pointerEvents: 'none' }} />

                <div style={{ width: '100%', maxWidth: 420, padding: '44px 40px', backgroundColor: '#0d0d0d', borderRadius: 20, border: `1px solid ${PRIMARY}10`, boxShadow: '0 8px 40px rgba(0,0,0,0.4)', position: 'relative', zIndex: 1 }}>
                    <div style={{ textAlign: 'center', marginBottom: 32 }}>
                        <div style={{ width: 52, height: 52, borderRadius: 14, background: `linear-gradient(135deg, ${PRIMARY}, ${PURPLE})`, display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px', fontWeight: 900, fontSize: 24, color: '#000' }}>
                            C
                        </div>
                        <Title level={2} style={{ margin: '0 0 8px', color: '#fff' }}>Forgot Password</Title>
                        <Text style={{ color: '#666' }}>Enter your email and we will send you a reset link</Text>
                    </div>

                    <Form name="forgot-password" onFinish={onFinish} layout="vertical" size="large">
                        <Form.Item name="email" rules={[{ required: true, message: 'Please input your Email!' }, { type: 'email', message: 'Invalid email!' }]}>
                            <Input prefix={<MailOutlined style={{ color: '#555' }} />} placeholder="Email address" />
                        </Form.Item>
                        <Form.Item>
                            <Button type="primary" htmlType="submit" block loading={loading} style={{ height: 50, fontWeight: 700, fontSize: 16, color: '#000', borderRadius: 12 }}>
                                Send Reset Link
                            </Button>
                        </Form.Item>
                    </Form>

                    <div style={{ textAlign: 'center', marginTop: 20 }}>
                        <Link to="/login" style={{ color: PURPLE, fontWeight: 600 }}>Back to Login</Link>
                    </div>
                </div>
            </div>
        </ConfigProvider>
    )
}
