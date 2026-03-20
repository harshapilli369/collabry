import { useState, useEffect } from 'react'
import { Typography, ConfigProvider, Layout, Menu, Card, Row, Col, Avatar, Table, Tag, Button, message, theme } from 'antd'
import { UserOutlined, LogoutOutlined, MailOutlined, AppstoreOutlined, DollarOutlined, DownloadOutlined, TeamOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { getMyPayments, getInvoice, PAYMENT_STATUS_LABELS, PAYMENT_STATUS_COLORS, type PaymentResponse, type PaymentStatus } from '../services/paymentService'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography

export const PaymentsDashboard = () => {
    const navigate = useNavigate()
    const [payments, setPayments] = useState<PaymentResponse[]>([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        getMyPayments()
            .then(setPayments)
            .catch(() => setPayments([]))
            .finally(() => setLoading(false))
    }, [])

    const handleLogout = () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
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

    const totalEarned = payments.filter(p => p.status === 'PAID').reduce((sum, p) => sum + p.amount, 0)
    const totalPending = payments.filter(p => p.status === 'PENDING' || p.status === 'DELAYED').reduce((sum, p) => sum + p.amount, 0)
    const totalProcessing = payments.filter(p => p.status === 'PROCESSING').reduce((sum, p) => sum + p.amount, 0)

    const primaryColor = '#EFEE96'
    const secondaryColor = '#BD72EB'
    const textColor = '#ffffff'
    const pageBackgroundColor = '#000000'

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
                <Button size="small" icon={<DownloadOutlined />} onClick={() => handleDownloadInvoice(record.id)}>Download</Button>
            )
        },
    ]

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: { colorPrimary: primaryColor, colorTextBase: textColor, fontFamily: 'Inter, sans-serif' },
                components: {
                    Button: {},
                    Layout: { bodyBg: '#000000', headerBg: '#000000', siderBg: '#000000'},
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
                        defaultSelectedKeys={['payments']}
                        items={[
                            { key: 'dashboard', icon: <AppstoreOutlined />, label: 'Dashboard', onClick: () => navigate('/influencer/dashboard') },
                            { key: 'profile', icon: <UserOutlined />, label: 'Profile', onClick: () => navigate('/influencer/profile-setup') },
                            { key: 'invitations', icon: <MailOutlined />, label: 'Invitations', onClick: () => navigate('/influencer/invitations') },
                            { key: 'collaborations', icon: <TeamOutlined />, label: 'Collaborations', onClick: () => navigate('/influencer/collaborations') },
                            { key: 'payments', icon: <DollarOutlined />, label: 'Payments', onClick: () => navigate('/influencer/payments') },
                            { key: 'logout', icon: <LogoutOutlined />, label: 'Logout', onClick: handleLogout, danger: true },
                        ]}
                    />
                </Sider>
                <Layout>
                    <Header style={{ padding: '0 24px', display: 'flex', alignItems: 'center', justifyContent: 'flex-end', background: pageBackgroundColor }}>
                        <Avatar size="large" icon={<UserOutlined />} style={{ backgroundColor: secondaryColor }} />
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>
                        <div style={{ marginBottom: 30 }}>
                            <Title level={1} style={{ color: secondaryColor, margin: 0, fontSize: '3rem' }}>My Payments</Title>
                            <Text style={{ color: '#aaa', fontSize: '1.2rem' }}>Track your campaign earnings and milestones.</Text>
                        </div>

                        <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
                            <Col span={8}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Total Earned</Text>
                                    <Title level={2} style={{ margin: '10px 0 0', color: '#52c41a' }}>${totalEarned.toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                                </Card>
                            </Col>
                            <Col span={8}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Pending</Text>
                                    <Title level={2} style={{ margin: '10px 0 0', color: '#fa8c16' }}>${totalPending.toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                                </Card>
                            </Col>
                            <Col span={8}>
                                <Card bordered={false} style={{ borderRadius: 12, textAlign: 'center' }}>
                                    <Text type="secondary">Processing</Text>
                                    <Title level={2} style={{ margin: '10px 0 0', color: '#1890ff' }}>${totalProcessing.toLocaleString('en-US', { minimumFractionDigits: 2 })}</Title>
                                </Card>
                            </Col>
                        </Row>

                        <Card bordered={false} style={{ borderRadius: 12 }}>
                            <Table
                                dataSource={payments}
                                columns={columns}
                                rowKey="id"
                                loading={loading}
                                pagination={{ pageSize: 10 }}
                                locale={{ emptyText: 'No payments yet.' }}
                            />
                        </Card>
                    </Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
