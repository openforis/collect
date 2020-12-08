import './RecordEditActionBar.css'

import React, { useState } from 'react'
import classNames from 'classnames'
import DoneIcon from '@material-ui/icons/Done'
import WarningIcon from '@material-ui/icons/Warning'
import { IconButton } from '@material-ui/core'

import L from 'utils/Labels'
import RecordValidationReport from './RecordValidationReport'
import { useRecordEvent } from 'common/hooks'
import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'

const RecordEditActionBar = (props) => {
  const { record } = props
  const { preview, rootEntity, step, validationSummary: recordValidationSummary } = record
  const { definition } = rootEntity

  const [validationSummary, setValidationSummary] = useState(recordValidationSummary)
  const [rootEntitySummary, setRootEntitySummary] = useState(rootEntity.summaryValues)
  const [showValidationReport, setShowValidationReport] = useState(false)
  const toggleShowValidationReport = () => setShowValidationReport(!showValidationReport)

  const errorsOrWarnings = validationSummary.errors || validationSummary.warnings

  useRecordEvent({
    parentEntity: rootEntity,
    onEvent: (event) => {
      if (event.isRelativeToRecord(record)) {
        setValidationSummary(record.validationSummary)
        if (
          event instanceof AttributeValueUpdatedEvent &&
          event.isRelativeToEntityKeyAttributes({ entity: rootEntity })
        ) {
          setRootEntitySummary(rootEntity.summaryValues)
        }
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
          className={classNames('icon', 'validation-report', { error: errorsOrWarnings })}
          aria-label="validation-report"
          title={
            errorsOrWarnings
              ? L.l('dataManagement.recordValidationReport.tooltipErrors', [
                  validationSummary.errors,
                  validationSummary.warnings,
                ])
              : L.l('dataManagement.recordValidationReport.tooltipOk')
          }
          onClick={toggleShowValidationReport}
        >
          {errorsOrWarnings ? <WarningIcon /> : <DoneIcon />}
        </IconButton>
      </div>
      {showValidationReport && <RecordValidationReport record={record} onClose={toggleShowValidationReport} />}
    </>
  )
}

export default RecordEditActionBar
