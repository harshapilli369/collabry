const AUTH_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth';
const API_BASE = AUTH_API.replace(/\/api\/auth\/?$/, '') || 'http://localhost:8080';
export const CAMPAIGNS_URL = `${API_BASE}/api/campaigns`;

function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

export type BudgetRange =
    | 'UNDER_1K'
    | 'ONE_K_5K'
    | 'FIVE_K_10K'
    | 'TEN_K_50K'
    | 'FIFTY_K_PLUS';

export type CampaignGoal = 'AWARENESS' | 'CONVERSIONS' | 'ENGAGEMENT' | 'PRODUCT_LAUNCH';
export type CampaignStatus = 'DRAFT' | 'ACTIVE' | 'COMPLETED' | 'CANCELLED';

export const CAMPAIGN_STATUS_LABELS: Record<CampaignStatus, string> = {
    DRAFT: 'Draft',
    ACTIVE: 'Active',
    COMPLETED: 'Completed',
    CANCELLED: 'Cancelled',
};

export const BUDGET_RANGE_OPTIONS: { value: BudgetRange; label: string }[] = [
    { value: 'UNDER_1K', label: 'Under $1,000' },
    { value: 'ONE_K_5K', label: '$1,000 – $5,000' },
    { value: 'FIVE_K_10K', label: '$5,000 – $10,000' },
    { value: 'TEN_K_50K', label: '$10,000 – $50,000' },
    { value: 'FIFTY_K_PLUS', label: '$50,000+' },
];

export const CAMPAIGN_GOAL_OPTIONS: { value: CampaignGoal; label: string }[] = [
    { value: 'AWARENESS', label: 'Brand awareness' },
    { value: 'CONVERSIONS', label: 'Conversions / sales' },
    { value: 'ENGAGEMENT', label: 'Engagement' },
    { value: 'PRODUCT_LAUNCH', label: 'Product launch' },
];

export const PREFERRED_CONTENT_OPTIONS: { value: string; label: string }[] = [
    { value: 'INSTAGRAM_REEL', label: 'Instagram Reel' },
    { value: 'INSTAGRAM_STORY', label: 'Instagram Story' },
    { value: 'YOUTUBE_VIDEO', label: 'YouTube video' },
    { value: 'TIKTOK', label: 'TikTok' },
    { value: 'BLOG', label: 'Blog post' },
];

export interface CampaignRequest {
    name: string;
    description?: string;
    budgetRange: BudgetRange;
    campaignGoal?: CampaignGoal;
    preferredContentTypes?: string;
    startDate?: string;
    endDate?: string;
    numberOfInfluencers?: number;
}

export interface CampaignResponse {
    id: number;
    userId: number;
    name: string;
    description?: string;
    budgetRange: BudgetRange;
    status: CampaignStatus;
    campaignGoal?: CampaignGoal;
    preferredContentTypes?: string;
    startDate?: string;
    endDate?: string;
    numberOfInfluencers?: number;
    createdAt?: string;
    updatedAt?: string;
}

export async function createCampaign(payload: CampaignRequest): Promise<CampaignResponse> {
    const response = await fetch(CAMPAIGNS_URL, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload),
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        if (response.status === 403) {
            throw new Error(data.message || 'Only verified brands can create campaigns. Please complete verification.');
        }
        throw new Error(data.message || 'Failed to create campaign');
    }
    if (!data || typeof data.id !== 'number') {
        throw new Error('Invalid response from server');
    }
    return data as CampaignResponse;
}

export async function getMyCampaigns(): Promise<CampaignResponse[]> {
    const response = await fetch(`${CAMPAIGNS_URL}/me`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load campaigns');
    }
    return response.json();
}

export async function downloadCampaignReport(campaignId: number): Promise<void> {
    const response = await fetch(`${CAMPAIGNS_URL}/${campaignId}/report`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to download campaign report');
    }
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `campaign-${campaignId}-report.pdf`;
    a.click();
    URL.revokeObjectURL(url);
}

export async function updateCampaignStatus(campaignId: number, status: CampaignStatus): Promise<CampaignResponse> {
    const response = await fetch(`${CAMPAIGNS_URL}/${campaignId}/status`, {
        method: 'PATCH',
        headers: getAuthHeaders(),
        body: JSON.stringify({ status }),
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(data.message || 'Failed to update campaign status');
    }
    return data as CampaignResponse;
}
