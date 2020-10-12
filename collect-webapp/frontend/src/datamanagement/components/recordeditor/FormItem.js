import React from 'react'
import { Label, Row, Col } from 'reactstrap'

import { FieldDefinition } from 'model/ui/FieldDefinition'
import Fieldset from './Fieldset'
import MultipleFieldset from './MultipleFieldset'
import Table from './Table'
import FormItemFieldComponent from './FormItemFieldComponent'
import FormItemTypes from '../../../model/ui/FormItemTypes'

const internalComponentByFieldType = {
  [FormItemTypes.FIELD]: FormItemFieldComponent,
  [FormItemTypes.MULTIPLE_FIELDSET]: MultipleFieldset,
  [FormItemTypes.FIELDSET]: Fieldset,
  [FormItemTypes.TABLE]: Table,
}

const includeLabel = (itemDef) => itemDef instanceof FieldDefinition

const FormItem = (props) => {
  const { itemDef, parentEntity, fullSize } = props

  const InternalComponentClass = internalComponentByFieldType[itemDef.type]

  const internalComponent = <InternalComponentClass itemDef={itemDef} parentEntity={parentEntity} />

  return fullSize ? (
    internalComponent
  ) : (
    <Row>
      {includeLabel(itemDef) && (
        <Col style={{ maxWidth: '150px' }}>
          <Label>{itemDef.label}</Label>
        </Col>
      )}
      <Col>{internalComponent}</Col>
    </Row>
  )
}

FormItem.defaultProps = {
  fullSize: false,
}

export default FormItem
