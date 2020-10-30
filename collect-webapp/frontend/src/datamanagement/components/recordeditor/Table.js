import './Table.css'

import React from 'react'
import { Column, Table as TableVirtualized } from 'react-virtualized'
import { Button } from 'reactstrap'

import { ColumnGroupDefinition } from 'model/ui/TableDefinition'

import EntityCollectionComponent from './EntityCollectionComponent'
import FormItemFieldComponent from './FormItemFieldComponent'
import * as FieldsSizes from './fields/FieldsSizes'

const ROW_NUMBER_COLUMN_WIDTH = 60
const DELETE_COLUMN_WIDTH = 60

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
    const { colSpan, col, label, row, rowSpan } = headingComponent
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
        {label}
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
    }
  }

  componentDidMount() {
    super.componentDidMount()

    const { itemDef } = this.props
    const readOnly = false

    const { headingColumns, showRowNumbers } = itemDef
    const headingColumnWidths = headingColumns.reduce((acc, headingColumn) => {
      acc.push(calculateWidth(headingColumn))
      return acc
    }, [])

    const columnWidths = []
    if (showRowNumbers) {
      columnWidths.push(ROW_NUMBER_COLUMN_WIDTH)
    }
    columnWidths.push(...headingColumnWidths)
    if (!readOnly) {
      columnWidths.push(DELETE_COLUMN_WIDTH)
    }
    const totalWidth = columnWidths.reduce((acc, width) => acc + width, 0)
    const gridTemplateColumns = columnWidths.map((width) => `${width}px`).join(' ')

    this.setState({
      ...this.state,
      totalWidth,
      gridTemplateColumns,
    })
  }

  handleDeleteButtonClick(entity) {
    this.commandService.deleteEntity(entity)
  }

  headerRowRenderer() {
    const { itemDef } = this.props
    const { gridTemplateColumns } = this.state
    const { headingRows, totalHeadingRows, totalHeadingColumns, showRowNumbers } = itemDef
    const readOnly = false

    return (
      <div className="grid header" style={{ gridTemplateColumns }}>
        {headingRows.map((headingRow, index) => (
          <HeadingRow
            key={`heading-row-${index + 1}`}
            headingRow={headingRow}
            totalHeadingRows={totalHeadingRows}
            totalHeadingColumns={totalHeadingColumns}
            firstRow={index === 0}
            includeRowNumberColumn={showRowNumbers}
            includeDeleteColumn={!readOnly}
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
    return (
      <div>
        <Button color="danger" onClick={() => this.handleDeleteButtonClick(entity)}>
          <span className="fa fa-trash" />
        </Button>
      </div>
    )
  }

  render() {
    const { itemDef } = this.props
    const { totalWidth, gridTemplateColumns, entities } = this.state

    const { headingColumns, showRowNumbers } = itemDef
    const readOnly = false

    return (
      <fieldset>
        <legend>{itemDef.entityDefinition.label}</legend>
        <TableVirtualized
          headerRowRenderer={this.headerRowRenderer}
          width={totalWidth}
          rowStyle={{ display: 'grid', gridTemplateColumns }}
          height={300}
          rowHeight={40}
          rowCount={entities.length}
          rowGetter={({ index }) => entities[index]}
          onDelete={(entity) => this.handleDeleteButtonClick(entity)}
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
            ...(readOnly
              ? []
              : [
                  <Column
                    key="delete-col"
                    width={DELETE_COLUMN_WIDTH}
                    dataKey="delete-col"
                    cellRenderer={this.deleteCellRenderer}
                    className="grid-cell"
                  />,
                ]),
          ]}
        </TableVirtualized>
        <Button variant="outlined" color="primary" onClick={this.handleNewButtonClick}>
          Add
        </Button>
      </fieldset>
    )
  }
}
