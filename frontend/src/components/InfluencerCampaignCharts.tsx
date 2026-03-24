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

const { Text } = Typography

interface Props {
    invitations: InvitationResponse[]
}

type ExpandedChart = 'platform' | 'trend' | 'involvement' | null

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

export function InfluencerCampaignCharts({ invitations }: Props) {
    const [expanded, setExpanded] = useState<ExpandedChart>(null)

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
            const key = inv.createdAt.slice(0, 7)
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

    const hasTrendData = trendData.some((d) => d.value > 0)

    const renderPieLabel = ({ name, percent }: { name: string; percent: number }) =>
        percent > 0.05 ? `${(percent * 100).toFixed(0)}%` : ''

    // --- Chart renderers ---
    const renderPlatformChart = (height: number, isModal = false) =>
        platformData.length === 0 ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <PieChart>
                    <Pie
                        data={platformData}
                        cx="50%"
                        cy={isModal ? '48%' : '58%'}
                        innerRadius={isModal ? 70 : 50}
                        outerRadius={isModal ? 120 : 75}
                        dataKey="value"
                        label={renderPieLabel}
                        labelLine={false}
                    >
                        {platformData.map((entry, idx) => (
                            <Cell key={entry.name} fill={PLATFORM_COLORS[idx % PLATFORM_COLORS.length]} />
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

    const renderTrendChart = (height: number, isModal = false) =>
        !hasTrendData ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <BarChart data={trendData} margin={{ top: 8, right: 8, left: isModal ? 0 : -20, bottom: 0 }}>
                    <XAxis dataKey="name" tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#333' }} tickLine={false} />
                    <YAxis allowDecimals={false} tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#333' }} tickLine={false} />
                    <Tooltip
                        contentStyle={TOOLTIP_STYLE}
                        itemStyle={{ color: '#ccc' }}
                        cursor={{ fill: '#ffffff08' }}
                        formatter={(value: number) => [value, 'Invitations']}
                    />
                    <Bar dataKey="value" name="Invitations" fill="#7c3aed" radius={[6, 6, 0, 0]} />
                </BarChart>
            </ResponsiveContainer>
        )

    const renderInvolvementChart = (height: number, isModal = false) =>
        involvementData.length === 0 ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <BarChart data={involvementData} margin={{ top: 8, right: 8, left: isModal ? 0 : -20, bottom: 0 }}>
                    <XAxis dataKey="name" tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#333' }} tickLine={false} />
                    <YAxis allowDecimals={false} tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#333' }} tickLine={false} />
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
                                fill={entry.name === 'Total' ? '#7c3aed' : (INVOLVEMENT_COLORS[entry.name] ?? '#888')}
                            />
                        ))}
                    </Bar>
                </BarChart>
            </ResponsiveContainer>
        )

    const CHART_META: Record<NonNullable<ExpandedChart>, { title: string; render: () => React.ReactNode }> = {
        platform: { title: 'Platform Distribution', render: () => renderPlatformChart(420, true) },
        trend: { title: 'Monthly Invitations (Last 6 Months)', render: () => renderTrendChart(380, true) },
        involvement: { title: 'Campaign Involvement', render: () => renderInvolvementChart(380, true) },
    }

    return (
        <>
            <Row gutter={[20, 20]}>
                <Col xs={24} md={8}>
                    <ChartCard title="Platform Distribution" onExpand={() => setExpanded('platform')}>
                        {renderPlatformChart(220)}
                    </ChartCard>
                </Col>

                <Col xs={24} md={8}>
                    <ChartCard title="Monthly Invitations (Last 6 Months)" onExpand={() => setExpanded('trend')}>
                        {renderTrendChart(220)}
                    </ChartCard>
                </Col>

                <Col xs={24} md={8}>
                    <ChartCard title="Campaign Involvement" onExpand={() => setExpanded('involvement')}>
                        {renderInvolvementChart(220)}
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
