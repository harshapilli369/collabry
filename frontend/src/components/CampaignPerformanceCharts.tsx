import { Card, Col, Row, Typography } from 'antd'
import {
    PieChart,
    Pie,
    Cell,
    AreaChart,
    Area,
    BarChart,
    Bar,
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

const DARK_BG = '#0d0d0d'
const CARD_BORDER = '#1a1a1a'
const BRAND_YELLOW = '#FFFD82'
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


function ChartCard({
    title,
    subtitle,
    children,
}: {
    title: string
    subtitle?: string
    children: React.ReactNode
}) {
    return (
        <Card
            title={
                <div>
                    <span style={{ color: '#fff', fontSize: 14 }}>{title}</span>
                    {subtitle && <div style={{ color: '#666', fontSize: 11, fontWeight: 400, marginTop: 2 }}>{subtitle}</div>}
                </div>
            }
            style={{
                borderRadius: 16,
                background: DARK_BG,
                border: `1px solid ${CARD_BORDER}`,
                height: '100%',
                transition: 'border-color 0.2s, box-shadow 0.2s',
            }}
            className="chart-card-hover"
            styles={{ body: { paddingTop: 8 } }}
        >
            {children}
        </Card>
    )
}

const renderPieLabel = (props: PieLabelRenderProps) => {
    const { cx, cy, midAngle, outerRadius, percent } = props as any
    if (!percent || percent < 0.05) return null
    const RADIAN = Math.PI / 180
    const radius = (outerRadius ?? 78) + 18
    const x = (cx ?? 0) + radius * Math.cos(-midAngle * RADIAN)
    const y = (cy ?? 0) + radius * Math.sin(-midAngle * RADIAN)
    return (
        <text x={x} y={y} fill="#ccc" textAnchor={x > (cx ?? 0) ? 'start' : 'end'} dominantBaseline="central" fontSize={12} fontWeight={600}>
            {`${(percent * 100).toFixed(0)}%`}
        </text>
    )
}

function buildCampaignStatusData(campaigns: CampaignResponse[]) {
    return [
        { name: 'Draft', value: campaigns.filter((c) => c.status === 'DRAFT').length },
        { name: 'Active', value: campaigns.filter((c) => c.status === 'ACTIVE').length },
        { name: 'Completed', value: campaigns.filter((c) => c.status === 'COMPLETED').length },
        { name: 'Cancelled', value: campaigns.filter((c) => c.status === 'CANCELLED').length },
    ].filter((d) => d.value > 0)
}

function buildInvitationData(sentInvitations: InvitationResponse[]) {
    return [
        { name: 'Accepted', value: sentInvitations.filter((i) => i.status === 'ACCEPTED' || i.status === 'CONFIRMED').length },
        { name: 'Rejected', value: sentInvitations.filter((i) => i.status === 'REJECTED').length },
        { name: 'Pending', value: sentInvitations.filter((i) => i.status === 'PENDING' || i.status === 'NEGOTIATING').length },
        { name: 'Other', value: sentInvitations.filter((i) => i.status === 'EXPIRED' || i.status === 'WITHDRAWN').length },
    ].filter((d) => d.value > 0)
}

function buildMonthlyCampaignData(campaigns: CampaignResponse[]) {
    const months: Record<string, number> = {}
    const now = new Date()
    for (let i = 5; i >= 0; i--) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
        const key = d.toLocaleString('default', { month: 'short', year: '2-digit' })
        months[key] = 0
    }
    campaigns.forEach((c) => {
        if (!c.createdAt) return
        const d = new Date(c.createdAt)
        const key = d.toLocaleString('default', { month: 'short', year: '2-digit' })
        if (key in months) months[key]++
    })
    return Object.entries(months).map(([name, count]) => ({ name, campaigns: count }))
}

function buildInvitationTrendData(invitations: InvitationResponse[]) {
    const months: Record<string, { sent: number; accepted: number }> = {}
    const now = new Date()
    for (let i = 5; i >= 0; i--) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
        const key = d.toLocaleString('default', { month: 'short', year: '2-digit' })
        months[key] = { sent: 0, accepted: 0 }
    }
    invitations.forEach((inv) => {
        if (!inv.createdAt) return
        const d = new Date(inv.createdAt)
        const key = d.toLocaleString('default', { month: 'short', year: '2-digit' })
        if (key in months) {
            months[key].sent++
            if (inv.status === 'ACCEPTED' || inv.status === 'CONFIRMED') months[key].accepted++
        }
    })
    return Object.entries(months).map(([name, data]) => ({ name, ...data }))
}

function EmptyChart() {
    return (
        <div style={{ textAlign: 'center', padding: '32px 0', color: '#555', fontSize: 13 }}>
            No data yet
        </div>
    )
}

export function CampaignPerformanceCharts({ campaigns, sentInvitations }: Props) {
    const campaignStatusData = buildCampaignStatusData(campaigns)
    const invitationData = buildInvitationData(sentInvitations)
    const totalCampaigns = campaigns.length
    const totalInvitations = sentInvitations.length
    const monthlyCampaignData = buildMonthlyCampaignData(campaigns)
    const invitationTrendData = buildInvitationTrendData(sentInvitations)

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
                        cy={isModal ? '45%' : '48%'}
                        innerRadius={isModal ? 75 : 40}
                        outerRadius={isModal ? 125 : 68}
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
                        verticalAlign="bottom"
                        iconType="circle"
                        iconSize={8}
                        formatter={(value: string) => {
                            const item = campaignStatusData.find((d) => d.name === value)
                            const pct = item && totalCampaigns > 0 ? Math.round((item.value / totalCampaigns) * 100) : 0
                            return <Text style={{ color: '#aaa', fontSize: isModal ? 13 : 11 }}>{value} ({pct}%)</Text>
                        }}
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
                        cy={isModal ? '45%' : '48%'}
                        innerRadius={isModal ? 75 : 40}
                        outerRadius={isModal ? 125 : 68}
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

    const renderMonthlyCampaignChart = (height: number, isModal = false) => {
        const hasData = monthlyCampaignData.some((d) => d.campaigns > 0)
        return !hasData ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <BarChart data={monthlyCampaignData} margin={{ top: 8, right: 16, left: isModal ? 0 : -16, bottom: 0 }}>
                    <defs>
                        <linearGradient id="monthlyBarGrad" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stopColor={BRAND_YELLOW} stopOpacity={0.9} />
                            <stop offset="100%" stopColor={BRAND_YELLOW} stopOpacity={0.4} />
                        </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1a1a2e" />
                    <XAxis dataKey="name" tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <YAxis allowDecimals={false} tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <Tooltip contentStyle={TOOLTIP_STYLE} itemStyle={{ color: '#ccc' }} formatter={(value: any) => [Number(value ?? 0), 'Campaigns']} />
                    <Bar dataKey="campaigns" name="Campaigns" fill="url(#monthlyBarGrad)" radius={[6, 6, 0, 0]} maxBarSize={40} />
                </BarChart>
            </ResponsiveContainer>
        )
    }

    const renderInvitationTrendChart = (height: number, isModal = false) => {
        const hasData = invitationTrendData.some((d) => d.sent > 0)
        return !hasData ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <AreaChart data={invitationTrendData} margin={{ top: 8, right: 16, left: isModal ? 0 : -16, bottom: 0 }}>
                    <defs>
                        <linearGradient id="sentGrad" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stopColor="#faad14" stopOpacity={0.35} />
                            <stop offset="100%" stopColor="#faad14" stopOpacity={0.02} />
                        </linearGradient>
                        <linearGradient id="acceptGrad" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stopColor="#52c41a" stopOpacity={0.35} />
                            <stop offset="100%" stopColor="#52c41a" stopOpacity={0.02} />
                        </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1a1a2e" />
                    <XAxis dataKey="name" tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <YAxis allowDecimals={false} tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <Tooltip contentStyle={TOOLTIP_STYLE} itemStyle={{ color: '#ccc' }} />
                    <Area type="monotone" dataKey="sent" name="Sent" stroke="#faad14" strokeWidth={2} fill="url(#sentGrad)" dot={{ fill: '#faad14', r: 3, strokeWidth: 0 }} activeDot={{ r: 5, stroke: '#faad14', strokeWidth: 2, fill: '#0d0d0d' }} />
                    <Area type="monotone" dataKey="accepted" name="Accepted" stroke="#52c41a" strokeWidth={2} fill="url(#acceptGrad)" dot={{ fill: '#52c41a', r: 3, strokeWidth: 0 }} activeDot={{ r: 5, stroke: '#52c41a', strokeWidth: 2, fill: '#0d0d0d' }} />
                    <Legend
                        iconType="circle"
                        iconSize={8}
                        formatter={(value: string) => <Text style={{ color: '#aaa', fontSize: isModal ? 13 : 11 }}>{value}</Text>}
                    />
                </AreaChart>
            </ResponsiveContainer>
        )
    }

    return (
        <Row gutter={[20, 20]}>
            <Col xs={24} md={8}>
                <ChartCard title="Campaigns by Status" subtitle={`${campaigns.length} campaigns`}>
                    {renderStatusChart(220)}
                </ChartCard>
            </Col>

            <Col xs={24} md={8}>
                <ChartCard title="Invitation Responses" subtitle={`${sentInvitations.length} sent`}>
                    {renderInvitationChart(220)}
                </ChartCard>
            </Col>

            <Col xs={24} md={8}>
                <ChartCard title="Monthly Campaigns" subtitle="Last 6 months">
                    {renderMonthlyCampaignChart(220)}
                </ChartCard>
            </Col>

            <Col xs={24} md={24}>
                <ChartCard title="Invitation Trends" subtitle="Sent vs Accepted (Last 6 months)">
                    {renderInvitationTrendChart(220)}
                </ChartCard>
            </Col>
        </Row>
    )
}
