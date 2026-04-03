import { useState } from 'react'
import { Form, Input, Button, Typography, ConfigProvider, theme, message } from 'antd'
import { LockOutlined } from '@ant-design/icons'
import { Link } from 'react-router-dom'
import { resetPassword } from '../services/authService'

const { Title, Text } = Typography
const PRIMARY = '#FFFD82'
const PURPLE = '#BD72EB'

const makeConfirmPasswordRule = (getFieldValue: (name: string) => string) => ({
    validator(_: unknown, value: string) {
        if (!value || getFieldValue('password') === value) return Promise.resolve()
        return Promise.reject(new Error('Passwords do not match!'))
    },
})

export const ResetPassword = () => {
    const [loading, setLoading] = useState(false)
    const queryParameters = new URLSearchParams(window.location.search)
    const token = queryParameters.get('token')

    const onFinish = async (values: any) => {
        if (!token) {
            message.error('Invalid or missing token')
            return
        }
        setLoading(true)
        try {
            await resetPassword(token, values.password)
            message.success('Password reset successful! Redirecting to login...')
            setTimeout(() => { window.location.href = '/login' }, 1500)
        } catch (error) {
            const msg = error instanceof Error ? error.message : 'Failed to reset password'
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
                <div style={{ position: 'absolute', top: '25%', right: '15%', width: 350, height: 350, borderRadius: '50%', background: `radial-gradient(circle, ${PRIMARY}06 0%, transparent 70%)`, pointerEvents: 'none' }} />
                <div style={{ position: 'absolute', bottom: '15%', left: '20%', width: 300, height: 300, borderRadius: '50%', background: `radial-gradient(circle, ${PURPLE}06 0%, transparent 70%)`, pointerEvents: 'none' }} />

                <div style={{ width: '100%', maxWidth: 420, padding: '44px 40px', backgroundColor: '#0d0d0d', borderRadius: 20, border: `1px solid ${PRIMARY}10`, boxShadow: '0 8px 40px rgba(0,0,0,0.4)', position: 'relative', zIndex: 1 }}>
                    <div style={{ textAlign: 'center', marginBottom: 32 }}>
                        <div style={{ width: 52, height: 52, borderRadius: 14, background: `linear-gradient(135deg, ${PRIMARY}, ${PURPLE})`, display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px', fontWeight: 900, fontSize: 24, color: '#000' }}>
                            C
                        </div>

                        {!token ? (
                            <>
                                <Title level={2} style={{ margin: '0 0 8px', color: '#fff' }}>Invalid Link</Title>
                                <Text style={{ color: '#666', display: 'block', marginBottom: 24 }}>This reset link is invalid or has expired.</Text>
                                <div style={{ display: 'flex', justifyContent: 'center', gap: 16 }}>
                                    <Link to="/forgot-password" style={{ color: PURPLE, fontWeight: 600 }}>Request new link</Link>
                                    <Link to="/login" style={{ color: PURPLE, fontWeight: 600 }}>Back to Login</Link>
                                </div>
                            </>
                        ) : (
                            <>
                                <Title level={2} style={{ margin: '0 0 8px', color: '#fff' }}>Reset Password</Title>
                                <Text style={{ color: '#666' }}>Enter your new password</Text>
                            </>
                        )}
                    </div>

                    {token && (
                        <>
                            <Form name="reset-password" onFinish={onFinish} layout="vertical" size="large">
                                <Form.Item name="password" rules={[{ required: true, message: 'Please input your new password!' }]}>
                                    <Input.Password prefix={<LockOutlined style={{ color: '#555' }} />} placeholder="New Password" />
                                </Form.Item>
                                <Form.Item
                                    name="confirm"
                                    dependencies={['password']}
                                    hasFeedback
                                    rules={[
                                        { required: true, message: 'Please confirm your password!' },
                                        ({ getFieldValue }) => makeConfirmPasswordRule(getFieldValue),
                                    ]}
                                >
                                    <Input.Password prefix={<LockOutlined style={{ color: '#555' }} />} placeholder="Confirm Password" />
                                </Form.Item>
                                <Form.Item>
                                    <Button type="primary" htmlType="submit" block loading={loading} style={{ height: 50, fontWeight: 700, fontSize: 16, color: '#000', borderRadius: 12 }}>
                                        Reset Password
                                    </Button>
                                </Form.Item>
                            </Form>
                            <div style={{ textAlign: 'center', marginTop: 20 }}>
                                <Link to="/login" style={{ color: PURPLE, fontWeight: 600 }}>Back to Login</Link>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </ConfigProvider>
    )
}
