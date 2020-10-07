import React from 'react'
import { Column, Table as TableVirtualized } from 'react-virtualized'
import { withStyles } from '@material-ui/core/styles'
import clsx from 'clsx'
import TableCell from '@material-ui/core/TableCell'
import { Button, FormControl, FormLabel, IconButton } from '@material-ui/core'
import DeleteIcon from '@material-ui/icons/Delete'

import EntityCollectionComponent from './EntityCollectionComponent'
import FormItemFieldComponent from './FormItemFieldComponent'

const styles = (theme) => ({
  flexContainer: {
    display: 'flex',
    alignItems: 'center',
    boxSizing: 'border-box',
  },
  table: {
    // temporary right-to-left patch, waiting for
    // https://github.com/bvaughn/react-virtualized/issues/454
    '& .ReactVirtualized__Table__headerRow': {
      flip: false,
      paddingRight: theme.direction === 'rtl' ? '0 !important' : undefined,
    },
  },
  tableRow: {
    cursor: 'pointer',
  },
  tableRowHover: {
    '&:hover': {
      backgroundColor: theme.palette.grey[200],
    },
  },
  tableCell: {
    flex: 1,
  },
  noClick: {
    cursor: 'initial',
  },
})

class MuiVirtualizedTable extends React.PureComponent {
  getRowClassName = ({ index }) => {
    const { classes, onRowClick } = this.props

    return clsx(classes.tableRow, classes.flexContainer, {
      [classes.tableRowHover]: index !== -1 && onRowClick != null,
    })
  }

  cellRenderer = ({ rowData: entity, dataKey: headingColumn }) => {
    const { classes, onRowClick } = this.props

    return (
      <TableCell
        component="div"
        className={clsx(classes.tableCell, classes.flexContainer, {
          [classes.noClick]: onRowClick == null,
        })}
        variant="body"
      >
        <FormItemFieldComponent itemDef={headingColumn} parentEntity={entity} />
      </TableCell>
    )
  }

  deleteCellRenderer = ({ rowData }) => {
    const { onDelete } = this.props
    return (
      <TableCell component="div" variant="body">
        <IconButton color="secondary" onClick={() => onDelete(rowData)}>
          <DeleteIcon />
        </IconButton>
      </TableCell>
    )
  }

  headerRenderer = ({ label, columnIndex }) => {
    const { classes } = this.props

    return (
      <TableCell
        component="div"
        variant="head"
        className={clsx(classes.tableCell, classes.flexContainer, classes.noClick)}
      >
        <span>{label}</span>
      </TableCell>
    )
  }

  render() {
    const { classes, rowCount, rowGetter, headingColumns } = this.props

    return (
      <TableVirtualized
        className={classes.table}
        rowClassName={this.getRowClassName}
        height={300}
        width={400}
        headerHeight={48}
        rowHeight={48}
        rowCount={rowCount}
        rowGetter={rowGetter}
      >
        {[
          ...headingColumns.map((headingColumn, columnIndex) => {
            const { attributeDefinitionId, label } = headingColumn
            return (
              <Column
                key={attributeDefinitionId}
                width={100}
                headerRenderer={() => this.headerRenderer({ label, columnIndex })}
                cellRenderer={this.cellRenderer}
                dataKey={headingColumn}
                headingColumn={headingColumn}
              />
            )
          }),
          <Column
            key="delete-col"
            width={100}
            headerRenderer={() => this.headerRenderer({ label: 'Delete' })}
            dataKey="delete-col"
            cellRenderer={this.deleteCellRenderer}
          />,
        ]}
      </TableVirtualized>
    )
  }
}

const VirtualizedTable = withStyles(styles)(MuiVirtualizedTable)

export default class Table extends EntityCollectionComponent {
  constructor() {
    super()
    this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
  }

  handleDeleteButtonClick(entity) {
    this.commandService.deleteEntity(entity)
  }

  render() {
    const { itemDef } = this.props
    const { entities } = this.state

    const { headingColumns } = itemDef

    return (
      <FormControl component="fieldset">
        <FormLabel component="legend">{itemDef.entityDefinition.label}</FormLabel>
        <VirtualizedTable
          headingColumns={headingColumns}
          rowCount={entities.length}
          rowGetter={({ index }) => entities[index]}
          onDelete={(entity) => this.handleDeleteButtonClick(entity)}
        />
        <Button variant="outlined" color="primary" onClick={this.handleNewButtonClick}>
          Add
        </Button>
      </FormControl>
    )
  }
}
