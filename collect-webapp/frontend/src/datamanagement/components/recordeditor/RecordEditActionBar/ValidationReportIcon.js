import React, { useState } from 'react'
import classNames from 'classnames'
import DoneIcon from '@mui/icons-material/Done'
import WarningIcon from '@mui/icons-material/Warning'
import { IconButton } from '@mui/material'

import { useRecordEvent } from 'common/hooks'
import L from 'utils/Labels'

import RecordValidationReport from './RecordValidationReport'

const ValidationReportIcon = (props) => {
  const { record } = props
  const { rootEntity, validationSummary: recordValidationSummary } = record

  const [validationSummary, setValidationSummary] = useState(recordValidationSummary)
  const [showValidationReport, setShowValidationReport] = useState(false)
  const toggleShowValidationReport = () => setShowValidationReport(!showValidationReport)

  useRecordEvent({
    parentEntity: rootEntity,
    onEvent: (event) => {
      if (event.isRelativeToRecord(record)) {
        setValidationSummary(record.validationSummary)
      }
    },
  })

  const errorsOrWarnings = validationSummary.errors || validationSummary.warnings

  return <>
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
      size="large">
      {errorsOrWarnings ? <WarningIcon /> : <DoneIcon />}
    </IconButton>
    {showValidationReport && <RecordValidationReport record={record} onClose={toggleShowValidationReport} />}
  </>;
}

export default ValidationReportIcon
