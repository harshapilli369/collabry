import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { Avatar, ConfigProvider, Layout, Menu, Typography, theme } from 'antd'
import type { MenuProps } from 'antd'
import { useNavigate } from 'react-router-dom'
import {
    UserOutlined,
    LogoutOutlined,
    AppstoreOutlined,
    DollarOutlined,
    TeamOutlined,
    MailOutlined,
    CheckCircleFilled,
} from '@ant-design/icons'
import { getMyInfluencerProfile, type InfluencerProfileResponse } from '../services/influencerProfileService'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography

export const INFLUENCER_PORTAL_PRIMARY = '#BD72EB'
export const INFLUENCER_PORTAL_SECONDARY = '#FFFD82'

export type InfluencerPortalMenuKey =
    | 'dashboard'
    | 'invitations'
    | 'collaborations'
    | 'payments'
    | 'profile'

type InfluencerPortalLayoutProps = {
    children: ReactNode
    activeMenuKey: InfluencerPortalMenuKey
    influencerProfileForHeader?: InfluencerProfileResponse | null
}

export function InfluencerPortalLayout({
    children,
    activeMenuKey,
    influencerProfileForHeader,
}: InfluencerPortalLayoutProps) {
    const navigate = useNavigate()
    const [fetchedProfile, setFetchedProfile] = useState<InfluencerProfileResponse | null | undefined>(undefined)

    const userStr = localStorage.getItem('user')
    const user = userStr ? (JSON.parse(userStr) as { email?: string; isVerified?: boolean; role?: string }) : null

    const useOverride = influencerProfileForHeader !== undefined
    const headerProfile = useOverride ? influencerProfileForHeader : fetchedProfile ?? null

    useEffect(() => {
        if (useOverride || user?.role !== 'INFLUENCER') return
        getMyInfluencerProfile()
            .then((p) => setFetchedProfile(p))
            .catch(() => setFetchedProfile(null))
    }, [useOverride, user?.role])

    const handleLogout = useCallback(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        navigate('/login', { replace: true })
    }, [navigate])

    const onMenuClick = useCallback<NonNullable<MenuProps['onClick']>>(
        ({ key }) => {
            if (key === 'logout') {
                handleLogout()
                return
            }
            const paths: Record<string, string> = {
                dashboard: '/influencer/dashboard',
                invitations: '/influencer/invitations',
                collaborations: '/influencer/collaborations',
                payments: '/influencer/payments',
                profile: '/influencer/profile',
            }
            const to = paths[key]
            if (to) navigate(to)
        },
        [navigate, handleLogout],
    )

    const menuItems: MenuProps['items'] = useMemo(
        () => [
            {
                key: 'dashboard',
                icon: <AppstoreOutlined />,
                label: 'Dashboard',
            },
            {
                key: 'invitations',
                icon: <MailOutlined />,
                label: 'Invitations',
            },
            {
                key: 'collaborations',
                icon: <TeamOutlined />,
                label: 'Collaborations',
            },
            {
                key: 'payments',
                icon: <DollarOutlined />,
                label: 'Payments',
            },
            {
                key: 'profile',
                icon: <UserOutlined />,
                label: 'Profile',
            },
            {
                key: 'logout',
                icon: <LogoutOutlined />,
                label: 'Logout',
                danger: true,
            },
        ],
        [],
    )

    const headerHandle = (() => {
        let handle = headerProfile?.instagramHandle
            ? headerProfile.instagramHandle
            : headerProfile?.name || user?.email
        if (handle && !handle.startsWith('@') && !handle.includes('@')) {
            handle = `@${handle}`
        }
        return handle ?? ''
    })()

    return (
        <ConfigProvider
            theme={{
                algorithm: theme.darkAlgorithm,
                token: {
                    colorPrimary: INFLUENCER_PORTAL_PRIMARY,
                    colorTextBase: '#ffffff',
                    fontFamily: 'Inter, sans-serif',
                },
                components: {
                    Layout: {
                        bodyBg: '#000000',
                        headerBg: '#000000',
                        siderBg: '#000000',
                    },
                    Menu: {
                        darkItemBg: '#000000',
                        darkItemSelectedBg: '#2a1a3a',
                    },
                    Descriptions: {
                        colorTextSecondary: '#8c8c8c',
                    },
                },
            }}
        >
            <Layout style={{ minHeight: '100vh' }} className="influencer-portal">
                <Sider
                    width={250}
                    theme="dark"
                    style={{
                        borderRight: `1px solid ${INFLUENCER_PORTAL_PRIMARY}20`,
                        boxShadow: `1px 0 20px ${INFLUENCER_PORTAL_PRIMARY}08`,
                    }}
                >
                    {/* Logo + Portal label */}
                    <div style={{ padding: '24px 20px 8px', textAlign: 'center', cursor: 'pointer' }} onClick={() => navigate('/influencer/dashboard')}>
                        <div
                            style={{
                                width: 44,
                                height: 44,
                                borderRadius: 12,
                                background: `linear-gradient(135deg, ${INFLUENCER_PORTAL_PRIMARY}, #9b59b6)`,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                margin: '0 auto 8px',
                                fontWeight: 900,
                                fontSize: 20,
                                color: '#fff',
                            }}
                        >
                            C
                        </div>
                        <Title level={4} style={{ color: '#fff', margin: 0 }}>
                            Collabry
                        </Title>
                        <Text style={{ color: INFLUENCER_PORTAL_PRIMARY, fontSize: 12, letterSpacing: 1, textTransform: 'uppercase' }}>
                            Influencer Portal
                        </Text>
                    </div>

                    {/* User avatar section */}
                    {headerProfile && (
                        <div style={{ padding: '10px 14px', margin: '6px 16px', background: '#0d0d0d', borderRadius: 10, border: '1px solid #1a1a1a' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                <Avatar
                                    size={32}
                                    src={headerProfile.profilePictureUrl || undefined}
                                    icon={!headerProfile.profilePictureUrl ? <UserOutlined /> : undefined}
                                    style={{ border: `2px solid ${INFLUENCER_PORTAL_PRIMARY}40`, flexShrink: 0 }}
                                />
                                <div style={{ overflow: 'hidden' }}>
                                    <Text style={{ color: '#fff', fontSize: 13, fontWeight: 600, display: 'block', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                        {headerProfile.name || 'Influencer'}
                                    </Text>
                                    <Text style={{ color: '#666', fontSize: 11, display: 'block', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                        {headerProfile.niche || user?.email}
                                    </Text>
                                </div>
                            </div>
                        </div>
                    )}

                    <Menu
                        theme="dark"
                        mode="inline"
                        selectedKeys={[activeMenuKey]}
                        items={menuItems}
                        onClick={onMenuClick}
                        style={{ marginTop: 8 }}
                    />
                </Sider>
                <Layout>
                    <Header
                        style={{
                            padding: '0 24px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'flex-end',
                            background: '#000000',
                            borderBottom: '1px solid #111',
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <Text style={{ color: '#888', fontSize: '0.95rem', fontWeight: 500 }}>{headerHandle}</Text>
                            {user?.isVerified && (
                                <CheckCircleFilled style={{ color: INFLUENCER_PORTAL_PRIMARY, fontSize: '1.2rem' }} title="Verified Influencer" />
                            )}
                            <Avatar
                                size={36}
                                src={headerProfile?.profilePictureUrl || undefined}
                                icon={!headerProfile?.profilePictureUrl ? <UserOutlined /> : undefined}
                                style={{ border: `2px solid ${INFLUENCER_PORTAL_PRIMARY}40` }}
                            />
                        </div>
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>{children}</Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
