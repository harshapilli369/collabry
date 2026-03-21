import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import { Login } from './pages/Login'
import { Signup } from './pages/Signup'
import { ConfirmEmail } from './pages/ConfirmEmail'
import { ForgotPassword } from './pages/ForgotPassword'
import { ResetPassword } from './pages/ResetPassword'
import { ProfileSetup } from './pages/ProfileSetup'
import { InfluencerDashboard } from './pages/InfluencerDashboard'
import { Invitations } from './pages/Invitations'
import { InvitationDetail } from './pages/InvitationDetail'
import { Collaborations } from './pages/Collaborations'
import { PaymentsDashboard } from './pages/PaymentsDashboard'
import { BrandDashboard } from './pages/BrandDashboard'
import { BrandProfile } from './pages/BrandProfile'
import { ViewBrandProfile } from './pages/ViewBrandProfile'
import { ViewInfluencerProfile } from './pages/ViewInfluencerProfile'
import { BrandPayments } from './pages/BrandPayments'
import { BrandCollaborations } from './pages/BrandCollaborations'
import { CreateCampaign } from './pages/CreateCampaign'
import { InfluencerSearch } from './pages/InfluencerSearch'
import { ProtectedRoute } from './components/ProtectedRoute'
import './App.css'
// This is the main App component that renders the routes for the application.
function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/login" element={<Login />} />

        {/* Protected Routes */}
        <Route element={<ProtectedRoute allowedRole="INFLUENCER" />}>
          <Route path="/influencer/dashboard" element={<InfluencerDashboard />} />
          <Route path="/influencer/profile" element={<ViewInfluencerProfile />} />
          <Route path="/influencer/profile/edit" element={<ProfileSetup />} />
          <Route path="/influencer/invitations" element={<Invitations />} />
          <Route path="/influencer/invitations/:id" element={<InvitationDetail />} />
          <Route path="/influencer/collaborations" element={<Collaborations />} />
          <Route path="/influencer/payments" element={<PaymentsDashboard />} />
        </Route>

        <Route element={<ProtectedRoute allowedRole="BRAND" />}>
          <Route path="/brand/dashboard" element={<BrandDashboard />} />
          <Route path="/brand/profile" element={<ViewBrandProfile />} />
          <Route path="/brand/profile/edit" element={<BrandProfile />} />
          <Route path="/brand/collaborations" element={<BrandCollaborations />} />
          <Route path="/brand/campaigns/create" element={<CreateCampaign />} />
          <Route path="/brand/influencers" element={<InfluencerSearch />} />
          <Route path="/brand/payments" element={<BrandPayments />} />
        </Route>

        <Route path="/signup" element={<Signup />} />
        <Route path="/confirm-email" element={<ConfirmEmail />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
      </Routes>
    </Router>
  )
}

export default App
