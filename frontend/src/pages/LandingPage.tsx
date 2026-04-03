import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Typography } from 'antd'
import {
    RocketOutlined,
    TeamOutlined,
    ThunderboltOutlined,
    SafetyCertificateOutlined,
    BarChartOutlined,
    RobotOutlined,
    ArrowRightOutlined,
} from '@ant-design/icons'

const { Title, Text } = Typography

const PRIMARY = '#FFFD82'
const PURPLE = '#BD72EB'

const FEATURES = [
    { icon: <RobotOutlined style={{ fontSize: 32, color: PRIMARY }} />, title: 'AI-Powered Matching', desc: 'Our AI matchmaker analyzes campaign goals, budgets, and influencer niches to find your perfect collaboration partner.' },
    { icon: <TeamOutlined style={{ fontSize: 32, color: PURPLE }} />, title: 'Seamless Collaboration', desc: 'Manage invitations, track collaborations, and communicate with influencers — all in one place.' },
    { icon: <BarChartOutlined style={{ fontSize: 32, color: PRIMARY }} />, title: 'Campaign Analytics', desc: 'Track campaign performance with real-time metrics and visual dashboards to optimize your ROI.' },
    { icon: <SafetyCertificateOutlined style={{ fontSize: 32, color: PURPLE }} />, title: 'Verified Profiles', desc: 'Every brand and influencer goes through identity verification to ensure trust and authenticity.' },
    { icon: <ThunderboltOutlined style={{ fontSize: 32, color: PRIMARY }} />, title: 'Fast Payments', desc: 'Secure and transparent payment tracking between brands and influencers with full audit history.' },
    { icon: <RocketOutlined style={{ fontSize: 32, color: PURPLE }} />, title: 'Campaign Builder', desc: 'Create campaigns in minutes with guided forms, budget planning, and audience targeting tools.' },
]

const STEPS = [
    { num: '01', title: 'Sign Up', desc: 'Create your account as a Brand or Influencer in seconds.' },
    { num: '02', title: 'Build Your Profile', desc: 'Set up your niche, portfolio, rates, and social links.' },
    { num: '03', title: 'Create or Discover', desc: 'Brands launch campaigns. Influencers get matched by AI.' },
    { num: '04', title: 'Collaborate & Grow', desc: 'Work together, track progress, and get paid securely.' },
]

const STATS = [
    { value: '500+', label: 'Influencers' },
    { value: '200+', label: 'Brands' },
    { value: '1.2K+', label: 'Campaigns' },
    { value: '98%', label: 'Match Rate' },
]

type NavFn = () => void

const LandingNavbar = ({ scrollY, onLogin, onSignup }: { scrollY: number; onLogin: NavFn; onSignup: NavFn }) => (
    <nav style={{ position: 'fixed', top: 0, left: 0, right: 0, zIndex: 100, padding: '16px 48px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: scrollY > 50 ? 'rgba(0,0,0,0.9)' : 'transparent', backdropFilter: scrollY > 50 ? 'blur(20px)' : 'none', borderBottom: scrollY > 50 ? '1px solid #1a1a1a' : 'none', transition: 'all 0.3s ease' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <div style={{ width: 36, height: 36, borderRadius: 10, background: `linear-gradient(135deg, ${PRIMARY}, ${PURPLE})`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 900, fontSize: 18, color: '#000' }}>C</div>
            <span style={{ color: '#fff', fontSize: 22, fontWeight: 700, letterSpacing: -0.5 }}>Collabry</span>
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
            <Button onClick={onLogin} style={{ background: 'transparent', border: '1px solid #333', color: '#fff', borderRadius: 20, padding: '0 24px', height: 40, fontWeight: 500 }}>Log in</Button>
            <Button onClick={onSignup} style={{ background: PRIMARY, border: 'none', color: '#000', borderRadius: 20, padding: '0 24px', height: 40, fontWeight: 700 }}>Get Started</Button>
        </div>
    </nav>
)

const HeroSection = ({ onLogin, onSignup }: { onLogin: NavFn; onSignup: NavFn }) => (
    <section style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', padding: '120px 24px 80px', position: 'relative' }}>
        <div className="landing-glow" style={{ position: 'absolute', top: '20%', left: '50%', transform: 'translate(-50%, -50%)', width: 600, height: 600, borderRadius: '50%', background: `radial-gradient(circle, ${PRIMARY}15 0%, transparent 70%)`, pointerEvents: 'none' }} />
        <div className="landing-glow-purple" style={{ position: 'absolute', top: '60%', right: '10%', width: 400, height: 400, borderRadius: '50%', background: `radial-gradient(circle, ${PURPLE}10 0%, transparent 70%)`, pointerEvents: 'none' }} />
        <div className="landing-fade-in" style={{ position: 'relative', zIndex: 1 }}>
            <div style={{ display: 'inline-block', padding: '6px 20px', borderRadius: 20, border: `1px solid ${PRIMARY}40`, background: `${PRIMARY}10`, marginBottom: 32 }}>
                <Text style={{ color: PRIMARY, fontSize: 14, fontWeight: 500 }}>AI-Powered Influencer Marketing Platform</Text>
            </div>
            <Title className="landing-title" style={{ color: '#fff', fontSize: 'clamp(40px, 6vw, 72px)', fontWeight: 800, lineHeight: 1.1, margin: 0, maxWidth: 800 }}>
                Where Brands Meet{' '}
                <span style={{ background: `linear-gradient(135deg, ${PRIMARY}, ${PURPLE})`, WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>Their Perfect</span>{' '}
                Influencers
            </Title>
            <Text className="landing-fade-in-delay" style={{ color: '#888', fontSize: 'clamp(16px, 2vw, 20px)', display: 'block', maxWidth: 600, margin: '28px auto 0', lineHeight: 1.6 }}>
                Collabry uses AI to match brands with the right influencers based on niche, audience, and budget — making collaborations smarter and faster.
            </Text>
            <div className="landing-fade-in-delay-2" style={{ marginTop: 48, display: 'flex', gap: 16, justifyContent: 'center', flexWrap: 'wrap' }}>
                <Button size="large" onClick={onSignup} style={{ background: PRIMARY, border: 'none', color: '#000', borderRadius: 28, padding: '0 36px', height: 52, fontWeight: 700, fontSize: 16 }}>Start Free <ArrowRightOutlined /></Button>
                <Button size="large" onClick={onLogin} style={{ background: 'transparent', border: '1px solid #333', color: '#fff', borderRadius: 28, padding: '0 36px', height: 52, fontWeight: 600, fontSize: 16 }}>Log in</Button>
            </div>
            <div className="landing-fade-in-delay-3" style={{ marginTop: 80, display: 'flex', gap: 60, justifyContent: 'center', flexWrap: 'wrap' }}>
                {STATS.map((stat) => (
                    <div key={stat.label} style={{ textAlign: 'center' }}>
                        <div style={{ color: PRIMARY, fontSize: 32, fontWeight: 800 }}>{stat.value}</div>
                        <div style={{ color: '#666', fontSize: 14, marginTop: 4 }}>{stat.label}</div>
                    </div>
                ))}
            </div>
        </div>
    </section>
)

const FeaturesSection = () => (
    <section style={{ padding: '100px 24px', maxWidth: 1100, margin: '0 auto' }}>
        <div style={{ textAlign: 'center', marginBottom: 64 }}>
            <Text style={{ color: PURPLE, fontSize: 14, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 2 }}>Features</Text>
            <Title level={2} style={{ color: '#fff', marginTop: 12, fontSize: 36, fontWeight: 700 }}>Everything you need to <span style={{ color: PRIMARY }}>grow together</span></Title>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 24 }}>
            {FEATURES.map((f, idx) => (
                <div key={idx} className="landing-feature-card" style={{ background: '#0d0d0d', border: '1px solid #1a1a1a', borderRadius: 16, padding: 32, transition: 'all 0.3s ease', cursor: 'default' }}>
                    <div style={{ width: 56, height: 56, borderRadius: 14, background: '#141414', display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: 20 }}>{f.icon}</div>
                    <div style={{ color: '#fff', fontSize: 18, fontWeight: 600, marginBottom: 10 }}>{f.title}</div>
                    <div style={{ color: '#777', fontSize: 14, lineHeight: 1.7 }}>{f.desc}</div>
                </div>
            ))}
        </div>
    </section>
)

const HowItWorksSection = () => (
    <section style={{ padding: '100px 24px', background: '#060606' }}>
        <div style={{ maxWidth: 900, margin: '0 auto' }}>
            <div style={{ textAlign: 'center', marginBottom: 64 }}>
                <Text style={{ color: PRIMARY, fontSize: 14, fontWeight: 600, textTransform: 'uppercase', letterSpacing: 2 }}>How It Works</Text>
                <Title level={2} style={{ color: '#fff', marginTop: 12, fontSize: 36, fontWeight: 700 }}>Get started in <span style={{ color: PURPLE }}>4 simple steps</span></Title>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 32 }}>
                {STEPS.map((step, idx) => (
                    <div key={idx} className="landing-step" style={{ display: 'flex', gap: 28, alignItems: 'center', padding: '28px 32px', background: '#0d0d0d', border: '1px solid #1a1a1a', borderRadius: 16, transition: 'all 0.3s ease' }}>
                        <div style={{ fontSize: 40, fontWeight: 900, background: `linear-gradient(135deg, ${PRIMARY}, ${PURPLE})`, WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent', minWidth: 60 }}>{step.num}</div>
                        <div>
                            <div style={{ color: '#fff', fontSize: 20, fontWeight: 700, marginBottom: 6 }}>{step.title}</div>
                            <div style={{ color: '#777', fontSize: 15, lineHeight: 1.6 }}>{step.desc}</div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    </section>
)

const CtaSection = ({ onSignup }: { onSignup: NavFn }) => (
    <section style={{ padding: '120px 24px', textAlign: 'center', position: 'relative' }}>
        <div style={{ position: 'absolute', top: '50%', left: '50%', transform: 'translate(-50%, -50%)', width: 500, height: 500, borderRadius: '50%', background: `radial-gradient(circle, ${PURPLE}12 0%, transparent 70%)`, pointerEvents: 'none' }} />
        <div style={{ position: 'relative', zIndex: 1 }}>
            <Title level={2} style={{ color: '#fff', fontSize: 'clamp(28px, 4vw, 44px)', fontWeight: 800, maxWidth: 600, margin: '0 auto' }}>
                Ready to find your{' '}
                <span style={{ background: `linear-gradient(135deg, ${PRIMARY}, ${PURPLE})`, WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>perfect match?</span>
            </Title>
            <Text style={{ color: '#666', fontSize: 18, display: 'block', marginTop: 16, maxWidth: 500, margin: '16px auto 0' }}>Join hundreds of brands and influencers already growing together on Collabry.</Text>
            <Button size="large" onClick={onSignup} style={{ marginTop: 40, background: PRIMARY, border: 'none', color: '#000', borderRadius: 28, padding: '0 48px', height: 56, fontWeight: 700, fontSize: 18 }}>Get Started Free <ArrowRightOutlined /></Button>
        </div>
    </section>
)

const LandingFooter = ({ onLogin, onSignup }: { onLogin: NavFn; onSignup: NavFn }) => (
    <footer style={{ borderTop: '1px solid #1a1a1a', padding: '40px 48px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <div style={{ width: 28, height: 28, borderRadius: 8, background: `linear-gradient(135deg, ${PRIMARY}, ${PURPLE})`, display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 900, fontSize: 14, color: '#000' }}>C</div>
            <span style={{ color: '#555', fontSize: 14 }}>Collabry &copy; 2026. All rights reserved.</span>
        </div>
        <div style={{ display: 'flex', gap: 24 }}>
            <span style={{ color: '#555', fontSize: 14, cursor: 'pointer', transition: 'color 0.2s' }} onClick={onLogin}>Login</span>
            <span style={{ color: '#555', fontSize: 14, cursor: 'pointer', transition: 'color 0.2s' }} onClick={onSignup}>Sign Up</span>
        </div>
    </footer>
)

export const LandingPage = () => {
    const navigate = useNavigate()
    const [scrollY, setScrollY] = useState(0)

    useEffect(() => {
        const handleScroll = () => setScrollY(window.scrollY)
        window.addEventListener('scroll', handleScroll)
        return () => window.removeEventListener('scroll', handleScroll)
    }, [])

    const toLogin = () => navigate('/login')
    const toSignup = () => navigate('/signup')

    return (
        <div style={{ background: '#000', minHeight: '100vh', overflow: 'hidden' }}>
            <LandingNavbar scrollY={scrollY} onLogin={toLogin} onSignup={toSignup} />
            <HeroSection onLogin={toLogin} onSignup={toSignup} />
            <FeaturesSection />
            <HowItWorksSection />
            <CtaSection onSignup={toSignup} />
            <LandingFooter onLogin={toLogin} onSignup={toSignup} />
        </div>
    )
}
