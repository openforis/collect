import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table'
import { UncontrolledTooltip } from 'reactstrap'

import * as Formatters from 'common/components/datatable/formatters'
import OwnerColumnEditor from './OwnerColumnEditor'
import RecordOwnerFilter from 'datamanagement/components/RecordOwnerFilter'
import Tables from 'common/components/Tables'
import L from 'utils/Labels'
import { getDataManagementState } from 'datamanagement/state'
import {
	reloadRecordSummaries,
	sortRecordSummaries, 
	changeRecordSummariesPage, 
	filterRecordSummaries, 
	filterOnlyOwnedRecords,
	updateRecordOwner
} from 'datamanagement/recordDataTable/actions'
import { 
	getRecordDataTableState
} from 'datamanagement/recordDataTable/state';

class RecordDataTable extends Component {

	constructor(props) {
		super(props)
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
		this.props.reloadRecordSummaries()
	}

	componentDidUpdate (prevProps) {
		const {surveyId} = this.props
		const {surveyId: prevSurveyId} = prevProps
		if (surveyId && (!prevSurveyId || surveyId !== prevSurveyId)) {
			this.props.reloadRecordSummaries()
		}
	}

	handlePageChange(page, recordsPerPage) {
		if (page === 0) {
			page = 1
		}
		this.props.changeRecordSummariesPage(page, recordsPerPage)
	}

	handleSizePerPageChange(recordsPerPage) {
		//fetch data handled by page change handler
	}
	
	handleCellEdit(row, fieldName, value) {
		const {updateRecordOwner} = this.props

		if (fieldName === 'owner') {
			const newOwner = value.owner
			updateRecordOwner(row, newOwner)
		}
	}

	handleSortChange(sortName, sortOrder) {
		const {sortRecordSummaries} = this.props

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

		sortRecordSummaries(sortFields)
	}

	handleFilterChange(filterObj) {
		const {filterRecordSummaries} = this.props

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
		filterRecordSummaries({keyValues, summaryValues, ownerIds})
	}

	handleOnlyMyOwnRecordsChange(event, checked) {
		this.props.filterOnlyOwnedRecords(checked)
	}

	render() {
		const {
			surveyId, 
			loggedUser, 
			availableOwners, 
			page, 
			records, 
			totalSize, 
			recordsPerPage,
			keyAttributes,
			keyValues,
			attributeDefsShownInSummaryList,
			summaryValues,
			userCanChangeRecordOwner,
			roleInSurvey
		} = this.props

		if (surveyId === null) {
			return <div>Please select a survey first</div>
		}
		
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
                if (userCanChangeRecordOwner) {
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
		
		const lockedByFormatter = (cell, row) => {
			if (!cell) {
				return ''
			}
			const iconClass = cell === loggedUser.username || loggedUser.canUnlockRecords()
				? "circle-orange"
				: "circle-red"

			const iconId = `record-table-${row.id}-locked-by-icon`
			return <span>
				<span className={iconClass} id={iconId}></span>
				<UncontrolledTooltip placement="top" target={iconId}>{L.l('dataManagement.recordLockedBy', cell)}</UncontrolledTooltip>
			</span>
		}

		function createAttributeFilter(attrDef, defaultValue) {
			switch(attrDef.attributeType) {
				case 'NUMBER':
					return {type: 'NumberFilter', defaultValue: {number: defaultValue}}
				default:
					return {type: 'TextFilter', defaultValue}
			}
		}

		var columns = [];
		columns.push(<TableHeaderColumn key="id" dataField="id" isKey hidden dataAlign="center">Id</TableHeaderColumn>);

		const keyAttributeColumns = keyAttributes.map((keyAttr, i) => 
			<TableHeaderColumn key={'key'+(i+1)} 
				dataField={'key'+(i+1)} 
				dataFormat={rootEntityKeyFormatter} width="80"
				editable={false}
				dataSort 
				filter={createAttributeFilter(keyAttr, keyValues[i])}
				>{keyAttr.label}</TableHeaderColumn>)
		columns = columns.concat(keyAttributeColumns)

		const summaryAttributeColumns = attributeDefsShownInSummaryList.map((attr, i) => {
			const prefix = 'summary_'
			const canFilterOrSort = loggedUser.canFilterRecordsBySummaryAttribute(attr, roleInSurvey)
			return <TableHeaderColumn key={prefix+i} 
				dataSort={canFilterOrSort} 
				dataField={prefix+i} 
				dataFormat={shownInSummaryListFormatter} 
				width="80"
				filter={canFilterOrSort ? createAttributeFilter(attr, summaryValues[i]): null}
				editable={false}
				>{attr.label}</TableHeaderColumn>
		})
		columns = columns.concat(summaryAttributeColumns)

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
				editable={userCanChangeRecordOwner}
				filter={ { type: 'CustomFilter', getElement: createOwnerFilter } }
				customEditor={{ getElement: createOwnerEditor, customEditorParameters: { users: this.props.users } }}
				dataAlign="left" width="150" dataSort>Owner</TableHeaderColumn>,
			<TableHeaderColumn key="lockedBy" dataField="lockedBy" dataFormat={lockedByFormatter}
				dataAlign="center" width="30">
				<i className="fa fa-lock" aria-hidden="true" title={L.l('dataManagement.recordLocked')}/>
			</TableHeaderColumn>
		);

		return (
			<div>
				<BootstrapTable
					data={records}
					options={{
						onPageChange: this.handlePageChange,
						onSizePerPageList: this.handleSizePerPageChange,
						onRowDoubleClick: this.props.handleRowDoubleClick,
						onCellEdit: this.handleCellEdit,
						onSortChange: this.handleSortChange,
						onFilterChange: this.handleFilterChange,
						page,
						sizePerPage: recordsPerPage,
						sizePerPageList: [10, 25, 50, 100],
						paginationShowsTotal: true,
						sizePerPageDropDown: Tables.renderSizePerPageDropUp
					}}
					fetchInfo={{ dataTotalSize: totalSize }}
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
}

const mapStateToProps = state => {
	const dataManagementState = getDataManagementState(state)
	const recordDataTableState = getRecordDataTableState(dataManagementState)

	const survey = state.activeSurvey ? state.activeSurvey.survey : null
	const users = state.users ? state.users.users : null
	const loggedUser = state.session ? state.session.loggedUser : null

	const roleInSurvey = survey.userInGroupRole
    
	const userCanChangeRecordOwner = loggedUser.canChangeRecordOwner(survey.userInGroupRole)

	const rootEntityDef = survey.schema.firstRootEntityDefinition
	const keyAttributes = rootEntityDef.keyAttributeDefinitions
	const attributeDefsShownInSummaryList = rootEntityDef.attributeDefinitionsShownInRecordSummaryList
	
	const {
		page, 
		records, 
		totalSize, 
		recordsPerPage,
		keyValues,
		summaryValues,
		availableOwners,
	} = recordDataTableState

	return {
		surveyId : survey ? survey.id : null,
		users,
		loggedUser,
		page, 
		records, 
		totalSize, 
		recordsPerPage,
		keyAttributes,
		keyValues,
		attributeDefsShownInSummaryList,
		summaryValues,
		availableOwners,
		userCanChangeRecordOwner,
		roleInSurvey,
	}
}

export default connect(mapStateToProps, {
	reloadRecordSummaries, 
	sortRecordSummaries, 
	changeRecordSummariesPage,
	filterRecordSummaries, 
	filterOnlyOwnedRecords,
	updateRecordOwner
})(RecordDataTable)