import './DataGrid.scss'

import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import classNames from 'classnames'
import { DataGrid as MuiDataGrid, useGridApiContext } from '@mui/x-data-grid'

import L from 'utils/Labels'
import { QuickSearchHeader } from './QuickSearchHeader'
import Strings from 'utils/Strings'

const EditCell = (props) => {
  const { id, field, row, renderEditCell } = props

  const apiRef = useGridApiContext()

  const onValueUpdate = ({ id, field, value }) => {
    apiRef.current.setEditCellValue({ id, field, value })
  }

  return renderEditCell({ id, field, row, api: apiRef.current, onValueUpdate })
}

// max page size allowed in the MIT version of the MUI DataGrid
const MAX_PAGE_SIZE = 100

export const DataGrid = (props) => {
  const {
    checkboxSelection = false,
    className,
    columns,
    dataMode = 'client',
    density = 'standard',
    disableMultipleSelection,
    disableSelectionOnClick,
    exportFileName = null,
    getRowId,
    headerHeight = 56,
    hideFooter,
    hideFooterPagination = false,
    isCellEditable,
    loading,
    onFilterModelChange: onFilterModelChangeProp = null,
    onPageChange,
    onPageSizeChange: onPageSizeChangeProp,
    onRowDoubleClick: onRowDoubleClickProp,
    onSelectedIdsChange,
    onSortModelChange,
    pageSize: pageSizeProp = 25,
    rowCount,
    rows,
    selectionModel,
    showToolbar = false,
    sortModel,
  } = props

  const filterModelRef = useRef(null)

  const [filterModel, setFilterModel] = useState(undefined)

  // when footer or pagination is hidden, show all rows
  const allRowsShown = hideFooter || hideFooterPagination

  const [paginationModel, setPaginationModel] = useState({
    page: 0,
    pageSize: allRowsShown ? MAX_PAGE_SIZE : pageSizeProp,
  })

  const rowSelectionModel = useMemo(
    () => (selectionModel ? { type: 'include', ids: new Set(selectionModel) } : undefined),
    [selectionModel]
  )

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
        const itemsUpdated = Strings.isBlank(value)
          ? []
          : [{ id: field, field, operator: 'contains', value }]
        const filterModelUpdated = { items: itemsUpdated }
        filterModelRef.current = filterModelUpdated
        setFilterModel(filterModelUpdated)

        onFilterModelChangeProp?.(filterModelUpdated)
      },
    [filterModel, setFilterModel]
  )

  const onPaginationModelChange = useCallback(
    (paginationModelNew) => {
      const { page, pageSize } = paginationModelNew
      if (pageSize !== paginationModel.pageSize) {
        if (onPageSizeChangeProp) {
          onPageSizeChangeProp(pageSize)
        }
        setPaginationModel({ page: 0, pageSize })
      } else {
        if (page !== paginationModel.page) {
          onPageChange?.(page)
        }
        setPaginationModel(paginationModelNew)
      }
    },
    [onPageChange, onPageSizeChangeProp, paginationModel]
  )

  useEffect(() => {
    setPaginationModel((paginationModelPrev) => ({
      ...paginationModelPrev,
      pageSize: allRowsShown ? MAX_PAGE_SIZE : pageSizeProp,
    }))
  }, [allRowsShown, pageSizeProp])

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
          ? () => (
              <QuickSearchHeader
                field={field}
                filterModel={filterModel}
                headerName={headerName}
                onChange={onQuickSearchChange({ field })}
              />
            )
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
            ? (value, row, column, apiRef) => {
                const { current: api } = apiRef
                return valueFormatterProp({ id: api.getRowId(row), field: column.field, value, api, row })
              }
            : undefined,
          width,
        }
      })}
      columnHeaderHeight={headerHeight}
      density={density}
      disableMultipleRowSelection={disableMultipleSelection}
      disableRowSelectionExcludeModel
      disableRowSelectionOnClick={disableSelectionOnClick}
      filterMode={dataMode}
      filterModel={filterModel}
      hideFooter={hideFooter}
      hideFooterPagination={hideFooterPagination}
      isCellEditable={isCellEditable}
      loading={loading}
      onCellDoubleClick={onCellDoubleClick}
      onPaginationModelChange={onPaginationModelChange}
      onRowSelectionModelChange={onSelectedIdsChange ? (model) => onSelectedIdsChange(Array.from(model.ids)) : undefined}
      onSortModelChange={onSortModelChange}
      pageSizeOptions={[10, 15, 25, 50, MAX_PAGE_SIZE]}
      paginationMode={dataMode}
      paginationModel={paginationModel}
      rowCount={rowCount}
      rows={rows}
      rowSelectionModel={rowSelectionModel}
      showToolbar={showToolbar}
      slotProps={{ toolbar: { csvOptions: { fileName: exportFileName }, showQuickFilter: false } }}
      sortingMode={dataMode}
      sortModel={sortModel}
    />
  )
}
