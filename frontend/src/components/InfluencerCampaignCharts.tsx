import { useState } from 'react'
import { Card, Col, Modal, Row, Button } from 'antd'
import { FullscreenOutlined } from '@ant-design/icons'
import {
    AreaChart,
    Area,
    XAxis,
    YAxis,
    Tooltip,
    ResponsiveContainer,
    CartesianGrid,
} from 'recharts'
import type { InvitationResponse } from '../services/invitationService'

// Typography unused after cleanup

interface Props {
    invitations: InvitationResponse[]
}

const DARK_BG = '#0d0d0d'
const CARD_BORDER = '#1a1a1a'
const TOOLTIP_STYLE = { background: '#1a1a2e', border: '1px solid #444', borderRadius: 10, color: '#fff', fontSize: 13 }

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

function EmptyChart() {
    return (
        <div style={{ textAlign: 'center', padding: '32px 0', color: '#555', fontSize: 13 }}>
            No data yet
        </div>
    )
}

function buildMonthLabels(): { label: string; key: string }[] {
    const now = new Date()
    const months: { label: string; key: string }[] = []
    for (let i = 5; i >= 0; i--) {
        const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
        months.push({
            key: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`,
            label: d.toLocaleString('default', { month: 'short', year: '2-digit' }),
        })
    }
    return months
}

function buildTrendData(invitations: InvitationResponse[]): { name: string; invitations: number }[] {
    const months = buildMonthLabels()
    const counts: Record<string, number> = {}
    for (const inv of invitations) {
        if (inv.createdAt) {
            const key = inv.createdAt.slice(0, 7)
            if (counts[key] !== undefined || months.some((m) => m.key === key)) {
                counts[key] = (counts[key] ?? 0) + 1
            }
        }
    }
    return months.map((m) => ({ name: m.label, invitations: counts[m.key] ?? 0 }))
}

export function InfluencerCampaignCharts({ invitations }: Props) {
    const [expanded, setExpanded] = useState(false)

    const trendData = buildTrendData(invitations)
    const hasTrendData = trendData.some((d) => d.invitations > 0)

    const renderTrendChart = (height: number, isModal = false) =>
        !hasTrendData ? (
            <EmptyChart />
        ) : (
            <ResponsiveContainer width="100%" height={height}>
                <AreaChart data={trendData} margin={{ top: 8, right: 16, left: isModal ? 0 : -16, bottom: 0 }}>
                    <defs>
                        <linearGradient id="trendGrad" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stopColor="#7c3aed" stopOpacity={0.4} />
                            <stop offset="100%" stopColor="#7c3aed" stopOpacity={0.02} />
                        </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" stroke="#1a1a2e" />
                    <XAxis dataKey="name" tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <YAxis allowDecimals={false} tick={{ fill: '#888', fontSize: isModal ? 13 : 11 }} axisLine={{ stroke: '#222' }} tickLine={false} />
                    <Tooltip
                        contentStyle={TOOLTIP_STYLE}
                        itemStyle={{ color: '#ccc' }}
                        formatter={(value: any) => [Number(value ?? 0), 'Invitations']}
                    />
                    <Area type="monotone" dataKey="invitations" name="Invitations" stroke="#7c3aed" strokeWidth={2} fill="url(#trendGrad)" dot={{ fill: '#7c3aed', r: 4, strokeWidth: 0 }} activeDot={{ r: 6, stroke: '#7c3aed', strokeWidth: 2, fill: '#0d0d0d' }} />
                </AreaChart>
            </ResponsiveContainer>
        )

    return (
        <>
            <Row gutter={[20, 20]}>
                <Col xs={24}>
                    <Card
                        title={
                            <div>
                                <span style={{ color: '#fff', fontSize: 14 }}>Monthly Invitations</span>
                                <div style={{ color: '#666', fontSize: 11, fontWeight: 400, marginTop: 2 }}>Last 6 months</div>
                            </div>
                        }
                        extra={
                            <Button
                                type="text"
                                icon={<FullscreenOutlined />}
                                onClick={() => setExpanded(true)}
                                style={{ color: '#888' }}
                                title="View fullscreen"
                            />
                        }
                        onClick={() => setExpanded(true)}
                        style={{
                            borderRadius: 16,
                            background: DARK_BG,
                            border: `1px solid ${CARD_BORDER}`,
                            cursor: 'pointer',
                            transition: 'border-color 0.2s, box-shadow 0.2s',
                        }}
                        className="chart-card-hover"
                        styles={{ body: { paddingTop: 8 } }}
                    >
                        {renderTrendChart(200)}
                    </Card>
                </Col>
            </Row>

            <Modal
                open={expanded}
                onCancel={() => setExpanded(false)}
                footer={null}
                width="68vw"
                destroyOnClose
                title={<span style={{ color: '#fff', fontSize: 16, fontWeight: 600 }}>Monthly Invitations (Last 6 Months)</span>}
                styles={MODAL_STYLES}
            >
                {renderTrendChart(380, true)}
            </Modal>
        </>
    )
}
