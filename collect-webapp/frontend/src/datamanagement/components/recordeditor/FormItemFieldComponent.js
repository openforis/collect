import React, { useRef, useState } from 'react'
import classNames from 'classnames'

import { AttributeDefinition } from 'model/Survey'

import { useRecordEvent } from 'common/hooks'
import ValidationTooltip from 'common/components/ValidationTooltip'

import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'

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

const extractValidation = (props) => {
  const { parentEntity, itemDef, attribute: attributeParam } = props
  let errors = null,
    warnings = null
  if (parentEntity) {
    const attrDef = itemDef.attributeDefinition
    let attr = null
    if (!attrDef.multiple) {
      attr = parentEntity.getSingleChild(attrDef.id)
    } else if (attributeParam) {
      attr = attributeParam
    }
    if (attr) {
      const { errors: errorsArray, warnings: warningsArray } = attr.validationResults
      errors = errorsArray ? errorsArray.join('; ') : null
      warnings = warningsArray ? warningsArray.join('; ') : null
      return { errors, warnings }
    }
  }
  return { errors, warnings }
}

const FormItemFieldComponent = (props) => {
  const { itemDef, parentEntity, attribute, inTable } = props

  const wrapperIdRef = useRef(`form-item-field-${new Date().getTime()}`)
  const wrapperId = wrapperIdRef.current

  const [validation, setValidation] = useState({ errors: null, warnings: null })

  useRecordEvent({
    parentEntity,
    onEvent: (event) => {
      if (
        event instanceof AttributeValueUpdatedEvent &&
        event.isRelativeToNodes({ parentEntity, nodeDefId: itemDef.attributeDefinitionId })
      ) {
        setValidation(extractValidation(props))
      }
    },
  })

  const { attributeDefinition } = itemDef
  const { attributeType } = attributeDefinition
  const { errors, warnings } = validation
  const Component = FIELD_COMPONENTS_BY_TYPE[attributeType]

  return Component ? (
    <>
      <div
        id={wrapperId}
        className={classNames('form-item-field-wrapper', { error: Boolean(errors), warning: Boolean(warnings) })}
      >
        <Component
          fieldDef={itemDef}
          parentEntity={parentEntity}
          attribute={attribute}
          inTable={inTable}
          error={Boolean(errors)}
          warning={Boolean(warnings)}
        />
      </div>
      <ValidationTooltip target={wrapperId} errors={errors} warnings={warnings} />
    </>
  ) : (
    <div>Field type {attributeType} not supported yet</div>
  )
}

export default FormItemFieldComponent
