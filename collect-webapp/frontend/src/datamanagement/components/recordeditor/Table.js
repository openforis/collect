import './Table.css'

import React from 'react'
import { Column, Table as TableVirtualized } from 'react-virtualized'
import { Button } from 'reactstrap'
import classNames from 'classnames'

import { CoordinateAttributeDefinition, NumericAttributeDefinition, TaxonAttributeDefinition } from 'model/Survey'
import { NodeRelevanceUpdatedEvent } from 'model/event/RecordEvent'
import { ColumnGroupDefinition } from 'model/ui/TableDefinition'
import L from 'utils/Labels'

import DeleteIconButton from 'common/components/DeleteIconButton'

import EntityCollectionComponent from './EntityCollectionComponent'
import FormItemFieldComponent from './FormItemFieldComponent'
import * as FieldsSizes from './fields/FieldsSizes'

const ROW_NUMBER_COLUMN_WIDTH = 60
const DELETE_COLUMN_WIDTH = 60

const getHeadingLabel = ({ headingComponent }) => {
  const { nodeDefinition } = headingComponent
  const { labelOrName } = nodeDefinition

  if (nodeDefinition instanceof NumericAttributeDefinition) {
    const { precisions } = nodeDefinition
    if (precisions && precisions.length === 1) {
      const precision = precisions[0]
      const unit = nodeDefinition.survey.units.find((unit) => unit.id === precision.unitId)
      const suffix = ` (${unit.abbreviation})`
      if (!labelOrName.endsWith(suffix)) {
        return labelOrName + suffix
      }
    }
  }
  return labelOrName
}

const isCompositeAttribute = (attrDef) =>
  attrDef instanceof CoordinateAttributeDefinition || attrDef instanceof TaxonAttributeDefinition

const calculateWidth = (headingComponent) => {
  if (headingComponent instanceof ColumnGroupDefinition) {
    return headingComponent.descendantColumns.reduce((acc, headingColumn) => acc + calculateWidth(headingColumn), 0)
  } else {
    return FieldsSizes.getWidth({ fieldDef: headingComponent, inTable: true })
  }
}

const HeadingRow = ({
  columnInfoByDefId,
  headingRow,
  firstRow,
  totalHeadingRows,
  totalHeadingColumns,
  includeRowNumberColumn,
  includeDeleteColumn,
}) => [
  ...(firstRow && includeRowNumberColumn
    ? [
        <div
          key="heading-cell-row-number"
          className="grid-cell"
          style={{
            gridRowStart: 1,
            gridRowEnd: totalHeadingRows + 1,
            gridColumnStart: 1,
            gridColumnEnd: 2,
          }}
        >
          #
        </div>,
      ]
    : []),
  ...headingRow.map((headingComponent) => {
    const { colSpan, col, row, rowSpan } = headingComponent
    const { attributeDefinition } = headingComponent
    const { id: attributeDefId } = attributeDefinition

    const columnInfo = columnInfoByDefId[attributeDefId]
    const { relevant } = columnInfo

    return (
      <div
        key={`heading-cell-${row}-${attributeDefId}`}
        className={classNames('grid-cell', { 'not-relevant': !relevant })}
        style={{
          gridRowStart: row,
          gridRowEnd: row + rowSpan,
          gridColumnStart: col + (includeRowNumberColumn ? 1 : 0),
          gridColumnEnd: col + colSpan + (includeRowNumberColumn ? 1 : 0),
        }}
      >
        <div style={{ width: '100%' }}>
          <div style={{ textAlign: 'center' }}>{getHeadingLabel({ headingComponent })}</div>
          {isCompositeAttribute(attributeDefinition) && (
            <div style={{ display: 'flex' }}>
              {attributeDefinition.availableFieldNames.map((fieldName) => (
                <div
                  key={fieldName}
                  style={{ width: FieldsSizes.getFieldWidthPx({ fieldDef: headingComponent, fieldName }) }}
                >
                  {attributeDefinition.getFieldLabel(fieldName) ||
                    L.l(
                      `dataManagement.dataEntry.attribute.${attributeDefinition.attributeType.toLocaleLowerCase()}.${fieldName.toLocaleLowerCase()}`
                    )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    )
  }),
  ...(firstRow && includeDeleteColumn
    ? [
        <div
          key="heading-cell-delete"
          className="grid-cell"
          style={{
            gridRowStart: 1,
            gridRowEnd: totalHeadingRows + 1,
            gridColumnStart: totalHeadingColumns + 1 + (includeRowNumberColumn ? 1 : 0),
            gridColumnEnd: totalHeadingColumns + 1 + (includeRowNumberColumn ? 1 : 0),
          }}
        />,
      ]
    : []),
]

const determineColumnInfo = ({ headingColumn, entities }) => {
  const { attributeDefinition } = headingColumn
  const { alwaysRelevant, hideWhenNotRelevant } = attributeDefinition

  const relevant =
    alwaysRelevant ||
    // has some descendant in entities that is relevant
    entities.some((entity) => {
      const nodes = entity.getDescendantsByNodeDefinition(attributeDefinition)
      return nodes.some((node) => node.relevant)
    })

  const isEmpty = () =>
    entities.length === 0 ||
    entities.every((entity) => {
      const nodes = entity.getDescendantsByNodeDefinition(attributeDefinition)
      return nodes.some((node) => node.isEmpty())
    })

  return {
    relevant,
    visible: !hideWhenNotRelevant || relevant || !isEmpty(),
  }
}

const determineColumnInfoByAttributeDefinitionId = ({ entities, itemDef }) => {
  const { headingColumns } = itemDef

  return headingColumns.reduce((infoByDefId, headingColumn) => {
    const { attributeDefinition } = headingColumn
    infoByDefId[attributeDefinition.id] = determineColumnInfo({ headingColumn, entities })
    return infoByDefId
  }, [])
}

const determineColumnsVisible = ({ itemDef, columnInfoByDefId }) => {
  const { headingColumns } = itemDef
  let currentCol = 1

  return headingColumns.reduce((columnsVisible, headingColumn) => {
    const { attributeDefinition } = headingColumn

    if (columnInfoByDefId[attributeDefinition.id].visible) {
      headingColumn.col = currentCol
      columnsVisible.push(headingColumn)
      currentCol = currentCol + headingColumn.colSpan
    }
    return columnsVisible
  }, [])
}

export default class Table extends EntityCollectionComponent {
  constructor() {
    super()
    this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
    this.headerRowRenderer = this.headerRowRenderer.bind(this)
    this.cellRenderer = this.cellRenderer.bind(this)
    this.rowNumberCellRenderer = this.rowNumberCellRenderer.bind(this)
    this.deleteCellRenderer = this.deleteCellRenderer.bind(this)

    this.state = {
      ...this.state,
      headingRows: [],
      headingColumns: [],
      totalWidth: 0,
      addingRow: false,
    }
  }

  componentDidMount() {
    super.componentDidMount()

    this.tableRef = React.createRef(null)

    this.updateLayoutState()
  }

  updateLayoutState() {
    const { itemDef, parentEntity } = this.props
    const { columnInfoByDefId: columnInfoByDefIdPrev } = this.state
    const { record } = parentEntity
    const { readOnly } = record

    const { showRowNumbers, entityDefinition, headingRows } = itemDef
    const { enumerate } = entityDefinition

    const entities = this.determineEntities()
    const columnInfoByDefId = determineColumnInfoByAttributeDefinitionId({ entities, itemDef })
    const columnsVisible = determineColumnsVisible({ itemDef, columnInfoByDefId })
    const attributeDefIdsVisible = columnsVisible.map((headingColumn) => headingColumn.attributeDefinition.id)

    if (JSON.stringify(columnInfoByDefId) === JSON.stringify(columnInfoByDefIdPrev)) {
      // columns visibility/relevance hasn't change, don't update state/layout
      return
    }
    const headingRowsFiltered = headingRows.map((headingRow) =>
      headingRow.reduce((headingRowFiltered, headingComponent) => {
        const headingColIndex = attributeDefIdsVisible.indexOf(headingComponent.attributeDefinition.id)
        if (headingColIndex >= 0) {
          headingRowFiltered.push(columnsVisible[headingColIndex])
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

    this.setState({
      ...this.state,
      entities,
      columnInfoByDefId,
      headingColumns: columnsVisible,
      headingRows: headingRowsFiltered,
      totalWidth,
      gridTemplateColumns,
    })
  }

  onEntitiesUpdated() {
    const { entities } = this.state

    this.updateLayoutState()
    // make last row visible
    this.tableRef.current.scrollToRow(entities.length - 1)
  }

  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { itemDef, parentEntity } = this.props
    const { entityDefinition } = itemDef

    if (
      event instanceof NodeRelevanceUpdatedEvent &&
      event.isRelativeToDescendantsOf({ parentEntity, entityDefinition })
    ) {
      this.updateLayoutState()
    }
  }

  handleDeleteButtonClick(entity) {
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
    return <DeleteIconButton onClick={() => this.handleDeleteButtonClick(entity)} />
  }

  render() {
    const { itemDef, fullSize, parentEntity } = this.props
    const { addingEntity, columnInfoByDefId, entities, gridTemplateColumns, headingColumns, totalWidth } = this.state
    const { record } = parentEntity

    const { showRowNumbers, entityDefinition } = itemDef
    const { enumerate } = entityDefinition
    const { readOnly } = record
    const canAddOrDeleteRows = !readOnly && !enumerate

    const content = (
      <>
        <div className="table-wrapper" style={{ width: totalWidth }}>
          <TableVirtualized
            headerRowRenderer={this.headerRowRenderer}
            className="form-item-table"
            rowStyle={{ display: 'grid', gridTemplateColumns }}
            width={totalWidth}
            height={300}
            rowHeight={32}
            rowCount={entities.length}
            rowGetter={({ index }) => entities[index]}
            onDelete={(entity) => this.handleDeleteButtonClick(entity)}
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
                    width={FieldsSizes.getWidth({ fieldDef: headingColumn, inTable: true })}
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
          <Button variant="outlined" color="primary" disabled={addingEntity} onClick={this.handleNewButtonClick}>
            {L.l('common.add')}
          </Button>
        )}
      </>
    )
    return fullSize ? (
      content
    ) : (
      <fieldset>
        <legend>{itemDef.entityDefinition.label}</legend>
        {content}
      </fieldset>
    )
  }
}
