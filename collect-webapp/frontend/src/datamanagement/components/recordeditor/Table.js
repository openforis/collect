import './Table.css'

import React from 'react'
import { Column, Table as TableVirtualized } from 'react-virtualized'
import { Button } from 'reactstrap'

import { AttributeDefinition } from 'model/Survey'
import { ColumnGroupDefinition } from 'model/ui/TableDefinition'

import EntityCollectionComponent from './EntityCollectionComponent'
import FormItemFieldComponent from './FormItemFieldComponent'

const widthCalculatorByAttributeType = {
  [AttributeDefinition.Types.BOOLEAN]: () => 100,
  [AttributeDefinition.Types.CODE]: () => 100,
  [AttributeDefinition.Types.COORDINATE]: () => 200,
  [AttributeDefinition.Types.DATE]: () => 150,
  [AttributeDefinition.Types.NUMBER]: () => 140,
  [AttributeDefinition.Types.TEXT]: () => 200,
}

const calculateWidth = (headingComponent) => {
  if (headingComponent instanceof ColumnGroupDefinition) {
    return headingComponent.descendantColumns.reduce((acc, headingColumn) => acc + calculateWidth(headingColumn), 0)
  } else {
    const widthCalculator = widthCalculatorByAttributeType[headingComponent.type]
    return widthCalculator ? widthCalculator(headingComponent) : 100
  }
}

const HeadingRow = ({ headingRow, firstRow, totalHeadingRows, totalHeadingColumns, includeDeleteColumn }) => [
  ...headingRow.map((headingComponent) => {
    const { colSpan, col, label, row, rowSpan } = headingComponent
    return (
      <div
        key={`heading-cell-${row}-${col}`}
        className="grid-cell"
        style={{
          gridRowStart: row,
          gridRowEnd: row + rowSpan,
          gridColumnStart: col,
          gridColumnEnd: col + colSpan,
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
            gridColumnStart: totalHeadingColumns + 1,
            gridColumnEnd: totalHeadingColumns + 1,
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
    this.deleteCellRenderer = this.deleteCellRenderer.bind(this)
  }

  componentDidMount() {
    super.componentDidMount()

    const { itemDef } = this.props

    const { headingColumns } = itemDef
    const headingColumnWidths = headingColumns.reduce((acc, headingColumn) => {
      acc.push(calculateWidth(headingColumn))
      return acc
    }, [])
    const gridTemplateColumns = [...headingColumnWidths.map((width) => width + 'px'), '60px'].join(' ')

    this.setState({
      ...this.state,
      gridTemplateColumns,
    })
  }

  handleDeleteButtonClick(entity) {
    this.commandService.deleteEntity(entity)
  }

  headerRowRenderer() {
    const { itemDef } = this.props
    const { gridTemplateColumns } = this.state
    const { headingRows, totalHeadingRows, totalHeadingColumns } = itemDef

    return (
      <div className="grid header" style={{ gridTemplateColumns }}>
        {headingRows.map((headingRow, index) => (
          <HeadingRow
            key={`heading-row-${index + 1}`}
            headingRow={headingRow}
            totalHeadingRows={totalHeadingRows}
            totalHeadingColumns={totalHeadingColumns}
            firstRow={index === 0}
            includeDeleteColumn={true}
          />
        ))}
      </div>
    )
  }

  cellRenderer({ rowData: rowEntity, dataKey: headingColumn }) {
    const { attributeDefinition } = headingColumn

    const parentEntity = rowEntity.getDescendantEntityClosestToNode(attributeDefinition)

    return (
      <div className="grid-cell">
        <FormItemFieldComponent itemDef={headingColumn} parentEntity={parentEntity} />
      </div>
    )
  }

  deleteCellRenderer({ rowData: entity }) {
    return (
      <div className="grid-cell">
        <Button color="danger" onClick={() => this.handleDeleteButtonClick(entity)}>
          <span className="fa fa-trash" />
        </Button>
      </div>
    )
  }

  render() {
    const { itemDef } = this.props
    const { gridTemplateColumns, entities } = this.state

    const { headingColumns } = itemDef

    return (
      <fieldset>
        <legend>{itemDef.entityDefinition.label}</legend>
        <TableVirtualized
          headerRowRenderer={this.headerRowRenderer}
          rowStyle={{ display: 'grid', gridTemplateColumns }}
          height={300}
          width={600}
          rowHeight={40}
          rowCount={entities.length}
          rowGetter={({ index }) => entities[index]}
          onDelete={(entity) => this.handleDeleteButtonClick(entity)}
        >
          {[
            ...headingColumns.map((headingColumn) => (
              <Column
                key={headingColumn.attributeDefinitionId}
                width={100}
                cellRenderer={this.cellRenderer}
                dataKey={headingColumn}
                headingColumn={headingColumn}
              />
            )),
            <Column key="delete-col" width={56} dataKey="delete-col" cellRenderer={this.deleteCellRenderer} />,
          ]}
        </TableVirtualized>
        <Button variant="outlined" color="primary" onClick={this.handleNewButtonClick}>
          Add
        </Button>
      </fieldset>
    )
  }
}
