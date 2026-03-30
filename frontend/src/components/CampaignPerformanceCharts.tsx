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
import type { PieLabelRenderProps } from 'recharts'
import type { CampaignResponse } from '../services/campaignService'
import type { InvitationResponse } from '../services/invitationService'
import type { PaymentResponse } from '../services/paymentService'

const { Text } = Typography

interface Props {
    campaigns: CampaignResponse[]
    sentInvitations: InvitationResponse[]
    payments: PaymentResponse[]
}

type ExpandedChart = 'status' | 'invitation' | 'payment' | null

const DARK_BG = '#0d0d0d'
const CARD_BORDER = '#1a1a1a'
const TOOLTIP_STYLE = { background: '#1a1a2e', border: '1px solid #444', borderRadius: 10, color: '#fff', fontSize: 13 }

const STATUS_COLORS: Record<string, string> = {
    Draft: '#888888',
    Active: '#52c41a',
    Completed: '#1890ff',
    Cancelled: '#ff4d4f',
}

const INVITATION_COLORS: Record<string, string> = {
    Accepted: '#52c41a',
    Rejected: '#ff4d4f',
    Pending: '#faad14',
    Other: '#888888',
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
    subtitle,
    children,
    onExpand,
}: {
    title: string
    subtitle?: string
    children: React.ReactNode
    onExpand: () => void
}) {
    return (
        <Card
            title={
                <div>
                    <span style={{ color: '#fff', fontSize: 14 }}>{title}</span>
                    {subtitle && <div style={{ color: '#666', fontSize: 11, fontWeight: 400, marginTop: 2 }}>{subtitle}</div>}
                </div>
            }
            extra={
                <Button
                    type="text"
                    icon={<FullscreenOutlined />}
                    onClick={(e) => { e.stopPropagation(); onExpand() }}
                    style={{ color: '#888' }}
                    title="View fullscreen"
                />
            }
            onClick={onExpand}
            style={{
                borderRadius: 16,
                background: DARK_BG,
                border: `1px solid ${CARD_BORDER}`,
                height: '100%',
                cursor: 'pointer',
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

export function CampaignPerformanceCharts({ campaigns, sentInvitations, payments }: Props) {
    const [expanded, setExpanded] = useState<ExpandedChart>(null)

    // --- Campaign Status Data ---
    const campaignStatusData = [
        { name: 'Draft', value: campaigns.filter((c) => c.status === 'DRAFT').length },
        { name: 'Active', value: campaigns.filter((c) => c.status === 'ACTIVE').length },
        { name: 'Completed', value: campaigns.filter((c) => c.status === 'COMPLETED').length },
        { name: 'Cancelled', value: campaigns.filter((c) => c.status === 'CANCELLED').length },
    ].filter((d) => d.value > 0)

    // --- Invitation Rate Data ---
    const accepted = sentInvitations.filter((i) => i.status === 'ACCEPTED' || i.status === 'CONFIRMED').length
    const rejected = sentInvitations.filter((i) => i.status === 'REJECTED').length
    const pending = sentInvitations.filter((i) => i.status === 'PENDING' || i.status === 'NEGOTIATING').length
    const other = sentInvitations.filter((i) => i.status === 'EXPIRED' || i.status === 'WITHDRAWN').length

    const invitationData = [
        { name: 'Accepted', value: accepted },
        { name: 'Rejected', value: rejected },
        { name: 'Pending', value: pending },
        { name: 'Other', value: other },
    ].filter((d) => d.value > 0)

    // --- Payment Area Data ---
    const paymentAreaData = [
        { name: 'Pending', count: payments.filter((p) => p.status === 'PENDING').length, amount: payments.filter((p) => p.status === 'PENDING').reduce((s, p) => s + (p.amount ?? 0), 0) },
        { name: 'Processing', count: payments.filter((p) => p.status === 'PROCESSING').length, amount: payments.filter((p) => p.status === 'PROCESSING').reduce((s, p) => s + (p.amount ?? 0), 0) },
        { name: 'Paid', count: payments.filter((p) => p.status === 'PAID').length, amount: payments.filter((p) => p.status === 'PAID').reduce((s, p) => s + (p.amount ?? 0), 0) },
        { name: 'Delayed', count: payments.filter((p) => p.status === 'DELAYED').length, amount: payments.filter((p) => p.status === 'DELAYED').reduce((s, p) => s + (p.amount ?? 0), 0) },
    ]

    const hasPaymentData = paymentAreaData.some((d) => d.count > 0)
    const totalSpend = payments.reduce((s, p) => s + (p.amount ?? 0), 0)
    const formatDollar = (value: number) =>
        value >= 1000 ? `$${(value / 1000).toFixed(1)}k` : `$${value}`

    const renderPieLabel = (props: PieLabelRenderProps) => {
        const percent = props.percent ?? 0
        return percent > 0.05 ? `${(percent * 100).toFixed(0)}%` : ''
    }

    // --- Chart renderers ---
    const renderStatusChart = (height: number, isModal = false) =>
        campaignStatusData.length === 0 ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <PieChart>
                    <defs>
                        {campaignStatusData.map((entry) => (
                            <linearGradient key={entry.name} id={`cs-${entry.name}`} x1="0" y1="0" x2="0" y2="1">
                                <stop offset="0%" stopColor={STATUS_COLORS[entry.name] ?? '#888'} stopOpacity={1} />
                                <stop offset="100%" stopColor={STATUS_COLORS[entry.name] ?? '#888'} stopOpacity={0.6} />
                            </linearGradient>
                        ))}
                    </defs>
                    <Pie
                        data={campaignStatusData}
                        cx="50%"
                        cy={isModal ? '48%' : '55%'}
                        innerRadius={isModal ? 75 : 48}
                        outerRadius={isModal ? 125 : 78}
                        dataKey="value"
                        label={renderPieLabel}
                        labelLine={false}
                        stroke="none"
                        paddingAngle={3}
                    >
                        {campaignStatusData.map((entry) => (
                            <Cell key={entry.name} fill={`url(#cs-${entry.name})`} />
                        ))}
                    </Pie>
                    <Tooltip
                        contentStyle={TOOLTIP_STYLE}
                        itemStyle={{ color: '#ccc' }}
                        formatter={(value: any, name: any) => [Number(value ?? 0), String(name ?? '')]}
                    />
                    <Legend
                        formatter={(value: string) => (
                            <Text style={{ color: '#aaa', fontSize: isModal ? 13 : 11 }}>{value}</Text>
                        )}
                    />
                </PieChart>
            </ResponsiveContainer>
        )

    const renderInvitationChart = (height: number, isModal = false) =>
        invitationData.length === 0 ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <PieChart>
                    <defs>
                        {invitationData.map((entry) => (
                            <linearGradient key={entry.name} id={`ci-${entry.name}`} x1="0" y1="0" x2="0" y2="1">
                                <stop offset="0%" stopColor={INVITATION_COLORS[entry.name] ?? '#888'} stopOpacity={1} />
                                <stop offset="100%" stopColor={INVITATION_COLORS[entry.name] ?? '#888'} stopOpacity={0.6} />
                            </linearGradient>
                        ))}
                    </defs>
                    <Pie
                        data={invitationData}
                        cx="50%"
                        cy={isModal ? '48%' : '55%'}
                        innerRadius={isModal ? 75 : 48}
                        outerRadius={isModal ? 125 : 78}
                        dataKey="value"
                        label={renderPieLabel}
                        labelLine={false}
                        stroke="none"
                        paddingAngle={3}
                    >
                        {invitationData.map((entry) => (
                            <Cell key={entry.name} fill={`url(#ci-${entry.name})`} />
                        ))}
                    </Pie>
                    <Tooltip
                        contentStyle={TOOLTIP_STYLE}
                        itemStyle={{ color: '#ccc' }}
                        formatter={(value: any, name: any) => [Number(value ?? 0), String(name ?? '')]}
                    />
                    <Legend
                        formatter={(value: string) => (
                            <Text style={{ color: '#aaa', fontSize: isModal ? 13 : 11 }}>{value}</Text>
                        )}
                    />
                </PieChart>
            </ResponsiveContainer>
        )

    const renderPaymentChart = (height: number, isModal = false) =>
        !hasPaymentData ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <AreaChart data={paymentAreaData} margin={{ top: 8, right: 16, left: isModal ? 10 : -4, bottom: 0 }}>
                    <defs>
                        <linearGradient id="brandPayGrad" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stopColor="#FFFD82" stopOpacity={0.35} />
                            <stop offset="100%" stopColor="#FFFD82" stopOpacity={0.02} />
                        </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1a1a2e" />
                    <XAxis dataKey="name" tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <YAxis tickFormatter={formatDollar} tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <Tooltip
                        contentStyle={TOOLTIP_STYLE}
                        itemStyle={{ color: '#ccc' }}
                        formatter={(value: any) => [`$${Number(value ?? 0).toLocaleString()}`, 'Spend']}
                    />
                    <Area type="monotone" dataKey="amount" name="Spend" stroke="#FFFD82" strokeWidth={2} fill="url(#brandPayGrad)" dot={{ fill: '#FFFD82', r: 4, strokeWidth: 0 }} activeDot={{ r: 6, stroke: '#FFFD82', strokeWidth: 2, fill: '#0d0d0d' }} />
                </AreaChart>
            </ResponsiveContainer>
        )

    const CHART_META: Record<NonNullable<ExpandedChart>, { title: string; render: () => React.ReactNode }> = {
        status: { title: 'Campaigns by Status', render: () => renderStatusChart(420, true) },
        invitation: { title: 'Invitation Response Rate', render: () => renderInvitationChart(420, true) },
        payment: { title: 'Payment Overview', render: () => renderPaymentChart(380, true) },
    }

    return (
        <>
            <Row gutter={[20, 20]}>
                <Col xs={24} md={8}>
                    <ChartCard title="Campaigns by Status" subtitle={`${campaigns.length} campaigns`} onExpand={() => setExpanded('status')}>
                        {renderStatusChart(220)}
                    </ChartCard>
                </Col>

                <Col xs={24} md={8}>
                    <ChartCard title="Invitation Responses" subtitle={`${sentInvitations.length} sent`} onExpand={() => setExpanded('invitation')}>
                        {renderInvitationChart(220)}
                    </ChartCard>
                </Col>

                <Col xs={24} md={8}>
                    <ChartCard title="Payment Overview" subtitle={`${formatDollar(totalSpend)} total`} onExpand={() => setExpanded('payment')}>
                        {renderPaymentChart(220)}
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
