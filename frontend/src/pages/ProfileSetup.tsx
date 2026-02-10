import { useState } from 'react'
import { Form, Input, Button, Typography, ConfigProvider } from 'antd'
import { UserOutlined, TeamOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'

const { Title, Text } = Typography

export const ProfileSetup = () => {
    const [loading, setLoading] = useState(false)
    const navigate = useNavigate()

    const onFinish = (values: { displayName?: string; companyOrHandle?: string }) => {
        setLoading(true)
        // Optional: send to backend when profile API exists
        console.log('Profile setup:', values)
        setLoading(false)
        navigate('/')
    }

    const onSkip = () => {
        navigate('/')
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
                        <Title level={2} style={{ margin: '0 0 8px', color: textColor }}>Complete your profile</Title>
                        <Text type="secondary">Add a few details to get started (optional)</Text>
                    </div>

                    <Form
                        name="profile-setup"
                        onFinish={onFinish}
                        layout="vertical"
                        size="large"
                    >
                        <Form.Item
                            name="displayName"
                            label="Display name"
                        >
                            <Input
                                prefix={<UserOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="Your name"
                            />
                        </Form.Item>

                        <Form.Item
                            name="companyOrHandle"
                            label="Company name or social handle"
                        >
                            <Input
                                prefix={<TeamOutlined style={{ color: 'rgba(0,0,0,.25)' }} />}
                                placeholder="Brand / company or @handle"
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
                                Save and continue
                            </Button>
                        </Form.Item>

                        <Form.Item>
                            <Button
                                type="link"
                                block
                                onClick={onSkip}
                                style={{ color: secondaryColor, fontWeight: 500 }}
                            >
                                Skip for now
                            </Button>
                        </Form.Item>
                    </Form>
                </div>
            </div>
        </ConfigProvider>
    )
}
