import { useState, useEffect } from 'react'
import { Typography, Button, Card, Row, Col, Modal, Form, Input, InputNumber, Select, message } from 'antd'
import { PlusCircleOutlined, FundProjectionScreenOutlined, UnorderedListOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'
import { getMyBrandProfile } from '../services/brandService'
import { getMyCampaigns, PREFERRED_CONTENT_OPTIONS, type CampaignResponse } from '../services/campaignService'
import { getSentInvitations, withdrawInvitation, updateInvitation, INVITATION_STATUS_LABELS, type InvitationResponse, type UpdateInvitationRequest } from '../services/invitationService'

const { Title, Text } = Typography

export const BrandDashboard = () => {
    const navigate = useNavigate()
    const [profileCheckDone, setProfileCheckDone] = useState(false)
    const [brandProfile, setBrandProfile] = useState<any>(null)
    const [campaigns, setCampaigns] = useState<CampaignResponse[]>([])
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
        getMyCampaigns()
            .then(setCampaigns)
            .catch(() => setCampaigns([]))
    }, [profileCheckDone, user?.role])

    useEffect(() => {
        if (!profileCheckDone || user?.role !== 'BRAND') return
        setSentInvitationsLoading(true)
        getSentInvitations()
            .then(setSentInvitations)
            .catch(() => setSentInvitations([]))
            .finally(() => setSentInvitationsLoading(false))
    }, [profileCheckDone, user?.role])

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

    const primaryColor = BRAND_PORTAL_PRIMARY

    if (!profileCheckDone && user?.role === 'BRAND') {
        return null
    }

    return (
        <BrandPortalLayout activeMenuKey="dashboard" brandProfileForHeader={brandProfile}>
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

                            <Col span={24}>
                                <Card
                                    title={
                                        <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                            <FundProjectionScreenOutlined />
                                            Campaigns
                                        </span>
                                    }
                                    bordered={false}
                                    style={{ borderRadius: 12 }}
                                >
                                    <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
                                        Open your full campaign list by status, send invitations, and manage details on a dedicated page.
                                    </Text>
                                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12 }}>
                                        <Button
                                            type="primary"
                                            icon={<UnorderedListOutlined />}
                                            onClick={() => navigate('/brand/campaigns')}
                                            style={{ color: '#000000' }}
                                        >
                                            View my campaigns
                                        </Button>
                                        <Button
                                            icon={<PlusCircleOutlined />}
                                            onClick={() => navigate('/brand/campaigns/create')}
                                            disabled={!user?.isVerified}
                                            title={!user?.isVerified ? 'Only verified brands can create campaigns' : ''}
                                        >
                                            Create campaign
                                        </Button>
                                    </div>
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
        </BrandPortalLayout>
    )
}
