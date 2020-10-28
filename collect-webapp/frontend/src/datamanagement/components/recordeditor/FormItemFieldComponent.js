import React from 'react'

import { AttributeDefinition } from 'model/Survey'

import BooleanField from './fields/BooleanField'
import CodeField from './fields/CodeField'
import CoordinateField from './fields/CoordinateField'
import DateField from './fields/DateField'
import FileField from './fields/FileField'
import NumberField from './fields/NumberField'
import TaxonField from './fields/TaxonField'
import TextField from './fields/TextField'
import TimeField from './fields/TimeField'

const FIELD_COMPONENTS_BY_TYPE = {
  [AttributeDefinition.Types.BOOLEAN]: BooleanField,
  [AttributeDefinition.Types.CODE]: CodeField,
  [AttributeDefinition.Types.COORDINATE]: CoordinateField,
  [AttributeDefinition.Types.DATE]: DateField,
  [AttributeDefinition.Types.FILE]: FileField,
  [AttributeDefinition.Types.NUMBER]: NumberField,
  [AttributeDefinition.Types.TAXON]: TaxonField,
  [AttributeDefinition.Types.TEXT]: TextField,
  [AttributeDefinition.Types.TIME]: TimeField,
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
