import React from 'react'

import { AttributeDefinition } from 'model/Survey'

import BooleanField from './fields/BooleanField'
import CodeField from './fields/CodeField'
import DateField from './fields/DateField'
import NumberField from './fields/NumberField'
import TextField from './fields/TextField'

const FIELD_COMPONENTS_BY_TYPE = {
  [AttributeDefinition.Types.BOOLEAN]: BooleanField,
  [AttributeDefinition.Types.CODE]: CodeField,
  [AttributeDefinition.Types.DATE]: DateField,
  [AttributeDefinition.Types.NUMBER]: NumberField,
  [AttributeDefinition.Types.TEXT]: TextField,
}

export default (props) => {
  const { itemDef, parentEntity } = props
  const attrDef = itemDef.attributeDefinition
  const component = FIELD_COMPONENTS_BY_TYPE[attrDef.attributeType]

  return component ? (
    React.createElement(component, { fieldDef: itemDef, parentEntity })
  ) : (
    <div>Field type {attrDef.attributeType} not supported yet</div>
  )
}
