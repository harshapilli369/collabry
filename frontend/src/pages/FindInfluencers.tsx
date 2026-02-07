import { useState } from 'react'
import { SearchOutlined } from '@ant-design/icons'
import { Header } from '../components/Header'
import { InfluencerCard, type Influencer } from '../components/InfluencerCard'
import { theme } from '../theme'

const MOCK_INFLUENCERS: Influencer[] = [
  { id: '1', name: 'Manoj Vandhe', profession: 'Fashion Creator', followers: '787k Followers', advertisingPrice: '₹ 95k', tag: 'VLOGGER' },
  { id: '2', name: 'Moana Patel', profession: 'Lifestyle Creator', followers: '612k Followers', advertisingPrice: '₹ 78k', tag: 'VLOGGER' },
  { id: '3', name: 'Assmeen tote', profession: 'Beauty Creator', followers: '945k Followers', advertisingPrice: '₹ 120k', tag: 'VLOGGER' },
]

export const FindInfluencers = () => {
  const [search, setSearch] = useState('')

  return (
    <div style={{ minHeight: '100vh', backgroundColor: theme.white }}>
      <Header />
      {/* Dark hero + search section */}
      <div
        style={{
          backgroundColor: theme.dark,
          color: theme.white,
          padding: '48px 32px 64px',
          fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
        }}
      >
        <div style={{ maxWidth: 1200, margin: '0 auto', display: 'flex', gap: 32, alignItems: 'flex-start' }}>
          <div style={{ flex: 1 }}>
            <h1
              style={{
                fontSize: 42,
                fontWeight: 700,
                margin: '0 0 24px',
                lineHeight: 1.2,
                letterSpacing: '-0.02em',
              }}
            >
              Find Influencers to collaborate with
            </h1>
            <div
              style={{
                display: 'flex',
                backgroundColor: 'rgba(255,255,255,0.08)',
                borderRadius: 12,
                border: '1px solid rgba(255,255,255,0.2)',
                overflow: 'hidden',
              }}
            >
              <input
                type="text"
                placeholder="Search"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                style={{
                  flex: 1,
                  padding: '14px 20px',
                  border: 'none',
                  background: 'transparent',
                  color: theme.white,
                  fontSize: 16,
                  outline: 'none',
                }}
              />
              <button
                style={{
                  padding: '14px 24px',
                  backgroundColor: theme.yellow,
                  border: 'none',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <SearchOutlined style={{ fontSize: 20, color: theme.dark }} />
              </button>
            </div>
          </div>
          <div
            style={{
              width: 220,
              backgroundColor: theme.yellow,
              borderRadius: 12,
              padding: 24,
              position: 'relative',
            }}
          >
            <div style={{ fontSize: 32, marginBottom: 8 }}>📷</div>
            <p style={{ fontSize: 18, fontWeight: 700, color: theme.dark, margin: 0 }}>See how it's done</p>
          </div>
        </div>
      </div>
      {/* Light section - influencer cards */}
      <div style={{ maxWidth: 1200, margin: '0 auto', padding: '40px 32px' }}>
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
            gap: 24,
          }}
        >
          {MOCK_INFLUENCERS.map((influencer) => (
            <InfluencerCard key={influencer.id} influencer={influencer} />
          ))}
          {/* Show All card */}
          <div
            style={{
              backgroundColor: theme.lavender,
              borderRadius: 12,
              boxShadow: theme.shadow,
              padding: 32,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              minHeight: 320,
              cursor: 'pointer',
            }}
          >
            <span style={{ fontSize: 24, marginBottom: 12 }}>→</span>
            <p style={{ fontSize: 22, fontWeight: 700, color: theme.white, margin: '0 0 4px' }}>Show All</p>
            <p style={{ fontSize: 14, color: 'rgba(255,255,255,0.9)', margin: 0 }}>4,223,452 Influencer's</p>
          </div>
        </div>
      </div>
    </div>
  )
}
