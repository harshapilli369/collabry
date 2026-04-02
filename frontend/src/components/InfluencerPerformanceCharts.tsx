import { useState } from 'react'
import { Card, Col, Modal, Row, Typography, Button } from 'antd'
import { FullscreenOutlined } from '@ant-design/icons'
import {
    PieChart,
    Pie,
    Cell,
    AreaChart,
    Area,
    XAxis,
    YAxis,
    Tooltip,
    ResponsiveContainer,
    Legend,
    CartesianGrid,
} from 'recharts'
import type { InvitationResponse } from '../services/invitationService'
import type { PaymentResponse } from '../services/paymentService'

const { Text } = Typography

interface Props {
    invitations: InvitationResponse[]
    payments: PaymentResponse[]
}

type ExpandedChart = 'payment' | 'earnings' | null

const DARK_BG = '#0d0d0d'
const CARD_BORDER = '#1a1a1a'
const TOOLTIP_STYLE = { background: '#1a1a2e', border: '1px solid #444', borderRadius: 10, color: '#fff', fontSize: 13 }

const INVITATION_COLORS: Record<string, string> = {
    Pending: '#faad14',
    Negotiating: '#1890ff',
    Accepted: '#52c41a',
    Rejected: '#ff4d4f',
    Other: '#888888',
}

/* Payment status colors kept for potential future per-status coloring */

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

const formatDollar = (value: number) =>
    value >= 1000 ? `$${(value / 1000).toFixed(1)}k` : `$${value}`

function buildInfluencerInvitationData(invitations: InvitationResponse[]) {
    return [
        { name: 'Pending', value: invitations.filter((i) => i.status === 'PENDING').length },
        { name: 'Negotiating', value: invitations.filter((i) => i.status === 'NEGOTIATING').length },
        { name: 'Accepted', value: invitations.filter((i) => i.status === 'ACCEPTED' || i.status === 'CONFIRMED').length },
        { name: 'Rejected', value: invitations.filter((i) => i.status === 'REJECTED').length },
        { name: 'Other', value: invitations.filter((i) => i.status === 'EXPIRED' || i.status === 'WITHDRAWN').length },
    ].filter((d) => d.value > 0)
}

function buildInfluencerPaymentData(payments: PaymentResponse[]) {
    const statuses = ['PENDING', 'PROCESSING', 'PAID', 'DELAYED']
    return statuses.map((status) => {
        const filtered = payments.filter((p) => p.status === status)
        return {
            name: status.charAt(0) + status.slice(1).toLowerCase(),
            count: filtered.length,
            amount: filtered.reduce((s, p) => s + (p.amount ?? 0), 0),
        }
    })
}

function ChartCard({
    title,
    subtitle,
    children,
    onExpand,
}: {
    title: string
    subtitle?: string
    children: React.ReactNode
    onExpand?: () => void
}) {
    return (
        <Card
            title={
                <div>
                    <span style={{ color: '#fff', fontSize: 14 }}>{title}</span>
                    {subtitle && <div style={{ color: '#666', fontSize: 11, fontWeight: 400, marginTop: 2 }}>{subtitle}</div>}
                </div>
            }
            extra={onExpand ? (
                <Button
                    type="text"
                    icon={<FullscreenOutlined />}
                    onClick={(e) => { e.stopPropagation(); onExpand() }}
                    style={{ color: '#888' }}
                    title="View fullscreen"
                />
            ) : undefined}
            onClick={onExpand}
            style={{
                borderRadius: 16,
                background: DARK_BG,
                border: `1px solid ${CARD_BORDER}`,
                height: '100%',
                cursor: onExpand ? 'pointer' : 'default',
                transition: 'border-color 0.2s, box-shadow 0.2s',
            }}
            className="chart-card-hover"
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

    const invitationData = buildInfluencerInvitationData(invitations)
    const paymentAreaData = buildInfluencerPaymentData(payments)
    const totalInvitations = invitations.length
    const hasPaymentData = paymentAreaData.some((d) => d.count > 0)
    const totalEarnings = payments.reduce((s, p) => s + (p.amount ?? 0), 0)

    // --- Chart renderers ---
    const renderInvitationChart = (height: number, isModal = false) =>
        invitationData.length === 0 ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <PieChart>
                    <defs>
                        {invitationData.map((entry) => (
                            <linearGradient key={entry.name} id={`inv-${entry.name}`} x1="0" y1="0" x2="0" y2="1">
                                <stop offset="0%" stopColor={INVITATION_COLORS[entry.name] ?? '#888'} stopOpacity={1} />
                                <stop offset="100%" stopColor={INVITATION_COLORS[entry.name] ?? '#888'} stopOpacity={0.6} />
                            </linearGradient>
                        ))}
                    </defs>
                    <Pie
                        data={invitationData}
                        cx="50%"
                        cy={isModal ? '45%' : '48%'}
                        innerRadius={isModal ? 70 : 40}
                        outerRadius={isModal ? 115 : 65}
                        dataKey="value"
                        label={false}
                        stroke="none"
                        paddingAngle={4}
                    >
                        {invitationData.map((entry) => (
                            <Cell key={entry.name} fill={`url(#inv-${entry.name})`} />
                        ))}
                    </Pie>
                    <Tooltip
                        contentStyle={TOOLTIP_STYLE}
                        itemStyle={{ color: '#ccc' }}
                        formatter={(value: any, name: any) => [`${Number(value ?? 0)} (${totalInvitations > 0 ? Math.round((Number(value) / totalInvitations) * 100) : 0}%)`, String(name ?? '')]}
                    />
                    <Legend
                        verticalAlign="bottom"
                        iconType="circle"
                        iconSize={8}
                        formatter={(value: string) => {
                            const item = invitationData.find((d) => d.name === value)
                            const pct = item && totalInvitations > 0 ? Math.round((item.value / totalInvitations) * 100) : 0
                            return <Text style={{ color: '#aaa', fontSize: isModal ? 13 : 11 }}>{value} ({pct}%)</Text>
                        }}
                    />
                </PieChart>
            </ResponsiveContainer>
        )

    const renderPaymentChart = (height: number, isModal = false) =>
        !hasPaymentData ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <AreaChart data={paymentAreaData} margin={{ top: 8, right: 16, left: isModal ? 0 : -16, bottom: 0 }}>
                    <defs>
                        <linearGradient id="payCountGrad" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stopColor="#52c41a" stopOpacity={0.4} />
                            <stop offset="100%" stopColor="#52c41a" stopOpacity={0.02} />
                        </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1a1a2e" />
                    <XAxis dataKey="name" tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <YAxis allowDecimals={false} tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <Tooltip contentStyle={TOOLTIP_STYLE} itemStyle={{ color: '#ccc' }} />
                    <Area type="monotone" dataKey="count" name="Payments" stroke="#52c41a" strokeWidth={2} fill="url(#payCountGrad)" dot={{ fill: '#52c41a', r: 4, strokeWidth: 0 }} activeDot={{ r: 6, stroke: '#52c41a', strokeWidth: 2, fill: '#0d0d0d' }} />
                </AreaChart>
            </ResponsiveContainer>
        )

    const renderEarningsChart = (height: number, isModal = false) =>
        !hasPaymentData ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <AreaChart data={paymentAreaData} margin={{ top: 8, right: 16, left: isModal ? 10 : -4, bottom: 0 }}>
                    <defs>
                        <linearGradient id="earnGrad" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stopColor="#BD72EB" stopOpacity={0.4} />
                            <stop offset="100%" stopColor="#BD72EB" stopOpacity={0.02} />
                        </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1a1a2e" />
                    <XAxis dataKey="name" tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <YAxis tickFormatter={formatDollar} tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <Tooltip
                        contentStyle={TOOLTIP_STYLE}
                        itemStyle={{ color: '#ccc' }}
                        formatter={(value: any) => {
                            const num = Number(value ?? 0)
                            return [`$${num.toLocaleString()}`, 'Earnings']
                        }}
                    />
                    <Area type="monotone" dataKey="amount" name="Earnings" stroke="#BD72EB" strokeWidth={2} fill="url(#earnGrad)" dot={{ fill: '#BD72EB', r: 4, strokeWidth: 0 }} activeDot={{ r: 6, stroke: '#BD72EB', strokeWidth: 2, fill: '#0d0d0d' }} />
                </AreaChart>
            </ResponsiveContainer>
        )

    const CHART_META: Record<NonNullable<ExpandedChart>, { title: string; render: () => React.ReactNode }> = {
        payment: { title: 'Payment Overview', render: () => renderPaymentChart(380, true) },
        earnings: { title: 'Earnings by Status', render: () => renderEarningsChart(380, true) },
    }

    return (
        <>
            <Row gutter={[20, 20]}>
                <Col xs={24} md={8}>
                    <ChartCard title="Invitation Breakdown" subtitle={`${totalInvitations} total`}>
                        {renderInvitationChart(220)}
                    </ChartCard>
                </Col>

                <Col xs={24} md={8}>
                    <ChartCard title="Payment Overview" subtitle={`${payments.length} payments`} onExpand={() => setExpanded('payment')}>
                        {renderPaymentChart(220)}
                    </ChartCard>
                </Col>

                <Col xs={24} md={8}>
                    <ChartCard title="Earnings" subtitle={`${formatDollar(totalEarnings)} total`} onExpand={() => setExpanded('earnings')}>
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
