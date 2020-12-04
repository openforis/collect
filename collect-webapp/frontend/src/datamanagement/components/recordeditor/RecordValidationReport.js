import './RecordValidationReport.css'

import React from 'react'
import { Button, Dialog, DialogContent, DialogTitle, DialogActions } from '@material-ui/core'
import { DataGrid } from '@material-ui/data-grid'

import L from 'utils/Labels'
import { Attribute } from 'model/Record/Attribute'
import * as Validations from 'model/Validations'

const RecordValidationReport = (props) => {
  const { record, onClose } = props

  const columns = [
    { field: 'id', hide: true },
    { field: 'path', hide: true },
    { field: 'pathHR', headerName: L.l('common.node'), width: 600 },
    { field: 'message', headerName: L.l('common.message'), width: 500 },
  ]

  const rows = []

  record.traverse((node) => {
    if (node instanceof Attribute) {
      const { errors: errorsArray, warnings: warningsArray } = node.validationResults

      const errors = errorsArray?.join('; ')
      const warnings = warningsArray?.join('; ')

      if (errors || warnings) {
        const row = { path: node.path, pathHR: node.pathHR }
        row['id'] = node.path + (errors ? '(err)' : '(warn)')
        row['severity'] = errors ? 'error' : 'warning'
        row['message'] = errors ? errors : warnings
        rows.push(row)
      }
    } else {
      const cardinalityErrorsByChildDefName = Validations.getCardinalityErrorsByChildDefName({ entity: node })
      Object.entries(cardinalityErrorsByChildDefName).forEach(([childDefName, message]) => {
        const childDef = node.definition.getChildDefinitionByName(childDefName)
        rows.push({
          id: `${node.path}/${childDefName}(min/max count)`,
          path: `${node.path}/${childDefName}`,
          pathHR: `${node.pathHR} / ${childDef.labelOrName}`,
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
        <DataGrid rows={rows} columns={columns} pageSize={20} />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{L.l('general.cancel')}</Button>
      </DialogActions>
    </Dialog>
  )
}

export default RecordValidationReport
