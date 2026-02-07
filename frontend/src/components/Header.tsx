import { Link } from 'react-router-dom'
import { SettingOutlined, EnvironmentOutlined, UserOutlined } from '@ant-design/icons'
import { theme } from '../theme'

const LogoIcon = () => (
  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    <path d="M2 17L12 22L22 17" stroke="white" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
)

const DiscordIcon = () => (
  <svg width="22" height="22" viewBox="0 0 24 24" fill="white">
    <path d="M20.317 4.37a19.791 19.791 0 0 0-4.885-1.515.074.074 0 0 0-.079.037c-.21.375-.444.864-.608 1.25a18.27 18.27 0 0 0-5.487 0 12.64 12.64 0 0 0-.617-1.25.077.077 0 0 0-.079-.037A19.736 19.736 0 0 0 3.677 4.37a.07.07 0 0 0-.032.027C.533 9.046-.32 13.58.099 18.057a.082.082 0 0 0 .031.057 19.9 19.9 0 0 0 5.993 3.03.078.078 0 0 0 .084-.028 14.09 14.09 0 0 0 1.226-1.994.076.076 0 0 0-.041-.106 13.107 13.107 0 0 1-1.872-.892.077.077 0 0 1-.008-.128 10.2 10.2 0 0 0 .372-.292.074.074 0 0 1 .077-.01c3.928 1.793 8.18 1.793 12.062 0a.074.074 0 0 1 .078.01c.12.098.246.198.373.292a.077.077 0 0 1-.006.127 12.299 12.299 0 0 1-1.873.892.077.077 0 0 0-.041.107c.36.698.772 1.362 1.225 1.993a.076.076 0 0 0 .084.028 19.839 19.839 0 0 0 6.002-3.03.077.077 0 0 0 .032-.054c.5-5.177-.838-9.674-3.549-13.66a.061.061 0 0 0-.031-.03zM8.02 15.33c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.956-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.956 2.418-2.157 2.418zm7.975 0c-1.183 0-2.157-1.085-2.157-2.419 0-1.333.955-2.419 2.157-2.419 1.21 0 2.176 1.096 2.157 2.42 0 1.333-.946 2.418-2.157 2.418z"/>
  </svg>
)

export const Header = () => {
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  const displayName = user.displayName || user.companyName || user.email?.split('@')[0] || 'User'

  return (
    <header
      style={{
        backgroundColor: theme.dark,
        color: theme.white,
        padding: '16px 32px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', gap: 48 }}>
        <Link to="/dashboard" style={{ display: 'flex', alignItems: 'center' }}>
          <LogoIcon />
        </Link>
        <nav style={{ display: 'flex', gap: 24, fontSize: 15, fontWeight: 500 }}>
          <Link to="/dashboard" style={{ color: theme.white, textDecoration: 'none' }}>Home</Link>
          <Link to="/messages" style={{ color: theme.white, textDecoration: 'none' }}>Messages</Link>
          <Link to="/community" style={{ color: theme.white, textDecoration: 'none' }}>Community</Link>
        </nav>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
        <span style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 14, color: 'rgba(255,255,255,0.9)' }}>
          <EnvironmentOutlined />
          Gujarat, India
        </span>
        <Link to="/settings" style={{ color: theme.white }}>
          <SettingOutlined style={{ fontSize: 18 }} />
        </Link>
        <a href="#" style={{ color: theme.white }} aria-label="Discord">
          <DiscordIcon />
        </a>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <span style={{ fontSize: 14, fontWeight: 500 }}>{displayName}</span>
          <div
            style={{
              width: 36,
              height: 36,
              borderRadius: '50%',
              backgroundColor: theme.lavender,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <UserOutlined style={{ fontSize: 18, color: theme.dark }} />
          </div>
        </div>
      </div>
    </header>
  )
}
