import { useState, useEffect } from 'react'
import { Typography, Card, Button, message, Modal, Form, Input, Rate } from 'antd'
import { StarOutlined, CheckOutlined } from '@ant-design/icons'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'
import {
    getMyInvitationsAsBrand,
    confirmTerms,
    INVITATION_STATUS_LABELS,
    type InvitationResponse,
    type InvitationStatus,
} from '../services/invitationService'
import { submitRating, type RatingRequest } from '../services/ratingService'

const { Title, Text } = Typography
const { TextArea } = Input

const primaryColor = BRAND_PORTAL_PRIMARY

function formatDate(s: string | undefined) {
    if (!s) return '—'
    try {
        return new Date(s).toLocaleDateString(undefined, { dateStyle: 'medium' })
    } catch {
        return s
    }
}

export const BrandCollaborations = () => {
    const [invitations, setInvitations] = useState<InvitationResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [rateModalOpen, setRateModalOpen] = useState(false)
    const [ratingInvitation, setRatingInvitation] = useState<InvitationResponse | null>(null)
    const [submitting, setSubmitting] = useState(false)
    const [confirmingId, setConfirmingId] = useState<number | null>(null)
    const [form] = Form.useForm()

    const load = () => {
        getMyInvitationsAsBrand()
            .then(setInvitations)
            .catch(() => {
                message.error('Failed to load invitations')
                setInvitations([])
            })
            .finally(() => setLoading(false))
    }

    useEffect(() => {
        load()
    }, [])

    const rateableInvitations = invitations.filter(
        (i) => (i.status === 'CONFIRMED' || i.status === 'ACCEPTED') && !i.rated,
    )

    const openRateModal = (inv: InvitationResponse) => {
        setRatingInvitation(inv)
        form.resetFields()
        form.setFieldsValue({ rating: 5 })
        setRateModalOpen(true)
    }

    const closeRateModal = () => {
        setRateModalOpen(false)
        setRatingInvitation(null)
    }

    const onRateSubmit = async (values: { rating: number; review?: string }) => {
        if (!ratingInvitation) return
        setSubmitting(true)
        try {
            const request: RatingRequest = {
                invitationId: ratingInvitation.id,
                rating: values.rating,
                review: values.review?.trim() || undefined,
            }
            await submitRating(request)
            message.success('Thank you! Your rating has been submitted.')
            closeRateModal()
            load()
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to submit rating')
        } finally {
            setSubmitting(false)
        }
    }

    const onConfirmTerms = async (inv: InvitationResponse) => {
        if (inv.status !== 'NEGOTIATING') return
        setConfirmingId(inv.id)
        try {
            await confirmTerms(inv.id)
            message.success('Terms confirmed. You can now rate this influencer after the collaboration.')
            load()
        } catch (e) {
            message.error(e instanceof Error ? e.message : 'Failed to confirm terms')
        } finally {
            setConfirmingId(null)
        }
    }

    return (
        <BrandPortalLayout activeMenuKey="collaborations">
            <div style={{ marginBottom: 30 }}>
                <Title level={1} style={{ color: primaryColor, margin: 0, fontSize: '3rem' }}>
                    My Collaborations
                </Title>
                <Text style={{ color: '#aaa', fontSize: '1.2rem' }}>
                    Invitations you’ve sent. Rate influencers after a collaboration is accepted or confirmed.
                </Text>
            </div>

            {loading ? (
                <Text type="secondary">Loading…</Text>
            ) : invitations.length === 0 ? (
                <Card bordered={false} style={{ borderRadius: 12, background: '#1c1c1c', border: '1px solid #333' }}>
                    <Text type="secondary">
                        You have no invitations yet. Create a campaign and invite influencers from the Dashboard.
                    </Text>
                </Card>
            ) : (
                <>
                    {rateableInvitations.length > 0 && (
                        <Card
                            bordered={false}
                            style={{ marginBottom: 24, borderRadius: 12, background: '#1c1c1c', border: '1px solid #333' }}
                        >
                            <Title level={5} style={{ color: primaryColor, marginBottom: 12 }}>
                                <StarOutlined style={{ marginRight: 8 }} />
                                Rate completed collaborations
                            </Title>
                            <Text type="secondary" style={{ display: 'block', marginBottom: 16 }}>
                                Help other brands by rating influencers you’ve worked with. Your rating and optional
                                review will appear on their profile.
                            </Text>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                {rateableInvitations.map((inv) => (
                                    <Card key={inv.id} size="small" style={{ background: '#0d0d0d', borderRadius: 8, borderColor: '#333' }}>
                                        <div
                                            style={{
                                                display: 'flex',
                                                justifyContent: 'space-between',
                                                alignItems: 'center',
                                                flexWrap: 'wrap',
                                                gap: 12,
                                            }}
                                        >
                                            <div>
                                                <Text strong style={{ color: '#fff' }}>
                                                    Invitation #{inv.id}
                                                </Text>
                                                <Text type="secondary" style={{ display: 'block', fontSize: 12 }}>
                                                    Campaign #{inv.campaignId} · Influencer ID {inv.influencerId} ·{' '}
                                                    {inv.status === 'CONFIRMED' ? 'Confirmed' : 'Accepted'}{' '}
                                                    {formatDate(inv.updatedAt ?? inv.createdAt)}
                                                </Text>
                                            </div>
                                            <Button
                                                type="primary"
                                                icon={<StarOutlined />}
                                                onClick={() => openRateModal(inv)}
                                                style={{ color: '#000000' }}
                                            >
                                                Rate influencer
                                            </Button>
                                        </div>
                                    </Card>
                                ))}
                            </div>
                        </Card>
                    )}
                    <Title level={5} style={{ color: '#888', marginBottom: 16 }}>
                        All invitations
                    </Title>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                        {invitations.map((inv) => (
                            <Card key={inv.id} size="small" style={{ background: '#1c1c1c', borderRadius: 8, borderColor: '#333' }}>
                                <div
                                    style={{
                                        display: 'flex',
                                        justifyContent: 'space-between',
                                        alignItems: 'flex-start',
                                        flexWrap: 'wrap',
                                        gap: 12,
                                    }}
                                >
                                    <div>
                                        <Text strong style={{ color: '#fff', fontSize: 16 }}>
                                            Invitation #{inv.id}
                                        </Text>
                                        {inv.brandMessage && (
                                            <div style={{ marginTop: 6 }}>
                                                <Text type="secondary" style={{ fontSize: 13 }}>
                                                    {inv.brandMessage.slice(0, 100)}
                                                    {inv.brandMessage.length > 100 ? '…' : ''}
                                                </Text>
                                            </div>
                                        )}
                                        <div style={{ marginTop: 6 }}>
                                            <Text type="secondary" style={{ fontSize: 12 }}>
                                                Campaign #{inv.campaignId} · Influencer ID {inv.influencerId}
                                                {inv.proposedAmount != null &&
                                                    ` · $${Number(inv.proposedAmount).toLocaleString('en-US', { minimumFractionDigits: 2 })}`}
                                                {inv.respondedAt && ` · Responded ${formatDate(inv.respondedAt)}`}
                                            </Text>
                                        </div>
                                    </div>
                                    <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                                        <Text
                                            style={{
                                                fontSize: 12,
                                                fontWeight: 600,
                                                color:
                                                    inv.status === 'CONFIRMED' || inv.status === 'ACCEPTED'
                                                        ? primaryColor
                                                        : inv.status === 'NEGOTIATING'
                                                          ? '#faad14'
                                                          : '#888',
                                            }}
                                        >
                                            {INVITATION_STATUS_LABELS[inv.status as InvitationStatus]}
                                        </Text>
                                        {inv.status === 'NEGOTIATING' && (
                                            <Button
                                                type="primary"
                                                size="small"
                                                icon={<CheckOutlined />}
                                                loading={confirmingId === inv.id}
                                                onClick={() => onConfirmTerms(inv)}
                                                style={{ color: '#000000' }}
                                            >
                                                Confirm terms
                                            </Button>
                                        )}
                                        {(inv.status === 'CONFIRMED' || inv.status === 'ACCEPTED') && !inv.rated && (
                                            <Button type="default" size="small" icon={<StarOutlined />} onClick={() => openRateModal(inv)}>
                                                Rate
                                            </Button>
                                        )}
                                        {(inv.status === 'CONFIRMED' || inv.status === 'ACCEPTED') && inv.rated && (
                                            <Text type="secondary" style={{ fontSize: 12 }}>
                                                Rated
                                            </Text>
                                        )}
                                    </div>
                                </div>
                            </Card>
                        ))}
                    </div>
                </>
            )}

            <Modal title="Rate this influencer" open={rateModalOpen} onCancel={closeRateModal} footer={null} destroyOnClose>
                {ratingInvitation && (
                    <div style={{ marginBottom: 16 }}>
                        <Text type="secondary">
                            Invitation #{ratingInvitation.id} · Campaign #{ratingInvitation.campaignId} · Influencer ID{' '}
                            {ratingInvitation.influencerId}
                        </Text>
                    </div>
                )}
                <Form form={form} layout="vertical" onFinish={onRateSubmit} initialValues={{ rating: 5 }}>
                    <Form.Item name="rating" label="Star rating (1–5)" rules={[{ required: true, message: 'Please select a rating' }]}>
                        <Rate count={5} style={{ fontSize: 28 }} />
                    </Form.Item>
                    <Form.Item name="review" label="Written review (optional)">
                        <TextArea
                            rows={4}
                            placeholder="Share your experience working with this influencer. Your review will be visible on their profile."
                            maxLength={1000}
                            showCount
                        />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={submitting} style={{ color: '#000000' }}>
                            Submit rating
                        </Button>
                        <Button style={{ marginLeft: 8 }} onClick={closeRateModal}>
                            Cancel
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>
        </BrandPortalLayout>
    )
}
