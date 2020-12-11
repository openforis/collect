import './RecordValidationReport.css'

import React from 'react'
import { Button, Dialog, DialogContent, DialogTitle, DialogActions } from '@material-ui/core'
import BootstrapTable from 'react-bootstrap-table-next'
import paginationFactory from 'react-bootstrap-table2-paginator'
import ToolkitProvider from 'react-bootstrap-table2-toolkit'

import L from 'utils/Labels'
import { Attribute } from 'model/Record/Attribute'
import * as Validations from 'model/Validations'

const RecordValidationReport = (props) => {
  const { record, onClose } = props

  const columns = [
    { dataField: 'id', hidden: true, csvExport: false },
    { dataField: 'path', hidden: true, csvExport: false },
    { dataField: 'severity', hidden: true, csvExport: false },
    { dataField: 'pathHR', text: L.l('common.node'), width: 600 },
    { dataField: 'message', text: L.l('common.message'), width: 500 },
  ]

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
    <ToolkitProvider
      data={rows}
      columns={columns}
      keyField="id"
      exportCSV={{ fileName: `record-validation-report-${record.rootEntity.summaryValues}.csv` }}
    >
      {(toolkitProps) => (
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
            <BootstrapTable
              {...toolkitProps.baseProps}
              pagination={paginationFactory({ sizePerPage: 10, hideSizePerPage: true, hidePageListOnlyOnePage: true })}
            />
          </DialogContent>
          <DialogActions>
            <Button onClick={() => toolkitProps.csvProps.onExport()} variant="contained" color="primary">
              {L.l('common.exportToCsv')}
            </Button>
            <Button onClick={onClose}>{L.l('common.close')}</Button>
          </DialogActions>
        </Dialog>
      )}
    </ToolkitProvider>
  )
}

export default RecordValidationReport
