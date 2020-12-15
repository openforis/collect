import React, { useCallback, useRef, useState } from 'react'
import classNames from 'classnames'

import { AttributeDefinition } from 'model/Survey'

import { useRecordEvent } from 'common/hooks'
import ValidationTooltip from 'common/components/ValidationTooltip'

import { AttributeValueUpdatedEvent, NodeRelevanceUpdatedEvent } from 'model/event/RecordEvent'

import BooleanField from './fields/BooleanField'
import CodeField from './fields/CodeField'
import CoordinateField from './fields/CoordinateField'
import DateField from './fields/DateField'
import FileField from './fields/FileField'
import NumberField from './fields/NumberField'
import RangeField from './fields/RangeField'
import TaxonField from './fields/TaxonField'
import TextField from './fields/TextField'
import TimeField from './fields/TimeField'
import * as Validations from 'model/Validations'

const FIELD_COMPONENTS_BY_TYPE = {
  [AttributeDefinition.Types.BOOLEAN]: BooleanField,
  [AttributeDefinition.Types.CODE]: CodeField,
  [AttributeDefinition.Types.COORDINATE]: CoordinateField,
  [AttributeDefinition.Types.DATE]: DateField,
  [AttributeDefinition.Types.FILE]: FileField,
  [AttributeDefinition.Types.NUMBER]: NumberField,
  [AttributeDefinition.Types.RANGE]: RangeField,
  [AttributeDefinition.Types.TAXON]: TaxonField,
  [AttributeDefinition.Types.TEXT]: TextField,
  [AttributeDefinition.Types.TIME]: TimeField,
}

const FormItemFieldComponent = (props) => {
  const { itemDef, parentEntity, attribute, inTable } = props
  const { attributeDefinition } = itemDef
  const { attributeType, id: attributeDefinitionId } = attributeDefinition

  const wrapperIdRef = useRef(`form-item-field-${new Date().getTime()}`)
  const wrapperId = wrapperIdRef.current

  const calculateIsRelevant = useCallback(() => parentEntity.childrenRelevanceByDefinitionId[attributeDefinitionId], [
    parentEntity,
  ])

  const calculateValidation = useCallback(
    () =>
      Validations.getAttributeValidation({
        parentEntity,
        attributeDefinition,
        attribute,
      }),
    [parentEntity, attributeDefinition, attribute]
  )

  const [validation, setValidation] = useState(calculateValidation())
  const [relevant, setRelevant] = useState(calculateIsRelevant())

  useRecordEvent({
    parentEntity,
    onEvent: (event) => {
      if (
        event instanceof AttributeValueUpdatedEvent &&
        event.isRelativeToNodes({ parentEntity, nodeDefId: attributeDefinitionId })
      ) {
        setValidation(calculateValidation())
      } else if (
        event instanceof NodeRelevanceUpdatedEvent &&
        event.isRelativeToNodes({ parentEntity, nodeDefId: attributeDefinitionId })
      ) {
        setRelevant(calculateIsRelevant())
      }
    },
  })

  const Component = FIELD_COMPONENTS_BY_TYPE[attributeType]

  if (!Component) {
    return <div>Field type {attributeType} not supported yet</div>
  }
  return (
    <>
      <div
        id={wrapperId}
        className={classNames('form-item-field-wrapper', {
          error: validation.hasErrors(),
          warning: validation.hasWarnings(),
          'not-relevant': !relevant,
        })}
      >
        <Component fieldDef={itemDef} parentEntity={parentEntity} attribute={attribute} inTable={inTable} />
      </div>
      <ValidationTooltip target={wrapperId} validation={validation} />
    </>
  )
}

export default FormItemFieldComponent
