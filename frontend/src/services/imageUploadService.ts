const AUTH_API = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/auth'
const API_BASE = AUTH_API.replace(/\/api\/auth\/?$/, '') || 'http://localhost:8080'
const UPLOAD_URL = `${API_BASE}/api/upload/image`

/**
 * Uploads an image file to the backend which proxies it to Cloudinary.
 * Returns the public CDN URL to store in the profile.
 */
export async function uploadProfileImage(file: File, folder = 'profile-pictures'): Promise<string> {
    const token = localStorage.getItem('token')
    const formData = new FormData()
    formData.append('file', file)
    formData.append('folder', folder)

    const response = await fetch(`${UPLOAD_URL}?folder=${encodeURIComponent(folder)}`, {
        method: 'POST',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        body: formData,
    })

    if (!response.ok) {
        const data = await response.json().catch(() => ({}))
        throw new Error(data.error || 'Image upload failed')
    }

    const data = await response.json()
    return data.url as string
}
