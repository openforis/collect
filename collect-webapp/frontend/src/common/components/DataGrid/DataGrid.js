import React, { useCallback } from 'react'
import classNames from 'classnames'
import { DataGrid as MuiDataGrid } from '@material-ui/data-grid'

import L from 'utils/Labels'

export const DataGrid = (props) => {
  const {
    className,
    columns,
    isCellEditable,
    onRowDoubleClick: onRowDoubleClickProp,
    onSelectedIdsChange,
    rows,
  } = props

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
          align,
          disableColumnMenu = true,
          editable,
          field,
          filterable = false,
          headerName: headerNameProp,
          renderCell,
          renderEditCell,
          sortable = false,
          width,
          ...otherColProps
        } = col
        const headerName = headerNameProp ? L.l(headerNameProp) : null

        return {
          ...otherColProps,
          align,
          disableColumnMenu,
          editable,
          field,
          filterable,
          headerName,
          renderCell,
          renderEditCell,
          sortable,
          width,
        }
      })}
      rows={rows}
      checkboxSelection
      isCellEditable={isCellEditable}
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
