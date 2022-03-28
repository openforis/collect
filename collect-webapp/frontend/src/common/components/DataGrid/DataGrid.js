import './DataGrid.scss'

import React, { useCallback } from 'react'
import classNames from 'classnames'
import { DataGrid as MuiDataGrid, GridToolbar } from '@material-ui/data-grid'

import L from 'utils/Labels'
import { QuickSearchHeader } from './QuickSearchHeader'

export const DataGrid = (props) => {
  const {
    checkboxSelection,
    className,
    columns,
    dataMode,
    disableMultipleSelection,
    disableSelectionOnClick,
    exportFileName,
    getRowId,
    headerHeight,
    hideFooterPagination,
    isCellEditable,
    loading,
    onPageChange,
    onPageSizeChange,
    onRowDoubleClick: onRowDoubleClickProp,
    onSelectedIdsChange,
    onSortModelChange,
    pageSize,
    rowCount,
    rows,
    selectionModel,
    showToolbar,
    sortModel,
  } = props

  const onCellDoubleClick = useCallback(
    (params) => {
      const { id, row, colDef } = params
      if (!colDef.editable && onRowDoubleClickProp) onRowDoubleClickProp({ id, row })
    },
    [onRowDoubleClickProp]
  )

  const filterMode = dataMode
  const sortingMode = dataMode
  const paginationMode = dataMode

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
          quickSearch = null,
          renderCell,
          renderEditCell,
          renderHeader: renderHeaderProp,
          sortable = false,
          valueFormatter,
          width,
          ...otherColProps
        } = col
        const headerName = headerNameProp ? L.l(headerNameProp) : null
        const renderHeader = quickSearch
          ? () => <QuickSearchHeader headerName={headerName} onChange={quickSearch.onChange} />
          : renderHeaderProp

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
      filterMode={filterMode}
      headerHeight={headerHeight}
      hideFooterPagination={hideFooterPagination}
      isCellEditable={isCellEditable}
      loading={loading}
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
      rows={rows}
      rowsPerPageOptions={[25, 50, 100]}
      selectionModel={selectionModel}
      sortingMode={sortingMode}
      sortModel={sortModel}
    />
  )
}

DataGrid.defaultProps = {
  checkboxSelection: false,
  disableMultipleSelection: false,
  exportFileName: null,
  hideFooterPagination: false,
  pageSize: 25,
  selectionModel: undefined,
  showToolbar: false,
}
