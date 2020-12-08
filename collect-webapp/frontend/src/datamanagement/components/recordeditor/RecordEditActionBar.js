import './RecordEditActionBar.css'

import React, { useState } from 'react'
import WarningIcon from '@material-ui/icons/Warning'
import { IconButton } from '@material-ui/core'

import L from 'utils/Labels'
import RecordValidationReport from './RecordValidationReport'
import { useRecordEvent } from 'common/hooks'
import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'

const RecordEditActionBar = (props) => {
  const { record } = props
  const { preview, rootEntity, step } = record
  const { definition } = rootEntity

  const [rootEntitySummary, setRootEntitySummary] = useState(rootEntity.summaryValues)
  const [showValidationReport, setShowValidationReport] = useState(false)
  const toggleShowValidationReport = () => setShowValidationReport(!showValidationReport)

  useRecordEvent({
    parentEntity: rootEntity,
    onEvent: (event) => {
      if (
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
    <>
      <div className="record-edit-action-bar">
        <label>
          {stepSummary}
          {definition.labelOrName}: {rootEntitySummary}
        </label>
        <IconButton
          className="icon validation-report"
          aria-label="validation-report"
          title={L.l('dataManagement.recordValidationReport.title')}
          onClick={toggleShowValidationReport}
        >
          <WarningIcon />
        </IconButton>
      </div>
      {showValidationReport && <RecordValidationReport record={record} onClose={toggleShowValidationReport} />}
    </>
  )
}

export default RecordEditActionBar
