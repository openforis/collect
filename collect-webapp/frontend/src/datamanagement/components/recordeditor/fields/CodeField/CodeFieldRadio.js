import React from 'react'
import { Checkbox, FormControlLabel, Radio, RadioGroup } from '@material-ui/core'
import { CodeAttributeDefinition } from '../../../../../model/Survey'

const CodeFieldRadio = (props) => {
  const { attributeDefinition, selectedItems, items, itemLabelFunction, onChange } = props
  const { multiple, itemsOrientation } = attributeDefinition

  const selectedItem = multiple ? null : selectedItems[0]
  const selectedCode = selectedItem?.code

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
        const checked = selectedItems?.some((selectedItem) => selectedItem.code === item.code)
        const control = multiple ? (
          <Checkbox checked={checked} onChange={() => onChange(item, !checked)} />
        ) : (
          <Radio color="primary" onClick={() => onChange(item, !checked)} />
        )
        return <FormControlLabel key={item.code} value={item.code} control={control} label={itemLabelFunction(item)} />
      })}
    </Wrapper>
  )
}

export default CodeFieldRadio
