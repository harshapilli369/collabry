import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { Login } from './pages/Login'
import { Signup } from './pages/Signup'
import { ConfirmEmail } from './pages/ConfirmEmail'
import { ForgotPassword } from './pages/ForgotPassword'
import { ResetPassword } from './pages/ResetPassword'
import { ProfileSetup } from './pages/ProfileSetup'
import { InfluencerDashboard } from './pages/InfluencerDashboard'
import { BrandDashboard } from './pages/BrandDashboard'
import { ProtectedRoute } from './components/ProtectedRoute'
import './App.css'

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />

        {/* Protected Routes */}
        <Route element={<ProtectedRoute allowedRole="INFLUENCER" />}>
          <Route path="/influencer/dashboard" element={<InfluencerDashboard />} />
        </Route>

        <Route element={<ProtectedRoute allowedRole="BRAND" />}>
          <Route path="/brand/dashboard" element={<BrandDashboard />} />
        </Route>

        <Route path="/signup" element={<Signup />} />
        <Route path="/confirm-email" element={<ConfirmEmail />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/profile-setup" element={<ProfileSetup />} />
      </Routes>
    </Router>
  )
}

export default App
