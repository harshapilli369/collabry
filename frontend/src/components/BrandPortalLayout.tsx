import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { ConfigProvider, Layout, Menu, Typography, theme } from 'antd'
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
    /** Force open submenu keys (defaults include `campaign` when active key is a campaign sub-item). */
    menuOpenKeys?: string[]
    /**
     * When provided (including `null`), used for the header and no extra /me fetch runs.
     * Omit to load profile for the header inside the layout.
     */
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

    /** Submenu item `onClick` is unreliable in Ant Design Menu; navigation is handled here. */
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
                        darkItemSelectedBg: '#333333',
                    },
                    Descriptions: {
                        colorTextSecondary: '#8c8c8c',
                    },
                },
            }}
        >
            <Layout style={{ minHeight: '100vh' }}>
                <Sider width={250} theme="dark">
                    <div style={{ padding: '20px', textAlign: 'center' }}>
                        <Title level={4} style={{ color: '#fff', margin: 0 }}>
                            Collabry
                        </Title>
                        <Text style={{ color: BRAND_PORTAL_PRIMARY }}>Brand Portal</Text>
                    </div>
                    <Menu
                        theme="dark"
                        mode="inline"
                        selectedKeys={[activeMenuKey]}
                        defaultOpenKeys={computedOpenKeys}
                        items={menuItems}
                        onClick={onMenuClick}
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
                        }}
                    >
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <Text style={{ color: '#fff', fontSize: '1.1rem', fontWeight: 500 }}>{headerHandle}</Text>
                            {user?.isVerified && (
                                <CheckCircleFilled style={{ color: '#1890ff', fontSize: '1.2rem' }} title="Verified Brand" />
                            )}
                        </div>
                    </Header>
                    <Content style={{ margin: '24px 16px', padding: 24, minHeight: 280 }}>{children}</Content>
                </Layout>
            </Layout>
        </ConfigProvider>
    )
}
