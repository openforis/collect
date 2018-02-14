import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import ServiceFactory from 'services/ServiceFactory'
import * as Formatters from 'components/datatable/formatters'
import OwnerColumnEditor from './OwnerColumnEditor'
import Tables from 'components/Tables'
import L from 'utils/Labels'

class RecordDataTable extends Component {

	constructor(props) {
		super(props);

		this.state = {
			records: [],
			totalSize: 0,
			page: 1,
			recordsPerPage: 25,
			keyValues: [],
			summaryValues: [],
			sortFields: []
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
		this.fetchData()
		this.props.onRef(this)
	}

	componentWillUnmount() {
		this.props.onRef(undefined)
	}

	componentWillReceiveProps(nextProps) {
		if (nextProps.survey !== null && (this.props.survey === null || 
				nextProps.survey.id !== this.props.survey.id)) {
			this.setState({records: null})
			this.fetchData(this.state.page, this.state.recordsPerPage, nextProps.survey);
		}
	}

	handlePageChange(page, recordsPerPage) {
		if (page === 0) {
			page = 1
		}
		if (this.state.page !== page || this.state.recordsPerPage !== recordsPerPage) {
			this.fetchData(page, recordsPerPage)
		}
	}

	handleSizePerPageChange(recordsPerPage) {
		//fetch data handled by page change handler
	}

	fetchData(page = this.state.page, recordsPerPage = this.state.recordsPerPage, survey = this.props.survey, 
			keyValues = this.state.keyValues, summaryValues = this.state.summaryValues, sortFields) {
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
				keyValues: keyValues,
				summaryValues: summaryValues
			}, sortFields).then((res) => {
			this.setState({page: page, recordsPerPage: recordsPerPage, records: res.records, totalSize: res.count, 
				keyValues: keyValues, summaryValues: summaryValues, sortFields: sortFields});
		});
	}

	handleCellEdit(row, fieldName, value) {
		if (fieldName === 'owner') {
			const recordId = row.id
			const newOwner = value.owner
			ServiceFactory.recordService.updateOwner(row, newOwner).then(res => {
				const newRecords = this.state.records.map(r => r.id === recordId ? { ...r, owner: newOwner } : r)
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
			case 'summary_0':
				sortField = 'SUMMARY1'
				break
			case 'summary_1':
				sortField = 'SUMMARY2'
				break
			case 'summary_2':
				sortField = 'SUMMARY3'
				break
			case 'errors':
				sortField = 'ERRORS'
				break
			case 'warnings':
				sortField = 'WARNINGS'
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
			default:
				console.log('unsupported sort field: ' + sortName)
				return
		}
		let sortFields = [{field: sortField, descending: sortOrder === 'desc'}]
		this.fetchData(1, this.state.recordsPerPage, this.props.survey, this.state.keyValues, this.state.summaryValues, sortFields)
	}

	handleFilterChange(filterObj) {
		const keyValuesFilter = []
		const summaryValues = []
		if (Object.keys(filterObj).length > 0) {
			for (const fieldName in filterObj) {
				let val = filterObj[fieldName].value
				if (fieldName.startsWith('key')) {
					const keyValueIdx = parseInt(fieldName.substr(3)) - 1
					keyValuesFilter[keyValueIdx] = val
				} else if (fieldName.startsWith('summary_')) {
					const summaryValueIdx = parseInt(fieldName.substring(fieldName.indexOf('_') + 1))
					summaryValues[summaryValueIdx] = val
				}
			}
		}
		this.fetchData(1, this.state.recordsPerPage, this.props.survey, keyValuesFilter, summaryValues)
	}

	render() {
		const noSurveySelected = this.props.survey == null;
		if (noSurveySelected) {
			return <div>Please select a survey first</div>
		}
		const survey = this.props.survey
		const rootEntityDef = survey.schema.firstRootEntityDefinition
		const keyAttributes = rootEntityDef.keyAttributeDefinitions
		const attributeDefsShownInSummaryList = rootEntityDef.attributeDefinitionsShownInRecordSummaryList
		const loggedUser = this.props.loggedUser
		const surveyUserGroup = this.props.userGroups.find(ug => ug.id === survey.userGroupId)
		const userInGroup = loggedUser.findUserInGroupOrDescendants(surveyUserGroup)
		const mostSpecificGroup = this.props.userGroups.find(ug => ug.id === userInGroup.groupId)
		
		const createOwnerEditor = (onUpdate, props) => (<OwnerColumnEditor onUpdate={onUpdate} {...props} />);

		function rootEntityKeyFormatter(cell, row) {
			var idx = this.name.substring(3) - 1
			return row.rootEntityKeys[idx]
		}

		function shownInSummaryListFormatter(cell, row) {
			var idx = this.name.substring(this.name.indexOf('_') + 1)
			return row.summaryValues[idx]
		}

		function usernameFormatter(cell, row) {
			return cell ? cell.username : ''
		}

		function createKeyAttributeFilter(attrDef) {
			switch(attrDef.attributeType) {
				case 'NUMBER':
					return {type: 'NumberFilter'}
				default:
					return {type: 'TextFilter'}
			}
		}

		var columns = [];
		columns.push(<TableHeaderColumn key="id" dataField="id" isKey hidden dataAlign="center">Id</TableHeaderColumn>);

		const keyAttributeColumns = keyAttributes.map((keyAttr, i) => 
			<TableHeaderColumn key={'key'+(i+1)} dataField={'key'+(i+1)} dataFormat={rootEntityKeyFormatter} width="80"
				editable={false} dataSort 
				filter={createKeyAttributeFilter(keyAttr)}
				>{keyAttr.label}</TableHeaderColumn>)
		columns = columns.concat(keyAttributeColumns)

		const attributeDefsShownInSummaryListColumns = attributeDefsShownInSummaryList.map((attr, i) => {
			const isQualifier = rootEntityDef.qualifierAttributeDefinitions.find(qDef => qDef.name === attr.name) != null
			const roleInGroup = userInGroup.role
			const prefix = 'summary_'
			const canFilterOrSort = ! isQualifier || roleInGroup === 'ADMINISTRATOR' || roleInGroup === 'OWNER'
			return <TableHeaderColumn key={prefix+i} dataSort={canFilterOrSort} dataField={prefix+i} 
				dataFormat={shownInSummaryListFormatter} width="80"
				filter={canFilterOrSort ? {type: 'TextFilter'} : null}
				editable={false}>{attr.label}</TableHeaderColumn>
		})
		columns = columns.concat(attributeDefsShownInSummaryListColumns)

		columns.push(
			<TableHeaderColumn key="totalErrors" dataField="totalErrors"
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
				editable={loggedUser.canChangeRecordOwner(mostSpecificGroup)}
				customEditor={{ getElement: createOwnerEditor, customEditorParameters: { users: this.props.users } }}
				dataAlign="center" width="150"  dataSort>Owner</TableHeaderColumn>
		);

		return (
			<div>
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
						sizePerPageList: [10, 25, 50, 100],
						paginationShowsTotal: true,
						sizePerPageDropDown: Tables.renderSizePerPageDropUp
					}}
					fetchInfo={{ dataTotalSize: this.state.totalSize }}
					remote pagination striped hover condensed
					height="100%"
					selectRow={{
						mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue',
						onSelect: this.props.handleRowSelect,
						onSelectAll: this.props.handleAllRowsSelect,
						selected: this.props.selectedItemIds
					}}
					cellEdit={{ mode: 'click', blurToSave: true }}
				>{columns}</BootstrapTable>
			</div>
		)
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
		userGroups: state.userGroups ? state.userGroups.items : null,
		loggedUser: state.session ? state.session.loggedUser : null,
		records: state.records ? state.records.list : null
	}
}

export default connect(mapStateToProps)(RecordDataTable)