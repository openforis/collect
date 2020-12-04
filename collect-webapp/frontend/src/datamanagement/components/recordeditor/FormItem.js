import React, { useState } from 'react'
import { Row, Col } from 'reactstrap'
import classNames from 'classnames'

import { FieldDefinition } from 'model/ui/FieldDefinition'
import { CodeAttributeDefinition } from 'model/Survey'
import FormItemTypes from 'model/ui/FormItemTypes'
import * as Validations from 'model/Validations'
import { NodeCountUpdatedEvent, NodeCountValidationUpdatedEvent } from 'model/event/RecordEvent'

import ValidationTooltip from 'common/components/ValidationTooltip'
import { useRecordEvent } from 'common/hooks'
import Fieldset from './Fieldset'
import MultipleFieldset from './MultipleFieldset'
import Table from './Table'
import FormItemFieldComponent from './FormItemFieldComponent'
import FormItemMultipleFieldComponent from './FormItemMultipleFieldComponent'
import NodeDefLabel from './NodeDefLabel'

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
  const { nodeDefinitionId, nodeDefinition, type } = itemDef

  const [cardinalityErrors, setCardinalityErrors] = useState(
    Validations.getCardinalityErrors({ nodeDefinition: itemDef.nodeDefinition, parentEntity })
  )

  useRecordEvent({
    parentEntity,
    onEvent: (event) => {
      if (
        (event instanceof NodeCountValidationUpdatedEvent || event instanceof NodeCountUpdatedEvent) &&
        event.isRelativeToNodes({ parentEntity, nodeDefId: nodeDefinitionId })
      ) {
        setCardinalityErrors(_getCardinalityErrors({ itemDef, parentEntity }))
      }
    },
  })

  const wrapperId = `form-item-${parentEntity.id}-node-def-${nodeDefinitionId}`

  const InternalComponentClass =
    nodeDefinition instanceof CodeAttributeDefinition ? FormItemFieldComponent : internalComponentByFieldType[type]

  const internalComponent = <InternalComponentClass itemDef={itemDef} parentEntity={parentEntity} fullSize={fullSize} />

  return fullSize ? (
    internalComponent
  ) : (
    <Row>
      {_includeLabel(itemDef) && (
        <Col style={{ maxWidth: '150px' }}>
          <NodeDefLabel nodeDefinition={nodeDefinition} />
        </Col>
      )}
      <Col>
        <>
          <div id={wrapperId} className={classNames('form-item-wrapper', { error: Boolean(cardinalityErrors) })}>
            {internalComponent}
          </div>
          <ValidationTooltip target={wrapperId} errors={cardinalityErrors} />
        </>
      </Col>
    </Row>
  )
}

FormItem.defaultProps = {
  fullSize: false,
}

export default FormItem
