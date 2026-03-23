import { useState, useEffect } from 'react'
import { Typography, Card, Row, Col, Table, Tag, Button, message } from 'antd'
import { DownloadOutlined, DollarOutlined, ClockCircleOutlined, SyncOutlined } from '@ant-design/icons'
import { getMyPayments, getInvoice, PAYMENT_STATUS_LABELS, PAYMENT_STATUS_COLORS, type PaymentResponse, type PaymentStatus } from '../services/paymentService'
import { InfluencerPortalLayout, INFLUENCER_PORTAL_PRIMARY } from '../components/InfluencerPortalLayout'

const { Title, Text } = Typography

export const PaymentsDashboard = () => {
    const [payments, setPayments] = useState<PaymentResponse[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        getMyPayments()
            .then(setPayments)
            .catch(() => setPayments([]))
            .finally(() => setLoading(false))
    }, [])

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

    const totalEarned = payments.filter(p => p.status === 'PAID').reduce((sum, p) => sum + p.amount, 0)
    const totalPending = payments.filter(p => p.status === 'PENDING' || p.status === 'DELAYED').reduce((sum, p) => sum + p.amount, 0)
    const totalProcessing = payments.filter(p => p.status === 'PROCESSING').reduce((sum, p) => sum + p.amount, 0)

    const columns = [
        { title: 'Campaign', dataIndex: 'campaignName', key: 'campaignName', render: (v: string) => v || 'N/A' },
        { title: 'Milestone', dataIndex: 'milestoneName', key: 'milestoneName' },
        { title: 'Amount', dataIndex: 'amount', key: 'amount', render: (v: number) => `$${v.toLocaleString('en-US', { minimumFractionDigits: 2 })}` },
        { title: 'Status', dataIndex: 'status', key: 'status', render: (status: PaymentStatus) => <Tag color={PAYMENT_STATUS_COLORS[status]}>{PAYMENT_STATUS_LABELS[status]}</Tag> },
        { title: 'Due Date', dataIndex: 'dueDate', key: 'dueDate', render: (v: string) => v || '—' },
        { title: 'Paid Date', dataIndex: 'paidDate', key: 'paidDate', render: (v: string) => v || '—' },
        { title: 'Invoice', dataIndex: 'invoiceNumber', key: 'invoiceNumber' },
        {
            title: 'Actions', key: 'actions', render: (_: unknown, record: PaymentResponse) => (
                <Button size="small" icon={<DownloadOutlined />} onClick={() => handleDownloadInvoice(record.id)} style={{ borderRadius: 8 }}>Download</Button>
            )
        },
    ]

    return (
        <InfluencerPortalLayout activeMenuKey="payments">
            <div style={{ marginBottom: 30 }}>
                <Title level={1} style={{ color: INFLUENCER_PORTAL_PRIMARY, margin: 0, fontSize: '2.5rem' }}>My Payments</Title>
                <Text style={{ color: '#aaa', fontSize: '1.1rem' }}>Track your campaign earnings and milestones.</Text>
            </div>

            <Row gutter={[20, 20]} style={{ marginBottom: 24 }}>
                <Col span={8}>
                    <Card className="influencer-stat-card" style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: '1px solid #52c41a20' }}>
                        <DollarOutlined style={{ fontSize: 24, color: '#52c41a', marginBottom: 8 }} />
                        <Text type="secondary" style={{ display: 'block' }}>Total Earned</Text>
                        <Title level={2} style={{ margin: '8px 0 0', color: '#52c41a' }}>${totalEarned.toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                    </Card>
                </Col>
                <Col span={8}>
                    <Card className="influencer-stat-card" style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: '1px solid #fa8c1620' }}>
                        <ClockCircleOutlined style={{ fontSize: 24, color: '#fa8c16', marginBottom: 8 }} />
                        <Text type="secondary" style={{ display: 'block' }}>Pending</Text>
                        <Title level={2} style={{ margin: '8px 0 0', color: '#fa8c16' }}>${totalPending.toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                    </Card>
                </Col>
                <Col span={8}>
                    <Card className="influencer-stat-card" style={{ borderRadius: 16, textAlign: 'center', background: '#0d0d0d', border: '1px solid #1890ff20' }}>
                        <SyncOutlined style={{ fontSize: 24, color: '#1890ff', marginBottom: 8 }} />
                        <Text type="secondary" style={{ display: 'block' }}>Processing</Text>
                        <Title level={2} style={{ margin: '8px 0 0', color: '#1890ff' }}>${totalProcessing.toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                    </Card>
                </Col>
            </Row>

            <Card style={{ borderRadius: 16, background: '#0d0d0d', border: '1px solid #1a1a1a' }}>
                <Table
                    dataSource={payments}
                    columns={columns}
                    rowKey="id"
                    loading={loading}
                    pagination={{ pageSize: 10 }}
                    locale={{ emptyText: 'No payments yet.' }}
                    className="influencer-table"
                />
            </Card>
        </InfluencerPortalLayout>
    )
}
