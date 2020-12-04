import './RecordEditActionBar.css'

import React, { useState } from 'react'
import WarningIcon from '@material-ui/icons/Warning'
import { IconButton } from '@material-ui/core'

import L from 'utils/Labels'
import RecordValidationReport from './RecordValidationReport'

const RecordEditActionBar = (props) => {
  const { record } = props
  const [showValidationReport, setShowValidationReport] = useState(false)
  const toggleShowValidationReport = () => setShowValidationReport(!showValidationReport)

  return (
    <>
      <div className="record-edit-action-bar">
        <IconButton className="icon validation-report"
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
