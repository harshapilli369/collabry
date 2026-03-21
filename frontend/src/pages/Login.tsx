import { useState } from 'react'
import { Form, Input, Button, Checkbox, Typography, Divider, ConfigProvider, message, theme } from 'antd'
import { MailOutlined, LockOutlined, GoogleOutlined } from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { useGoogleLogin } from '@react-oauth/google'
import { loginUser, googleLoginUser } from '../services/authService'
import { getMyInfluencerProfile } from '../services/influencerProfileService'

const { Title, Text } = Typography



export const Login = () => {
    const [loading, setLoading] = useState(false)
    const [form] = Form.useForm()
    const navigate = useNavigate()

    const submitLogin = async (values: { email?: string; password?: string; rememberMe?: boolean }) => {
        setLoading(true)
        try {
            const payload = { email: values.email ?? '', password: values.password ?? '', rememberMe: values.rememberMe ?? false };
            const data = await loginUser(payload);
            if (!data?.token) {
                message.error('Invalid response from server');
                alert('Invalid response from server');
                return;
            }
            console.log('Login success:', data);
            localStorage.setItem('token', data.token);
            localStorage.setItem('user', JSON.stringify({ id: data.id, email: data.email, role: data.role, isVerified: data.isVerified }));
            message.success('Login successful!');
            alert('Login successful!');

            if (data.role === 'INFLUENCER') {
                const profile = await getMyInfluencerProfile();
                if (!profile?.complete) {
                    navigate('/influencer/profile/edit', { replace: true });
                } else {
                    navigate('/influencer/dashboard', { replace: true });
                }
            } else if (data.role === 'BRAND') {
                navigate('/brand/dashboard', { replace: true });
            } else {
                navigate('/', { replace: true });
            }
        } catch (error) {
            console.error('Login error:', error);
            const msg = error instanceof Error ? error.message : 'Login failed';
            message.error(msg);
            alert(msg);
        } finally {
            setLoading(false)
        }
    }

    const onFinish = (values: { email?: string; password?: string; rememberMe?: boolean }) => {
        submitLogin(values)
    }

    const googleLogin = useGoogleLogin({
        onSuccess: async (tokenResponse) => {
            console.log('Google Success:', tokenResponse);
            try {
                // Send access token to backend to verify and get JWT
                const data = await googleLoginUser(tokenResponse.access_token);
                localStorage.setItem('token', data.token);
                localStorage.setItem('user', JSON.stringify({ id: data.id, email: data.email, role: data.role, isVerified: data.isVerified }));
                alert("Google Login Successful! Redirecting...");

                if (data.role === 'INFLUENCER') {
                    const profile = await getMyInfluencerProfile();
                    if (!profile?.complete) {
                        navigate('/influencer/profile/edit', { replace: true });
                    } else {
                        navigate('/influencer/dashboard', { replace: true });
                    }
                } else if (data.role === 'BRAND') {
                    navigate('/brand/dashboard', { replace: true });
                } else {
                    navigate('/', { replace: true });
                }
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
    const textColor = '#ffffff'; // Black (for inside the white card)
    const pageBackgroundColor = '#000000'; // Primary BG
    const cardBackgroundColor = '#141414'; // Pure White

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: {
                    colorPrimary: primaryColor,
                    colorText: textColor,
                    borderRadius: 8,
                    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
                },
                components: {
                    Button: {},
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
                            <img src="/logo.png" alt="Collabry Logo" style={{ height: 60, borderRadius: 8 }} />
                        </div>

                        <Title level={2} style={{ margin: '0 0 8px', color: textColor }}>Collabry</Title>
                        <Text type="secondary">Log in to your account to continue</Text>
                    </div>

                    {/* Form Section */}
                    <Form
                        form={form}
                        name="login"
                        initialValues={{ rememberMe: true }}
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
                                            color: 'textColor',
                                        }}
                                        className="custom-checkbox"
                                    >
                                        Remember me
                                    </Checkbox>
                                </Form.Item>
                                <Link to="/forgot-password" style={{ color: secondaryColor, fontWeight: 500 }}>
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
                                    color: "#000000"
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
                            color: "#000000",
                            borderColor: '#eee',
                            backgroundColor: '#fff'
                        }}
                    >
                        Continue with Google
                    </Button>

                    <div style={{ textAlign: 'center', marginTop: 30 }}>
                        <Text style={{ color: 'textColor' }}>Don't have an account? </Text>
                        <Link to="/signup" style={{ color: secondaryColor, fontWeight: 500 }}>Sign up</Link>
                    </div>
                </div>
            </div>
        </ConfigProvider>
    )
}
