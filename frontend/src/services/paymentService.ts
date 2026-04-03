const AUTH_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth';
const API_BASE = AUTH_API.replace(/\/api\/auth\/?$/, '') || 'http://localhost:8080';
const PAYMENTS_URL = `${API_BASE}/api/payments`;

function getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
    };
}

export type PaymentStatus = 'PENDING' | 'PROCESSING' | 'PAID' | 'DELAYED';

export const PAYMENT_STATUS_LABELS: Record<PaymentStatus, string> = {
    PENDING: 'Pending',
    PROCESSING: 'Processing',
    PAID: 'Paid',
    DELAYED: 'Delayed',
};

export const PAYMENT_STATUS_COLORS: Record<PaymentStatus, string> = {
    PENDING: 'orange',
    PROCESSING: 'blue',
    PAID: 'green',
    DELAYED: 'red',
};

export interface PaymentRequest {
    campaignId: number;
    influencerId: number;
    milestoneName: string;
    amount: number;
    dueDate?: string;
    notes?: string;
}

export interface PaymentResponse {
    id: number;
    campaignId: number;
    campaignName?: string;
    influencerId: number;
    brandId: number;
    milestoneName: string;
    amount: number;
    status: PaymentStatus;
    dueDate?: string;
    paidDate?: string;
    invoiceNumber: string;
    notes?: string;
    createdAt?: string;
    updatedAt?: string;
}

export async function createPayment(payload: PaymentRequest): Promise<PaymentResponse> {
    const response = await fetch(PAYMENTS_URL, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(payload),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to create payment');
    }
    return response.json();
}

export async function getMyPayments(): Promise<PaymentResponse[]> {
    const response = await fetch(`${PAYMENTS_URL}/me`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load payments');
    }
    return response.json();
}

export async function getPaymentsForCampaign(campaignId: number): Promise<PaymentResponse[]> {
    const response = await fetch(`${PAYMENTS_URL}/campaign/${campaignId}`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load campaign payments');
    }
    return response.json();
}

export async function updatePaymentStatus(paymentId: number, status: PaymentStatus): Promise<PaymentResponse> {
    const response = await fetch(`${PAYMENTS_URL}/${paymentId}/status`, {
        method: 'PUT',
        headers: getAuthHeaders(),
        body: JSON.stringify({ status }),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to update payment status');
    }
    return response.json();
}

export async function getInvoice(paymentId: number): Promise<PaymentResponse> {
    const response = await fetch(`${PAYMENTS_URL}/${paymentId}/invoice`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load invoice');
    }
    return response.json();
}

export async function getDelayedPayments(): Promise<PaymentResponse[]> {
    const response = await fetch(`${PAYMENTS_URL}/delayed`, {
        method: 'GET',
        headers: getAuthHeaders(),
    });
    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw new Error(data.message || 'Failed to load delayed payments');
    }
    return response.json();
}
