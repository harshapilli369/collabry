import { useState, useEffect } from 'react'
import { Typography, Button, ConfigProvider, Layout, Menu, Card, Row, Col, Avatar, Tabs, theme } from 'antd'
import { UserOutlined, LogoutOutlined, PlusCircleOutlined, AppstoreOutlined, FundProjectionScreenOutlined, UnorderedListOutlined, DollarOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyBrandProfile } from '../services/brandService'
import { getMyCampaigns, CAMPAIGN_STATUS_LABELS, BUDGET_RANGE_OPTIONS, type CampaignResponse, type CampaignStatus } from '../services/campaignService'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography
const STATUS_ORDER: CampaignStatus[] = ['DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED']

export const BrandDashboard = () => {
    const navigate = useNavigate()
    const [profileCheckDone, setProfileCheckDone] = useState(false)
    const [campaigns, setCampaigns] = useState<CampaignResponse[]>([])
    const [campaignsLoading, setCampaignsLoading] = useState(false)
    const userStr = localStorage.getItem('user')
    const user = userStr ? JSON.parse(userStr) : null

    useEffect(() => {
        if (user?.role !== 'BRAND') {
            setProfileCheckDone(true)
            return
        }
        getMyBrandProfile()
            .then((profile) => {
                if (profile == null) {
                    navigate('/brand/profile', { replace: true })
                    return
                }
                setProfileCheckDone(true)
            })
            .catch(() => {
                setProfileCheckDone(true)
            })
    }, [user?.role, navigate])

    useEffect(() => {
        if (!profileCheckDone || user?.role !== 'BRAND') return
        setCampaignsLoading(true)
        getMyCampaigns()
            .then(setCampaigns)
            .catch(() => setCampaigns([]))
            .finally(() => setCampaignsLoading(false))
    }, [profileCheckDone, user?.role])

    const campaignsByStatus = STATUS_ORDER.map((status) => ({
        status,
        label: CAMPAIGN_STATUS_LABELS[status],
        list: campaigns.filter((c) => c.status === status),
    }))

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }

    const primaryColor = '#FFFD82'; // Neon Yellow-Green
    const textColor = '#ffffff';
    const pageBackgroundColor = '#000000';


    if (!profileCheckDone && user?.role === 'BRAND') {
        return null
    }

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: {
                    colorPrimary: primaryColor,
                    colorTextBase: textColor,
                    fontFamily: 'Inter, sans-serif',
                },
                components: {
                    Layout: {
                        bodyBg: '#000000',
                        headerBg: '#000000',
                        siderBg: '#000000',
                    },
                    Menu: {
                        darkItemBg: '#000000',
                        darkItemSelectedBg: '#333333',
                    }
                }
            }}
        >
            <Layout style={{ minHeight: '100vh' }}>
                <Sider width={250} theme="dark">
                    <div style={{ padding: '20px', textAlign: 'center' }}>
                        <Title level={4} style={{ color: '#fff', margin: 0 }}>Collabry</Title>
                        <Text style={{ color: primaryColor }}>Brand Portal</Text>
                    </div>
                    <Menu
                        theme="dark"
                        mode="inline"
                        defaultSelectedKeys={['dashboard']}
                        defaultOpenKeys={['campaign']}
                        items={[
                            {
                                key: 'dashboard',
                                icon: <AppstoreOutlined />,
                                label: 'Dashboard',
                            },
                            {
                                key: 'campaign',
                                icon: <FundProjectionScreenOutlined />,
                                label: 'Campaign',
                                children: [
                                    {
                                        key: 'campaign-create',
                                        icon: <PlusCircleOutlined />,
                                        label: 'Create campaign',
                                        onClick: () => navigate('/brand/campaigns/create'),
                                    },
                                    {
                                        key: 'campaign-view',
                                        icon: <UnorderedListOutlined />,
                                        label: 'View my campaigns',
                                    },
                                ],
                            },
                            {
                                key: 'payments',
                                icon: <DollarOutlined />,
                                label: 'Payments',
                                onClick: () => navigate('/brand/payments'),
                            },
                            {
                                key: 'profile',
                                icon: <UserOutlined />,
                                label: 'Profile',
                                onClick: () => navigate('/brand/profile'),
                            },
                            {
                                key: 'logout',
                                icon: <LogoutOutlined />,
                                label: 'Logout',
                                onClick: handleLogout,
                                danger: true
                            },
                        ]}
                    />
                </Sider>
                <Layout>
                    <Header style={{ padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'flex-end', background: pageBackgroundColor }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>

                            <Avatar size="large" icon={<UserOutlined />} style={{ backgroundColor: primaryColor, color: '#000' }} />
                        </div>
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
                        <div style={{ marginBottom: 30 }}>
                            <Title level={1} style={{ color: primaryColor, margin: 0, fontSize: '3rem' }}>Welcome!</Title>
                            <Text style={{ color: '#aaa', fontSize: '1.2rem' }}>Overview of your improved brand performance.</Text>
                        </div>

                        <Row gutter={[24, 24]}>
                            {/* Stats Row - real campaign counts */}
                            <Col span={6}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Draft</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>{campaigns.filter((c) => c.status === 'DRAFT').length}</Title>
                                </Card>
                            </Col>
                            <Col span={6}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Active</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>{campaigns.filter((c) => c.status === 'ACTIVE').length}</Title>
                                </Card>
                            </Col>
                            <Col span={6}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Completed</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>{campaigns.filter((c) => c.status === 'COMPLETED').length}</Title>
                                </Card>
                            </Col>
                            <Col span={6}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Total campaigns</Text>
                                    <Title level={2} style={{ margin: '10px 0 0' }}>{campaigns.length}</Title>
                                </Card>
                            </Col>

                            {/* Campaign section: Create campaign + View my campaigns by status */}
                            <Col span={24}>
                                <Card
                                    title={
                                        <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                            <FundProjectionScreenOutlined />
                                            Campaign
                                        </span>
                                    }
                                    bordered={false}
                                    style={{ borderRadius: 12 }}
                                    extra={
                                        <Button type="primary" icon={<PlusCircleOutlined />} onClick={() => navigate('/brand/campaigns/create')} style={{ color: '#000000' }}>
                                            Create campaign
                                        </Button>
                                    }
                                >
                                    <Title level={5} style={{ color: '#888', marginBottom: 16 }}>View my campaigns</Title>
                                    {campaignsLoading ? (
                                        <Text type="secondary">Loading campaigns…</Text>
                                    ) : campaigns.length === 0 ? (
                                        <Text type="secondary">No campaigns yet. Create one to get started.</Text>
                                    ) : (
                                        <Tabs
                                            defaultActiveKey={STATUS_ORDER.find((s) => campaigns.some((c) => c.status === s)) ?? 'DRAFT'}
                                            items={campaignsByStatus.map(({ status, label, list }) => ({
                                                key: status,
                                                label: `${label} (${list.length})`,
                                                children: (
                                                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                                        {list.length === 0 ? (
                                                            <Text type="secondary">No {label.toLowerCase()} campaigns.</Text>
                                                        ) : (
                                                            list.map((campaign) => (
                                                                <Card key={campaign.id} size="small" style={{ background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                                                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                                                        <div>
                                                                            <Text strong style={{ color: '#fff', fontSize: 16 }}>{campaign.name}</Text>
                                                                            {campaign.description && (
                                                                                <div><Text type="secondary" style={{ fontSize: 13 }}>{campaign.description.slice(0, 100)}{campaign.description.length > 100 ? '…' : ''}</Text></div>
                                                                            )}
                                                                            <div style={{ marginTop: 6 }}>
                                                                                <Text type="secondary" style={{ fontSize: 12 }}>
                                                                                    Budget: {BUDGET_RANGE_OPTIONS.find((o) => o.value === campaign.budgetRange)?.label ?? campaign.budgetRange}
                                                                                    {campaign.numberOfInfluencers != null && ` · ${campaign.numberOfInfluencers} influencer(s)`}
                                                                                    {campaign.startDate && ` · ${campaign.startDate}`}
                                                                                </Text>
                                                                            </div>
                                                                        </div>
                                                                        <Text style={{ fontSize: 12, fontWeight: 600, color: primaryColor }}>{CAMPAIGN_STATUS_LABELS[campaign.status]}</Text>
                                                                    </div>
                                                                </Card>
                                                            ))
                                                        )}
                                                    </div>
                                                ),
                                            }))}
                                        />
                                    )}
                                </Card>
                            </Col>

                            <Col span={12}>
                                <Card title="Invited Influencers" bordered={false} style={{ borderRadius: 12, height: '100%' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
                                        <Text>Pending Responses: 0</Text>
                                        <Button type="link">Manage</Button>
                                    </div>
                                    <Text type="secondary">Invite influencers from your campaigns.</Text>
                                </Card>
                            </Col>
                        </Row>
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
