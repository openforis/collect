import React from 'react'

import L from 'utils/Labels'
import { DataGrid, DataGridValueFormatters } from 'common/components'

export const ConflictingRecordsDataGrid = (props) => {
  const { keyAttributeColumns, conflictingRecords, selectedConflictingRecordsIds, stepColumns, onSelectedIdsChange } =
    props

  const importabilityRenderer = ({ row, value }) => {
    const prefix = 'dataManagement.backupDataImport.recordImportability.'
    let iconClass
    let importabilityMessageKey
    switch (value) {
      case -1:
        iconClass = 'circle-red'
        importabilityMessageKey = 'importNotSuggested'
        break
      case 0:
        iconClass = 'equal-sign'
        importabilityMessageKey = 'recordAndConflictingRecordAreEqual'
        break
      case 1:
        iconClass = 'circle-green'
        importabilityMessageKey = 'importSuggested'
        break
      default:
        iconClass = ''
    }
    const title = `${L.l(`${prefix}${importabilityMessageKey}`)}
${L.l(`${prefix}recordFilledValues`)}: ${row.recordFilledAttributesCount}
${L.l(`${prefix}conflictingRecordFilledValues`)}: ${row.conflictingRecordFilledAttributesCount}`

    return <span className={iconClass} title={title}></span>
  }

  return (
    <DataGrid
      className="data-import-conflicting-records"
      rows={conflictingRecords}
      columns={[
        { field: 'entryId', hide: true },
        ...keyAttributeColumns,
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
        ...stepColumns,
        {
          field: 'conflictingRecordCreationDate',
          headerName: 'dataManagement.backupDataImport.oldCreatedOn',
          valueFormatter: DataGridValueFormatters.dateTime,
          width: 150,
        },
        {
          field: 'conflictingRecordModifiedDate',
          headerName: 'dataManagement.backupDataImport.oldModifiedOn',
          valueFormatter: DataGridValueFormatters.dateTime,
          width: 150,
        },
        {
          field: 'conflictingRecordStep',
          headerName: 'dataManagement.backupDataImport.oldStep',
          width: 120,
          align: 'right',
        },
        {
          field: 'importabilityLevel',
          headerName: 'dataManagement.backupDataImport.importability',
          renderCell: importabilityRenderer,
          width: 120,
          align: 'right',
        },
      ]}
      getRowId={(row) => row.entryId}
      checkboxSelection
      onSelectedIdsChange={onSelectedIdsChange}
      selectionModel={selectedConflictingRecordsIds}
    />
  )
}
