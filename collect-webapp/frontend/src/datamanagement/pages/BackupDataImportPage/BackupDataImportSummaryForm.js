import './BackupDataImportSummaryForm.scss'

import React from 'react'
import { FormGroup } from 'reactstrap'

import { DataGrid } from 'common/components/DataGrid'
import L from 'utils/Labels'
import Strings from 'utils/Strings'
import Workflow from 'model/Workflow'

import { NewRecordsDataGrid } from './NewRecordsDataGrid'
import { ConflictingRecordsDataGrid } from './ConflictingRecordsDataGrid'

const BackupDataImportSummaryForm = (props) => {
  const {
    survey,
    dataImportSummary,
    selectedRecordsToImportIds,
    handleRecordsToImportSelectedIdsChange,
    selectedConflictingRecordsIds,
    handleConflictingRecordsSelectedIdsChange,
  } = props

  const createKeyAttributesColumns = () => {
    const keyAttributes = survey.schema.firstRootEntityDefinition.keyAttributeDefinitions

    const rootEntityKeyFormatter = ({ row, field }) => {
      const keyIdx = field.substring(3) - 1
      return row.record.rootEntityKeys[keyIdx]
    }

    return keyAttributes.map((keyAttr, i) => ({
      field: `key${i + 1}`,
      valueFormatter: rootEntityKeyFormatter,
      headerName: keyAttr.labelOrName,
      flex: 1,
    }))
  }

  const keyAttributeColumns = createKeyAttributesColumns()

  const stepColumns = Workflow.STEP_CODES.map((step) => ({
    field: `${step.toLocaleLowerCase()}DataPresent`,
    renderCell: ({ row, value }) => {
      const { warnings } = row
      const warningsInStep = warnings
        .filter((warning) => warning.step == step)
        .map((warning) => `${warning.path}: ${L.l(warning.message)}`)

      const warningsTitle = [...new Set(warningsInStep)].join('\n')

      return (
        <span className="icons-wrapper align-center">
          {warningsTitle && <span className="fa fa-exclamation-triangle warning" title={warningsTitle} />}
          {value && <span className="checked small" />}
        </span>
      )
    },
    width: 120,
    headerName: step,
    align: 'center',
  }))

  return (
    <FormGroup tag="fieldset">
      <legend>{L.l('dataManagement.backupDataImport.dataImportSummary')}</legend>

      {dataImportSummary.recordsToImport.length > 0 && (
        <fieldset className="secondary">
          <legend>
            {L.l('dataManagement.backupDataImport.newRecordsToBeImported', [
              selectedRecordsToImportIds.length,
              dataImportSummary.recordsToImport.length,
            ])}
          </legend>
          <NewRecordsDataGrid
            keyAttributeColumns={keyAttributeColumns}
            stepColumns={stepColumns}
            recordsToImport={dataImportSummary.recordsToImport}
            selectedRecordsToImportIds={selectedRecordsToImportIds}
            onSelectedIdsChange={handleRecordsToImportSelectedIdsChange}
          />
        </fieldset>
      )}
      {dataImportSummary.conflictingRecords.length > 0 && (
        <fieldset className="secondary">
          <legend>
            {L.l('dataManagement.backupDataImport.conflictingRecordsToBeImported', [
              selectedConflictingRecordsIds.length,
              dataImportSummary.conflictingRecords.length,
            ])}
          </legend>
          <ConflictingRecordsDataGrid
            keyAttributeColumns={keyAttributeColumns}
            stepColumns={stepColumns}
            conflictingRecords={dataImportSummary.conflictingRecords}
            selectedConflictingRecordsIds={selectedConflictingRecordsIds}
            onSelectedIdsChange={handleConflictingRecordsSelectedIdsChange}
          />
        </fieldset>
      )}
      {dataImportSummary.skippedFileErrors.length > 0 && (
        <fieldset className="secondary">
          <legend>
            {L.l('dataManagement.backupDataImport.errorsFound', [dataImportSummary.skippedFileErrors.length])}
          </legend>
          <DataGrid
            className="data-import-errors-data-grid"
            columns={[
              { field: 'fileName', headerName: 'dataManagement.backupDataImport.errors.fileName', width: 200 },
              {
                field: 'errors',
                headerName: 'dataManagement.backupDataImport.errors.messages',
                renderCell: ({ row }) => (
                  <ul className="errors-list">
                    {row.errors.map((error) => {
                      let message = error.message
                      if (Strings.isNotBlank(error.path)) {
                        message += ' ' + L.l('dataManagement.backupDataImport.errors.path') + ': ' + error.path
                      }
                      return <li>{message}</li>
                    })}
                  </ul>
                ),
                flex: 1,
              },
            ]}
            getRowId={(row) => row.fileName}
            hideFooterPagination
            rows={dataImportSummary.skippedFileErrors}
          />
        </fieldset>
      )}
    </FormGroup>
  )
}

export default BackupDataImportSummaryForm
