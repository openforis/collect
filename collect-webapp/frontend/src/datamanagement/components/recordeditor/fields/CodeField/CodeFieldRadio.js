import React from 'react'
import { Label, Input, FormGroup } from 'reactstrap'

const CodeFieldRadio = (props) => {
  const { parentEntity, attributeDefinition, selectedItems, items, onChange } = props

  const { showCode } = attributeDefinition
  return (
    <div>
      {items.map((item) => {
        const checked = selectedItems && Boolean(selectedItems.find((selectedItem) => selectedItem.code === item.code))
        return (
          <FormGroup check key={item.code}>
            <Label check>
              <Input
                type="radio"
                name={`code_group_${parentEntity.id}_${attributeDefinition.id}`}
                value={item.code}
                checked={checked}
                onChange={onChange}
              />
              {showCode ? `${item.code} - ` : ''}
              {item.label}
            </Label>
          </FormGroup>
        )
      })}
    </div>
  )
}

export default CodeFieldRadio
