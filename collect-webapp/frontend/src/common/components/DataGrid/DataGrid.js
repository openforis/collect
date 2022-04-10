import './DataGrid.scss'

import React, { useCallback, useRef, useState } from 'react'
import classNames from 'classnames'
import { DataGrid as MuiDataGrid, GridToolbar } from '@material-ui/data-grid'

import L from 'utils/Labels'
import { QuickSearchHeader } from './QuickSearchHeader'
import Arrays from 'utils/Arrays'

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
    hideFooter,
    hideFooterPagination,
    isCellEditable,
    loading,
    onFilterModelChange: onFilterModelChangeProp,
    onPageChange,
    onPageSizeChange,
    onRowDoubleClick: onRowDoubleClickProp,
    onSelectedIdsChange,
    onSortModelChange,
    pageSize: pageSizeProp,
    rowCount,
    rows,
    selectionModel,
    showToolbar,
    sortModel,
  } = props

  const filterModelRef = useRef(null)

  const [filterModel, setFilterModel] = useState(undefined)

  const onCellDoubleClick = useCallback(
    (params) => {
      const { id, row, colDef } = params
      if (!colDef.editable && onRowDoubleClickProp) onRowDoubleClickProp({ id, row })
    },
    [onRowDoubleClickProp]
  )

  const onQuickSearchChange = useCallback(
    ({ field }) =>
      (value) => {
        const itemsOld = filterModel?.items || []
        const item = { id: field, columnField: field, operatorValue: 'contains', value }
        const itemIndex = Arrays.indexOf(itemsOld, item, 'columnField')
        const itemsUpdated = itemIndex >= 0 ? Arrays.replaceItemAt(itemsOld, itemIndex, item) : [...itemsOld, item]
        const filterModelUpdated = { items: itemsUpdated }
        filterModelRef.current = filterModelUpdated
        setFilterModel(filterModelUpdated)

        onFilterModelChangeProp?.(filterModelUpdated)
      },
    [filterModel, setFilterModel]
  )

  // when footer or pagination is hidden, show all rows
  const pageSize = hideFooter || hideFooterPagination ? rows?.length : pageSizeProp

  // TODO handle DataGrid onFilterModelChange
  // const onFilterModelChange = useCallback(
  //   (filterModelUpdated) => {
  //     if (JSON.stringify(filterModelRef.current) !== JSON.stringify(filterModelUpdated)) {
  //       filterModelRef.current = filterModelUpdated
  //       onFilterModelChangeProp?.(filterModelUpdated)
  //     }
  //   },
  //   [filterModelRef, onFilterModelChangeProp]
  // )

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
          ? () => <QuickSearchHeader headerName={headerName} onChange={onQuickSearchChange({ field })} />
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
      filterMode={dataMode}
      filterModel={filterModel}
      headerHeight={headerHeight}
      hideFooter={hideFooter}
      hideFooterPagination={hideFooterPagination}
      isCellEditable={isCellEditable}
      loading={loading}
      onCellClick={({ api, colDef, id, isEditable }) => {
        if (isEditable) {
          // open cell editor on single click
          api.setCellMode(id, colDef.field, 'edit')
        }
      }}
      onCellDoubleClick={onCellDoubleClick}
      onPageChange={onPageChange}
      onPageSizeChange={onPageSizeChange}
      onSelectionModelChange={onSelectedIdsChange}
      onSortModelChange={onSortModelChange}
      pageSize={pageSize}
      paginationMode={dataMode}
      rowCount={rowCount}
      rows={rows}
      rowsPerPageOptions={[25, 50, 100]}
      selectionModel={selectionModel}
      sortingMode={dataMode}
      sortModel={sortModel}
    />
  )
}

DataGrid.defaultProps = {
  checkboxSelection: false,
  disableMultipleSelection: false,
  exportFileName: null,
  hideFooterPagination: false,
  onFilterModelChange: null,
  pageSize: 25,
  selectionModel: undefined,
  showToolbar: false,
}
