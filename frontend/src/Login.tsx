import { useState } from 'react'
import { Form, Input, Button, Checkbox, Typography, Divider, ConfigProvider } from 'antd'
import { MailOutlined, LockOutlined, GoogleOutlined } from '@ant-design/icons'
import { useGoogleLogin } from '@react-oauth/google'
import { Link } from 'react-router-dom'

const { Title, Text } = Typography

export const Login = () => {
    const [loading, setLoading] = useState(false)

    const onFinish = async (values: any) => {
        setLoading(true)
        try {
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(values),
            });

            if (response.ok) {
                const data = await response.json();
                console.log('Login success:', data);
                // Store token
                localStorage.setItem('token', data.token);
                localStorage.setItem('user', JSON.stringify({ email: data.email, role: data.role, displayName: data.displayName, companyName: data.companyName }));

                // Show success feedback
                alert("Login Successful! Redirecting...");
                window.location.href = '/dashboard';
            } else {
                alert("Login Failed: Invalid credentials");
            }
        } catch (error) {
            console.error('Login error:', error);
            alert("Network error. Is the backend running?");
        } finally {
            setLoading(false)
        }
    }

    const googleLogin = useGoogleLogin({
        onSuccess: async (tokenResponse) => {
            console.log('Google Success:', tokenResponse);
            try {
                // Send access token to backend to verify and get JWT
                const res = await fetch('http://localhost:8080/api/auth/google', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ token: tokenResponse.access_token }),
                });

                if (res.ok) {
                    const data = await res.json();
                    localStorage.setItem('token', data.token);
                    localStorage.setItem('user', JSON.stringify({ email: data.email, role: data.role, displayName: data.displayName, companyName: data.companyName }));
                    alert("Google Login Successful! Redirecting...");
                    window.location.href = '/dashboard';
                } else {
                    alert("Google Login Failed on Backend");
                }
            } catch (err) {
                console.error("Google Backend Error", err);
            }
        },
        onError: () => console.log('Google Login Failed'),
    });

    // Brand colors: dark charcoal + bright yellow accent
    const primaryColor = '#FFF066'

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
                backgroundColor: '#1A1A1A'
            }}>
                <div style={{ width: '100%', maxWidth: 400, padding: 20, backgroundColor: '#252525', borderRadius: 16, padding: 32 }}>

                    {/* Logo Section */}
                    <div style={{ textAlign: 'center', marginBottom: 40 }}>
                        <div style={{ marginBottom: 20 }}>
                            <img src="/logo.png" alt="Collabry Logo" style={{ height: 60 }} />
                        </div>

                        <Title level={2} style={{ margin: '0 0 8px', color: '#fff' }}>Welcome Back</Title>
                        <Text style={{ color: 'rgba(255,255,255,0.7)' }}>Log in to your account to continue</Text>
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
                                    <Checkbox style={{ color: 'rgba(255,255,255,0.7)' }}>Remember me</Checkbox>
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
                                    backgroundColor: '#FFF066',
                                    borderColor: '#FFF066',
                                    color: '#1A1A1A',
                                }}
                            >
                                Log In
                            </Button>
                        </Form.Item>
                    </Form>

                    <Divider style={{ color: 'rgba(255,255,255,0.5)', fontSize: 12 }}>OR</Divider>

                    <Button
                        block
                        size="large"
                        icon={<GoogleOutlined style={{ color: '#0F9D58' }} />} // Google Color
                        onClick={() => googleLogin()}
                        style={{
                            height: 50,
                            fontWeight: 500,
                            color: '#1A1A1A',
                            borderColor: 'rgba(255,255,255,0.3)',
                            backgroundColor: '#FFF066',
                        }}
                    >
                        Continue with Google
                    </Button>

                    <div style={{ textAlign: 'center', marginTop: 30 }}>
                        <Text style={{ color: 'rgba(255,255,255,0.7)' }}>Don't have an account? </Text>
                        <Link to="/signup" style={{ color: '#FFF066', fontWeight: 500 }}>Sign up</Link>
                    </div>
                </div>
            </div>
        </ConfigProvider>
    )
}
