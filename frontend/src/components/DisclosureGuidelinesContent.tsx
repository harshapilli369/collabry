import { Typography } from 'antd'
import { DISCLOSURE_GUIDELINES_TITLE, DISCLOSURE_POINTS } from '../content/disclosureGuidelines'

const { Title, Paragraph, Text } = Typography

type Props = {
  compact?: boolean
}

export function DisclosureGuidelinesContent({ compact }: Props) {
  return (
    <div data-testid="disclosure-guidelines-content">
      <Title level={compact ? 5 : 4} style={{ color: '#fff', marginTop: 0 }}>
        {DISCLOSURE_GUIDELINES_TITLE}
      </Title>
      <Paragraph type="secondary" style={{ marginBottom: compact ? 8 : 16, fontSize: compact ? 13 : 14 }}>
        Before you accept paid or sponsored work, use these guidelines to stay transparent with your audience and compliant with regulations.
      </Paragraph>
      <ul style={{ margin: 0, paddingLeft: 20, color: '#ccc', fontSize: compact ? 13 : 14, lineHeight: 1.6 }}>
        {DISCLOSURE_POINTS.map((p) => (
          <li key={p.slice(0, 48)} style={{ marginBottom: 8 }}>
            <Text style={{ color: '#ddd' }}>{p}</Text>
          </li>
        ))}
      </ul>
    </div>
  )
}
