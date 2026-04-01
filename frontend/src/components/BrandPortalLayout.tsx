import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { Avatar, ConfigProvider, Layout, Menu, Typography, theme } from 'antd'
import type { MenuProps } from 'antd'
import { useNavigate } from 'react-router-dom'
import {
    UserOutlined,
    LogoutOutlined,
    PlusCircleOutlined,
    AppstoreOutlined,
    FundProjectionScreenOutlined,
    UnorderedListOutlined,
    DollarOutlined,
    TeamOutlined,
    SearchOutlined,
    CheckCircleFilled,
} from '@ant-design/icons'
import { getMyBrandProfile, type BrandProfileResponse } from '../services/brandService'

const { Header, Content, Sider } = Layout
const { Title, Text } = Typography

export const BRAND_PORTAL_PRIMARY = '#FFFD82'

export type BrandPortalMenuKey =
    | 'dashboard'
    | 'campaign-create'
    | 'campaign-view'
    | 'influencers'
    | 'collaborations'
    | 'payments'
    | 'profile'

type BrandPortalLayoutProps = {
    children: ReactNode
    activeMenuKey: BrandPortalMenuKey
    menuOpenKeys?: string[]
    brandProfileForHeader?: BrandProfileResponse | null
}

const pageBackgroundColor = '#000000'

export function BrandPortalLayout({
    children,
    activeMenuKey,
    menuOpenKeys: menuOpenKeysProp,
    brandProfileForHeader,
}: BrandPortalLayoutProps) {
    const navigate = useNavigate()
    const [fetchedProfile, setFetchedProfile] = useState<BrandProfileResponse | null | undefined>(undefined)

    const userStr = localStorage.getItem('user')
    const user = userStr ? (JSON.parse(userStr) as { email?: string; isVerified?: boolean; role?: string }) : null

    const useOverride = brandProfileForHeader !== undefined
    const headerProfile = useOverride ? brandProfileForHeader : fetchedProfile ?? null

    useEffect(() => {
        if (useOverride || user?.role !== 'BRAND') return
        getMyBrandProfile()
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
                dashboard: '/brand/dashboard',
                'campaign-create': '/brand/campaigns/create',
                'campaign-view': '/brand/campaigns',
                influencers: '/brand/influencers',
                collaborations: '/brand/collaborations',
                payments: '/brand/payments',
                profile: '/brand/profile',
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
                key: 'campaign',
                icon: <FundProjectionScreenOutlined />,
                label: 'Campaign',
                children: [
                    {
                        key: 'campaign-create',
                        icon: <PlusCircleOutlined />,
                        label: 'Create campaign',
                    },
                    {
                        key: 'campaign-view',
                        icon: <UnorderedListOutlined />,
                        label: 'View my campaigns',
                    },
                ],
            },
            {
                key: 'influencers',
                icon: <SearchOutlined />,
                label: 'Find influencers',
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

    const computedOpenKeys =
        menuOpenKeysProp ??
        (activeMenuKey === 'dashboard' ||
        activeMenuKey === 'campaign-create' ||
        activeMenuKey === 'campaign-view'
            ? ['campaign']
            : [])

    const headerHandle = (() => {
        let handle = headerProfile?.instagramUrl
            ? headerProfile.instagramUrl.split('/').filter(Boolean).pop()
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
                    colorPrimary: BRAND_PORTAL_PRIMARY,
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
                        darkItemSelectedBg: '#2a2a10',
                    },
                    Descriptions: {
                        colorTextSecondary: '#8c8c8c',
                    },
                },
            }}
        >
            <Layout style={{ minHeight: '100vh' }}>
                <Sider
                    width={250}
                    theme="dark"
                    style={{
                        borderRight: `1px solid ${BRAND_PORTAL_PRIMARY}15`,
                        boxShadow: `1px 0 20px ${BRAND_PORTAL_PRIMARY}05`,
                    }}
                >
                    {/* Logo */}
                    <div style={{ padding: '24px 20px 8px', textAlign: 'center', cursor: 'pointer' }} onClick={() => navigate('/brand/dashboard')}>
                        <div
                            style={{
                                width: 44,
                                height: 44,
                                borderRadius: 12,
                                background: `linear-gradient(135deg, ${BRAND_PORTAL_PRIMARY}, #e6d800)`,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                margin: '0 auto 8px',
                                fontWeight: 900,
                                fontSize: 20,
                                color: '#000',
                            }}
                        >
                            C
                        </div>
                        <Title level={4} style={{ color: '#fff', margin: 0 }}>
                            Collabry
                        </Title>
                        <Text style={{ color: BRAND_PORTAL_PRIMARY, fontSize: 12, letterSpacing: 1, textTransform: 'uppercase' }}>
                            Brand Portal
                        </Text>
                    </div>

                    {/* Brand profile card in sidebar */}
                    {headerProfile && (
                        <div style={{ padding: '16px 20px', margin: '8px 16px', background: '#0d0d0d', borderRadius: 12, border: '1px solid #1a1a1a' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                                <Avatar
                                    size={40}
                                    src={headerProfile.logoUrl || undefined}
                                    style={{
                                        border: `2px solid ${BRAND_PORTAL_PRIMARY}40`,
                                        flexShrink: 0,
                                        backgroundColor: !headerProfile.logoUrl ? BRAND_PORTAL_PRIMARY : undefined,
                                        color: !headerProfile.logoUrl ? '#000' : undefined,
                                        fontWeight: 700,
                                    }}
                                >
                                    {!headerProfile.logoUrl && (headerProfile.name?.charAt(0) || 'B')}
                                </Avatar>
                                <div style={{ overflow: 'hidden' }}>
                                    <Text style={{ color: '#fff', fontSize: 14, fontWeight: 600, display: 'block', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                        {headerProfile.name || 'Brand'}
                                    </Text>
                                    <Text style={{ color: '#666', fontSize: 12, display: 'block', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                        {headerProfile.industry || user?.email}
                                    </Text>
                                </div>
                            </div>
                        </div>
                    )}

                    <Menu
                        theme="dark"
                        mode="inline"
                        selectedKeys={[activeMenuKey]}
                        defaultOpenKeys={computedOpenKeys}
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
                            background: pageBackgroundColor,
                            borderBottom: '1px solid #111',
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            <Text style={{ color: '#888', fontSize: '0.95rem', fontWeight: 500 }}>{headerHandle}</Text>
                            {user?.isVerified && (
                                <CheckCircleFilled style={{ color: BRAND_PORTAL_PRIMARY, fontSize: '1.2rem' }} title="Verified Brand" />
                            )}
                            <Avatar
                                size={36}
                                src={headerProfile?.logoUrl || undefined}
                                style={{
                                    border: `2px solid ${BRAND_PORTAL_PRIMARY}40`,
                                    backgroundColor: !headerProfile?.logoUrl ? BRAND_PORTAL_PRIMARY : undefined,
                                    color: !headerProfile?.logoUrl ? '#000' : undefined,
                                    fontWeight: 700,
                                }}
                            >
                                {!headerProfile?.logoUrl && (headerProfile?.name?.charAt(0) || 'B')}
                            </Avatar>
                        </div>
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>{children}</Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
