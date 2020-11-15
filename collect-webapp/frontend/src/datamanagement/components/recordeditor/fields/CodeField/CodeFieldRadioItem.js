import React from 'react'
import { Checkbox, FormControlLabel, Radio, TextField } from '@material-ui/core'

import InfoIcon from 'common/components/InfoIcon'

const CodeFieldRadioItem = (props) => {
  const { item, itemLabelFunction, multiple, onChange, onChangeQualifier, value } = props
  const { code } = item
  const selected = Boolean(value)
  const qualifier = value?.qualifier || ''
  const control = multiple ? (
    <Checkbox checked={selected} color="primary" onChange={() => onChange({ item, selected: !selected })} />
  ) : (
    <Radio value={code} color="primary" onClick={() => onChange({ item, selected: !selected })} />
  )
  return (
    <div key={code} style={{ display: 'flex', flexDirection: 'row', alignItems: 'baseline' }}>
      <FormControlLabel
        value={code}
        control={control}
        label={
          <span title={item.description}>
            {itemLabelFunction(item)}
            {item.description && <InfoIcon />}
          </span>
        }
        title={item.description}
      />
      {item.qualifiable && selected && (
        <TextField
          value={qualifier}
          variant="outlined"
          onChange={(event) => onChangeQualifier({ code, qualifier: event.target.value })}
        />
      )}
    </div>
  )
}

export default CodeFieldRadioItem
