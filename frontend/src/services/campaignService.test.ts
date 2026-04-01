import { beforeEach, describe, expect, it, vi } from 'vitest'
import { downloadCampaignReport } from './campaignService'

describe('downloadCampaignReport', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    localStorage.setItem('token', 'tkn')
  })

  it('downloads report PDF as attachment blob', async () => {
    const blob = new Blob(['%PDF-1.4'], { type: 'application/pdf' })
    const fetchMock = vi.spyOn(global, 'fetch').mockResolvedValue({
      ok: true,
      blob: async () => blob,
      json: async () => ({}),
    } as Response)

    Object.defineProperty(URL, 'createObjectURL', { value: vi.fn(() => 'blob:url'), writable: true })
    Object.defineProperty(URL, 'revokeObjectURL', { value: vi.fn(), writable: true })
    const click = vi.fn()
    vi.spyOn(document, 'createElement').mockImplementation(
      () => ({ click, href: '', download: '' } as unknown as HTMLElement)
    )

    await downloadCampaignReport(55)

    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringContaining('/api/campaigns/55/report'),
      expect.objectContaining({ method: 'GET' })
    )
    expect(URL.createObjectURL).toHaveBeenCalled()
    expect(click).toHaveBeenCalled()
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:url')
  })
})
