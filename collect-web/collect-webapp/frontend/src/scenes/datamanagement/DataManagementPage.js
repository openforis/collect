import React, { Component } from 'react';
import RecordDataTable from 'components/datamanagement/RecordDataTable';
import { Button, ButtonGroup, ButtonToolbar, Container, ButtonDropdown, 
	DropdownToggle, DropdownMenu, DropdownItem, Row, Col, UncontrolledDropdown } from 'reactstrap';
import { connect } from 'react-redux';

import * as Actions from 'actions';
import * as JobActions from 'actions/job';
import MaxAvailableSpaceContainer from 'components/MaxAvailableSpaceContainer';
import SurveySelect from 'components/SurveySelect';
import WithSurveySelectContainer from 'containers/WithSurveySelectContainer';
import Workflow from 'model/Workflow';
import ServiceFactory from 'services/ServiceFactory';
import Arrays from 'utils/Arrays';
import Containers from 'components/Containers';
import Dialogs from 'components/Dialogs';
import L from 'utils/Labels';
import RouterUtils from 'utils/RouterUtils';

const INITIAL_STATE = {
	selectedItems: [],
	selectedItemIds: [],
	selectedItem: null
}

class DataManagementPage extends Component {

	mainContainer = null
	recordDataTable = null
	recordService = ServiceFactory.recordService

	constructor(props) {
		super(props)

		this.state = INITIAL_STATE

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
		this.handleWindowResize = this.handleWindowResize.bind(this)
        this.updateTableHeight = this.updateTableHeight.bind(this)
	}

	componentWillReceiveProps(nextProps) {
		if (this.props.survey && nextProps.survey && this.props.survey.id !== nextProps.survey.id) {
			this.setState(INITIAL_STATE)
		}
	}

	componentDidMount() {
        this.handleWindowResize();
        window.addEventListener("resize", this.handleWindowResize);
    }

    componentWillUnmount() {
        window.removeEventListener("resize", this.handleWindowResize);
	}
	
	componentDidUpdate() {
		this.updateTableHeight()
	}

    handleWindowResize() {
		this.updateTableHeight()
    }

    updateTableHeight() {
		if (this.mainContainer) {
			Containers.extendTableHeightToMaxAvailable(this.mainContainer, 120)
		}
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
		const $this = this
		const confirmMessage = this.state.selectedItemIds.length == 1 ? 
			L.l('dataManagement.deleteRecords.confirmDeleteSingleRecordMessage') 
			: L.l('dataManagement.deleteRecords.confirmDeleteMultipleRecordsMessage', [this.state.selectedItemIds.length])
		
		Dialogs.confirm(L.l('dataManagement.deleteRecords.confirmDeleteTitle'), confirmMessage, function() {
			$this.recordService.delete($this.props.survey.id, $this.props.loggedUser.id, $this.state.selectedItemIds).then(response => {
				$this.recordDataTable.fetchData()
				$this.props.dispatch(Actions.recordsDeleted($this.state.selectedItems));
				$this.deselectAllRecords()
			})
		}, null, {confirmButtonLabel: L.l('global.delete')})
	}

	deselectAllRecords() {
		this.setState({
			selectedItem: null, 
			selectedItems: [],
			selectedItemIds: []
		})
	}

	navigateToItemEditView(itemId) {
		RouterUtils.navigateToRecordEditPage(this.props.history, itemId)
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
			selectedItem: Arrays.uniqueItemOrNull(selectedItems)
		})
	}

	handleExportToCsvButtonClick() {
		RouterUtils.navigateToRecordCsvExportPage(this.props.history)
	}

	handleBackupButtonClick() {
		RouterUtils.navigateToRecordBackupPage(this.props.history)
	}

	handleBackupImportButtonClick() {
		RouterUtils.navigateToRecordBackupImportPage(this.props.history)
	}

	handleCsvImportButtonClick() {
		RouterUtils.navigateToRecordCsvImportPage(this.props.history)
	}

	handleValidationReportButtonClick() {
		const survey = this.props.survey
        
        ServiceFactory.recordService.startValidationReport(survey.id, null).then(job => {
            this.props.dispatch(JobActions.startJobMonitor({
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
		Dialogs.confirm(L.l('dataManagement.workflow.confirmPromoteToCleansing.title'), 
			L.l('dataManagement.workflow.confirmPromoteToCleansing.message'), () => {
				this.startRecordsMoveJob(Workflow.STEPS.entry, true)
			})
	}

	handlePromoteCleansingToAnalysisButtonClick() {
		Dialogs.confirm(L.l('dataManagement.workflow.confirmPromoteToAnalysis.title'), 
			L.l('dataManagement.workflow.confirmPromoteToAnalysis.message'), () => {
				this.startRecordsMoveJob(Workflow.STEPS.cleansing, true)
			})
	}

	handleDemoteAnalysisToCleansingButtonClick() {
		Dialogs.confirm(L.l('dataManagement.workflow.confirmDemoteToCleansing.title'), 
			L.l('dataManagement.workflow.confirmDemoteToCleansing.message'), () => {
				this.startRecordsMoveJob(Workflow.STEPS.analysis, false)
			})
	}

	handleDemoteCleansingToEntryButtonClick() {
		Dialogs.confirm(L.l('dataManagement.workflow.confirmDemoteToEntry.title'), 
			L.l('dataManagement.workflow.confirmDemoteToEntry.message'), () => {
				this.startRecordsMoveJob(Workflow.STEPS.cleansing, false)
			})
	}

	startRecordsMoveJob(fromStep, promote) {
		const surveyId = this.props.survey.id
		ServiceFactory.recordService.startRecordMoveJob(surveyId, fromStep.code, promote).then(job => {
			this.props.dispatch(JobActions.startJobMonitor({
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
		if (!this.props.survey) {
			return <div>{L.l('survey.selectPublishedSurveyFirst')}</div>
		}
		const surveyUserGroup = this.props.userGroups.find(ug => ug.id === this.props.survey.userGroup.id)
		const loggedUser = this.props.loggedUser
		return (
			<MaxAvailableSpaceContainer ref={ r => this.mainContainer = r }>
				<Row className="justify-content-between">
					<Col sm={{size: 4}}>
						{loggedUser.canCreateRecords(surveyUserGroup) && 
							<Button color="info" onClick={this.handleNewButtonClick}>New</Button>
						}{' '}
						{loggedUser.canEditRecords(surveyUserGroup) && this.state.selectedItem &&
							<Button color="success" onClick={this.handleEditButtonClick}>Edit</Button>
						}{' '}
						{loggedUser.canDeleteRecords(surveyUserGroup, this.state.selectedItems) && this.state.selectedItemIds.length > 0 &&
							<Button color="danger" onClick={this.handleDeleteButtonClick}><i className="fa fa-trash"/></Button>
						}
					</Col>
					<Col sm={{size: 2}}>
						<Button color="success" onClick={this.handleValidationReportButtonClick}><i className="fa fa-exclamation-triangle" aria-hidden="true"></i> Validation Report</Button>
					</Col>
					<Col sm={{size: 2}}>
						<ButtonDropdown isOpen={this.state.exportDropdownOpen} 
								toggle={() => this.setState({exportDropdownOpen: !this.state.exportDropdownOpen})}>
							<DropdownToggle color="primary" caret><span className="fa fa-download"/>{L.l('dataManagement.export')}</DropdownToggle>
							<DropdownMenu>
								<DropdownItem onClick={this.handleExportToCsvButtonClick}><i className="fa fa-file-excel-o" aria-hidden="true"></i> {L.l('dataManagement.export.toCsv')}</DropdownItem>
								<DropdownItem onClick={this.handleBackupButtonClick}><i className="fa fa-file-code-o" aria-hidden="true"></i> {L.l('dataManagement.export.toCollectFormat')}</DropdownItem>
							</DropdownMenu>
						</ButtonDropdown>
					</Col>
					<Col sm={{size: 2}}>
						{loggedUser.canImportRecords(surveyUserGroup) &&
							<ButtonDropdown isOpen={this.state.importDropdownOpen} 
									toggle={() => this.setState({importDropdownOpen: !this.state.importDropdownOpen})}>
								<DropdownToggle color="warning" caret><span className="fa fa-upload"/>{L.l('dataManagement.import')}</DropdownToggle>
								<DropdownMenu>
									<DropdownItem onClick={this.handleCsvImportButtonClick}><i className="fa fa-file-excel-o" aria-hidden="true"></i> {L.l('dataManagement.import.fromCsv')}</DropdownItem>
									<DropdownItem onClick={this.handleBackupImportButtonClick}><i className="fa fa-file-code-o" aria-hidden="true"></i> {L.l('dataManagement.import.fromCollectFormat')}</DropdownItem>
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
				<Row>
					<Col>
						<RecordDataTable onRef={ref => this.recordDataTable = ref}
							selectedItemIds={this.state.selectedItemIds}
							handleRowSelect={this.handleRowSelect}
							handleAllRowsSelect={this.handleAllRowsSelect}
							handleRowDoubleClick={this.handleRowDoubleClick} />
					</Col>
				</Row>
			</MaxAvailableSpaceContainer>
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