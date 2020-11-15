import React from 'react'
import { RadioGroup } from '@material-ui/core'

import { CodeAttributeDefinition } from 'model/Survey'
import CodeFieldRadioItem from './CodeFieldRadioItem'

const CodeFieldRadio = (props) => {
  const { attributeDefinition, values, items, onChange, onChangeQualifier } = props
  const { multiple, itemsOrientation } = attributeDefinition

  const value = multiple ? null : values[0]
  const selectedCode = value ? value.code : null

  const wrapperStyle = {
    display: 'flex',
    flexDirection: itemsOrientation === CodeAttributeDefinition.ItemsOrientations.HORIZONTAL ? 'row' : 'column',
  }

  const itemComponents = items.map((item) => (
    <CodeFieldRadioItem
      key={item.code}
      attributeDefinition={attributeDefinition}
      item={item}
      multiple={multiple}
      onChange={onChange}
      onChangeQualifier={onChangeQualifier}
      value={values?.find((value) => value.code === item.code)}
    />
  ))

  return multiple ? (
    <div style={wrapperStyle}>{itemComponents}</div>
  ) : (
    <RadioGroup style={wrapperStyle} value={selectedCode}>
      {itemComponents}
    </RadioGroup>
  )
}

export default CodeFieldRadio
