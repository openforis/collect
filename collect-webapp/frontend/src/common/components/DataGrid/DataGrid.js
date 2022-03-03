import React from 'react'
import { DataGrid as MuiDataGrid } from '@material-ui/data-grid'

export const DataGrid = (props) => {
  const { columns, rows, onSelectedIdsChange } = props

  return (
    <MuiDataGrid
      columns={columns.map((col) => ({ ...col, disableColumnMenu: true }))}
      rows={rows}
      checkboxSelection
      onSelectionModelChange={onSelectedIdsChange}
    />
  )
}
