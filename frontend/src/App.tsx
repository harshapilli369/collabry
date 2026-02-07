import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Login } from './Login'
import { Signup } from './Signup'
import { FindInfluencers } from './pages/FindInfluencers'
import { Messages } from './pages/Messages'
import { Community } from './pages/Community'
import { Settings } from './pages/Settings'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/dashboard" element={<FindInfluencers />} />
        <Route path="/messages" element={<Messages />} />
        <Route path="/community" element={<Community />} />
        <Route path="/settings" element={<Settings />} />
        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
