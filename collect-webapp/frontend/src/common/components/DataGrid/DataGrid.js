import React, { useCallback } from 'react'
import classNames from 'classnames'
import { DataGrid as MuiDataGrid, GridToolbar } from '@material-ui/data-grid'

import L from 'utils/Labels'

export const DataGrid = (props) => {
  const {
    checkboxSelection,
    className,
    columns,
    disableMultipleSelection,
    disableSelectionOnClick,
    exportFileName,
    getRowId,
    hideFooterPagination,
    isCellEditable,
    onPageChange,
    onPageSizeChange,
    onRowDoubleClick: onRowDoubleClickProp,
    onSelectedIdsChange,
    onSortModelChange,
    pageSize,
    paginationMode,
    rowCount,
    rows,
    selectionModel,
    showToolbar,
    sortingMode,
    sortModel,
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
      getRowId={getRowId}
      checkboxSelection={checkboxSelection}
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
          renderHeader,
          sortable = false,
          valueFormatter,
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
          renderHeader,
          sortable,
          valueFormatter,
          width,
        }
      })}
      components={{ Toolbar: showToolbar ? GridToolbar : null }}
      componentsProps={{ ...(showToolbar ? { toolbar: { csvOptions: { fileName: exportFileName } } } : {}) }}
      disableMultipleSelection={disableMultipleSelection}
      disableSelectionOnClick={disableSelectionOnClick}
      hideFooterPagination={hideFooterPagination}
      rows={rows}
      isCellEditable={isCellEditable}
      onSelectionModelChange={onSelectedIdsChange}
      onCellClick={({ api, colDef, id, isEditable }) => {
        if (isEditable) {
          // open cell editor on single click
          api.setCellMode(id, colDef.field, 'edit')
        }
      }}
      onCellDoubleClick={onCellDoubleClick}
      onPageChange={onPageChange}
      onPageSizeChange={onPageSizeChange}
      onSortModelChange={onSortModelChange}
      pageSize={pageSize}
      paginationMode={paginationMode}
      rowCount={rowCount}
      rowsPerPageOptions={[25, 50, 100]}
      selectionModel={selectionModel}
      sortingMode={sortingMode}
      sortMode={sortModel}
    />
  )
}

DataGrid.defaultProps = {
  checkboxSelection: false,
  disableMultipleSelection: false,
  exportFileName: null,
  hideFooterPagination: false,
  pageSize: 25,
  paginationMode: 'client',
  selectionModel: undefined,
  showToolbar: false,
  sortingMode: 'client',
}
