import React, { useState } from 'react'
import { Label, Row, Col } from 'reactstrap'
import classNames from 'classnames'

import { FieldDefinition } from 'model/ui/FieldDefinition'
import { CodeAttributeDefinition } from 'model/Survey'
import FormItemTypes from 'model/ui/FormItemTypes'
import { ValidationResultFlag } from 'model/ValidationResultFlag'
import { NodeCountUpdatedEvent, NodeCountValidationUpdatedEvent } from 'model/event/RecordEvent'
import L from 'utils/Labels'

import ValidationTooltip from 'common/components/ValidationTooltip'
import Fieldset from './Fieldset'
import MultipleFieldset from './MultipleFieldset'
import Table from './Table'
import FormItemFieldComponent from './FormItemFieldComponent'
import FormItemMultipleFieldComponent from './FormItemMultipleFieldComponent'
import { useRecordEvent } from '../../../common/hooks'

const internalComponentByFieldType = {
  [FormItemTypes.FIELD]: FormItemFieldComponent,
  [FormItemTypes.MULTIPLE_FIELD]: FormItemMultipleFieldComponent,
  [FormItemTypes.MULTIPLE_FIELDSET]: MultipleFieldset,
  [FormItemTypes.FIELDSET]: Fieldset,
  [FormItemTypes.TABLE]: Table,
}

const _includeLabel = (itemDef) => itemDef instanceof FieldDefinition

const _getCardinalityErrors = ({ itemDef, parentEntity }) => {
  const { nodeDefinitionId: nodeDefId, nodeDefinition } = itemDef
  const minCount = parentEntity.childrenMinCountByDefinitionId[nodeDefId]
  const maxCount = parentEntity.childrenMaxCountByDefinitionId[nodeDefId]
  const validationMinCount = parentEntity.childrenMinCountValidationByDefinitionId[nodeDefId]
  const validationMaxCount = parentEntity.childrenMaxCountValidationByDefinitionId[nodeDefId]

  if (validationMinCount === ValidationResultFlag.ERROR) {
    return nodeDefinition.multiple
      ? L.l('dataManagement.dataEntry.validation.minCount', minCount)
      : L.l('dataManagement.dataEntry.validation.required')
  } else if (validationMaxCount === ValidationResultFlag.ERROR) {
    return L.l('dataManagement.dataEntry.validation.maxCount', maxCount)
  }
}

const FormItem = (props) => {
  const { itemDef, parentEntity, fullSize } = props
  const { nodeDefinitionId, nodeDefinition, type } = itemDef
  const { labelOrName } = nodeDefinition

  const [cardinalityErrors, setCardinalityErrors] = useState(_getCardinalityErrors({ itemDef, parentEntity }))

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
          <Label>{labelOrName}</Label>
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
