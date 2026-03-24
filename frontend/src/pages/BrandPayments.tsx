import { useState, useEffect } from 'react'
import { Typography, Card, Row, Col, Table, Tag, Button, Modal, Form, InputNumber, Input, DatePicker, Select, message } from 'antd'
import { PlusCircleOutlined, DownloadOutlined, DollarOutlined, ClockCircleOutlined, WarningOutlined } from '@ant-design/icons'
import { BrandPortalLayout, BRAND_PORTAL_PRIMARY } from '../components/BrandPortalLayout'
import { createPayment, getDelayedPayments, getInvoice, updatePaymentStatus, PAYMENT_STATUS_LABELS, PAYMENT_STATUS_COLORS, type PaymentResponse, type PaymentStatus, type PaymentRequest } from '../services/paymentService'
import { getMyCampaigns, type CampaignResponse } from '../services/campaignService'

const { Title, Text } = Typography

export const BrandPayments = () => {
    const [payments, setPayments] = useState<PaymentResponse[]>([])
    const [delayedPayments, setDelayedPayments] = useState<PaymentResponse[]>([])
    const [campaigns, setCampaigns] = useState<CampaignResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [modalOpen, setModalOpen] = useState(false)
    const [creating, setCreating] = useState(false)
    const [form] = Form.useForm()

    const loadData = () => {
        setLoading(true)
        Promise.all([
            getMyCampaigns().catch(() => []),
            getDelayedPayments().catch(() => []),
        ]).then(([camps, delayed]) => {
            setCampaigns(camps)
            setDelayedPayments(delayed)
            // Collect all payments from all campaigns
            const allCampaignIds = camps.map((c: CampaignResponse) => c.id)
            if (allCampaignIds.length === 0) {
                setPayments([])
                setLoading(false)
                return
            }
            Promise.all(
                allCampaignIds.map((id: number) =>
                    fetch(`${(import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth').replace(/\/api\/auth\/?$/, '')}/api/payments/campaign/${id}`, {
                        headers: {
                            'Content-Type': 'application/json',
                            ...(localStorage.getItem('token') ? { Authorization: `Bearer ${localStorage.getItem('token')}` } : {}),
                        },
                    }).then(r => r.ok ? r.json() : []).catch(() => [])
                )
            ).then(results => {
                const all = results.flat()
                setPayments(all)
            }).finally(() => setLoading(false))
        })
    }

    useEffect(() => { loadData() }, [])

    const handleCreate = async (values: { campaignId: number; influencerId: number; milestoneName: string; amount: number; dueDate?: unknown; notes?: string }) => {
        setCreating(true)
        try {
            const payload: PaymentRequest = {
                campaignId: values.campaignId,
                influencerId: values.influencerId,
                milestoneName: values.milestoneName,
                amount: values.amount,
                dueDate: values.dueDate ? (values.dueDate as { format: (f: string) => string }).format('YYYY-MM-DD') : undefined,
                notes: values.notes,
            }
            await createPayment(payload)
            message.success('Payment milestone created')
            setModalOpen(false)
            form.resetFields()
            loadData()
        } catch (e) {
            message.error((e as Error).message || 'Failed to create payment')
        } finally {
            setCreating(false)
        }
    }

    const handleStatusUpdate = async (paymentId: number, status: PaymentStatus) => {
        try {
            await updatePaymentStatus(paymentId, status)
            message.success(`Payment marked as ${PAYMENT_STATUS_LABELS[status]}`)
            loadData()
        } catch (e) {
            message.error((e as Error).message || 'Failed to update status')
        }
    }

    const handleDownloadInvoice = async (paymentId: number) => {
        try {
            const invoice = await getInvoice(paymentId)
            const blob = new Blob([JSON.stringify(invoice, null, 2)], { type: 'application/json' })
            const url = URL.createObjectURL(blob)
            const a = document.createElement('a')
            a.href = url
            a.download = `Invoice-${invoice.invoiceNumber}.json`
            a.click()
            URL.revokeObjectURL(url)
            message.success('Invoice downloaded')
        } catch {
            message.error('Failed to download invoice')
        }
    }

    const primaryColor = BRAND_PORTAL_PRIMARY

    const totalPaid = payments.filter(p => p.status === 'PAID').reduce((s, p) => s + p.amount, 0)
    const totalPending = payments.filter(p => p.status === 'PENDING' || p.status === 'DELAYED').reduce((s, p) => s + p.amount, 0)

    const columns = [
        { title: 'Campaign', dataIndex: 'campaignName', key: 'campaignName', render: (v: string) => v || 'N/A' },
        { title: 'Milestone', dataIndex: 'milestoneName', key: 'milestoneName' },
        { title: 'Influencer ID', dataIndex: 'influencerId', key: 'influencerId' },
        { title: 'Amount', dataIndex: 'amount', key: 'amount', render: (v: number) => `$${v.toLocaleString('en-US', { minimumFractionDigits: 2 })}` },
        {
            title: 'Status', dataIndex: 'status', key: 'status',
            render: (status: PaymentStatus) => <Tag color={PAYMENT_STATUS_COLORS[status]}>{PAYMENT_STATUS_LABELS[status]}</Tag>
        },
        { title: 'Due Date', dataIndex: 'dueDate', key: 'dueDate', render: (v: string) => v || '—' },
        { title: 'Invoice', dataIndex: 'invoiceNumber', key: 'invoiceNumber' },
        {
            title: 'Actions', key: 'actions', render: (_: unknown, record: PaymentResponse) => (
                <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                    {record.status === 'PENDING' && <Button size="small" type="primary" style={{ color: '#000000' }} onClick={() => handleStatusUpdate(record.id, 'PROCESSING')}>Processing</Button>}
                    {record.status === 'PROCESSING' && <Button size="small" type="primary" style={{ background: '#52c41a', borderColor: '#52c41a', color: '#000000' }} onClick={() => handleStatusUpdate(record.id, 'PAID')}>Mark Paid</Button>}
                    {record.status === 'DELAYED' && <Button size="small" type="primary" style={{ color: '#000000' }} onClick={() => handleStatusUpdate(record.id, 'PROCESSING')}>Resume</Button>}
                    <Button size="small" icon={<DownloadOutlined />} onClick={() => handleDownloadInvoice(record.id)} />
                </div>
            ),
        },
    ]

    return (
        <BrandPortalLayout activeMenuKey="payments">
                        <div style={{ marginBottom: 30, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div>
                                <Title level={1} style={{ color: primaryColor, margin: 0, fontSize: '2.5rem' }}>Payments</Title>
                                <Text style={{ color: '#aaa', fontSize: '1.1rem' }}>Manage milestone payments for your campaigns.</Text>
                            </div>
                            <Button type="primary" icon={<PlusCircleOutlined />} size="large" style={{ color: '#000', fontWeight: 600, borderRadius: 10 }} onClick={() => setModalOpen(true)}>
                                Create Payment
                            </Button>
                        </div>

                        <Row gutter={[20, 20]} style={{ marginBottom: 24 }}>
                            <Col span={8}>
                                <Card className="brand-stat-card" style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: '1px solid #52c41a20' }}>
                                    <DollarOutlined style={{ fontSize: 24, color: '#52c41a', marginBottom: 8 }} />
                                    <Text type="secondary" style={{ display: 'block' }}>Total Disbursed</Text>
                                    <Title level={2} style={{ margin: '8px 0 0', color: '#52c41a' }}>${totalPaid.toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                                </Card>
                            </Col>
                            <Col span={8}>
                                <Card className="brand-stat-card" style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: '1px solid #fa8c1620' }}>
                                    <ClockCircleOutlined style={{ fontSize: 24, color: '#fa8c16', marginBottom: 8 }} />
                                    <Text type="secondary" style={{ display: 'block' }}>Pending / Delayed</Text>
                                    <Title level={2} style={{ margin: '8px 0 0', color: '#fa8c16' }}>${totalPending.toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                                </Card>
                            </Col>
                            <Col span={8}>
                                <Card className="brand-stat-card" style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: '1px solid #ff4d4f20' }}>
                                    <WarningOutlined style={{ fontSize: 24, color: '#ff4d4f', marginBottom: 8 }} />
                                    <Text type="secondary" style={{ display: 'block' }}>Delayed Payments</Text>
                                    <Title level={2} style={{ margin: '8px 0 0', color: '#ff4d4f' }}>{delayedPayments.length}</Title>
                                </Card>
                            </Col>
                        </Row>

                        {delayedPayments.length > 0 && (
                            <Card style={{ borderRadius: 12, marginBottom: 24, borderLeft: '4px solid #ff4d4f', background: '#1a0a0a', border: '1px solid #ff4d4f20' }}>
                                <Title level={5} style={{ color: '#ff4d4f', margin: 0 }}>Payment Reminders</Title>
                                <Text type="secondary">{delayedPayments.length} payment(s) are overdue. Please process them as soon as possible.</Text>
                            </Card>
                        )}

                        <Card style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a' }}>
                            <Table
                                dataSource={payments}
                                columns={columns}
                                rowKey="id"
                                loading={loading}
                                pagination={{ pageSize: 10 }}
                                locale={{ emptyText: 'No payments yet. Create one using the button above.' }}
                                className="brand-table"
                            />
                        </Card>

            <Modal
                title="Create Milestone Payment"
                open={modalOpen}
                onCancel={() => setModalOpen(false)}
                footer={null}
            >
                <Form form={form} layout="vertical" onFinish={handleCreate}>
                    <Form.Item name="campaignId" label="Campaign" rules={[{ required: true, message: 'Select a campaign' }]}>
                        <Select placeholder="Select campaign">
                            {campaigns.map(c => <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>)}
                        </Select>
                    </Form.Item>
                    <Form.Item name="influencerId" label="Influencer ID" rules={[{ required: true, message: 'Enter influencer ID' }]}>
                        <InputNumber style={{ width: '100%' }} placeholder="Enter influencer user ID" min={1} />
                    </Form.Item>
                    <Form.Item name="milestoneName" label="Milestone Name" rules={[{ required: true, message: 'Enter milestone name' }]}>
                        <Input placeholder="e.g., Content Delivery, Final Review" />
                    </Form.Item>
                    <Form.Item name="amount" label="Amount ($)" rules={[{ required: true, message: 'Enter amount' }]}>
                        <InputNumber style={{ width: '100%' }} min={0.01} precision={2} placeholder="500.00" />
                    </Form.Item>
                    <Form.Item name="dueDate" label="Due Date">
                        <DatePicker style={{ width: '100%' }} />
                    </Form.Item>
                    <Form.Item name="notes" label="Notes">
                        <Input.TextArea rows={3} placeholder="Additional notes (optional)" />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit" loading={creating} block style={{ color: '#000000' }}>
                            Create Payment
                        </Button>
                    </Form.Item>
                </Form>
            </Modal>
        </BrandPortalLayout>
    )
}
