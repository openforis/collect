import React, { Component } from 'react'
import BootstrapTable from 'react-bootstrap-table-next'
import paginationFactory from 'react-bootstrap-table2-paginator'
import 'react-bootstrap-table2-paginator/dist/react-bootstrap-table2-paginator.min.css'

export default class QueryResultTable extends Component {

    constructor(props) {
        super(props)

    }

    render() {
        const { columns, queryResult, handleTableChange, page, sizePerPage, totalSize, maxHeight } = this.props

        const attributeColumnFormatter = (cell, row, rowIndex, formatExtraData) => {
			const columnIndex = formatExtraData.columnIndex
			return row.values[columnIndex]
        }
        
        const tableColumns = columns.map((c, idx) => {return {
			dataField: 'attributeDefinitionId_' + c.attributeDefinition.id,
			text: c.attributeDefinition.label,
			formatter: attributeColumnFormatter,
			formatExtraData: {
				columnIndex: idx
			}
        }})
        
        return <BootstrapTable keyField='id' 
            rowEvents={{
                onClick: this.props.handleRowClick
            }}
            maxHeight={ maxHeight }
            data={ queryResult.rows } 
            columns={ tableColumns }
            onTableChange={ handleTableChange }
            remote
            pagination={ paginationFactory({ page, sizePerPage, totalSize }) } />
    }
}
