import './RecordEditActionBar.css'

import React, { useState } from 'react'

import L from 'utils/Labels'
import { useRecordEvent } from 'common/hooks'
import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'

import ValidationReportIcon from './ValidationReportIcon'

const RecordEditActionBar = (props) => {
  const { record } = props
  const { preview, rootEntity, step } = record
  const { definition } = rootEntity

  const [rootEntitySummary, setRootEntitySummary] = useState(rootEntity.summaryValues)

  useRecordEvent({
    parentEntity: rootEntity,
    onEvent: (event) => {
      if (
        event.isRelativeToRecord(record) &&
        event instanceof AttributeValueUpdatedEvent &&
        event.isRelativeToEntityKeyAttributes({ entity: rootEntity })
      ) {
        setRootEntitySummary(rootEntity.summaryValues)
      }
    },
  })

  const stepLabel = L.l(`dataManagement.workflow.step.${step.toLocaleLowerCase()}`)
  const stepSummary = !preview ? '' : `${L.l('dataManagement.workflow.step.label')} : ${stepLabel} - `

  return (
    <div className="record-edit-action-bar">
      <label>
        {stepSummary}
        {definition.labelOrName}: {rootEntitySummary}
      </label>
      <ValidationReportIcon record={record} />
    </div>
  )
}

export default RecordEditActionBar
