import React from 'react'
import classNames from 'classnames'
import { DataGrid as MuiDataGrid } from '@material-ui/data-grid'
import { useCallback } from 'react'

export const DataGrid = (props) => {
  const { className, columns, rows, onRowDoubleClick: onRowDoubleClickProp, onSelectedIdsChange } = props

  const onRowDoubleClick = useCallback(
    ({ id, row }) => {
      if (onRowDoubleClickProp) onRowDoubleClickProp({ id, row })
    },
    [onRowDoubleClickProp]
  )

  return (
    <MuiDataGrid
      className={classNames('data-grid', className)}
      columns={columns.map((col) => {
        const { sortable = false, ...otherColProps } = col
        return { ...otherColProps, disableColumnMenu: true, sortable }
      })}
      rows={rows}
      checkboxSelection
      onSelectionModelChange={onSelectedIdsChange}
      onRowDoubleClick={onRowDoubleClick}
    />
  )
}
