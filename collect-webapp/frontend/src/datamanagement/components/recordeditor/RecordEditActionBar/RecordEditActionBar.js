import './RecordEditActionBar.scss'

import React, { useState } from 'react'
import { useSelector } from 'react-redux'
import { useNavigate } from 'react-router-dom'
import { Button, Icon, IconButton } from '@mui/material'
import { ThumbDown, ThumbUp } from '@mui/icons-material'

import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'
import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'
import Workflow from 'model/Workflow'
import ServiceFactory from 'services/ServiceFactory'

import { useRecordEvent } from 'common/hooks'
import Dialogs from 'common/components/Dialogs'
import ValidationReportIcon from './ValidationReportIcon'

const RecordEditActionBar = (props) => {
  const { record, inPopUp } = props
  const { id: recordId, preview, rootEntity, step, survey } = record
  const { id: surveyId, roleInGroup } = survey
  const { definition } = rootEntity

  const user = useSelector((state) => state.session.loggedUser)
  const navigate = useNavigate()

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
  const stepSummary = preview ? '' : `${L.l('dataManagement.workflow.step.label')} : ${stepLabel} - `
  const nextStep = Workflow.getNextStep(step)
  const nextStepLabel = nextStep ? L.l(`dataManagement.workflow.step.${nextStep.toLocaleLowerCase()}`) : null
  const prevStep = Workflow.getPrevStep(step)
  const prevStepLabel = prevStep ? L.l(`dataManagement.workflow.step.${prevStep.toLocaleLowerCase()}`) : null

  const onExportToExcel = () => ServiceFactory.recordService.exportRecordToExcel(record)
  const onExportToCollectFormat = () => ServiceFactory.recordService.exportRecordToCollectFormat(record)

  const onPromote = () => {
    const performPromote = async () => {
      await ServiceFactory.recordService.promoteRecord({ surveyId, recordId })
      Dialogs.alert(
        L.l('dataManagement.dataEntry.promoteCompleteTitle'),
        L.l('dataManagement.dataEntry.promoteCompleteMessage', [rootEntity.summaryValues, nextStepLabel])
      )
      RouterUtils.navigateToDataManagementHomePage(navigate)
    }

    if (record.errors) {
      if (user.canPromoteRecordWithErrors(roleInGroup)) {
        Dialogs.confirm(
          L.l('dataManagement.dataEntry.promote'),
          L.l('dataManagement.dataEntry.promoteWithErrorsForceConfirm', nextStepLabel),
          performPromote
        )
      } else {
        Dialogs.alert(L.l('dataManagement.dataEntry.promote'), L.l('dataManagement.dataEntry.promoteWithErrorsCannot'))
      }
    } else {
      Dialogs.confirm(
        L.l('dataManagement.dataEntry.promote'),
        L.l('dataManagement.dataEntry.promoteToConfirm', nextStepLabel),
        performPromote
      )
    }
  }

  const onDemote = () => {
    const performDemote = async () => {
      await ServiceFactory.recordService.demoteRecord({ surveyId, recordId })
      Dialogs.alert(
        L.l('dataManagement.dataEntry.demoteCompleteTitle'),
        L.l('dataManagement.dataEntry.demoteCompleteMessage', [rootEntity.summaryValues, prevStepLabel])
      )
      RouterUtils.navigateToDataManagementHomePage(navigate)
    }
    Dialogs.confirm(
      L.l('dataManagement.dataEntry.demote'),
      L.l('dataManagement.dataEntry.demoteToConfirm', prevStepLabel),
      performDemote
    )
  }

  return (
    <div className="record-edit-action-bar">
      <label>
        {stepSummary}
        {definition.labelOrName}: {rootEntitySummary}
      </label>
      <div className="button-group">
        <ValidationReportIcon record={record} />
        {!preview && (
          <>
            <IconButton title={L.l('common.exportToExcel')} onClick={onExportToExcel} size="large">
              <Icon className="fa fa-file-excel" color="primary" />
            </IconButton>
            <IconButton
              title={L.l('dataManagement.export.exportToCollectFormat')}
              onClick={onExportToCollectFormat}
              size="large"
            >
              <Icon className="fa fa-file-archive" color="primary" />
            </IconButton>
            {!inPopUp && (
              <>
                {user.canDemoteRecord({ record, roleInGroup }) && (
                  <Button
                    variant="contained"
                    color="warning"
                    size="small"
                    startIcon={<ThumbDown />}
                    title={L.l('dataManagement.dataEntry.demoteTo', [prevStepLabel])}
                    onClick={onDemote}
                  >
                    {L.l('dataManagement.dataEntry.demote')}
                  </Button>
                )}
                {user.canPromoteRecord({ record, roleInGroup }) && (
                  <Button
                    variant="contained"
                    color="secondary"
                    size="small"
                    endIcon={<ThumbUp />}
                    title={L.l('dataManagement.dataEntry.promoteTo', [nextStepLabel])}
                    onClick={onPromote}
                  >
                    {L.l('dataManagement.dataEntry.promote')}
                  </Button>
                )}
              </>
            )}
          </>
        )}
      </div>
    </div>
  )
}

export default RecordEditActionBar
