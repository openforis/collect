import React from 'react'
import { Label, Input, FormGroup } from 'reactstrap'

const CodeFieldRadio = (props) => {
  const { parentEntity, attrDef: attributeDefinition, selectedCode, items, onChange } = props

  return (
    <div>
      {items.map((item) => (
        <FormGroup check key={item.code}>
          <Label check>
            <Input
              type="radio"
              name={`code_group_${parentEntity.id}_${attributeDefinition.id}`}
              value={item.code}
              checked={item.code === selectedCode}
              onChange={onChange}
            />{' '}
            {item.label}
          </Label>
        </FormGroup>
      ))}
    </div>
  )
}

export default CodeFieldRadio
