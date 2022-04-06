import './RecordValidationReport.css'

import React from 'react'
import { Button, Dialog, DialogContent, DialogTitle, DialogActions } from '@material-ui/core'

import L from 'utils/Labels'
import { Attribute } from 'model/Record/Attribute'
import * as Validations from 'model/Validations'
import { DataGrid } from 'common/components'

const RecordValidationReport = (props) => {
  const { record, onClose } = props

  const rows = []

  record.traverse((node) => {
    if (node instanceof Attribute) {
      const { errorMessage, warningMessage } = node.validation

      if (errorMessage || warningMessage) {
        const row = { path: node.path, pathHR: node.pathHR }
        row['id'] = node.path + (errorMessage ? '(err)' : '(warn)')
        row['severity'] = errorMessage ? 'error' : 'warning'
        row['message'] = errorMessage ? errorMessage : warningMessage
        rows.push(row)
      }
    } else {
      const cardinalityErrorMessageByChildDefName = Validations.getCardinalityErrorMessageByChildDefName({
        entity: node,
      })
      Object.entries(cardinalityErrorMessageByChildDefName).forEach(([childDefName, message]) => {
        const nodePathHR = node.pathHR
        const childDef = node.definition.getChildDefinitionByName(childDefName)
        rows.push({
          id: `${node.path}/${childDefName}(min/max count)`,
          path: `${node.path}/${childDefName}`,
          pathHR: `${nodePathHR ? `${nodePathHR} / ` : ''}${childDef.labelOrName}`,
          severity: 'error',
          message,
        })
      })
    }
  })

  return (
    <Dialog
      className="record-validation-report"
      onClose={onClose}
      aria-labelledby="record-validation-report-dialog-title"
      open
      maxWidth="lg"
      fullWidth
    >
      <DialogTitle id="record-validation-report-dialog-title">
        {L.l('dataManagement.recordValidationReport.title')}
      </DialogTitle>
      <DialogContent className="dialog-content">
        <DataGrid
          columns={[
            { field: 'id', hide: true },
            { field: 'path', hide: true },
            { field: 'severity', hide: true },
            { field: 'pathHR', headerName: 'common.node', flex: 0.6 },
            { field: 'message', headerName: 'common.message', flex: 0.4 },
          ]}
          exportFileName={`record-validation-report-${record.rootEntity.summaryValues}.csv`}
          rows={rows}
          showToolbar
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{L.l('common.close')}</Button>
      </DialogActions>
    </Dialog>
  )
}

export default RecordValidationReport
