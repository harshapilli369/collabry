import { useState, useEffect } from 'react'
import { Typography, Button, ConfigProvider, Layout, Menu, Card, Row, Col, Tabs, Modal, Form, Input, InputNumber, message, theme, Drawer, Spin, Avatar, Progress, Tag, Divider } from 'antd'
import { UserOutlined, LogoutOutlined, PlusCircleOutlined, AppstoreOutlined, FundProjectionScreenOutlined, UnorderedListOutlined, DollarOutlined, MailOutlined, CheckCircleFilled, RobotOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyBrandProfile } from '../services/brandService'
import { getMyCampaigns, CAMPAIGN_STATUS_LABELS, BUDGET_RANGE_OPTIONS, type CampaignResponse, type CampaignStatus } from '../services/campaignService'
import { createInvitation } from '../services/invitationService'
import { getCampaignRecommendations, type InfluencerRecommendationDTO } from '../services/recommendationService'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography
const STATUS_ORDER: CampaignStatus[] = ['DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED']

export const BrandDashboard = () => {
    const navigate = useNavigate()
    const [profileCheckDone, setProfileCheckDone] = useState(false)
    const [brandProfile, setBrandProfile] = useState<any>(null)
    const [campaigns, setCampaigns] = useState<CampaignResponse[]>([])
    const [campaignsLoading, setCampaignsLoading] = useState(false)
    const [inviteModalOpen, setInviteModalOpen] = useState(false)
    const [inviteCampaignId, setInviteCampaignId] = useState<number | null>(null)
    const [inviteSubmitting, setInviteSubmitting] = useState(false)
    const [inviteForm] = Form.useForm()

    const [aiDrawerOpen, setAiDrawerOpen] = useState(false)
    const [aiDrawerCampaignId, setAiDrawerCampaignId] = useState<number | null>(null)
    const [aiRecommendations, setAiRecommendations] = useState<InfluencerRecommendationDTO[]>([])
    const [aiLoading, setAiLoading] = useState(false)

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
                    navigate('/brand/profile/edit', { replace: true })
                    return
                }
                setBrandProfile(profile)
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

    const openInviteModal = (campaignId: number) => {
        setInviteCampaignId(campaignId)
        inviteForm.resetFields()
        setInviteModalOpen(true)
    }
    const closeInviteModal = () => {
        setInviteModalOpen(false)
        setInviteCampaignId(null)
    }
    const onInviteSubmit = async (values: { influencerId: number; message?: string }) => {
        if (inviteCampaignId == null) return
        setInviteSubmitting(true)
        try {
            await createInvitation(inviteCampaignId, { influencerId: values.influencerId, message: values.message?.trim() || undefined })
            message.success('Invitation sent')
            closeInviteModal()
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to send invitation')
        } finally {
            setInviteSubmitting(false)
        }
    }

    const openAiDrawer = async (campaignId: number) => {
        setAiDrawerCampaignId(campaignId)
        setAiDrawerOpen(true)
        setAiLoading(true)
        setAiRecommendations([])
        try {
            const recs = await getCampaignRecommendations(campaignId)
            setAiRecommendations(recs)
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to load AI recommendations')
        } finally {
            setAiLoading(false)
        }
    }
    const closeAiDrawer = () => {
        setAiDrawerOpen(false)
        setAiDrawerCampaignId(null)
    }
    const handleInviteFromAi = (influencerId: number) => {
        setInviteCampaignId(aiDrawerCampaignId)
        inviteForm.setFieldsValue({ influencerId })
        setInviteModalOpen(true)
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
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <Text style={{ color: '#fff', fontSize: '1.1rem', fontWeight: 500 }}>
                                {(() => {
                                    let handle = brandProfile?.instagramUrl ? brandProfile.instagramUrl.split('/').filter(Boolean).pop() : brandProfile?.name || user?.email;
                                    if (handle && !handle.startsWith('@') && !handle.includes('@')) {
                                        handle = `@${handle}`;
                                    }
                                    return handle;
                                })()}
                            </Text>
                            {user?.isVerified && <CheckCircleFilled style={{ color: '#1890ff', fontSize: '1.2rem' }} title="Verified Brand" />}
                        </div>
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
                        <div style={{ marginBottom: 30 }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                                <Title level={1} style={{ color: primaryColor, margin: 0, fontSize: '3rem' }}>Welcome!</Title>
                            </div>
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
                                        <Button 
                                            type="primary" 
                                            icon={<PlusCircleOutlined />} 
                                            onClick={() => navigate('/brand/campaigns/create')} 
                                            style={{ color: '#000000' }}
                                            disabled={!user?.isVerified}
                                            title={!user?.isVerified ? "Only verified brands can create campaigns" : ""}
                                        >
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
                                                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 8 }}>
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
                                                                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                                                            <Text style={{ fontSize: 12, fontWeight: 600, color: '#aaa' }}>{CAMPAIGN_STATUS_LABELS[campaign.status]}</Text>
                                                                            <Button type="primary" size="small" icon={<RobotOutlined />} style={{ color: '#000', background: primaryColor, borderColor: primaryColor }} onClick={() => openAiDrawer(campaign.id)}>AI Match</Button>
                                                                            <Button type="default" size="small" style={{ background: 'transparent', color: '#fff', borderColor: '#555' }} icon={<MailOutlined />} onClick={() => openInviteModal(campaign.id)}>Invite</Button>
                                                                        </div>
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
                                    <Text type="secondary">Invite influencers to a campaign using the &quot;Invite&quot; button on each campaign card above. Enter the influencer’s user ID when sending an invite.</Text>
                                </Card>
                            </Col>
                        </Row>
                    </Content>
                </Layout>
            </Layout>

            <Modal
                title="Invite influencer"
                open={inviteModalOpen}
                onCancel={closeInviteModal}
                footer={null}
                destroyOnClose
            >
                <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit}>
                    <Form.Item name="influencerId" label="Influencer user ID" rules={[{ required: true, message: 'Enter the influencer’s user ID' }]}>
                        <InputNumber min={1} step={1} style={{ width: '100%' }} placeholder="e.g. 2" />
                    </Form.Item>
                    <Form.Item name="message" label="Message (optional)">
                        <Input.TextArea rows={3} placeholder="Personal message to the influencer" />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={inviteSubmitting} style={{ color: '#000' }}>Send invitation</Button>
                        <Button style={{ marginLeft: 8 }} onClick={closeInviteModal}>Cancel</Button>
                    </Form.Item>
                </Form>
            </Modal>

            <Drawer
                title={
                    <span style={{ color: primaryColor, fontSize: '1.2rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: 8 }}>
                        <RobotOutlined />
                        AI Matchmaker
                    </span>
                }
                placement="right"
                onClose={closeAiDrawer}
                open={aiDrawerOpen}
                width={550}
                styles={{ body: { background: '#0a0a0a', color: '#fff', padding: 24 }, header: { background: '#111', borderBottom: '1px solid #333' } }}
            >
                {aiLoading ? (
                    <div style={{ textAlign: 'center', padding: '60px 0' }}>
                        <Spin size="large" />
                        <div style={{ marginTop: 24, color: '#aaa', fontSize: 16 }}>Scanning influencer dataset...</div>
                        <div style={{ marginTop: 8, color: '#666', fontSize: 13 }}>Analyzing campaign metrics, niche resonance, and engagement rates</div>
                    </div>
                ) : aiRecommendations.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '100px 0', color: '#aaa' }}>
                        <RobotOutlined style={{ fontSize: 40, opacity: 0.2, marginBottom: 16 }} />
                        <div>No optimal matches found for this campaign's criteria.</div>
                    </div>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
                        <Text style={{ color: '#aaa', fontSize: 14 }}>We analyzed your campaign metrics against available influencers. Here are your top algorithmic matches:</Text>
                        
                        {aiRecommendations.map((rec, idx) => (
                            <Card 
                                key={rec.influencerId} 
                                bordered={false} 
                                style={{ 
                                    background: 'linear-gradient(145deg, #1c1c1c 0%, #151515 100%)', 
                                    borderRadius: 16,
                                    border: idx === 0 ? `1px solid ${primaryColor}` : '1px solid #2a2a2a',
                                    boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
                                    overflow: 'hidden'
                                }}
                                bodyStyle={{ padding: idx === 0 ? 0 : 20 }}
                            >
                                {idx === 0 && (
                                    <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
                                        <div style={{ background: primaryColor, color: '#000', fontSize: 11, fontWeight: 'bold', padding: '4px 12px', borderBottomLeftRadius: 12 }}>
                                            TOP MATCH
                                        </div>
                                    </div>
                                )}
                                <div style={{ display: 'flex', gap: 20, padding: idx === 0 ? '8px 20px 20px' : 0 }}>
                                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
                                        <Progress 
                                            type="circle" 
                                            percent={rec.matchScore} 
                                            size={70} 
                                            strokeWidth={8}
                                            strokeColor={
                                                rec.matchScore >= 90 ? { '0%': '#108ee9', '100%': primaryColor } 
                                                : rec.matchScore >= 75 ? { '0%': '#faad14', '100%': primaryColor } 
                                                : '#faad14'
                                            } 
                                            format={(percent) => <span style={{ color: '#fff', fontSize: '18px', fontWeight: 800 }}>{percent}%</span>}
                                        />
                                        <Text style={{ color: '#888', fontSize: 11, marginTop: 6, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 1 }}>Match</Text>
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                                                <Avatar src={rec.profilePictureUrl || undefined} icon={!rec.profilePictureUrl ? <UserOutlined /> : undefined} size={50} style={{ border: `2px solid ${primaryColor}40` }} />
                                                <div>
                                                    <div style={{ color: '#fff', fontSize: 18, fontWeight: 700 }}>{rec.name || `Influencer #${rec.influencerId}`}</div>
                                                    {rec.niche && <Tag style={{ marginTop: 6, background: '#112233', border: '1px solid #1890ff', color: '#40a9ff', borderRadius: 12, padding: '0 8px' }}>{rec.niche}</Tag>}
                                                </div>
                                            </div>
                                            <Button type="primary" onClick={() => handleInviteFromAi(rec.influencerId)} style={{ background: primaryColor, color: '#000', fontWeight: 600, borderRadius: 20, padding: '0 16px', border: 'none' }}>
                                                Invite
                                            </Button>
                                        </div>
                                        <Divider style={{ margin: '16px 0', borderColor: '#2a2a2a' }} />
                                        <div style={{ background: '#000000', padding: '12px 16px', borderRadius: 8, borderLeft: `3px solid ${primaryColor}` }}>
                                            <Text style={{ color: '#ccc', fontSize: 13, lineHeight: 1.5 }}>
                                                {rec.reason}
                                            </Text>
                                        </div>
                                    </div>
                                </div>
                            </Card>
                        ))}
                    </div>
                )}
            </Drawer>
        </ConfigProvider>
    )
}
