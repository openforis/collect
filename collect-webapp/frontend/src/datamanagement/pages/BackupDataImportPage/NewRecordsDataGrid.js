import React from 'react'

import { DataGrid, DataGridValueFormatters } from 'common/components/DataGrid'

export const NewRecordsDataGrid = (props) => {
  const { keyAttributeColumns, recordsToImport, selectedRecordsToImportIds, stepColumns, onSelectedIdsChange } = props

  return (
    <DataGrid
      className="data-import-new-records"
      rows={recordsToImport}
      columns={[
        { field: 'entryId', hide: true },
        ...keyAttributeColumns,
        ...stepColumns,
        {
          field: 'recordCreationDate',
          headerName: 'dataManagement.backupDataImport.createdOn',
          valueFormatter: DataGridValueFormatters.dateTime,
          width: 150,
        },
        {
          field: 'recordModifiedDate',
          headerName: 'dataManagement.backupDataImport.modifiedOn',
          valueFormatter: DataGridValueFormatters.dateTime,
          width: 150,
        },
        {
          field: 'recordFilledAttributesCount',
          headerName: 'dataManagement.backupDataImport.filledValues',
          width: 120,
          align: 'right',
        },
      ]}
      getRowId={(row) => row.entryId}
      checkboxSelection
      onSelectedIdsChange={onSelectedIdsChange}
      selectionModel={selectedRecordsToImportIds}
    />
  )
}
