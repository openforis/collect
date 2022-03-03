import React, { useCallback } from 'react'
import classNames from 'classnames'
import { DataGrid as MuiDataGrid } from '@material-ui/data-grid'

import L from 'utils/Labels'

export const DataGrid = (props) => {
  const { className, columns, rows, onRowDoubleClick: onRowDoubleClickProp, onSelectedIdsChange } = props

  const onCellDoubleClick = useCallback(
    (params) => {
      const { id, row, colDef } = params
      if (!colDef.editable && onRowDoubleClickProp) onRowDoubleClickProp({ id, row })
    },
    [onRowDoubleClickProp]
  )

  return (
    <MuiDataGrid
      className={classNames('data-grid', className)}
      columns={columns.map((col) => {
        const {
          editable,
          field,
          headerName: headerNameProp,
          renderCell,
          renderEditCell,
          sortable = false,
          ...otherColProps
        } = col
        const headerName = headerNameProp ? L.l(headerNameProp) : null

        return {
          ...otherColProps,
          disableColumnMenu: true,
          editable,
          field,
          headerName,
          renderCell,
          renderEditCell,
          sortable,
        }
      })}
      rows={rows}
      checkboxSelection
      onSelectionModelChange={onSelectedIdsChange}
      onCellClick={({ api, colDef, id, isEditable }) => {
        if (isEditable) {
          // open cell editor on single click
          api.setCellMode(id, colDef.field, 'edit')
        }
      }}
      onCellDoubleClick={onCellDoubleClick}
    />
  )
}
