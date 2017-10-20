import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import ServiceFactory from 'services/ServiceFactory'
import * as Formatters from 'components/datatable/formatters'
import OwnerColumnEditor from './OwnerColumnEditor'

class RecordDataTable extends Component {

	constructor(props) {
		super(props);

		this.state = {
			records: [],
			totalSize: 0,
			page: 1,
			recordsPerPage: 25
		}
		this.fetchData = this.fetchData.bind(this)
		this.handlePageChange = this.handlePageChange.bind(this)
		this.handleCellEdit = this.handleCellEdit.bind(this)
		this.handleSizePerPageChange = this.handleSizePerPageChange.bind(this)
		this.handleSortChange = this.handleSortChange.bind(this)
		this.handleFilterChange = this.handleFilterChange.bind(this)
	}

	static propTypes = {
		survey: PropTypes.object
	}

	componentDidMount() {
		this.fetchData();
	}

	componentWillReceiveProps(nextProps) {
		if (nextProps.survey != null && (this.props.survey == null || nextProps.survey.id != this.props.survey.id)) {
			this.setState({records: null})
			this.fetchData(this.state.page, this.state.recordsPerPage, nextProps.survey);
		}
	}

	handlePageChange(page, recordsPerPage) {
		if (this.state.page != page || this.state.recordsPerPage != recordsPerPage) {
			this.fetchData(page, recordsPerPage)
		}
	}

	handleSizePerPageChange(recordsPerPage) {
		//fetch data handled by page change handler
	}

	fetchData(page = this.state.page, recordsPerPage = this.state.recordsPerPage, survey = this.props.survey, keyValues, sortFields) {
		const userId = this.props.loggedUser.id
		const surveyId = survey.id
		const rootEntityName = survey.schema.firstRootEntityDefinition.name
		if (! keyValues) {
			keyValues = []
		}
		if (! sortFields) {
			sortFields = [{field: 'DATE_MODIFIED', descending: true}]
		}
		ServiceFactory.recordService.fetchRecordSummaries(surveyId, rootEntityName, userId, {
				recordsPerPage: recordsPerPage, 
				page: page,
				keyValues: keyValues}, sortFields).then((res) => {
			this.setState({page: page, recordsPerPage: recordsPerPage, records: res.records, totalSize: res.count});
		});
	}

	handleCellEdit(row, fieldName, value) {
		if (fieldName == 'owner') {
			const recordId = row.id
			const newOwner = value.owner
			ServiceFactory.recordService.updateOwner(row, newOwner).then(res => {
				const newRecords = this.state.records.map(r => r.id == recordId ? { ...r, owner: newOwner } : r)
				this.setState({ records: newRecords })
			})
		}
	}

	handleSortChange(sortName, sortOrder) {
		let sortField
		switch(sortName) {
			case 'key1':
				sortField = 'KEY1'
				break
			case 'key2':
				sortField = 'KEY2'
				break
			case 'key3':
				sortField = 'KEY3'
				break
			case 'owner':
				sortField = 'OWNER_NAME'
				break
			case 'entryComplete':
				sortField = 'STEP'
				break
			case 'cleansingComplete':
				sortField = 'STEP'
				break
			case 'creationDate':
				sortField = 'DATE_CREATED'
				break
			case 'modifiedDate':
				sortField = 'DATE_MODIFIED'
				break
		}
		let sortFields = [{field: sortField, descending: sortOrder === 'desc'}]
		this.fetchData(1, this.state.recordsPerPage, this.props.survey, this.state.keyValues, sortFields)
	}

	handleFilterChange(filterObj) {
		const keyValuesFilter = []
		if (Object.keys(filterObj).length > 0) {
			for (const fieldName in filterObj) {
				if (fieldName.indexOf('key') === 0) {
					const keyValueIdx = parseInt(fieldName.substr(3)) - 1
					keyValuesFilter[keyValueIdx] = filterObj[fieldName].value
				}
			}
		}
		this.fetchData(1, this.state.recordsPerPage, this.props.survey, keyValuesFilter)
	}

	render() {
		const noSurveySelected = this.props.survey == null;
		if (noSurveySelected) {
			return <div>Please select a survey first</div>
		}
		var survey = this.props.survey;

		const createOwnerEditor = (onUpdate, props) => (<OwnerColumnEditor onUpdate={onUpdate} {...props} />);

		function rootEntityKeyFormatter(cell, row) {
			var idx = this.name.substring(3) - 1;
			return row.rootEntityKeys[idx];
		}

		function shownInSummaryListFormatter(cell, row) {
			var idx = this.name.substring(5) - 1;
			return row.summaryValues[idx];
		}

		function usernameFormatter(cell, row) {
			return cell ? cell.username : ''
		}

		var columns = [];
		columns.push(<TableHeaderColumn key="id" dataField="id" isKey hidden dataAlign="center">Id</TableHeaderColumn>);

		const keyAttributes = survey.schema.firstRootEntityDefinition.keyAttributeDefinitions
		const keyAttributeColumns = keyAttributes.map((keyAttr, i) => 
			<TableHeaderColumn key={'key'+(i+1)} dataField={'key'+(i+1)} dataFormat={rootEntityKeyFormatter} width="80"
				editable={false} dataSort filter={ { type: 'TextFilter' } }>{keyAttr.label}</TableHeaderColumn>)
		columns = columns.concat(keyAttributeColumns)

		const attributeDefsShownInSummaryList = survey.schema.firstRootEntityDefinition.attributeDefinitionsShownInRecordSummaryList
		const attributeDefsShownInSummaryListColumns = attributeDefsShownInSummaryList.map((attr, i) => 
			<TableHeaderColumn key={'shown'+(i+1)} dataField={'shown'+(i+1)} dataFormat={shownInSummaryListFormatter} width="80"
				editable={false} dataSort filter={ { type: 'TextFilter' } }>{attr.label}</TableHeaderColumn>)
		columns = columns.concat(attributeDefsShownInSummaryListColumns)

		columns.push(
			<TableHeaderColumn key="errors" dataField="errors"
				dataAlign="right" width="80" editable={false} dataSort>Errors</TableHeaderColumn>,
			<TableHeaderColumn key="warnings" dataField="warnings"
				dataAlign="right" width="80" editable={false} dataSort>Warnings</TableHeaderColumn>,
			<TableHeaderColumn key="creationDate" dataField="creationDate" dataFormat={Formatters.dateTimeFormatter}
				dataAlign="center" width="110" editable={false} dataSort>Created</TableHeaderColumn>,
			<TableHeaderColumn key="modifiedDate" dataField="modifiedDate" dataFormat={Formatters.dateTimeFormatter}
				dataAlign="center" width="110" editable={false} dataSort>Modified</TableHeaderColumn>,
			<TableHeaderColumn key="entryComplete" dataField="entryComplete" dataFormat={Formatters.checkedIconFormatter}
				dataAlign="center" width="80" editable={false} dataSort>Entered</TableHeaderColumn>,
			<TableHeaderColumn key="cleansingComplete" dataField="cleansingComplete" dataFormat={Formatters.checkedIconFormatter}
				dataAlign="center" width="80" editable={false} dataSort>Cleansed</TableHeaderColumn>,
			<TableHeaderColumn key="owner" dataField="owner" dataFormat={usernameFormatter}
				customEditor={{ getElement: createOwnerEditor, customEditorParameters: { users: this.props.users } }}
				dataAlign="center" width="150"  dataSort>Owner</TableHeaderColumn>
		);

		return (
			<BootstrapTable
				data={this.state.records}
				options={{
					onPageChange: this.handlePageChange,
					onSizePerPageList: this.handleSizePerPageChange,
					onRowDoubleClick: this.props.handleRowDoubleClick,
					onCellEdit: this.handleCellEdit,
					onSortChange: this.handleSortChange,
					onFilterChange: this.handleFilterChange,
					page: this.state.page,
					sizePerPage: this.state.recordsPerPage,
					sizePerPageList: [25, 50, 100]
				}}
				fetchInfo={{ dataTotalSize: this.state.totalSize }}
				remote pagination striped hover condensed
				height={this.state.recordsPerPage === 25 ? '300px' : '600px'}
				selectRow={{
					mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue',
					onSelect: this.props.handleRowSelect,
					selected: this.props.selectedItemIds
				}}
				cellEdit={{ mode: 'click', blurToSave: true }}
			>{columns}</BootstrapTable>
		);
	}

	visitNodes(survey, visitFunction) {
		let stack = [];
		stack.push(survey.schema.rootEntities[0]);
		while (stack.length > 0) {
			let node = stack.pop();
			visitFunction(node);
			switch (node.type) {
				case 'ENTITY':
					for (let i = 0; i < node.children.length; i++) {
						let child = node.children[i];
						stack.push(child);
					}
					break;
				default:
			}
		}
	}
}

const mapStateToProps = state => {
	return {
		survey: state.preferredSurvey ? state.preferredSurvey.survey : null,
		users: state.users ? state.users.users : null,
		loggedUser: state.session ? state.session.loggedUser : null,
		records: state.records ? state.records.list : null
	}
}

export default connect(mapStateToProps)(RecordDataTable)