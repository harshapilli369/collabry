import { useCallback, useEffect, useState } from 'react'
import {
  Button,
  Card,
  Col,
  ConfigProvider,
  Layout,
  Modal,
  Row,
  Space,
  Spin,
  Switch,
  Table,
  Tag,
  Typography,
  message,
  theme,
  Input,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { LogoutOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import {
  fetchAdminDashboard,
  fetchAdminUsers,
  fetchAdminVerificationRequests,
  processAdminVerificationRequest,
  updateAdminUser,
  type AdminActiveCollaboration,
  type AdminDashboardData,
  type AdminRecentSignup,
  type AdminUserSummary,
  type AdminVerificationRequest,
} from '../services/adminService'

const { Header, Content } = Layout
const { Title, Text } = Typography
const { TextArea } = Input

const PRIMARY = '#FFFD82'
const PAGE_BG = '#000000'
const CARD_BG = '#141414'

export function AdminDashboard() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [dashboard, setDashboard] = useState<AdminDashboardData | null>(null)
  const [users, setUsers] = useState<AdminUserSummary[]>([])
  const [verifications, setVerifications] = useState<AdminVerificationRequest[]>([])
  const [usersTotal, setUsersTotal] = useState(0)
  const [usersPage, setUsersPage] = useState(0)
  const [usersPageSize, setUsersPageSize] = useState(10)
  const [updatingId, setUpdatingId] = useState<number | null>(null)
  const [verifyingId, setVerifyingId] = useState<number | null>(null)
  const [rejectModalId, setRejectModalId] = useState<number | null>(null)
  const [rejectReason, setRejectReason] = useState('')

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      const [dash, page, vers] = await Promise.all([
        fetchAdminDashboard(),
        fetchAdminUsers(usersPage, usersPageSize),
        fetchAdminVerificationRequests(),
      ])
      setDashboard(dash)
      setUsers(page.content)
      setUsersTotal(page.totalElements)
      setVerifications(vers)
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Failed to load admin data')
    } finally {
      setLoading(false)
    }
  }, [usersPage, usersPageSize])

  useEffect(() => {
    void loadData()
  }, [loadData])

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    navigate('/login', { replace: true })
  }

  const applyUserUpdate = async (id: number, active: boolean, flagged: boolean) => {
    setUpdatingId(id)
    try {
      await updateAdminUser(id, { active, flagged })
      message.success('User updated')
      await loadData()
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Update failed')
    } finally {
      setUpdatingId(null)
    }
  }

  const handleVerification = async (id: number, approved: boolean, reason?: string) => {
    setVerifyingId(id)
    try {
      await processAdminVerificationRequest(id, { approved, reason })
      message.success(approved ? 'Verification approved' : 'Verification rejected')
      setRejectModalId(null)
      setRejectReason('')
      await loadData()
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Action failed')
    } finally {
      setVerifyingId(null)
    }
  }

  const onActiveChange = (record: AdminUserSummary, checked: boolean) => {
    if (!checked) {
      Modal.confirm({
        title: 'Deactivate this account?',
        content: 'They will not be able to log in or use the API until reactivated.',
        okText: 'Deactivate',
        okType: 'danger',
        onOk: () => applyUserUpdate(record.id, false, record.flagged),
      })
    } else {
      void applyUserUpdate(record.id, true, record.flagged)
    }
  }

  const signupColumns: ColumnsType<AdminRecentSignup> = [
    { title: 'Id', dataIndex: 'id', key: 'id', width: 80 },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Role', dataIndex: 'role', key: 'role', width: 120 },
    {
      title: 'Joined',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (v: string | null) => (v ? new Date(v).toLocaleString() : '—'),
    },
    {
      title: 'Active',
      key: 'active',
      width: 90,
      render: (_, r) => (r.active ? <Tag color="green">Yes</Tag> : <Tag color="red">No</Tag>),
    },
    {
      title: 'Flagged',
      key: 'flagged',
      width: 90,
      render: (_, r) => (r.flagged ? <Tag color="orange">Yes</Tag> : <Tag>No</Tag>),
    },
  ]

  const collabColumns: ColumnsType<AdminActiveCollaboration> = [
    { title: 'Invitation', dataIndex: 'invitationId', key: 'invitationId', width: 100 },
    { title: 'Campaign', dataIndex: 'campaignId', key: 'campaignId', width: 100 },
    { title: 'Brand', dataIndex: 'brandId', key: 'brandId', width: 90 },
    { title: 'Influencer', dataIndex: 'influencerId', key: 'influencerId', width: 110 },
    {
      title: 'Updated',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      render: (v: string | null) => (v ? new Date(v).toLocaleString() : '—'),
    },
  ]

  const userColumns: ColumnsType<AdminUserSummary> = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Role', dataIndex: 'role', key: 'role', width: 120 },
    {
      title: 'Active',
      key: 'active',
      width: 100,
      render: (_, record) => (
        <Switch
          checked={record.active}
          loading={updatingId === record.id}
          onChange={(checked) => onActiveChange(record, checked)}
        />
      ),
    },
    {
      title: 'Flagged',
      key: 'flagged',
      width: 100,
      render: (_, record) => (
        <Switch
          checked={record.flagged}
          loading={updatingId === record.id}
          onChange={(checked) => void applyUserUpdate(record.id, record.active, checked)}
        />
      ),
    },
  ]

  const verificationColumns: ColumnsType<AdminVerificationRequest> = [
    {
      title: 'User',
      key: 'user',
      render: (_, r) => (
        <Space direction="vertical" size={0}>
          <Text strong>{r.userEmail}</Text>
          <Text type="secondary" style={{ fontSize: 11 }}>ID: {r.userId}</Text>
        </Space>
      ),
    },
    {
      title: 'Role',
      dataIndex: 'userRole',
      key: 'userRole',
      width: 120,
      render: (role: string) => (
        <Tag color={role === 'BRAND' ? 'blue' : 'magenta'}>{role}</Tag>
      ),
    },
    {
      title: 'Requested',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (v: string) => new Date(v).toLocaleString(),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 250,
      render: (_, record) => (
        <Space>
          <Button
            type="primary"
            size="small"
            loading={verifyingId === record.id}
            onClick={() => handleVerification(record.id, true)}
            style={{ background: '#52c41a', borderColor: '#52c41a', color: '#fff' }}
          >
            Approve
          </Button>
          <Button
            danger
            size="small"
            loading={verifyingId === record.id}
            onClick={() => setRejectModalId(record.id)}
          >
            Reject
          </Button>
        </Space>
      ),
    },
  ]

  const paymentEntries = dashboard
    ? Object.entries(dashboard.paymentsByStatus).sort(([a], [b]) => a.localeCompare(b))
    : []

  return (
    <ConfigProvider
      theme={{
        algorithm: theme.darkAlgorithm,
        token: {
          colorPrimary: PRIMARY,
          colorText: '#ffffff',
          borderRadius: 8,
          fontFamily:
            'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
        },
      }}
    >
      <Layout style={{ minHeight: '100vh', background: PAGE_BG }}>
        <Header
          style={{
            background: CARD_BG,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0 24px',
            borderBottom: '1px solid #303030',
          }}
        >
          <Title level={4} style={{ margin: 0, color: PRIMARY }}>
            Admin dashboard
          </Title>
          <Button type="text" icon={<LogoutOutlined />} onClick={handleLogout} style={{ color: '#fff' }}>
            Log out
          </Button>
        </Header>
        <Content style={{ padding: 24 }}>
          {loading && !dashboard ? (
            <div style={{ textAlign: 'center', padding: 48 }}>
              <Spin size="large" />
            </div>
          ) : (
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              <Row gutter={[16, 16]}>
                <Col xs={24} sm={8}>
                  <Card style={{ background: CARD_BG, borderColor: '#303030' }}>
                    <Text type="secondary">Brands</Text>
                    <Title level={2} style={{ margin: '8px 0 0', color: '#fff' }}>
                      {dashboard?.brandCount ?? 0}
                    </Title>
                  </Card>
                </Col>
                <Col xs={24} sm={8}>
                  <Card style={{ background: CARD_BG, borderColor: '#303030' }}>
                    <Text type="secondary">Influencers</Text>
                    <Title level={2} style={{ margin: '8px 0 0', color: '#fff' }}>
                      {dashboard?.influencerCount ?? 0}
                    </Title>
                  </Card>
                </Col>
                <Col xs={24} sm={8}>
                  <Card style={{ background: CARD_BG, borderColor: '#303030' }}>
                    <Text type="secondary">Campaigns</Text>
                    <Title level={2} style={{ margin: '8px 0 0', color: '#fff' }}>
                      {dashboard?.campaignCount ?? 0}
                    </Title>
                  </Card>
                </Col>
              </Row>

              <Card title="Platform payments" style={{ background: CARD_BG, borderColor: '#303030' }}>
                <Space wrap>
                  {paymentEntries.map(([status, count]) => (
                    <Tag key={status} color="purple" style={{ fontSize: 14, padding: '4px 10px' }}>
                      {status}: {count}
                    </Tag>
                  ))}
                </Space>
              </Card>

              <Card title="Pending Verification Requests" style={{ background: CARD_BG, borderColor: '#303030' }}>
                <Table
                  rowKey="id"
                  columns={verificationColumns}
                  dataSource={verifications}
                  pagination={false}
                  size="small"
                  locale={{ emptyText: 'No pending requests' }}
                />
              </Card>

              <Card title="Recent signups" style={{ background: CARD_BG, borderColor: '#303030' }}>
                <Table
                  rowKey="id"
                  columns={signupColumns}
                  dataSource={dashboard?.recentSignups ?? []}
                  pagination={false}
                  size="small"
                />
              </Card>

              <Card title="Active collaborations" style={{ background: CARD_BG, borderColor: '#303030' }}>
                <Table
                  rowKey="invitationId"
                  columns={collabColumns}
                  dataSource={dashboard?.activeCollaborations ?? []}
                  pagination={false}
                  size="small"
                />
              </Card>

              <Card title="Users" style={{ background: CARD_BG, borderColor: '#303030' }}>
                <Table
                  rowKey="id"
                  columns={userColumns}
                  dataSource={users}
                  loading={loading}
                  pagination={{
                    current: usersPage + 1,
                    pageSize: usersPageSize,
                    total: usersTotal,
                    showSizeChanger: true,
                    pageSizeOptions: [10, 20, 50],
                    onChange: (page, size) => {
                      setUsersPage(page - 1)
                      setUsersPageSize(size || 10)
                    },
                  }}
                  size="small"
                />
              </Card>
            </Space>
          )}
        </Content>

        <Modal
          title="Reject Verification"
          open={rejectModalId !== null}
          onOk={() => rejectModalId && handleVerification(rejectModalId, false, rejectReason)}
          onCancel={() => {
            setRejectModalId(null)
            setRejectReason('')
          }}
          okText="Reject"
          okType="danger"
          confirmLoading={verifyingId !== null}
        >
          <Space direction="vertical" style={{ width: '100%' }}>
            <Text>Please provide a reason for rejection (optional):</Text>
            <TextArea
              rows={4}
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="e.g. Incomplete profile details, invalid social links..."
            />
          </Space>
        </Modal>
      </Layout>
    </ConfigProvider>
  )
}
