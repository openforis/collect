import React from 'react'
import { Label, Input, FormGroup } from 'reactstrap'

const CodeFieldRadio = (props) => {
  const { parentEntity, attributeDefinition, selectedItems, items, itemLabelFunction, onChange } = props
  const { showCode, multiple } = attributeDefinition

  return (
    <div>
      {items.map((item) => {
        const checked = selectedItems?.some((selectedItem) => selectedItem.code === item.code)
        return (
          <FormGroup check key={item.code}>
            <Label check>
              <Input
                type={multiple ? 'checkbox' : 'radio'}
                name={`code_group_${parentEntity.id}_${attributeDefinition.id}`}
                value={item.code}
                checked={checked}
                onChange={() => onChange(item, !checked)}
                onClick={() => !multiple && checked && onChange(item, !checked)}
              />
              {itemLabelFunction(item)}
            </Label>
          </FormGroup>
        )
      })}
    </div>
  )
}

export default CodeFieldRadio
