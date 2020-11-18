import React from 'react'
import { Checkbox, FormControlLabel, Radio, TextField } from '@material-ui/core'

import L from 'utils/Labels'

import CodeFieldItemLabel from './CodeFieldItemLabel'

const CodeFieldRadioItem = (props) => {
  const { item, attributeDefinition, multiple, onChange, onChangeQualifier, value } = props
  const { calculated } = attributeDefinition
  const { code } = item
  const selected = Boolean(value)
  const qualifier = value?.qualifier || ''

  const commonProps = {
    color: 'primary',
    size: 'small',
    disabled: calculated,
    onChange: () => onChange({ item, selected: !selected }),
  }
  const control = multiple ? <Checkbox checked={selected} {...commonProps} /> : <Radio value={code} {...commonProps} />

  return (
    <div key={code} style={{ display: 'flex', flexDirection: 'row', alignItems: 'baseline' }}>
      <FormControlLabel
        value={code}
        control={control}
        label={<CodeFieldItemLabel item={item} attributeDefinition={attributeDefinition} />}
        title={item.description}
      />
      {item.qualifiable && selected && (
        <TextField
          value={qualifier}
          variant="outlined"
          placeholder={L.l('common.specify')}
          disabled={calculated}
          onChange={(event) => onChangeQualifier({ code, qualifier: event.target.value })}
        />
      )}
    </div>
  )
}

export default CodeFieldRadioItem
