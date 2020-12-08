import './Table.css'

import React from 'react'
import { Column, Table as TableVirtualized } from 'react-virtualized'
import { Button } from 'reactstrap'

import { CoordinateAttributeDefinition, NumericAttributeDefinition, TaxonAttributeDefinition } from 'model/Survey'
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

    return (
      <div
        key={`heading-cell-${row}-${col}`}
        className="grid-cell"
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
      totalWidth: 0,
      addingRow: false,
    }
  }

  componentDidMount() {
    super.componentDidMount()

    const { itemDef, parentEntity } = this.props
    const { record } = parentEntity
    const { readOnly } = record

    this.tableRef = React.createRef(null)

    const { headingColumns, showRowNumbers, entityDefinition } = itemDef
    const { enumerate } = entityDefinition
    const headingColumnWidths = headingColumns.reduce((acc, headingColumn) => {
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
      entities: this.determineEntities(),
      totalWidth,
      gridTemplateColumns,
    })
  }

  onEntitiesUpdated() {
    const { entities } = this.state
    // make last row visible
    this.tableRef.current.scrollToRow(entities.length - 1)
  }

  handleDeleteButtonClick(entity) {
    this.commandService.deleteEntity({ entity })
  }

  headerRowRenderer() {
    const { itemDef, parentEntity } = this.props
    const { gridTemplateColumns, totalWidth } = this.state
    const { record } = parentEntity
    const { headingRows, totalHeadingColumns, showRowNumbers, entityDefinition } = itemDef
    const { enumerate } = entityDefinition
    const { readOnly } = record

    return (
      <div className="grid header" style={{ gridTemplateColumns, width: `${totalWidth}px` }}>
        {headingRows.map((headingRow, index) => (
          <HeadingRow
            key={`heading-row-${index + 1}`}
            headingRow={headingRow}
            totalHeadingRows={headingRows.length}
            totalHeadingColumns={totalHeadingColumns}
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
    const { totalWidth, gridTemplateColumns, entities, addingEntity } = this.state
    const { record } = parentEntity

    const { headingColumns, showRowNumbers, entityDefinition } = itemDef
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
              ...headingColumns.map((headingColumn) => (
                <Column
                  key={headingColumn.attributeDefinitionId}
                  width={FieldsSizes.getWidth({ fieldDef: headingColumn, inTable: true })}
                  cellRenderer={this.cellRenderer}
                  dataKey={headingColumn}
                  headingColumn={headingColumn}
                  className="grid-cell"
                />
              )),
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
