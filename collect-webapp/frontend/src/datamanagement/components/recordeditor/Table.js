import React from 'react'
import { Column, Table as TableVirtualized } from 'react-virtualized'
import TableCell from '@material-ui/core/TableCell'
import { Button } from '@material-ui/core'

import EntityCollectionComponent from './EntityCollectionComponent'
import FormItemFieldComponent from './FormItemFieldComponent'

export default class Table extends EntityCollectionComponent {
  constructor() {
    super()
    this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
  }

  cellRenderer = ({ rowData: entity, dataKey: headingColumn }) => {
    const { onRowClick } = this.props

    return (
      <TableCell component="div" variant="body">
        <FormItemFieldComponent itemDef={headingColumn} parentEntity={entity} />
      </TableCell>
    )
  }

  headerRenderer = ({ label, columnIndex }) => {
    const { headerHeight, classes } = this.props

    return (
      <TableCell component="div" variant="head">
        <span>{label}</span>
      </TableCell>
    )
  }

  handleDeleteButtonClick(entity) {
    this.commandService.deleteNode(entity)
  }

  render() {
    const { itemDef } = this.props
    const { entities } = this.state

    const { headingColumns } = itemDef

    return (
      <div>
        <TableVirtualized
          height={200}
          width={400}
          headerHeight={48}
          rowHeight={48}
          rowCount={entities.length}
          rowGetter={({ index }) => entities[index]}
        >
          {headingColumns.map((headingColumn, index) => {
            const { attributeDefinitionId, label } = headingColumn
            return (
              <Column
                key={attributeDefinitionId}
                width={100}
                headerRenderer={(headerProps) =>
                  this.headerRenderer({
                    ...headerProps,
                    columnIndex: index,
                  })
                }
                cellRenderer={this.cellRenderer}
                dataKey={headingColumn}
                label={label}
                headingColumn={headingColumn}
              />
            )
          })}
        </TableVirtualized>
        <Button variant="outlined" color="primary" onClick={this.handleNewButtonClick}>
          Add
        </Button>
      </div>
    )
  }
}
