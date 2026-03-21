import { useState, useEffect } from 'react'
import { Typography, Button, ConfigProvider, Layout, Menu, Card, Row, Col, Tabs, Modal, Form, Input, InputNumber, Select, Table, message, theme } from 'antd'
import { UserOutlined, LogoutOutlined, PlusCircleOutlined, AppstoreOutlined, FundProjectionScreenOutlined, UnorderedListOutlined, DollarOutlined, MailOutlined, SearchOutlined, EditOutlined, DeleteOutlined, CheckCircleFilled, TeamOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyBrandProfile } from '../services/brandService'
import { getMyCampaigns, CAMPAIGN_STATUS_LABELS, BUDGET_RANGE_OPTIONS, PREFERRED_CONTENT_OPTIONS, type CampaignResponse, type CampaignStatus } from '../services/campaignService'
import {
    createInvitation,
    getSentInvitations,
    withdrawInvitation,
    updateInvitation,
    INVITATION_STATUS_LABELS,
    type InvitationResponse,
    type InvitationRequest,
    type UpdateInvitationRequest,
} from '../services/invitationService'
import { userService, type InfluencerSearchResult } from '../services/userService'

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
    const [findIdModalOpen, setFindIdModalOpen] = useState(false)
    const [influencerList, setInfluencerList] = useState<InfluencerSearchResult[]>([])
    const [influencerListLoading, setInfluencerListLoading] = useState(false)
    const [sentInvitations, setSentInvitations] = useState<InvitationResponse[]>([])
    const [sentInvitationsLoading, setSentInvitationsLoading] = useState(false)
    const [editModalOpen, setEditModalOpen] = useState(false)
    const [editingInvitation, setEditingInvitation] = useState<InvitationResponse | null>(null)
    const [editForm] = Form.useForm<UpdateInvitationRequest>()
    const [editSubmitting, setEditSubmitting] = useState(false)
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

    useEffect(() => {
        if (!profileCheckDone || user?.role !== 'BRAND') return
        setSentInvitationsLoading(true)
        getSentInvitations()
            .then(setSentInvitations)
            .catch(() => setSentInvitations([]))
            .finally(() => setSentInvitationsLoading(false))
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

    const openFindIdModal = () => {
        setFindIdModalOpen(true)
        setInfluencerListLoading(true)
        userService.listInfluencers()
            .then(setInfluencerList)
            .catch(() => {
                message.error('Failed to load influencers')
                setInfluencerList([])
            })
            .finally(() => setInfluencerListLoading(false))
    }
    const closeInviteModal = () => {
        setInviteModalOpen(false)
        setInviteCampaignId(null)
    }
    const onInviteSubmit = async (values: InvitationRequest & { expiresInDays?: number }) => {
        if (inviteCampaignId == null) return
        setInviteSubmitting(true)
        try {
            await createInvitation(inviteCampaignId, {
                influencerId: values.influencerId,
                message: values.message?.trim() || undefined,
                proposedAmount: values.proposedAmount,
                proposedTimeline: values.proposedTimeline?.trim() || undefined,
                proposedDeliverables: values.proposedDeliverables?.trim() || undefined,
                platform: values.platform || undefined,
                expiresInDays: values.expiresInDays ?? 14,
            })
            message.success('Invitation sent')
            closeInviteModal()
            getSentInvitations().then(setSentInvitations).catch(() => {})
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to send invitation')
        } finally {
            setInviteSubmitting(false)
        }
    }

    const handleWithdraw = async (inv: InvitationResponse) => {
        try {
            await withdrawInvitation(inv.id)
            message.success('Invitation withdrawn')
            setSentInvitations((prev) => prev.filter((i) => i.id !== inv.id))
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to withdraw')
        }
    }

    const openEditModal = (inv: InvitationResponse) => {
        setEditingInvitation(inv)
        editForm.setFieldsValue({
            message: inv.brandMessage,
            proposedAmount: inv.proposedAmount,
            proposedTimeline: inv.proposedTimeline,
            proposedDeliverables: inv.proposedDeliverables,
            platform: inv.platform,
        })
        setEditModalOpen(true)
    }
    const closeEditModal = () => {
        setEditModalOpen(false)
        setEditingInvitation(null)
    }
    const onEditSubmit = async (values: UpdateInvitationRequest) => {
        if (!editingInvitation) return
        setEditSubmitting(true)
        try {
            await updateInvitation(editingInvitation.id, values)
            message.success('Invitation updated')
            closeEditModal()
            getSentInvitations().then(setSentInvitations).catch(() => {})
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to update')
        } finally {
            setEditSubmitting(false)
        }
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
                                key: 'influencers',
                                icon: <SearchOutlined />,
                                label: 'Find influencers',
                                onClick: () => navigate('/brand/influencers'),
                            },
                            {
                                key: 'collaborations',
                                icon: <TeamOutlined />,
                                label: 'Collaborations',
                                onClick: () => navigate('/brand/collaborations'),
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
                                                                            <Text style={{ fontSize: 12, fontWeight: 600, color: primaryColor }}>{CAMPAIGN_STATUS_LABELS[campaign.status]}</Text>
                                                                            <Button type="default" size="small" icon={<MailOutlined />} onClick={() => openInviteModal(campaign.id)}>Invite</Button>
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

                            <Col span={24}>
                                <Card
                                    title="Sent invitations"
                                    bordered={false}
                                    style={{ borderRadius: 12 }}
                                    extra={
                                        <Button type="link" onClick={() => navigate('/brand/influencers')} style={{ color: primaryColor, padding: 0 }}>
                                            Find influencers
                                        </Button>
                                    }
                                >
                                    <Text type="secondary" style={{ display: 'block', marginBottom: 12 }}>
                                        Track status: Sent, Accepted, Rejected, Expired, Withdrawn. You can withdraw or edit an invitation before it is accepted.
                                    </Text>
                                    {sentInvitationsLoading ? (
                                        <Text type="secondary">Loading…</Text>
                                    ) : sentInvitations.length === 0 ? (
                                        <Text type="secondary">No invitations sent yet. Use &quot;Find influencers&quot; or Invite on a campaign to send one.</Text>
                                    ) : (
                                        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                                            {sentInvitations.map((inv) => {
                                                const canWithdrawOrEdit = inv.status === 'PENDING' || inv.status === 'NEGOTIATING'
                                                const campaign = campaigns.find((c) => c.id === inv.campaignId)
                                                return (
                                                    <Card key={inv.id} size="small" style={{ background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
                                                            <div>
                                                                <Text strong style={{ color: '#fff' }}>Campaign: {campaign?.name ?? `#${inv.campaignId}`}</Text>
                                                                <span style={{ marginLeft: 8 }}>
                                                                    <Text type="secondary">Influencer ID {inv.influencerId}</Text>
                                                                </span>
                                                                {inv.proposedAmount != null && (
                                                                    <span style={{ marginLeft: 8 }}>
                                                                        <Text type="secondary">· ${Number(inv.proposedAmount).toLocaleString()}</Text>
                                                                    </span>
                                                                )}
                                                                <div style={{ marginTop: 4 }}>
                                                                    <Text style={{ fontSize: 12, fontWeight: 600, color: primaryColor }}>{INVITATION_STATUS_LABELS[inv.status]}</Text>
                                                                </div>
                                                            </div>
                                                            {canWithdrawOrEdit && (
                                                                <div style={{ display: 'flex', gap: 8 }}>
                                                                    <Button type="default" size="small" icon={<EditOutlined />} onClick={() => openEditModal(inv)}>Edit</Button>
                                                                    <Button type="default" size="small" danger icon={<DeleteOutlined />} onClick={() => handleWithdraw(inv)}>Withdraw</Button>
                                                                </div>
                                                            )}
                                                        </div>
                                                    </Card>
                                                )
                                            })}
                                        </div>
                                    )}
                                </Card>
                            </Col>
                        </Row>
                    </Content>
                </Layout>
            </Layout>

            <Modal title="Invite influencer" open={inviteModalOpen} onCancel={closeInviteModal} footer={null} destroyOnClose width={520}>
                <Form form={inviteForm} layout="vertical" onFinish={onInviteSubmit}>
                    <Form.Item
                        name="influencerId"
                        label={
                            <span>
                                Influencer user ID
                                <Button type="link" size="small" onClick={openFindIdModal} style={{ paddingLeft: 8 }}>
                                    Find user ID
                                </Button>
                            </span>
                        }
                        rules={[{ required: true, message: 'Enter the influencer’s user ID' }]}
                    >
                        <InputNumber min={1} step={1} style={{ width: '100%' }} placeholder="e.g. 2" />
                    </Form.Item>
                    <Form.Item name="message" label="Message (optional)">
                        <Input.TextArea rows={2} placeholder="Personal message to the influencer" />
                    </Form.Item>
                    <Form.Item name="proposedDeliverables" label="Deliverables">
                        <Input.TextArea rows={2} placeholder="e.g. 1 Instagram Reel, 3 Stories" />
                    </Form.Item>
                    <Form.Item name="proposedTimeline" label="Timeline">
                        <Input placeholder="e.g. 2 weeks from acceptance" />
                    </Form.Item>
                    <Form.Item name="proposedAmount" label="Budget / proposed amount">
                        <InputNumber min={0} step={100} style={{ width: '100%' }} placeholder="Amount" addonBefore="$" />
                    </Form.Item>
                    <Form.Item name="platform" label="Platform">
                        <Select placeholder="Select platform" allowClear options={PREFERRED_CONTENT_OPTIONS} />
                    </Form.Item>
                    <Form.Item name="expiresInDays" label="Invitation valid for (days)" initialValue={14}>
                        <InputNumber min={1} max={90} style={{ width: '100%' }} />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={inviteSubmitting} style={{ color: '#000000' }}>Send invitation</Button>
                        <Button style={{ marginLeft: 8 }} onClick={closeInviteModal}>Cancel</Button>
                    </Form.Item>
                </Form>
            </Modal>

            <Modal
                title="Influencer user IDs"
                open={findIdModalOpen}
                onCancel={() => setFindIdModalOpen(false)}
                footer={<Button onClick={() => setFindIdModalOpen(false)}>Close</Button>}
                width={560}
            >
                <p style={{ color: '#666', marginBottom: 12 }}>Copy the ID and paste it into the invite form.</p>
                <Table
                    size="small"
                    loading={influencerListLoading}
                    dataSource={influencerList}
                    rowKey="id"
                    columns={[
                        { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
                        { title: 'Email', dataIndex: 'email', key: 'email' },
                        { title: 'Name', dataIndex: 'displayName', key: 'displayName' },
                    ]}
                    pagination={influencerList.length <= 10 ? false : { pageSize: 10 }}
                />
            </Modal>

            <Modal title="Edit invitation" open={editModalOpen} onCancel={closeEditModal} footer={null} destroyOnClose width={520}>
                <Form form={editForm} layout="vertical" onFinish={onEditSubmit}>
                    <Form.Item name="message" label="Message">
                        <Input.TextArea rows={2} placeholder="Message to influencer" />
                    </Form.Item>
                    <Form.Item name="proposedDeliverables" label="Deliverables">
                        <Input.TextArea rows={2} placeholder="e.g. 1 Instagram Reel" />
                    </Form.Item>
                    <Form.Item name="proposedTimeline" label="Timeline">
                        <Input placeholder="e.g. 2 weeks" />
                    </Form.Item>
                    <Form.Item name="proposedAmount" label="Proposed amount">
                        <InputNumber min={0} step={100} style={{ width: '100%' }} addonBefore="$" />
                    </Form.Item>
                    <Form.Item name="platform" label="Platform">
                        <Select placeholder="Select platform" allowClear options={PREFERRED_CONTENT_OPTIONS} />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={editSubmitting} style={{ color: '#000000' }}>Save changes</Button>
                        <Button style={{ marginLeft: 8 }} onClick={closeEditModal}>Cancel</Button>
                    </Form.Item>
                </Form>
            </Modal>
        </ConfigProvider>
    )
}
