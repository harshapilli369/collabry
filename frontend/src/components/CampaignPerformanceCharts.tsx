import { Card, Col, Row, Typography } from 'antd'
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
const TOOLTIP_STYLE = { background: '#1a1a1a', border: '1px solid #333', borderRadius: 8, color: '#fff' }

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

const PAYMENT_COLORS: Record<string, string> = {
    Pending: '#faad14',
    Processing: '#1890ff',
    Paid: '#52c41a',
    Delayed: '#ff4d4f',
}

function ChartCard({ title, children }: { title: string; children: React.ReactNode }) {
    return (
        <Card
            title={<span style={{ color: '#fff', fontSize: 14 }}>{title}</span>}
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

export function CampaignPerformanceCharts({ campaigns, sentInvitations, payments }: Props) {
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
    const other = sentInvitations.filter(
        (i) => i.status === 'EXPIRED' || i.status === 'WITHDRAWN'
    ).length

    const invitationData = [
        { name: 'Accepted', value: accepted },
        { name: 'Rejected', value: rejected },
        { name: 'Pending', value: pending },
        { name: 'Other', value: other },
    ].filter((d) => d.value > 0)

    // --- Payment Overview Data ---
    const paymentData = [
        { name: 'Pending', value: payments.filter((p) => p.status === 'PENDING').length },
        { name: 'Processing', value: payments.filter((p) => p.status === 'PROCESSING').length },
        { name: 'Paid', value: payments.filter((p) => p.status === 'PAID').length },
        { name: 'Delayed', value: payments.filter((p) => p.status === 'DELAYED').length },
    ].filter((d) => d.value > 0)

    const renderCustomLabel = ({ name, percent }: { name: string; percent: number }) =>
        percent > 0.05 ? `${(percent * 100).toFixed(0)}%` : ''

    return (
        <Row gutter={[20, 20]}>
            {/* Campaign Status Chart */}
            <Col xs={24} md={8}>
                <ChartCard title="Campaigns by Status">
                    {campaignStatusData.length === 0 ? (
                        <EmptyChart />
                    ) : (
                        <ResponsiveContainer width="100%" height={220}>
                            <PieChart>
                                <Pie
                                    data={campaignStatusData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={50}
                                    outerRadius={80}
                                    dataKey="value"
                                    label={renderCustomLabel}
                                    labelLine={false}
                                >
                                    {campaignStatusData.map((entry) => (
                                        <Cell key={entry.name} fill={STATUS_COLORS[entry.name] ?? '#888'} />
                                    ))}
                                </Pie>
                                <Tooltip
                                    contentStyle={TOOLTIP_STYLE}
                                    itemStyle={{ color: '#ccc' }}
                                    formatter={(value: number, name: string) => [value, name]}
                                />
                                <Legend
                                    formatter={(value) => (
                                        <Text style={{ color: '#aaa', fontSize: 12 }}>{value}</Text>
                                    )}
                                />
                            </PieChart>
                        </ResponsiveContainer>
                    )}
                </ChartCard>
            </Col>

            {/* Invitation Rate Chart */}
            <Col xs={24} md={8}>
                <ChartCard title="Invitation Response Rate">
                    {invitationData.length === 0 ? (
                        <EmptyChart />
                    ) : (
                        <ResponsiveContainer width="100%" height={220}>
                            <BarChart data={invitationData} margin={{ top: 8, right: 8, left: -20, bottom: 0 }}>
                                <XAxis
                                    dataKey="name"
                                    tick={{ fill: '#888', fontSize: 11 }}
                                    axisLine={{ stroke: '#333' }}
                                    tickLine={false}
                                />
                                <YAxis
                                    allowDecimals={false}
                                    tick={{ fill: '#888', fontSize: 11 }}
                                    axisLine={{ stroke: '#333' }}
                                    tickLine={false}
                                />
                                <Tooltip
                                    contentStyle={TOOLTIP_STYLE}
                                    itemStyle={{ color: '#ccc' }}
                                    cursor={{ fill: '#ffffff08' }}
                                />
                                <Bar dataKey="value" name="Invitations" radius={[6, 6, 0, 0]}>
                                    {invitationData.map((entry) => (
                                        <Cell key={entry.name} fill={INVITATION_COLORS[entry.name] ?? '#888'} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    )}
                </ChartCard>
            </Col>

            {/* Payment Overview Chart */}
            <Col xs={24} md={8}>
                <ChartCard title="Payment Overview">
                    {paymentData.length === 0 ? (
                        <EmptyChart />
                    ) : (
                        <ResponsiveContainer width="100%" height={220}>
                            <BarChart data={paymentData} margin={{ top: 8, right: 8, left: -20, bottom: 0 }}>
                                <XAxis
                                    dataKey="name"
                                    tick={{ fill: '#888', fontSize: 11 }}
                                    axisLine={{ stroke: '#333' }}
                                    tickLine={false}
                                />
                                <YAxis
                                    allowDecimals={false}
                                    tick={{ fill: '#888', fontSize: 11 }}
                                    axisLine={{ stroke: '#333' }}
                                    tickLine={false}
                                />
                                <Tooltip
                                    contentStyle={TOOLTIP_STYLE}
                                    itemStyle={{ color: '#ccc' }}
                                    cursor={{ fill: '#ffffff08' }}
                                />
                                <Bar dataKey="value" name="Payments" radius={[6, 6, 0, 0]}>
                                    {paymentData.map((entry) => (
                                        <Cell key={entry.name} fill={PAYMENT_COLORS[entry.name] ?? '#888'} />
                                    ))}
                                </Bar>
                            </BarChart>
                        </ResponsiveContainer>
                    )}
                </ChartCard>
            </Col>
        </Row>
    )
}
