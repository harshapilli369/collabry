import { useState } from 'react'
import { Form, Input, Button, Checkbox, Typography, Divider, ConfigProvider } from 'antd'
import { MailOutlined, LockOutlined, GoogleOutlined } from '@ant-design/icons'
import { useGoogleLogin } from '@react-oauth/google'
import { loginUser, googleLoginUser } from '../services/authService';

const { Title, Text, Link } = Typography



export const Login = () => {
    const [loading, setLoading] = useState(false)

    const onFinish = async (values: any) => {
        setLoading(true)
        try {
            const data = await loginUser(values);
            console.log('Login success:', data);
            // Store token
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify({ email: data.email, role: data.role }));

            // Show success feedback
            alert("Login Successful! Redirecting...");
            // In a real router setup: navigate('/dashboard');
        } catch (error) {
            console.error('Login error:', error);
            alert("Login Failed: Invalid credentials or Network error");
        } finally {
            setLoading(false)
        }
    }

    const googleLogin = useGoogleLogin({
        onSuccess: async (tokenResponse) => {
            console.log('Google Success:', tokenResponse);
            try {
                // Send access token to backend to verify and get JWT
                const data = await googleLoginUser(tokenResponse.access_token);
                localStorage.setItem('token', data.token);
                localStorage.setItem('user', JSON.stringify({ email: data.email, role: data.role }));
                alert("Google Login Successful! Redirecting...");
            } catch (err) {
                console.error("Google Backend Error", err);
                alert("Google Login Failed on Backend");
            }
        },
        onError: () => console.log('Google Login Failed'),
    });

    // Colors from User Palette (Synced with index.css)
    const primaryColor = '#FFFD82'; // Neon Yellow-Green
    const secondaryColor = '#BD72EB'; // Soft Purple
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
                        colorTextLightSolid: textColor, // Ensures text is black on the neon button
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

                    {/* Logo Section */}
                    <div style={{ textAlign: 'center', marginBottom: 30 }}>
                        <div style={{ marginBottom: 20 }}>
                            <img src="/logo.png" alt="Collabry Logo" style={{ height: 60 }} />
                        </div>

                        <Title level={2} style={{ margin: '0 0 8px', color: textColor }}>Collabry</Title>
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
                                <Form.Item name="rememberMe" valuePropName="checked" noStyle>
                                    <Checkbox
                                        style={{
                                            color: 'rgba(0,0,0,0.5)',
                                        }}
                                        className="custom-checkbox"
                                    >
                                        Remember me
                                    </Checkbox>
                                </Form.Item>
                                <Link style={{ color: secondaryColor, fontWeight: 500 }}>
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
                                    fontWeight: 600,
                                    fontSize: 16,
                                    // Let ConfigProvider handle colors, but ensure text is black
                                    color: textColor
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
                        icon={<GoogleOutlined style={{ color: '#000' }} />} // Black icon for consistency
                        onClick={() => googleLogin()}
                        style={{
                            height: 50,
                            fontWeight: 500,
                            color: textColor,
                            borderColor: '#eee',
                            backgroundColor: '#fff'
                        }}
                    >
                        Continue with Google
                    </Button>

                    <div style={{ textAlign: 'center', marginTop: 30 }}>
                        <Text style={{ color: 'rgba(0,0,0,0.5)' }}>Don't have an account? </Text>
                        <Link style={{ color: secondaryColor, fontWeight: 500 }}>Sign up</Link>
                    </div>
                </div>
            </div>
        </ConfigProvider>
    )
}
