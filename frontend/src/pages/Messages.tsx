import { Header } from '../components/Header'
import { theme } from '../theme'

export const Messages = () => (
  <div style={{ minHeight: '100vh', backgroundColor: theme.white }}>
    <Header />
    <div style={{ padding: 48, textAlign: 'center', color: theme.gray }}>
      <p style={{ fontSize: 18 }}>Messages — Coming soon</p>
    </div>
  </div>
)
