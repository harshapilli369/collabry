import { useState, useEffect } from 'react'
import { Typography, Card, Row, Col, Tag, message, Steps, Select, Button, Modal, Form, Input } from 'antd'
import { TeamOutlined, CheckCircleOutlined, SyncOutlined, SendOutlined, LinkOutlined } from '@ant-design/icons'
import {
    getMyCollaborations,
    updateDeliverable,
    INVITATION_STATUS_LABELS,
    DELIVERABLE_STATUS_LABELS,
    type InvitationResponse,
    type InvitationStatus,
    type DeliverableStatus,
} from '../services/invitationService'
import { InfluencerPortalLayout, INFLUENCER_PORTAL_PRIMARY } from '../components/InfluencerPortalLayout'

const { Title, Text } = Typography


const DELIVERABLE_STEPS: DeliverableStatus[] = ['NOT_STARTED', 'IN_PROGRESS', 'SUBMITTED', 'APPROVED']

function stepIndex(status?: string): number {
    const idx = DELIVERABLE_STEPS.indexOf((status ?? 'NOT_STARTED') as DeliverableStatus)
    return idx >= 0 ? idx : 0
}

function statusTagColor(status?: string): string {
    switch (status) {
        case 'IN_PROGRESS': return 'processing'
        case 'SUBMITTED': return 'warning'
        case 'APPROVED': return 'success'
        default: return 'default'
    }
}

export const Collaborations = () => {
    const [collaborations, setCollaborations] = useState<InvitationResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [submitModalOpen, setSubmitModalOpen] = useState(false)
    const [selectedInv, setSelectedInv] = useState<InvitationResponse | null>(null)
    const [submitting, setSubmitting] = useState(false)
    const [form] = Form.useForm()

    const load = () => {
        getMyCollaborations()
            .then(setCollaborations)
            .catch(() => {
                message.error('Failed to load collaborations')
                setCollaborations([])
            })
            .finally(() => setLoading(false))
    }

    useEffect(() => { load() }, [])

    const activeCount = collaborations.filter(c => c.deliverableStatus !== 'APPROVED').length
    const completedCount = collaborations.filter(c => c.deliverableStatus === 'APPROVED').length
    const submittedCount = collaborations.filter(c => c.deliverableStatus === 'SUBMITTED').length

    const handleStatusChange = async (inv: InvitationResponse, newStatus: string) => {
        try {
            await updateDeliverable(inv.id, { deliverableStatus: newStatus })
            message.success('Status updated')
            load()
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to update status')
        }
    }

    const openSubmitModal = (inv: InvitationResponse) => {
        setSelectedInv(inv)
        form.setFieldsValue({ contentLink: inv.contentLink || '', deliverableNotes: inv.deliverableNotes || '' })
        setSubmitModalOpen(true)
    }

    const closeSubmitModal = () => {
        setSubmitModalOpen(false)
        setSelectedInv(null)
    }

    const onSubmitContent = async (values: { contentLink: string; deliverableNotes?: string }) => {
        if (!selectedInv) return
        setSubmitting(true)
        try {
            await updateDeliverable(selectedInv.id, {
                deliverableStatus: 'SUBMITTED',
                contentLink: values.contentLink,
                deliverableNotes: values.deliverableNotes || undefined,
            })
            message.success('Content submitted! Waiting for brand approval.')
            closeSubmitModal()
            load()
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to submit content')
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <InfluencerPortalLayout activeMenuKey="collaborations">
            <div style={{ marginBottom: 30 }}>
                <Title level={1} style={{ color: INFLUENCER_PORTAL_PRIMARY, margin: 0, fontSize: '2.5rem' }}>My Collaborations</Title>
                <Text style={{ color: '#aaa', fontSize: '1.1rem' }}>Track your deliverables and manage active campaigns.</Text>
            </div>

            {loading ? (
                <Text type="secondary">Loading...</Text>
            ) : collaborations.length === 0 ? (
                <Card style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a', textAlign: 'center', padding: '40px 0' }}>
                    <TeamOutlined style={{ fontSize: 40, opacity: 0.2, color: INFLUENCER_PORTAL_PRIMARY, marginBottom: 16, display: 'block' }} />
                    <Text type="secondary" style={{ fontSize: 16 }}>You have no collaborations yet.</Text>
                    <br />
                    <Text type="secondary" style={{ fontSize: 13 }}>Accept an invitation to get started.</Text>
                </Card>
            ) : (
                <>
                    {/* Stats Row */}
                    <Row gutter={[20, 20]} style={{ marginBottom: 24 }}>
                        <Col span={8}>
                            <Card style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: `1px solid ${INFLUENCER_PORTAL_PRIMARY}20` }}>
                                <TeamOutlined style={{ fontSize: 20, color: INFLUENCER_PORTAL_PRIMARY, marginBottom: 8 }} />
                                <Text type="secondary" style={{ display: 'block' }}>Active</Text>
                                <Title level={2} style={{ margin: '8px 0 0', color: INFLUENCER_PORTAL_PRIMARY }}>{activeCount}</Title>
                            </Card>
                        </Col>
                        <Col span={8}>
                            <Card style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: '1px solid #faad1420' }}>
                                <SendOutlined style={{ fontSize: 20, color: '#faad14', marginBottom: 8 }} />
                                <Text type="secondary" style={{ display: 'block' }}>Submitted</Text>
                                <Title level={2} style={{ margin: '8px 0 0', color: '#faad14' }}>{submittedCount}</Title>
                            </Card>
                        </Col>
                        <Col span={8}>
                            <Card style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: '1px solid #52c41a20' }}>
                                <CheckCircleOutlined style={{ fontSize: 20, color: '#52c41a', marginBottom: 8 }} />
                                <Text type="secondary" style={{ display: 'block' }}>Completed</Text>
                                <Title level={2} style={{ margin: '8px 0 0', color: '#52c41a' }}>{completedCount}</Title>
                            </Card>
                        </Col>
                    </Row>

                    {/* Collaboration Cards */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                        {collaborations.map((inv) => {
                            const ds = (inv.deliverableStatus ?? 'NOT_STARTED') as DeliverableStatus
                            const isApproved = ds === 'APPROVED'
                            const borderColor = isApproved ? '#52c41a' : ds === 'SUBMITTED' ? '#faad14' : ds === 'IN_PROGRESS' ? INFLUENCER_PORTAL_PRIMARY : '#333'

                            return (
                                <Card
                                    key={inv.id}
                                    style={{
                                        background: '#0d0d0d',
                                        borderRadius: 16,
                                        borderLeft: `4px solid ${borderColor}`,
                                        borderColor: '#1a1a1a',
                                    }}
                                >
                                    {/* Header */}
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 12, marginBottom: 16 }}>
                                        <div>
                                            <Text strong style={{ color: '#fff', fontSize: 18 }}>
                                                {inv.campaignName || `Campaign #${inv.campaignId}`}
                                            </Text>
                                            {inv.brandName && (
                                                <div style={{ marginTop: 4 }}>
                                                    <Text type="secondary" style={{ fontSize: 13 }}>by {inv.brandName}</Text>
                                                    {inv.brandNiche && <Tag style={{ marginLeft: 8, fontSize: 11 }}>{inv.brandNiche}</Tag>}
                                                </div>
                                            )}
                                            <div style={{ marginTop: 6 }}>
                                                <Text type="secondary" style={{ fontSize: 12 }}>
                                                    {inv.proposedAmount != null && `$${Number(inv.proposedAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}`}
                                                    {inv.proposedTimeline && ` · Timeline: ${inv.proposedTimeline}`}
                                                    {inv.platform && ` · ${inv.platform}`}
                                                    {` · ${INVITATION_STATUS_LABELS[inv.status as InvitationStatus]}`}
                                                </Text>
                                            </div>
                                        </div>
                                        <Tag color={statusTagColor(ds)} icon={ds === 'IN_PROGRESS' ? <SyncOutlined spin /> : undefined}>
                                            {DELIVERABLE_STATUS_LABELS[ds]}
                                        </Tag>
                                    </div>

                                    {/* Progress Steps */}
                                    <Steps
                                        current={stepIndex(ds)}
                                        size="small"
                                        className="premium-steps"
                                        style={{ marginBottom: 16 }}
                                        items={DELIVERABLE_STEPS.map((step) => ({
                                            title: DELIVERABLE_STATUS_LABELS[step],
                                        }))}
                                    />

                                    {/* Deliverables info */}
                                    {inv.proposedDeliverables && (
                                        <div style={{ marginBottom: 12, padding: '8px 12px', background: '#141414', borderRadius: 8 }}>
                                            <Text type="secondary" style={{ fontSize: 12 }}>Deliverables: </Text>
                                            <Text style={{ fontSize: 13, color: '#ccc' }}>{inv.proposedDeliverables}</Text>
                                        </div>
                                    )}

                                    {/* Content link if submitted */}
                                    {inv.contentLink && (
                                        <div style={{ marginBottom: 12, padding: '8px 12px', background: '#141414', borderRadius: 8 }}>
                                            <LinkOutlined style={{ marginRight: 8, color: INFLUENCER_PORTAL_PRIMARY }} />
                                            <a href={inv.contentLink} target="_blank" rel="noopener noreferrer" style={{ color: INFLUENCER_PORTAL_PRIMARY }}>
                                                {inv.contentLink.length > 60 ? inv.contentLink.slice(0, 60) + '...' : inv.contentLink}
                                            </a>
                                            {inv.deliverableNotes && (
                                                <Text type="secondary" style={{ display: 'block', marginTop: 4, fontSize: 12 }}>
                                                    Notes: {inv.deliverableNotes}
                                                </Text>
                                            )}
                                        </div>
                                    )}

                                    {/* Actions */}
                                    {!isApproved && (
                                        <div style={{ display: 'flex', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
                                            {ds !== 'SUBMITTED' && (
                                                <Select
                                                    value={ds}
                                                    onChange={(val) => handleStatusChange(inv, val)}
                                                    style={{ width: 160 }}
                                                    options={[
                                                        { value: 'NOT_STARTED', label: 'Not Started' },
                                                        { value: 'IN_PROGRESS', label: 'In Progress' },
                                                    ]}
                                                />
                                            )}
                                            {ds !== 'SUBMITTED' && (
                                                <Button
                                                    type="primary"
                                                    icon={<SendOutlined />}
                                                    onClick={() => openSubmitModal(inv)}
                                                    style={{ color: '#000' }}
                                                >
                                                    Submit Content
                                                </Button>
                                            )}
                                            {ds === 'SUBMITTED' && (
                                                <Text type="secondary" style={{ fontSize: 13 }}>
                                                    <SyncOutlined spin style={{ marginRight: 6 }} />
                                                    Waiting for brand approval...
                                                </Text>
                                            )}
                                        </div>
                                    )}
                                    {isApproved && (
                                        <Text style={{ color: '#52c41a', fontSize: 13 }}>
                                            <CheckCircleOutlined style={{ marginRight: 6 }} />
                                            Deliverable approved by brand!
                                        </Text>
                                    )}
                                </Card>
                            )
                        })}
                    </div>
                </>
            )}

            {/* Submit Content Modal */}
            <Modal
                title="Submit Content"
                open={submitModalOpen}
                onCancel={closeSubmitModal}
                footer={null}
                destroyOnClose
                width={520}
                rootClassName="influencer-portal"
            >
                {selectedInv && (
                    <div style={{ marginBottom: 16 }}>
                        <Text type="secondary">
                            {selectedInv.campaignName || `Campaign #${selectedInv.campaignId}`}
                            {selectedInv.brandName && ` · ${selectedInv.brandName}`}
                        </Text>
                    </div>
                )}
                <Form form={form} layout="vertical" onFinish={onSubmitContent}>
                    <Form.Item
                        name="contentLink"
                        label="Content URL"
                        rules={[
                            { required: true, message: 'Please enter a link to your content' },
                            { type: 'url', message: 'Please enter a valid URL' },
                        ]}
                    >
                        <Input placeholder="https://drive.google.com/file/..." prefix={<LinkOutlined />} />
                    </Form.Item>
                    <Form.Item name="deliverableNotes" label="Notes (optional)">
                        <Input.TextArea rows={3} placeholder="Any additional notes for the brand..." maxLength={500} showCount />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={submitting} icon={<SendOutlined />} style={{ color: '#000' }}>
                            Submit for Review
                        </Button>
                        <Button style={{ marginLeft: 8 }} onClick={closeSubmitModal}>
                            Cancel
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>
        </InfluencerPortalLayout>
    )
}
