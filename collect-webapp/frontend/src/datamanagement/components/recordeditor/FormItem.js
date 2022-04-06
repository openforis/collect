import React from 'react'
import classNames from 'classnames'

import { FieldDefinition } from 'model/ui/FieldDefinition'
import { CodeAttributeDefinition } from 'model/Survey'
import FormItemTypes from 'model/ui/FormItemTypes'

import ValidationTooltip from 'common/components/ValidationTooltip'
import Fieldset from './Fieldset'
import MultipleFieldset from './MultipleFieldset'
import Table from './Table'
import FormItemFieldComponent from './FormItemFieldComponent'
import FormItemMultipleFieldComponent from './FormItemMultipleFieldComponent'
import NodeDefLabel from './NodeDefLabel'
import { useCardinalityValidation } from './useCardinalityValidation'

const internalComponentByFieldType = {
  [FormItemTypes.FIELD]: FormItemFieldComponent,
  [FormItemTypes.MULTIPLE_FIELD]: FormItemMultipleFieldComponent,
  [FormItemTypes.MULTIPLE_FIELDSET]: MultipleFieldset,
  [FormItemTypes.FIELDSET]: Fieldset,
  [FormItemTypes.TABLE]: Table,
}

const _includeLabel = (itemDef) => itemDef instanceof FieldDefinition

const FormItem = (props) => {
  const { itemDef, parentEntity, fullSize } = props
  const { nodeDefinition, type } = itemDef
  const { id: nodeDefinitionId } = nodeDefinition

  const { cardinalityValidation } = useCardinalityValidation({ nodeDefinition, parentEntity })

  const wrapperId = `form-item-${parentEntity.id}-node-def-${nodeDefinitionId}`

  const InternalComponentClass =
    nodeDefinition instanceof CodeAttributeDefinition ? FormItemFieldComponent : internalComponentByFieldType[type]

  const internalComponent = <InternalComponentClass itemDef={itemDef} parentEntity={parentEntity} fullSize={fullSize} />

  if (fullSize) {
    return internalComponent
  }

  return (
    <>
      {_includeLabel(itemDef) && (
        <div>
          <NodeDefLabel nodeDefinition={nodeDefinition} />
        </div>
      )}
      <div>
        <div id={wrapperId} className={classNames('form-item-wrapper', { error: cardinalityValidation.hasErrors() })}>
          {internalComponent}
        </div>
        <ValidationTooltip target={wrapperId} validation={cardinalityValidation} />
      </div>
    </>
  )
}

FormItem.defaultProps = {
  fullSize: false,
}

export default FormItem
