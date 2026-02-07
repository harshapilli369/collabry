import { HeartOutlined, MailOutlined } from '@ant-design/icons'
import { theme } from '../theme'

export interface Influencer {
  id: string
  name: string
  profession: string
  followers: string
  avatar?: string
  advertisingPrice: string
  verified?: boolean
  tag?: string
}

export const InfluencerCard = ({ influencer }: { influencer: Influencer }) => {
  return (
    <div
      style={{
        backgroundColor: theme.white,
        borderRadius: 12,
        boxShadow: theme.shadow,
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
      }}
    >
      <div
        style={{
          height: 160,
          backgroundColor: theme.lavender,
          backgroundImage: influencer.avatar ? `url(${influencer.avatar})` : undefined,
          backgroundSize: 'cover',
          backgroundPosition: 'center',
        }}
      >
        {!influencer.avatar && (
          <div
            style={{
              width: '100%',
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 48,
              color: theme.dark,
            }}
          >
            {influencer.name.charAt(0)}
          </div>
        )}
      </div>
      <div style={{ padding: 16 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 4 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <span style={{ fontSize: 16, fontWeight: 700, color: theme.dark }}>{influencer.name}</span>
            {influencer.verified !== false && (
              <span style={{ color: '#22c55e', fontSize: 14 }} title="Verified">✓</span>
            )}
          </div>
          <span style={{ fontSize: 14, fontWeight: 600, color: theme.dark }}>{influencer.followers}</span>
        </div>
        <p style={{ fontSize: 13, color: theme.gray, margin: '0 0 8px' }}>{influencer.profession}</p>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
          <span style={{ fontSize: 12 }} title="YouTube">▶</span>
          <span style={{ fontSize: 12 }} title="Instagram">📷</span>
          {influencer.tag && (
            <span
              style={{
                backgroundColor: theme.green,
                color: theme.dark,
                fontSize: 11,
                fontWeight: 600,
                padding: '2px 8px',
                borderRadius: 12,
              }}
            >
              {influencer.tag}
            </span>
          )}
        </div>
        <div style={{ marginBottom: 12 }}>
          <p style={{ fontSize: 12, color: theme.gray, margin: 0 }}>Advertising Prize</p>
          <p style={{ fontSize: 15, fontWeight: 700, color: theme.dark, margin: 0 }}>{influencer.advertisingPrice}</p>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <button
            style={{
              width: 36,
              height: 36,
              borderRadius: 8,
              border: `1px solid ${theme.border}`,
              backgroundColor: theme.white,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <MailOutlined />
          </button>
          <button
            style={{
              width: 36,
              height: 36,
              borderRadius: 8,
              border: `1px solid ${theme.border}`,
              backgroundColor: theme.white,
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <HeartOutlined />
          </button>
          <button
            style={{
              flex: 1,
              height: 36,
              borderRadius: 8,
              border: 'none',
              backgroundColor: theme.yellow,
              color: theme.dark,
              fontWeight: 600,
              fontSize: 13,
              cursor: 'pointer',
            }}
          >
            Send Message
          </button>
        </div>
      </div>
    </div>
  )
}
