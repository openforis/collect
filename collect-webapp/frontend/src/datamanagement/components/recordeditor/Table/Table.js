import './Table.scss'

import React from 'react'
import { Column, Table as TableVirtualized } from 'react-virtualized'
import { Button } from 'reactstrap'
import classNames from 'classnames'

import { NodeRelevanceUpdatedEvent } from 'model/event/RecordEvent'
import { ColumnDefinition, ColumnGroupDefinition } from 'model/ui/TableDefinition'
import L from 'utils/Labels'

import DeleteIconButton from 'common/components/DeleteIconButton'

import EntityCollectionComponent from '../EntityCollectionComponent'
import FormItemFieldComponent from '../FormItemFieldComponent'
import * as FieldsSizes from '../fields/FieldsSizes'
import HeadingRow from './HeadingRow'

const ROW_NUMBER_COLUMN_WIDTH = 60
const DELETE_COLUMN_WIDTH = 60

const calculateWidth = (headingColumn) => FieldsSizes.getWidth({ fieldDef: headingColumn, inTable: true })

const determineColumnInfo = ({ headingColumn, entities, col }) => {
  const { attributeDefinition, col: colOriginal, colSpan } = headingColumn
  const { alwaysRelevant, hideWhenNotRelevant, id: attributeDefinitionId, calculated, hidden } = attributeDefinition

  const relevant =
    alwaysRelevant ||
    entities.some((entity) => entity.hasSomeDescendantRelevant({ nodeDefinition: attributeDefinition }))

  const isNotEmpty = () =>
    entities.length > 0 &&
    entities.some((entity) => entity.hasSomeDescendantNotEmpty({ nodeDefinition: attributeDefinition }))

  const visible = !(calculated && hidden) && (!hideWhenNotRelevant || relevant || isNotEmpty())

  return {
    colOriginal,
    col: visible ? col : -1,
    colSpan,
    attributeDefinitionId,
    relevant,
    visible,
  }
}

const determineColumnInfoByAttributeDefinitionId = ({ entities, itemDef }) => {
  const { headingColumns } = itemDef

  let currentCol = 1
  return headingColumns.reduce((infoByDefId, headingColumn) => {
    const { attributeDefinition } = headingColumn
    const info = determineColumnInfo({ headingColumn, entities, col: currentCol })
    infoByDefId[attributeDefinition.id] = info
    if (info.visible) {
      currentCol = info.col + headingColumn.colSpan
    }
    return infoByDefId
  }, [])
}

const determineColumnsVisible = ({ itemDef, columnInfoByDefId }) => {
  const { headingColumns } = itemDef

  return headingColumns.reduce((columnsVisible, headingColumn) => {
    const { attributeDefinition } = headingColumn

    if (columnInfoByDefId[attributeDefinition.id].visible) {
      columnsVisible.push(headingColumn)
    }
    return columnsVisible
  }, [])
}

const calculateCol = (columnsInfo) => (headingColumn) => {
  const columnsInfoArr = Object.values(columnsInfo).sort((infoA, infoB) => infoA.colOriginal - infoB.colOriginal)

  const firstVisibleColumn =
    headingColumn instanceof ColumnDefinition
      ? headingColumn
      : // if headingColumn is a group, find the first visible descendant (attribute) column
        headingColumn.descendantColumns.find(
          (descendantColumn) => columnsInfo[descendantColumn.attributeDefinitionId].visible
        )

  let col = 1
  columnsInfoArr.some((info) => {
    if (info.attributeDefinitionId === firstVisibleColumn.attributeDefinitionId) {
      return true
    }
    if (info.visible) {
      col += info.colSpan
    }
    return false
  })
  return col
}

export default class Table extends EntityCollectionComponent {
  constructor() {
    super()
    this.onDeleteButtonClick = this.onDeleteButtonClick.bind(this)
    this.headerRowRenderer = this.headerRowRenderer.bind(this)
    this.cellRenderer = this.cellRenderer.bind(this)
    this.rowNumberCellRenderer = this.rowNumberCellRenderer.bind(this)
    this.deleteCellRenderer = this.deleteCellRenderer.bind(this)

    this.state = {
      ...this.state,
      headingRows: [],
      headingColumns: [],
      totalWidth: 0,
    }
  }

  componentDidMount() {
    super.componentDidMount()

    this.tableRef = React.createRef(null)
  }

  determineNewState() {
    const newState = super.determineNewState()
    const { entities } = newState

    const { itemDef, parentEntity } = this.props
    const { columnInfoByDefId: columnInfoByDefIdPrev } = this.state
    const { record } = parentEntity
    const { readOnly } = record

    const { showRowNumbers, entityDefinition, headingRows } = itemDef
    const { enumerate } = entityDefinition

    const columnInfoByDefId = determineColumnInfoByAttributeDefinitionId({ entities, itemDef })
    const columnsVisible = determineColumnsVisible({ itemDef, columnInfoByDefId })
    const attributeDefIdsVisible = columnsVisible.map((headingColumn) => headingColumn.attributeDefinition.id)

    if (JSON.stringify(columnInfoByDefId) === JSON.stringify(columnInfoByDefIdPrev)) {
      // columns visibility/relevance hasn't change, don't update state/layout
      return newState
    }

    const headingRowsFiltered = headingRows.map((headingRow) =>
      headingRow.reduce((headingRowFiltered, headingComponent) => {
        if (
          (headingComponent instanceof ColumnGroupDefinition &&
            headingComponent.descendantColumns.some((descendantCol) =>
              attributeDefIdsVisible.includes(descendantCol.attributeDefinition.id)
            )) ||
          (headingComponent instanceof ColumnDefinition &&
            attributeDefIdsVisible.includes(headingComponent.attributeDefinition.id))
        ) {
          headingRowFiltered.push(headingComponent)
        }
        return headingRowFiltered
      }, [])
    )

    const headingColumnWidths = columnsVisible.reduce((acc, headingColumn) => {
      acc.push(calculateWidth(headingColumn))
      return acc
    }, [])

    const columnWidths = []
    if (showRowNumbers) {
      columnWidths.push(ROW_NUMBER_COLUMN_WIDTH)
    }
    columnWidths.push(...headingColumnWidths)
    if (!readOnly && !enumerate) {
      columnWidths.push(DELETE_COLUMN_WIDTH)
    }
    const totalWidth = columnWidths.reduce((acc, width) => acc + width, 0)
    const gridTemplateColumns = columnWidths.map((width) => `${width}px`).join(' ')

    return {
      ...newState,
      entities,
      columnInfoByDefId,
      headingColumns: columnsVisible,
      headingRows: headingRowsFiltered,
      totalWidth,
      gridTemplateColumns,
    }
  }

  onStateUpdate(entityAdded) {
    super.onStateUpdate(entityAdded)

    const { entities } = this.state

    if (entityAdded) {
      // make last row visible
      this.tableRef.current.scrollToRow(entities.length - 1)
    }
  }

  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { itemDef, parentEntity } = this.props
    const { entityDefinition } = itemDef

    if (
      event instanceof NodeRelevanceUpdatedEvent &&
      event.isRelativeToDescendantsOf({ parentEntity, entityDefinition })
    ) {
      this.updateState()
    }
  }

  onDeleteButtonClick(entity) {
    this.commandService.deleteEntity({ entity })
  }

  headerRowRenderer() {
    const { itemDef, parentEntity } = this.props
    const { columnInfoByDefId, gridTemplateColumns, headingColumns, headingRows, totalWidth } = this.state
    const { record } = parentEntity
    const { showRowNumbers, entityDefinition } = itemDef
    const { enumerate } = entityDefinition
    const { readOnly } = record

    return (
      <div className="grid header" style={{ gridTemplateColumns, width: `${totalWidth}px` }}>
        {headingRows.map((headingRow, index) => (
          <HeadingRow
            key={`heading-row-${index + 1}`}
            columnInfoByDefId={columnInfoByDefId}
            columnCalculator={calculateCol(columnInfoByDefId)}
            headingRow={headingRow}
            totalHeadingRows={headingRows.length}
            totalHeadingColumns={headingColumns.length}
            firstRow={index === 0}
            includeRowNumberColumn={showRowNumbers}
            includeDeleteColumn={!readOnly && !enumerate}
          />
        ))}
      </div>
    )
  }

  cellRenderer({ rowData: rowEntity, dataKey: headingColumn }) {
    const { attributeDefinition } = headingColumn

    const parentEntity = rowEntity.getDescendantEntityClosestToNode(attributeDefinition)

    return <FormItemFieldComponent itemDef={headingColumn} parentEntity={parentEntity} inTable />
  }

  rowNumberCellRenderer({ rowData: entity }) {
    const { entities } = this.state
    return <div>{entities.indexOf(entity) + 1}</div>
  }

  deleteCellRenderer({ rowData: entity }) {
    return <DeleteIconButton onClick={() => this.onDeleteButtonClick(entity)} />
  }

  render() {
    const { itemDef, fullSize, parentEntity } = this.props
    const {
      addingEntity,
      columnInfoByDefId,
      entities,
      gridTemplateColumns,
      headingColumns,
      maxCount,
      totalWidth,
    } = this.state
    const { record } = parentEntity

    const { showRowNumbers, entityDefinition } = itemDef
    const { enumerate, labelOrName } = entityDefinition
    const { readOnly } = record
    const canAddOrDeleteRows = !readOnly && !enumerate
    const maxCountReached = maxCount && entities.length >= maxCount

    const content = (
      <div className="table-external-wrapper">
        <div className="table-wrapper" style={{ width: totalWidth }}>
          <TableVirtualized
            headerRowRenderer={this.headerRowRenderer}
            className="form-item-table"
            rowStyle={{ display: 'grid', gridTemplateColumns }}
            width={totalWidth}
            height={300}
            rowHeight={34}
            rowCount={entities.length}
            rowGetter={({ index }) => entities[index]}
            onDelete={(entity) => this.onDeleteButtonClick(entity)}
            ref={this.tableRef}
          >
            {[
              ...(showRowNumbers
                ? [
                    <Column
                      key="row-num-col"
                      width={ROW_NUMBER_COLUMN_WIDTH}
                      dataKey="row-num-col"
                      cellRenderer={this.rowNumberCellRenderer}
                      className="grid-cell row-number"
                    />,
                  ]
                : []),
              ...headingColumns.map((headingColumn) => {
                const { attributeDefinition } = headingColumn
                const { id: attributeDefId } = attributeDefinition
                const columnInfo = columnInfoByDefId[attributeDefId]
                const { relevant } = columnInfo
                return (
                  <Column
                    key={headingColumn.attributeDefinitionId}
                    width={calculateWidth(headingColumn)}
                    cellRenderer={this.cellRenderer}
                    dataKey={headingColumn}
                    headingColumn={headingColumn}
                    className={classNames('grid-cell', { 'not-relevant': !relevant })}
                  />
                )
              }),
              ...(canAddOrDeleteRows
                ? [
                    <Column
                      key="delete-col"
                      width={DELETE_COLUMN_WIDTH}
                      dataKey="delete-col"
                      cellRenderer={this.deleteCellRenderer}
                      className="grid-cell"
                    />,
                  ]
                : []),
            ]}
          </TableVirtualized>
        </div>

        {canAddOrDeleteRows && (
          <Button
            className="add-btn"
            variant="outlined"
            color="primary"
            onClick={this.onNewButtonClick}
            disabled={addingEntity || maxCountReached}
            title={
              maxCountReached
                ? L.l('dataManagement.dataEntry.multipleNodesComponent.cannotAddNewNodes.maxCountReached', [
                    maxCount,
                    labelOrName,
                  ])
                : ''
            }
          >
            {L.l('common.add')}
          </Button>
        )}
      </div>
    )

    if (fullSize) {
      return content
    }
    return (
      <fieldset className="form-item-fieldset">
        <legend>{itemDef.entityDefinition.labelOrName}</legend>
        {content}
      </fieldset>
    )
  }
}
