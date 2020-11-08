import React from 'react'
import { Label, Input, FormGroup } from 'reactstrap'

const CodeFieldRadio = (props) => {
  const { parentEntity, attributeDefinition, selectedItem, items, onChange } = props

  return (
    <div>
      {items.map((item) => (
        <FormGroup check key={item.code}>
          <Label check>
            <Input
              type="radio"
              name={`code_group_${parentEntity.id}_${attributeDefinition.id}`}
              value={item.code}
              checked={selectedItem && selectedItem.code === item.code}
              onChange={onChange}
            />
            {item.label}
          </Label>
        </FormGroup>
      ))}
    </div>
  )
}

export default CodeFieldRadio
