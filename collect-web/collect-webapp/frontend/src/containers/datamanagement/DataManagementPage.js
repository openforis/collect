import React, { Component } from 'react';
import RecordDataTable from 'components/datamanagement/RecordDataTable';
import { Button, ButtonGroup, ButtonToolbar, Container, ButtonDropdown, 
	DropdownToggle, DropdownMenu, DropdownItem, Row, Col, UncontrolledDropdown } from 'reactstrap';
import { connect } from 'react-redux';

import * as Actions from 'actions';
import ServiceFactory from 'services/ServiceFactory'
import Modals from 'components/Modals'
import Arrays from 'utils/Arrays'
import Workflow from 'model/Workflow'

class DataManagementPage extends Component {

	recordService = ServiceFactory.recordService

	constructor(props) {
		super(props)

		this.state = {
			selectedItems: [],
			selectedItemIds: [],
			selectedItem: null
		}

		this.handleRowSelect = this.handleRowSelect.bind(this)
		this.handleAllRowsSelect = this.handleAllRowsSelect.bind(this)
		this.handleRowDoubleClick = this.handleRowDoubleClick.bind(this)
		this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
		this.handleEditButtonClick = this.handleEditButtonClick.bind(this)
		this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
		this.navigateToItemEditView = this.navigateToItemEditView.bind(this)
		this.handleExportToCsvButtonClick = this.handleExportToCsvButtonClick.bind(this)
		this.handleBackupButtonClick = this.handleBackupButtonClick.bind(this)
		this.handleBackupImportButtonClick = this.handleBackupImportButtonClick.bind(this)
		this.handleCsvImportButtonClick = this.handleCsvImportButtonClick.bind(this)
		this.handleValidationReportButtonClick = this.handleValidationReportButtonClick.bind(this)
		this.handleDownloadValidationReportClick = this.handleDownloadValidationReportClick.bind(this)
		this.handlePromoteEntryToCleansingButtonClick = this.handlePromoteEntryToCleansingButtonClick.bind(this)
		this.handlePromoteCleansingToAnalysisButtonClick = this.handlePromoteCleansingToAnalysisButtonClick.bind(this)
		this.handleDemoteAnalysisToCleansingButtonClick = this.handleDemoteAnalysisToCleansingButtonClick.bind(this)
		this.handleDemoteCleansingToEntryButtonClick = this.handleDemoteCleansingToEntryButtonClick.bind(this)
		this.handleMoveRecordsJobCompleted = this.handleMoveRecordsJobCompleted.bind(this)
	}

	handleNewButtonClick() {
		let survey = this.props.survey
		this.recordService.createRecord(survey.id).then(res => {
			this.navigateToItemEditView(res.id);
		})
	}

	handleEditButtonClick() {
		this.navigateToItemEditView(this.state.selectedItem.id)
	}

	handleDeleteButtonClick() {
		const confirmMessage = this.state.selectedItemIds.length == 1 ? 
			'Delete the selected record?' 
			: 'Delete the selected ' + this.state.selectedItemIds.length + ' records?'
		
		if (window.confirm(confirmMessage))  {
			const $this = this
			//Modals.confirm('test', 'test', function() {
				this.recordService.delete(this.props.survey.id, this.props.loggedUser.id, this.state.selectedItemIds).then(response => {
					$this.recordDataTable.fetchData()
					$this.props.dispatch(Actions.recordsDeleted($this.state.selectedItems));
					$this.deselectAllRecords()
				})
			//})
		}
	}

	deselectAllRecords() {
		this.setState({
			selectedItem: null, 
			selectedItems: [],
			selectedItemIds: []
		})
	}

	navigateToItemEditView(itemId) {
		this.props.history.push('/datamanagement/' + itemId)
	}

	handleRowDoubleClick(record) {
		const loggedUser = this.props.loggedUser
		const userGroup = this.props.userGroups.find(ug => ug.id === this.props.survey.userGroup.id)
		if (loggedUser.canEditRecords(userGroup)) {
			this.navigateToItemEditView(record.id)
		}
	}

	handleRowSelect(row, isSelected, e) {
		const newSelectedItems = Arrays.addOrRemoveItem(this.state.selectedItems, row, !isSelected)
		this.handleItemsSelection(newSelectedItems)
	}

	handleAllRowsSelect(isSelected, rows) {
		const newSelectedItems = Arrays.addOrRemoveItems(this.state.selectedItems, rows, !isSelected)
		this.handleItemsSelection(newSelectedItems)
	}

	handleItemsSelection(selectedItems) {
		this.setState({
			...this.state,
			selectedItems: selectedItems,
			selectedItemIds: selectedItems.map(item => item.id),
			selectedItem: this.getUniqueItemOrNull(selectedItems)
		})
	}

	getUniqueItemOrNull(items) {
		return items.length === 1 ? items[0] : null;
	}

	handleExportToCsvButtonClick() {
		this.props.history.push('/datamanagement/csvexport')
	}

	handleBackupButtonClick() {
		this.props.history.push('/datamanagement/backup')
	}

	handleBackupImportButtonClick() {
		this.props.history.push('/datamanagement/backupimport')
	}

	handleCsvImportButtonClick() {
		this.props.history.push('/datamanagement/csvimport')
	}

	handleValidationReportButtonClick() {
		const survey = this.props.survey
        
        ServiceFactory.recordService.startValidationReport(survey.id, null).then(job => {
            this.props.dispatch(Actions.startJobMonitor({
                jobId: job.id, 
                title: 'Generating validation report',
                okButtonLabel: 'Download',                        
                handleOkButtonClick: this.handleDownloadValidationReportClick
            }))
        })
	}

	handleDownloadValidationReportClick(job) {
		if (job.completed) {
			const survey = this.props.survey
			const surveyId = survey.id
			ServiceFactory.recordService.downloadValidationReportResult(surveyId)
		}
	}

	handlePromoteEntryToCleansingButtonClick() {
		this.startRecordsMoveJob(Workflow.STEPS.entry, true)
	}

	handlePromoteCleansingToAnalysisButtonClick() {
		this.startRecordsMoveJob(Workflow.STEPS.cleansing, true)
	}

	handleDemoteAnalysisToCleansingButtonClick() {
		this.startRecordsMoveJob(Workflow.STEPS.analysis, false)
	}

	handleDemoteCleansingToEntryButtonClick() {
		this.startRecordsMoveJob(Workflow.STEPS.cleansing, false)
	}

	startRecordsMoveJob(fromStep, promote) {
		const surveyId = this.props.survey.id
		ServiceFactory.recordService.startRecordMoveJob(surveyId, fromStep.code, promote).then(job => {
			this.props.dispatch(Actions.startJobMonitor({
                jobId: job.id, 
				title: promote ? 'Promoting records': 'Demoting records',
				handleJobCompleted: this.handleMoveRecordsJobCompleted
            }))
		})
	}

	handleMoveRecordsJobCompleted(job) {
		this.recordDataTable.fetchData()		
	}
	
	render() {
		if (!this.props.loggedUser || !this.props.userGroups) {
			return <div>Loading...</div>
		}
		if (!this.props.survey) {
			return <div>Select a survey first</div>
		}
		const surveyUserGroup = this.props.userGroups.find(ug => ug.id === this.props.survey.userGroup.id)
		const loggedUser = this.props.loggedUser
		return (
			<Container fluid>
				<Row className="justify-content-between">
					<Col sm={{size: 4}}>
						{loggedUser.canCreateRecords(surveyUserGroup) && 
							<Button color="info" onClick={this.handleNewButtonClick}>New</Button>
						}{' '}
						{loggedUser.canEditRecords(surveyUserGroup) && this.state.selectedItem &&
							<Button color="success" onClick={this.handleEditButtonClick}>Edit</Button>
						}{' '}
						{loggedUser.canDeleteRecords(surveyUserGroup) && this.state.selectedItemIds.length > 0 &&
							<Button color="danger" onClick={this.handleDeleteButtonClick}><i className="fa fa-trash"/></Button>
						}
					</Col>
					<Col sm={{size: 2}}>
						<Button color="success" onClick={this.handleValidationReportButtonClick}><i className="fa fa-exclamation-triangle" aria-hidden="true"></i> Validation Report</Button>
					</Col>
					<Col sm={{size: 2}}>
						<ButtonDropdown isOpen={this.state.exportDropdownOpen} 
								toggle={() => this.setState({exportDropdownOpen: !this.state.exportDropdownOpen})}>
							<DropdownToggle color="primary" caret><span className="fa fa-download"/>Export</DropdownToggle>
							<DropdownMenu>
								<DropdownItem onClick={this.handleExportToCsvButtonClick}><i className="fa fa-file-excel-o" aria-hidden="true"></i> to CSV</DropdownItem>
								<DropdownItem onClick={this.handleBackupButtonClick}><i className="fa fa-file-code-o" aria-hidden="true"></i> to Collect format</DropdownItem>
							</DropdownMenu>
						</ButtonDropdown>
					</Col>
					<Col sm={{size: 2}}>
						{loggedUser.canImportRecords(surveyUserGroup) &&
							<ButtonDropdown isOpen={this.state.importDropdownOpen} 
									toggle={() => this.setState({importDropdownOpen: !this.state.importDropdownOpen})}>
								<DropdownToggle color="warning" caret><span className="fa fa-upload"/>Import</DropdownToggle>
								<DropdownMenu>
									<DropdownItem onClick={this.handleCsvImportButtonClick}><i className="fa fa-file-excel-o" aria-hidden="true"></i> from CSV</DropdownItem>
									<DropdownItem onClick={this.handleBackupImportButtonClick}><i className="fa fa-file-code-o" aria-hidden="true"></i> from Collect format</DropdownItem>
								</DropdownMenu>
							</ButtonDropdown>
						}
					</Col>
					<Col sm={{size: 2}}>
						{loggedUser.canPromoteRecordsInBulk(surveyUserGroup) &&
							<UncontrolledDropdown>
								<DropdownToggle color="warning" caret><span className="fa fa-arrow-right"/>Workflow</DropdownToggle>
								<DropdownMenu>
									<DropdownItem header>Promote records</DropdownItem>
									<DropdownItem onClick={this.handlePromoteEntryToCleansingButtonClick}><i className="fa fa-arrow-right" aria-hidden="true"></i> Entry -> Cleansing</DropdownItem>
									<DropdownItem onClick={this.handlePromoteCleansingToAnalysisButtonClick}><i className="fa fa-arrow-right" aria-hidden="true"></i> Cleansing -> Analysis</DropdownItem>
									<DropdownItem divider />
									<DropdownItem header>Demote records</DropdownItem>
									<DropdownItem onClick={this.handleDemoteCleansingToEntryButtonClick}><i className="fa fa-arrow-left" aria-hidden="true"></i> Cleansing -> Entry</DropdownItem>
									<DropdownItem onClick={this.handleDemoteAnalysisToCleansingButtonClick}><i className="fa fa-arrow-left" aria-hidden="true"></i> Analysis -> Cleansing</DropdownItem>
								</DropdownMenu>
							</UncontrolledDropdown>
						}
					</Col>
				</Row>
				<Row className="full-height">
					<Col>
						<RecordDataTable onRef={ref => this.recordDataTable = ref}
							className="full-height"
							selectedItemIds={this.state.selectedItemIds}
							handleRowSelect={this.handleRowSelect}
							handleAllRowsSelect={this.handleAllRowsSelect}
							handleRowDoubleClick={this.handleRowDoubleClick} />
					</Col>
				</Row>
			</Container>
		);
	}
}

const mapStateToProps = state => {
	return {
		survey: state.preferredSurvey ? state.preferredSurvey.survey : null,
		loggedUser: state.session ? state.session.loggedUser : null,
		userGroups: state.userGroups ? state.userGroups.items : null
	}
}

export default connect(mapStateToProps)(DataManagementPage)