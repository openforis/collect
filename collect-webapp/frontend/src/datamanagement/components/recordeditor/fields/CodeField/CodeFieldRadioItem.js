import React from 'react'
import { Checkbox, FormControlLabel, Radio } from '@mui/material'

import CodeFieldItemLabel from './CodeFieldItemLabel'
import CodeFieldQualifier from './CodeFieldQualifier'

const CodeFieldRadioItem = (props) => {
  const { item, attributeDefinition, multiple, onChange, onChangeQualifier, readOnly, value } = props
  const { code } = item
  const selected = Boolean(value)
  const qualifier = value?.qualifier || ''

  const commonProps = {
    color: 'primary',
    size: 'small',
    disabled: readOnly,
  }
  const control = multiple ? (
    <Checkbox checked={selected} {...commonProps} onChange={() => onChange({ item, selected: !selected })} />
  ) : (
    <Radio value={code} {...commonProps} onClick={() => onChange({ item, selected: !selected })} />
  )

  return (
    <div key={code} style={{ display: 'flex', flexDirection: 'row', alignItems: 'baseline' }}>
      <FormControlLabel
        value={code}
        control={control}
        label={<CodeFieldItemLabel item={item} attributeDefinition={attributeDefinition} />}
        title={item.description}
      />
      {item.qualifiable && selected && (
        <CodeFieldQualifier
          code={code}
          qualifier={qualifier}
          readOnly={readOnly}
          onChangeQualifier={onChangeQualifier}
        />
      )}
    </div>
  )
}

export default CodeFieldRadioItem
