import React from 'react'

import { AttributeDefinition } from 'model/Survey'

import BooleanField from './fields/BooleanField'
import CodeField from './fields/CodeField'
import CoordinateField from './fields/CoordinateField'
import DateField from './fields/DateField'
import NumberField from './fields/NumberField'
import TextField from './fields/TextField'

const FIELD_COMPONENTS_BY_TYPE = {
  [AttributeDefinition.Types.BOOLEAN]: BooleanField,
  [AttributeDefinition.Types.CODE]: CodeField,
  [AttributeDefinition.Types.COORDINATE]: CoordinateField,
  [AttributeDefinition.Types.DATE]: DateField,
  [AttributeDefinition.Types.NUMBER]: NumberField,
  [AttributeDefinition.Types.TEXT]: TextField,
}

const FormItemFieldComponent = (props) => {
  const { itemDef, parentEntity, attribute } = props
  const attrDef = itemDef.attributeDefinition
  const Component = FIELD_COMPONENTS_BY_TYPE[attrDef.attributeType]

  return Component ? (
    <Component fieldDef={itemDef} parentEntity={parentEntity} attribute={attribute} />
  ) : (
    <div>Field type {attrDef.attributeType} not supported yet</div>
  )
}

export default FormItemFieldComponent
