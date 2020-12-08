import './RecordEditActionBar.css'

import React, { useState } from 'react'
import { Button } from '@material-ui/core'
import { ThumbDown, ThumbUp } from '@material-ui/icons'

import L from 'utils/Labels'
import { useRecordEvent } from 'common/hooks'
import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'
import Workflow from 'model/Workflow'

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
  const nextStep = Workflow.getNextStep(step)
  const nextStepLabel = nextStep ? L.l(`dataManagement.workflow.step.${nextStep.toLocaleLowerCase()}`) : null
  const prevStep = Workflow.getPrevStep(step)
  const prevStepLabel = prevStep ? L.l(`dataManagement.workflow.step.${prevStep.toLocaleLowerCase()}`) : null

  return (
    <div className="record-edit-action-bar">
      <label>
        {stepSummary}
        {definition.labelOrName}: {rootEntitySummary}
      </label>
      <ValidationReportIcon record={record} />
      {prevStep && (
        <Button
          variant="contained"
          color="secondary"
          startIcon={<ThumbDown />}
          title={L.l('dataManagement.dataEntry.demoteTo', [prevStepLabel])}
        >
          {L.l('dataManagement.dataEntry.demote')}
        </Button>
      )}
      {nextStep && (
        <Button
          variant="contained"
          color="secondary"
          endIcon={<ThumbUp />}
          title={L.l('dataManagement.dataEntry.promoteTo', [nextStepLabel])}
        >
          {L.l('dataManagement.dataEntry.promote')}
        </Button>
      )}
    </div>
  )
}

export default RecordEditActionBar
