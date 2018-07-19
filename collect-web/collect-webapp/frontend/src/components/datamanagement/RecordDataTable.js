import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table'

import ServiceFactory from 'services/ServiceFactory'
import * as Formatters from 'components/datatable/formatters'
import OwnerColumnEditor from './OwnerColumnEditor'
import RecordOwnerFilter from 'components/datamanagement/RecordOwnerFilter'
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
			ownerIds: [],
			sortFields: [],
			availableOwners: [],
			onlyMyOwnRecords: false
		}
		this.fetchData = this.fetchData.bind(this)
		this.handlePageChange = this.handlePageChange.bind(this)
		this.handleCellEdit = this.handleCellEdit.bind(this)
		this.handleSizePerPageChange = this.handleSizePerPageChange.bind(this)
		this.handleSortChange = this.handleSortChange.bind(this)
		this.handleFilterChange = this.handleFilterChange.bind(this)
		this.handleOnlyMyOwnRecordsChange = this.handleOnlyMyOwnRecordsChange.bind(this)
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
			filter = {
				keyValues: this.state.keyValues, 
				summaryValues: this.state.summaryValues,
				ownerIds: this.state.ownerIds
			}, sortFields) {
		const userId = this.props.loggedUser.id
		const surveyId = survey.id
		const rootEntityName = survey.schema.firstRootEntityDefinition.name
		if (! filter.keyValues) {
			filter.keyValues = this.state.keyValues
		}
		if (! filter.summaryValues) {
			filter.summaryValues = this.state.summaryValues
		}
		if (this.state.onlyMyOwnRecords) {
			filter.ownerIds = [this.props.loggedUser.id]
		} else if (! filter.ownerIds) {
			filter.ownerIds = this.state.ownerIds
		}
		if (! sortFields) {
			sortFields = [{field: 'DATE_MODIFIED', descending: true}]
		}
		ServiceFactory.recordService.fetchRecordSummaries(surveyId, rootEntityName, userId, {
				recordsPerPage: recordsPerPage, 
				page: page,
				keyValues: filter.keyValues,
				summaryValues: filter.summaryValues,
				ownerIds: filter.ownerIds
			}, sortFields).then((res) => {
			this.setState({
				page: page, 
				recordsPerPage: recordsPerPage, 
				keyValues: filter.keyValues, 
				summaryValues: filter.summaryValues, 
				sortFields: sortFields,
				records: res.records, 
				totalSize: res.count, 
				availableOwners: res.owners
			});
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
			case 'step':
			case 'entryComplete':
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
		this.fetchData(1, this.state.recordsPerPage, this.props.survey, 
			{
				keyValues: this.state.keyValues,
				summaryValues: this.state.summaryValues,
				ownerIds: this.state.ownerIds
			}, 
			sortFields)
	}

	handleFilterChange(filterObj) {
		const keyValues = []
		const summaryValues = []
		let ownerIds = []
		if (Object.keys(filterObj).length > 0) {
			for (const fieldName in filterObj) {
				let val = filterObj[fieldName].value
				if (fieldName.startsWith('key')) {
					const keyValueIdx = parseInt(fieldName.substr(3), 10) - 1
					keyValues[keyValueIdx] = val
				} else if (fieldName.startsWith('summary_')) {
					const summaryValueIdx = parseInt(fieldName.substring(fieldName.indexOf('_') + 1), 10)
					summaryValues[summaryValueIdx] = val
				} else if (fieldName === 'owner') {
					ownerIds = val
				}
			}
		}
		this.fetchData(1, this.state.recordsPerPage, this.props.survey, {
				keyValues: keyValues, 
				summaryValues: summaryValues, 
				ownerIds: ownerIds
			})
	}

	handleOnlyMyOwnRecordsChange(event, checked) {
		const ownerIds = checked ? [] : this.state.ownerIds
		this.setState({
			ownerIds: ownerIds, 
			onlyMyOwnRecords: checked
		}, () => this.fetchData())
	}

	render() {
		const survey = this.props.survey
		if (survey === null) {
			return <div>Please select a survey first</div>
		}
		const { availableOwners } = this.state

		const rootEntityDef = survey.schema.firstRootEntityDefinition
		const keyAttributes = rootEntityDef.keyAttributeDefinitions
		const attributeDefsShownInSummaryList = rootEntityDef.attributeDefinitionsShownInRecordSummaryList
		const loggedUser = this.props.loggedUser
		const surveyUserGroup = this.props.userGroups.find(ug => ug.id === survey.userGroupId)
		const userInGroup = loggedUser.findUserInGroupOrDescendants(surveyUserGroup)
		const mostSpecificGroup = userInGroup === null ? null : this.props.userGroups.find(ug => ug.id === userInGroup.groupId)

		const createOwnerEditor = (onUpdate, props) => (<OwnerColumnEditor onUpdate={onUpdate} {...props} />);

		function rootEntityKeyFormatter(cell, row) {
			var idx = this.name.substring(3) - 1
			return row.rootEntityKeys[idx]
		}

		function shownInSummaryListFormatter(cell, row) {
			var idx = this.name.substring(this.name.indexOf('_') + 1)
			return row.summaryValues[idx]
		}

		function ownerFormatter(cell, row) {
            const owner = cell

            if (owner) {
                if (loggedUser.canChangeRecordOwner(mostSpecificGroup)) {
                    return <span>
                            <i className="fa fa-edit" aria-hidden="true" ></i>
                            &nbsp;
                            {owner.username}
                        </span>
                } else {
                    return owner.username
                }
            } else {
                return ''
            }
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
			const prefix = 'summary_'
			const canFilterOrSort = loggedUser.canFilterRecordsBySummaryAttribute(attr, surveyUserGroup)
			return <TableHeaderColumn key={prefix+i} dataSort={canFilterOrSort} dataField={prefix+i} 
				dataFormat={shownInSummaryListFormatter} width="80"
				filter={canFilterOrSort ? {type: 'TextFilter'} : null}
				editable={false}>{attr.label}</TableHeaderColumn>
		})
		columns = columns.concat(attributeDefsShownInSummaryListColumns)

		/*
		function createStepFilter(filterHandler, customFilterParameters) {
			const filterItems = []
			for(let stepName in Workflow.STEPS) {
				let s = Workflow.STEPS[stepName]
				filterItems.push({
					value: s.code,
					label: s.label
				})
			}
			return <SelectFilter multiple filterHandler={filterHandler} dataSource={filterItems} /> 
		}
		*/

		function createOwnerFilter(filterHandler, customFilterParameters) {
			const filterItems = availableOwners.map(u => {
				return {
					value: u.id,
					label: u.username
				}
			})
			if (availableOwners.length === 0 ) {
				return <div />
			}
			return <RecordOwnerFilter multiple filterHandler={filterHandler} dataSource={filterItems} /> 
		}

		columns.push(
			<TableHeaderColumn key="totalErrors" dataField="totalErrors"
				dataAlign="right" width="80" editable={false} dataSort>{L.l('dataManagement.errors')}</TableHeaderColumn>,
			<TableHeaderColumn key="warnings" dataField="warnings"
				dataAlign="right" width="80" editable={false} dataSort>{L.l('dataManagement.warnings')}</TableHeaderColumn>,
			<TableHeaderColumn key="creationDate" dataField="creationDate" dataFormat={Formatters.dateTimeFormatter}
				dataAlign="center" width="110" editable={false} dataSort>{L.l('dataManagement.created')}</TableHeaderColumn>,
			<TableHeaderColumn key="modifiedDate" dataField="modifiedDate" dataFormat={Formatters.dateTimeFormatter}
				dataAlign="center" width="110" editable={false} dataSort>{L.l('dataManagement.modified')}</TableHeaderColumn>,
			<TableHeaderColumn key="step" dataField="step" 
				dataAlign="center" width="80" editable={false} dataSort>{L.l('dataManagement.step')}</TableHeaderColumn>,
			<TableHeaderColumn key="owner" dataField="owner" dataFormat={ownerFormatter}
				editable={loggedUser.canChangeRecordOwner(mostSpecificGroup)}
				filter={ { type: 'CustomFilter', getElement: createOwnerFilter } }
				customEditor={{ getElement: createOwnerEditor, customEditorParameters: { users: this.props.users } }}
				dataAlign="left" width="150" dataSort>Owner</TableHeaderColumn>
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