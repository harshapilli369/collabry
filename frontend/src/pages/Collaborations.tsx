import { useState, useEffect } from 'react'
import { Typography, ConfigProvider, Layout, Menu, Card, Row, Col, Avatar, Tag, message, theme } from 'antd'
import { UserOutlined, LogoutOutlined, MailOutlined, AppstoreOutlined, DollarOutlined, TeamOutlined, FileTextOutlined } from '@ant-design/icons'
import { Link, useNavigate } from 'react-router-dom'
import { getMyCollaborations, INVITATION_STATUS_LABELS, type InvitationResponse, type InvitationStatus } from '../services/invitationService'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography

const primaryColor = '#EFEE96'
const secondaryColor = '#BD72EB'
const pageBackgroundColor = '#000000'

function formatDate(s: string | undefined) {
    if (!s) return '—'
    try {
        return new Date(s).toLocaleDateString(undefined, { dateStyle: 'medium' })
    } catch {
        return s
    }
}

export const Collaborations = () => {
    const navigate = useNavigate()
    const [collaborations, setCollaborations] = useState<InvitationResponse[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        getMyCollaborations()
            .then(setCollaborations)
            .catch(() => {
                message.error('Failed to load collaborations')
                setCollaborations([])
            })
            .finally(() => setLoading(false))
    }, [])

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: { colorPrimary: primaryColor, colorTextBase: '#ffffff', fontFamily: 'Inter, sans-serif' },
                components: {
                    Layout: { bodyBg: '#000000', headerBg: '#000000', siderBg: '#000000' },
                    Menu: { darkItemBg: '#000000', darkItemSelectedBg: '#333333' },
                },
            }}
        >
            <Layout style={{ minHeight: '100vh' }}>
                <Sider width={250} theme="dark">
                    <div style={{ padding: '20px', textAlign: 'center' }}>
                        <Title level={4} style={{ color: '#fff', margin: 0 }}>Collabry</Title>
                        <Text style={{ color: secondaryColor }}>Influencer</Text>
                    </div>
                    <Menu
                        theme="dark"
                        mode="inline"
                        defaultSelectedKeys={['collaborations']}
                        items={[
                            { key: 'dashboard', icon: <AppstoreOutlined />, label: 'Dashboard', onClick: () => navigate('/influencer/dashboard') },
                            { key: 'profile', icon: <UserOutlined />, label: 'Profile', onClick: () => navigate('/influencer/profile') },
                            { key: 'invitations', icon: <MailOutlined />, label: 'Invitations', onClick: () => navigate('/influencer/invitations') },
                            { key: 'collaborations', icon: <TeamOutlined />, label: 'Collaborations' },
                            { key: 'disclosure', icon: <FileTextOutlined />, label: 'Disclosure guidelines', onClick: () => navigate('/influencer/disclosure-guidelines') },
                            { key: 'payments', icon: <DollarOutlined />, label: 'Payments', onClick: () => navigate('/influencer/payments') },
                            { key: 'logout', icon: <LogoutOutlined />, label: 'Logout', onClick: handleLogout, danger: true },
                        ]}
                    />
                </Sider>
                <Layout>
                    <Header style={{ padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'flex-end', background: pageBackgroundColor }}>
                        <Text style={{ color: '#aaa', fontSize: '0.9rem', marginRight: 10 }}>ID: {user?.id}</Text>
                        <Avatar size="large" icon={<UserOutlined />} style={{ backgroundColor: secondaryColor }} />
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
                        <div style={{ marginBottom: 30 }}>
                            <Title level={1} style={{ color: secondaryColor, margin: 0, fontSize: '3rem' }}>My Collaborations</Title>
                            <Text style={{ color: '#aaa', fontSize: '1.2rem', display: 'block' }}>Campaigns you’ve accepted or confirmed.</Text>
                            <Link to="/influencer/disclosure-guidelines" style={{ color: primaryColor, fontSize: '0.95rem' }}>
                                Advertising & disclosure guidelines
                            </Link>
                        </div>

                        {loading ? (
                            <Text type="secondary">Loading…</Text>
                        ) : collaborations.length === 0 ? (
                            <Card bordered={false} style={{ borderRadius: 12, background: '#1c1c1c', border: '1px solid #333' }}>
                                <Text type="secondary">You have no collaborations yet. Accept an invitation from the Invitations page to get started.</Text>
                            </Card>
                        ) : (
                            <>
                                <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
                                    <Col span={8}>
                                        <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                            <Text type="secondary">Active collaborations</Text>
                                            <Title level={2} style={{ margin: '10px 0 0', color: secondaryColor }}>{collaborations.length}</Title>
                                        </Card>
                                    </Col>
                                </Row>
                                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                    {collaborations.map((inv) => (
                                        <Card key={inv.id} size="small" style={{ background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12 }}>
                                                <div>
                                                    <Text strong style={{ color: '#fff', fontSize: 16 }}>Campaign #{inv.campaignId}</Text>
                                                    {inv.brandMessage && (
                                                        <div style={{ marginTop: 6 }}>
                                                            <Text type="secondary" style={{ fontSize: 13 }}>{inv.brandMessage.slice(0, 100)}{inv.brandMessage.length > 100 ? '…' : ''}</Text>
                                                        </div>
                                                    )}
                                                    <div style={{ marginTop: 6 }}>
                                                        <Text type="secondary" style={{ fontSize: 12 }}>
                                                            {inv.proposedAmount != null && `$${Number(inv.proposedAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}`}
                                                            {inv.respondedAt && ` · Responded ${formatDate(inv.respondedAt)}`}
                                                        </Text>
                                                    </div>
                                                </div>
                                                <Tag color="green">{INVITATION_STATUS_LABELS[inv.status as InvitationStatus]}</Tag>
                                            </div>
                                        </Card>
                                    ))}
                                </div>
                            </>
                        )}
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
