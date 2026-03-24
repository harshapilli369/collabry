import { useState } from 'react'
import { Card, Col, Modal, Row, Typography, Button } from 'antd'
import { FullscreenOutlined } from '@ant-design/icons'
import {
    PieChart,
    Pie,
    Cell,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    Tooltip,
    ResponsiveContainer,
    Legend,
} from 'recharts'
import type { InvitationResponse } from '../services/invitationService'
import type { PaymentResponse } from '../services/paymentService'

const { Text } = Typography

interface Props {
    invitations: InvitationResponse[]
    payments: PaymentResponse[]
}

type ExpandedChart = 'invitation' | 'payment' | 'earnings' | null

const DARK_BG = '#0d0d0d'
const CARD_BORDER = '#1a1a1a'
const TOOLTIP_STYLE = { background: '#1a1a1a', border: '1px solid #333', borderRadius: 8, color: '#fff' }

const INVITATION_COLORS: Record<string, string> = {
    Pending: '#faad14',
    Negotiating: '#1890ff',
    Accepted: '#52c41a',
    Rejected: '#ff4d4f',
    Other: '#888888',
}

const PAYMENT_COLORS: Record<string, string> = {
    Pending: '#faad14',
    Processing: '#1890ff',
    Paid: '#52c41a',
    Delayed: '#ff4d4f',
}

const MODAL_STYLES = {
    mask: {
        backdropFilter: 'blur(10px)',
        WebkitBackdropFilter: 'blur(10px)',
        background: 'rgba(0, 0, 0, 0.65)',
    },
    content: {
        background: 'rgba(10, 10, 20, 0.92)',
        backdropFilter: 'blur(30px)',
        WebkitBackdropFilter: 'blur(30px)',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        borderRadius: 20,
        boxShadow: '0 40px 80px rgba(0, 0, 0, 0.85)',
    },
    header: {
        background: 'transparent',
        borderBottom: '1px solid rgba(255, 255, 255, 0.06)',
        paddingBottom: 12,
    },
    body: { padding: '24px 24px 16px' },
}

function ChartCard({
    title,
    children,
    onExpand,
}: {
    title: string
    children: React.ReactNode
    onExpand: () => void
}) {
    return (
        <Card
            title={<span style={{ color: '#fff', fontSize: 14 }}>{title}</span>}
            extra={
                <Button
                    type="text"
                    icon={<FullscreenOutlined />}
                    onClick={onExpand}
                    style={{ color: '#555' }}
                    title="View fullscreen"
                />
            }
            style={{
                borderRadius: 16,
                background: DARK_BG,
                border: `1px solid ${CARD_BORDER}`,
                height: '100%',
            }}
            styles={{ body: { paddingTop: 8 } }}
        >
            {children}
        </Card>
    )
}

function EmptyChart() {
    return (
        <div style={{ textAlign: 'center', padding: '32px 0', color: '#555', fontSize: 13 }}>
            No data yet
        </div>
    )
}

export function InfluencerPerformanceCharts({ invitations, payments }: Props) {
    const [expanded, setExpanded] = useState<ExpandedChart>(null)

    // --- Invitation Status Data ---
    const accepted = invitations.filter((i) => i.status === 'ACCEPTED' || i.status === 'CONFIRMED').length
    const pending = invitations.filter((i) => i.status === 'PENDING').length
    const negotiating = invitations.filter((i) => i.status === 'NEGOTIATING').length
    const rejected = invitations.filter((i) => i.status === 'REJECTED').length
    const other = invitations.filter((i) => i.status === 'EXPIRED' || i.status === 'WITHDRAWN').length

    const invitationData = [
        { name: 'Pending', value: pending },
        { name: 'Negotiating', value: negotiating },
        { name: 'Accepted', value: accepted },
        { name: 'Rejected', value: rejected },
        { name: 'Other', value: other },
    ].filter((d) => d.value > 0)

    // --- Payment Count Data ---
    const paymentCountData = [
        { name: 'Pending', value: payments.filter((p) => p.status === 'PENDING').length },
        { name: 'Processing', value: payments.filter((p) => p.status === 'PROCESSING').length },
        { name: 'Paid', value: payments.filter((p) => p.status === 'PAID').length },
        { name: 'Delayed', value: payments.filter((p) => p.status === 'DELAYED').length },
    ].filter((d) => d.value > 0)

    // --- Earnings by Status Data ---
    const sumByStatus = (status: string) =>
        payments
            .filter((p) => p.status === status)
            .reduce((acc, p) => acc + (p.amount ?? 0), 0)

    const earningsData = [
        { name: 'Pending', value: sumByStatus('PENDING') },
        { name: 'Processing', value: sumByStatus('PROCESSING') },
        { name: 'Paid', value: sumByStatus('PAID') },
        { name: 'Delayed', value: sumByStatus('DELAYED') },
    ].filter((d) => d.value > 0)

    const renderPieLabel = ({ name, percent }: { name: string; percent: number }) =>
        percent > 0.05 ? `${(percent * 100).toFixed(0)}%` : ''

    const formatDollar = (value: number) =>
        value >= 1000 ? `$${(value / 1000).toFixed(1)}k` : `$${value}`

    // --- Chart renderers (reused in card + modal) ---
    const renderInvitationChart = (height: number, isModal = false) =>
        invitationData.length === 0 ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <PieChart>
                    <Pie
                        data={invitationData}
                        cx="50%"
                        cy={isModal ? '48%' : '58%'}
                        innerRadius={isModal ? 70 : 50}
                        outerRadius={isModal ? 120 : 75}
                        dataKey="value"
                        label={renderPieLabel}
                        labelLine={false}
                    >
                        {invitationData.map((entry) => (
                            <Cell key={entry.name} fill={INVITATION_COLORS[entry.name] ?? '#888'} />
                        ))}
                    </Pie>
                    <Tooltip
                        contentStyle={TOOLTIP_STYLE}
                        itemStyle={{ color: '#ccc' }}
                        formatter={(value: number, name: string) => [value, name]}
                    />
                    <Legend
                        formatter={(value) => (
                            <Text style={{ color: '#aaa', fontSize: isModal ? 13 : 12 }}>{value}</Text>
                        )}
                    />
                </PieChart>
            </ResponsiveContainer>
        )

    const renderPaymentChart = (height: number, isModal = false) =>
        paymentCountData.length === 0 ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <BarChart
                    data={paymentCountData}
                    margin={{ top: 8, right: 8, left: isModal ? 0 : -20, bottom: 0 }}
                >
                    <XAxis dataKey="name" tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#333' }} tickLine={false} />
                    <YAxis allowDecimals={false} tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#333' }} tickLine={false} />
                    <Tooltip contentStyle={TOOLTIP_STYLE} itemStyle={{ color: '#ccc' }} cursor={{ fill: '#ffffff08' }} />
                    <Bar dataKey="value" name="Payments" radius={[6, 6, 0, 0]}>
                        {paymentCountData.map((entry) => (
                            <Cell key={entry.name} fill={PAYMENT_COLORS[entry.name] ?? '#888'} />
                        ))}
                    </Bar>
                </BarChart>
            </ResponsiveContainer>
        )

    const renderEarningsChart = (height: number, isModal = false) =>
        earningsData.length === 0 ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <BarChart
                    data={earningsData}
                    margin={{ top: 8, right: 8, left: isModal ? 10 : -4, bottom: 0 }}
                >
                    <XAxis dataKey="name" tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#333' }} tickLine={false} />
                    <YAxis tickFormatter={formatDollar} tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#333' }} tickLine={false} />
                    <Tooltip
                        contentStyle={TOOLTIP_STYLE}
                        itemStyle={{ color: '#ccc' }}
                        cursor={{ fill: '#ffffff08' }}
                        formatter={(value: number) => [`$${value.toLocaleString()}`, 'Amount']}
                    />
                    <Bar dataKey="value" name="Amount" radius={[6, 6, 0, 0]}>
                        {earningsData.map((entry) => (
                            <Cell key={entry.name} fill={PAYMENT_COLORS[entry.name] ?? '#888'} />
                        ))}
                    </Bar>
                </BarChart>
            </ResponsiveContainer>
        )

    const CHART_META: Record<NonNullable<ExpandedChart>, { title: string; render: () => React.ReactNode }> = {
        invitation: { title: 'Invitation Status Breakdown', render: () => renderInvitationChart(420, true) },
        payment: { title: 'Payment Overview', render: () => renderPaymentChart(380, true) },
        earnings: { title: 'Earnings by Status', render: () => renderEarningsChart(380, true) },
    }

    return (
        <>
            <Row gutter={[20, 20]}>
                <Col xs={24} md={8}>
                    <ChartCard title="Invitation Status Breakdown" onExpand={() => setExpanded('invitation')}>
                        {renderInvitationChart(220)}
                    </ChartCard>
                </Col>

                <Col xs={24} md={8}>
                    <ChartCard title="Payment Overview" onExpand={() => setExpanded('payment')}>
                        {renderPaymentChart(220)}
                    </ChartCard>
                </Col>

                <Col xs={24} md={8}>
                    <ChartCard title="Earnings by Status" onExpand={() => setExpanded('earnings')}>
                        {renderEarningsChart(220)}
                    </ChartCard>
                </Col>
            </Row>

            <Modal
                open={expanded !== null}
                onCancel={() => setExpanded(null)}
                footer={null}
                width="68vw"
                destroyOnClose
                title={
                    expanded ? (
                        <span style={{ color: '#fff', fontSize: 16, fontWeight: 600 }}>
                            {CHART_META[expanded].title}
                        </span>
                    ) : null
                }
                styles={MODAL_STYLES}
            >
                {expanded && CHART_META[expanded].render()}
            </Modal>
        </>
    )
}
