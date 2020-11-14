import React from 'react'
import { Checkbox, FormControlLabel, Radio, RadioGroup, TextField } from '@material-ui/core'

import { CodeAttributeDefinition } from 'model/Survey'

const CodeFieldRadio = (props) => {
  const { attributeDefinition, values, items, itemLabelFunction, onChange, onChangeQualifier } = props
  const { multiple, itemsOrientation } = attributeDefinition

  const value = multiple ? null : values[0]
  const selectedCode = value ? value.code : null

  const wrapperStyle = {
    display: 'flex',
    flexDirection: itemsOrientation === CodeAttributeDefinition.ItemsOrientations.HORIZONTAL ? 'row' : 'column',
  }

  const Wrapper = ({ children }) =>
    multiple ? (
      <div style={wrapperStyle}>{children}</div>
    ) : (
      <RadioGroup style={wrapperStyle} value={selectedCode}>
        {children}
      </RadioGroup>
    )

  return (
    <Wrapper>
      {items.map((item) => {
        const { code } = item
        const value = values?.find((value) => value.code === code)
        const selected = Boolean(value)
        const qualifier = value?.qualifier || ''
        const control = multiple ? (
          <Checkbox checked={selected} color="primary" onChange={() => onChange({ item, selected: !selected })} />
        ) : (
          <Radio value={code} color="primary" onClick={() => onChange({ item, selected: !selected })} />
        )
        return (
          <div key={code}>
            <FormControlLabel value={code} control={control} label={itemLabelFunction(item)} title={item.description} />
            {item.qualifiable && selected && (
              <TextField
                value={qualifier}
                variant="outlined"
                onChange={(event) => onChangeQualifier({ code, qualifier: event.target.value })}
              />
            )}
          </div>
        )
      })}
    </Wrapper>
  )
}

export default CodeFieldRadio
