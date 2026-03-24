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
import type { InvitationResponse } from '../services/invitationService'

const { Text } = Typography

interface Props {
    invitations: InvitationResponse[]
}

const DARK_BG = '#0d0d0d'
const CARD_BORDER = '#1a1a1a'
const TOOLTIP_STYLE = { background: '#1a1a1a', border: '1px solid #333', borderRadius: 8, color: '#fff' }

const PLATFORM_LABELS: Record<string, string> = {
    INSTAGRAM_REEL: 'IG Reel',
    INSTAGRAM_STORY: 'IG Story',
    YOUTUBE_VIDEO: 'YouTube',
    TIKTOK: 'TikTok',
    BLOG: 'Blog',
}

const PLATFORM_COLORS = ['#e1306c', '#fd1d1d', '#ff0000', '#69c9d0', '#52c41a', '#faad14', '#1890ff']

const INVOLVEMENT_COLORS: Record<string, string> = {
    Accepted: '#52c41a',
    Rejected: '#ff4d4f',
    Pending: '#faad14',
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

export function InfluencerCampaignCharts({ invitations }: Props) {
    // --- Platform Distribution ---
    const platformCounts: Record<string, number> = {}
    for (const inv of invitations) {
        if (inv.platform) {
            const label = PLATFORM_LABELS[inv.platform] ?? inv.platform
            platformCounts[label] = (platformCounts[label] ?? 0) + 1
        }
    }
    const platformData = Object.entries(platformCounts)
        .map(([name, value]) => ({ name, value }))
        .sort((a, b) => b.value - a.value)

    // --- Monthly Invitations Trend (last 6 months) ---
    const now = new Date()
    const months: { label: string; key: string }[] = []
    for (let i = 5; i >= 0; i--) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
        months.push({
            key: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`,
            label: d.toLocaleString('default', { month: 'short', year: '2-digit' }),
        })
    }

    const monthlyCounts: Record<string, number> = {}
    for (const inv of invitations) {
        if (inv.createdAt) {
            const key = inv.createdAt.slice(0, 7) // "YYYY-MM"
            if (monthlyCounts[key] !== undefined || months.some((m) => m.key === key)) {
                monthlyCounts[key] = (monthlyCounts[key] ?? 0) + 1
            }
        }
    }

    const trendData = months.map((m) => ({
        name: m.label,
        value: monthlyCounts[m.key] ?? 0,
    }))

    // --- Campaign Involvement ---
    const uniqueCampaigns = new Set(invitations.map((i) => i.campaignId))
    const acceptedCampaigns = new Set(
        invitations
            .filter((i) => i.status === 'ACCEPTED' || i.status === 'CONFIRMED')
            .map((i) => i.campaignId)
    )
    const rejectedCampaigns = new Set(
        invitations.filter((i) => i.status === 'REJECTED').map((i) => i.campaignId)
    )
    const pendingCampaigns = new Set(
        invitations
            .filter((i) => i.status === 'PENDING' || i.status === 'NEGOTIATING')
            .map((i) => i.campaignId)
    )

    const involvementData = [
        { name: 'Total', value: uniqueCampaigns.size },
        { name: 'Accepted', value: acceptedCampaigns.size },
        { name: 'Pending', value: pendingCampaigns.size },
        { name: 'Rejected', value: rejectedCampaigns.size },
    ].filter((d) => d.value > 0)

    const renderCustomLabel = ({ name, percent }: { name: string; percent: number }) =>
        percent > 0.05 ? `${(percent * 100).toFixed(0)}%` : ''

    const hasTrendData = trendData.some((d) => d.value > 0)

    return (
        <Row gutter={[20, 20]}>
            {/* Platform Distribution */}
            <Col xs={24} md={8}>
                <ChartCard title="Platform Distribution">
                    {platformData.length === 0 ? (
                        <EmptyChart />
                    ) : (
                        <ResponsiveContainer width="100%" height={220}>
                            <PieChart>
                                <Pie
                                    data={platformData}
                                    cx="50%"
                                    cy="50%"
                                    innerRadius={50}
                                    outerRadius={80}
                                    dataKey="value"
                                    label={renderCustomLabel}
                                    labelLine={false}
                                >
                                    {platformData.map((entry, idx) => (
                                        <Cell
                                            key={entry.name}
                                            fill={PLATFORM_COLORS[idx % PLATFORM_COLORS.length]}
                                        />
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

            {/* Monthly Invitations Trend */}
            <Col xs={24} md={8}>
                <ChartCard title="Monthly Invitations (Last 6 Months)">
                    {!hasTrendData ? (
                        <EmptyChart />
                    ) : (
                        <ResponsiveContainer width="100%" height={220}>
                            <BarChart data={trendData} margin={{ top: 8, right: 8, left: -20, bottom: 0 }}>
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
                                    formatter={(value: number) => [value, 'Invitations']}
                                />
                                <Bar dataKey="value" name="Invitations" fill="#7c3aed" radius={[6, 6, 0, 0]} />
                            </BarChart>
                        </ResponsiveContainer>
                    )}
                </ChartCard>
            </Col>

            {/* Campaign Involvement */}
            <Col xs={24} md={8}>
                <ChartCard title="Campaign Involvement">
                    {involvementData.length === 0 ? (
                        <EmptyChart />
                    ) : (
                        <ResponsiveContainer width="100%" height={220}>
                            <BarChart
                                data={involvementData}
                                margin={{ top: 8, right: 8, left: -20, bottom: 0 }}
                            >
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
                                    formatter={(value: number, name: string) => [value, name]}
                                />
                                <Bar dataKey="value" name="Campaigns" radius={[6, 6, 0, 0]}>
                                    {involvementData.map((entry) => (
                                        <Cell
                                            key={entry.name}
                                            fill={
                                                entry.name === 'Total'
                                                    ? '#7c3aed'
                                                    : (INVOLVEMENT_COLORS[entry.name] ?? '#888')
                                            }
                                        />
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
