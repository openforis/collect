import './DataGrid.scss'

import React, { useCallback, useRef, useState } from 'react'
import classNames from 'classnames'
import { DataGrid as MuiDataGrid, GridToolbar, useGridApiContext } from '@mui/x-data-grid'

import L from 'utils/Labels'
import { QuickSearchHeader } from './QuickSearchHeader'
import Arrays from 'utils/Arrays'

const EditCell = (props) => {
  const { id, field, row, renderEditCell } = props

  const apiRef = useGridApiContext()

  const onValueUpdate = ({ id, field, value }) => {
    apiRef.current.setEditCellValue({ id, field, value })
  }

  return renderEditCell({ id, field, row, api: apiRef.current, onValueUpdate })
}

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
          renderEditCell: renderEditCellProp,
          renderHeader: renderHeaderProp,
          sortable = false,
          valueFormatter: valueFormatterProp,
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
          renderEditCell: renderEditCellProp
            ? ({ id, field, row }) => <EditCell id={id} field={field} row={row} renderEditCell={renderEditCellProp} />
            : undefined,
          renderHeader,
          sortable,
          valueFormatter: valueFormatterProp
            ? ({ id, field, value, api }) => {
                const row = api.getRow(id)
                return valueFormatterProp({ id, field, value, api, row })
              }
            : undefined,
          width,
        }
      })}
      components={{ Toolbar: showToolbar ? GridToolbar : null }}
      componentsProps={{ ...(showToolbar ? { toolbar: { csvOptions: { fileName: exportFileName } } } : {}) }}
      disableMultipleSelection={disableMultipleSelection}
      disableSelectionOnClick={disableSelectionOnClick}
      experimentalFeatures={{ newEditingApi: true }}
      filterMode={dataMode}
      filterModel={filterModel}
      headerHeight={headerHeight}
      hideFooter={hideFooter}
      hideFooterPagination={hideFooterPagination}
      isCellEditable={isCellEditable}
      loading={loading}
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
  disableMultipleSelection: undefined,
  exportFileName: null,
  hideFooterPagination: false,
  onFilterModelChange: null,
  pageSize: 25,
  selectionModel: undefined,
  showToolbar: false,
}
