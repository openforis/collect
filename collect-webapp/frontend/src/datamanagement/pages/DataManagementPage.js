import React from 'react'
import { connect } from 'react-redux'

import {
	Button, ButtonDropdown,
	DropdownToggle, DropdownMenu, DropdownItem, Row, Col, UncontrolledDropdown
} from 'reactstrap'
import FormControl from '@material-ui/core/FormControl'
import FormHelperText from '@material-ui/core/FormHelperText'

import MaxAvailableSpaceContainer from 'common/components/MaxAvailableSpaceContainer'
import TableResizeOnWindowResizeComponent from 'common/components/TableResizeOnWindowResizeComponent';import Workflow from 'model/Workflow'
import Dialogs from 'common/components/Dialogs'
import SurveyLanguagesSelect from 'common/components/SurveyLanguagesSelect'

import RecordDataTable from 'datamanagement/components/RecordDataTable'
import NewRecordParametersDialog from 'datamanagement/components/NewRecordParametersDialog'

import ServiceFactory from 'services/ServiceFactory'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'

import { selectActiveSurveyLanguage } from 'actions'
import { startJobMonitor } from 'actions/job'
import { deleteRecords } from 'datamanagement/actions'
import { fetchRecordSummaries } from 'datamanagement/recordDataTable/actions'

const INITIAL_STATE = {
	selectedItems: [],
	selectedItemIds: [],
	selectedItem: null,
	newRecordParametersDialogOpen: false
}

class DataManagementPage extends React.Component {

	constructor(props) {
		super(props)
	
		this.state = INITIAL_STATE

		this.wrapperRef = React.createRef()

		this.handleRowSelect = this.handleRowSelect.bind(this)
		this.handleAllRowsSelect = this.handleAllRowsSelect.bind(this)
		this.handleRowDoubleClick = this.handleRowDoubleClick.bind(this)
		this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
		this.handleNewRecordParametersSelected = this.handleNewRecordParametersSelected.bind(this)
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
		this.handleSurveyLanguageChange = this.handleSurveyLanguageChange.bind(this)
	}

	componentWillReceiveProps(nextProps) {
		if (this.props.survey && nextProps.survey && this.props.survey.id !== nextProps.survey.id) {
			this.setState(INITIAL_STATE)
		}
	}

	handleNewButtonClick() {
		const { survey } = this.props
		if (survey.schema.rootEntities.length === 1 && survey.modelVersions.length <= 1) {
			ServiceFactory.recordService.createRecord({surveyId: survey.id}).then(record => {
				this.navigateToItemEditView(record.id)
			})
		} else {
			this.setState({
				newRecordParametersDialogOpen: true
			})
		}
	}

	handleNewRecordParametersSelected(versionName) {
		const { survey } = this.props
		ServiceFactory.recordService.createRecord({survey: survey.id, versionName}).then(record => {
			this.navigateToItemEditView(record.id)
		})
	}

	handleEditButtonClick() {
		this.navigateToItemEditView(this.state.selectedItem.id)
	}

	handleDeleteButtonClick() {
		const { selectedItemIds } = this.state
		const { surveyId, loggedUserId, deleteRecords } = this.props
		const $this = this
		const confirmMessage = selectedItemIds.length === 1 ?
			L.l('dataManagement.deleteRecords.confirmDeleteSingleRecordMessage')
			: L.l('dataManagement.deleteRecords.confirmDeleteMultipleRecordsMessage', [selectedItemIds.length])

		Dialogs.confirm(L.l('dataManagement.deleteRecords.confirmDeleteTitle'), confirmMessage, function () {
			deleteRecords(surveyId, loggedUserId, selectedItemIds)
			$this.deselectAllRecords()
		}, null, { confirmButtonLabel: L.l('common.delete.label') })
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
		const {loggedUser, userRoleInSurveyGroup} = this.props
		if (loggedUser.canEditRecords(userRoleInSurveyGroup)) {
			if (record.lockedBy && !loggedUser.canUnlockRecords()) {
				Dialogs.alert(L.l('dataManagement.recordLockedAlert.title'), L.l('dataManagement.recordLockedAlert.message', record.lockedBy))
			} else if (record.owner && record.owner.id !== loggedUser.id && !loggedUser.canEditNotOwnedRecords()) {
				Dialogs.alert(L.l('dataManagement.recordOwnedByAnotherUserAlert.title'), 
					L.l('dataManagement.recordOwnedByAnotherUserAlert.message', record.owner.username))
			} else {
				this.navigateToItemEditView(record.id)
			}
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
		const {survey, startJobMonitor} = this.props
		
		ServiceFactory.recordService.startValidationReport(survey.id, null).then(job => {
			startJobMonitor({
				jobId: job.id,
				title: 'Generating validation report',
				okButtonLabel: 'Download',
				handleOkButtonClick: this.handleDownloadValidationReportClick
			})
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
		const {survey, startJobMonitor} = this.props
		const surveyId = survey.id
		ServiceFactory.recordService.startRecordMoveJob(surveyId, fromStep.code, promote).then(job => {
			startJobMonitor({
				jobId: job.id,
				title: promote ? 'Promoting records' : 'Demoting records',
				handleJobCompleted: this.handleMoveRecordsJobCompleted
			})
		})
	}

	handleMoveRecordsJobCompleted(job) {
		setTimeout(() => this.props.fetchRecordSummaries(), 100)
	}

	handleSurveyLanguageChange(lang) {
		this.props.selectActiveSurveyLanguage(lang)
	}

	render() {
		const { survey, loggedUser, surveyLanguage, userRoleInSurveyGroup } = this.props

		if (!survey) {
			return <div>{L.l('survey.selectPublishedSurveyFirst')}</div>
		}

		return (
			<MaxAvailableSpaceContainer ref={this.wrapperRef}>
				<TableResizeOnWindowResizeComponent wrapperRef={this.wrapperRef} margin={124} />
				<Row className="justify-content-between">
					<Col md={2}>
						{loggedUser.canCreateRecords(userRoleInSurveyGroup) &&
							<Button color="info" onClick={this.handleNewButtonClick}>New</Button>
						}{' '}
						{loggedUser.canEditRecords(userRoleInSurveyGroup) && this.state.selectedItem &&
							<Button color="success" onClick={this.handleEditButtonClick}>Edit</Button>
						}{' '}
						{loggedUser.canDeleteRecords(userRoleInSurveyGroup, this.state.selectedItems) && this.state.selectedItemIds.length > 0 &&
							<Button color="danger" onClick={this.handleDeleteButtonClick}><i className="fa fa-trash" /></Button>
						}
					</Col>
					<Col md={2}>
						<Button color="success" onClick={this.handleValidationReportButtonClick}><i className="fa fa-exclamation-triangle" aria-hidden="true"></i> Validation Report</Button>
					</Col>
					<Col md={2}>
						<ButtonDropdown isOpen={this.state.exportDropdownOpen}
							toggle={() => this.setState({ exportDropdownOpen: !this.state.exportDropdownOpen })}>
							<DropdownToggle color="primary" caret><span className="fa fa-download" />{L.l('dataManagement.export')}</DropdownToggle>
							<DropdownMenu>
								<DropdownItem onClick={this.handleExportToCsvButtonClick}><i className="fa fa-file-excel-o" aria-hidden="true"></i> {L.l('dataManagement.export.toCsv')}</DropdownItem>
								<DropdownItem onClick={this.handleBackupButtonClick}><i className="fa fa-file-code-o" aria-hidden="true"></i> {L.l('dataManagement.export.toCollectFormat')}</DropdownItem>
							</DropdownMenu>
						</ButtonDropdown>
					</Col>
					<Col md={2}>
						{loggedUser.canImportRecords(userRoleInSurveyGroup) &&
							<ButtonDropdown isOpen={this.state.importDropdownOpen}
								toggle={() => this.setState({ importDropdownOpen: !this.state.importDropdownOpen })}>
								<DropdownToggle color="warning" caret><span className="fa fa-upload" />{L.l('dataManagement.import')}</DropdownToggle>
								<DropdownMenu>
									<DropdownItem onClick={this.handleCsvImportButtonClick}><i className="fa fa-file-excel-o" aria-hidden="true"></i> {L.l('dataManagement.import.fromCsv')}</DropdownItem>
									<DropdownItem onClick={this.handleBackupImportButtonClick}><i className="fa fa-file-code-o" aria-hidden="true"></i> {L.l('dataManagement.import.fromCollectFormat')}</DropdownItem>
								</DropdownMenu>
							</ButtonDropdown>
						}
					</Col>
					<Col md={2}>
						{loggedUser.canPromoteRecordsInBulk(userRoleInSurveyGroup) &&
							<UncontrolledDropdown>
								<DropdownToggle color="warning" caret><span className="fa fa-arrow-right" />Workflow</DropdownToggle>
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
					<Col md={2}>
						<FormControl>
							<SurveyLanguagesSelect survey={survey} value={surveyLanguage} onChange={this.handleSurveyLanguageChange}/>
							<FormHelperText>{L.l('dataManagement.formLanguage')}</FormHelperText>
						</FormControl>
					</Col>
				</Row>
				<Row>
					<Col>
						<RecordDataTable
							selectedItemIds={this.state.selectedItemIds}
							handleRowSelect={this.handleRowSelect}
							handleAllRowsSelect={this.handleAllRowsSelect}
							handleRowDoubleClick={this.handleRowDoubleClick} />
					</Col>
				</Row>
				<NewRecordParametersDialog
					open={this.state.newRecordParametersDialogOpen}
					versions={survey.modelVersions}
					onClose={() => this.setState({ newRecordParametersDialogOpen: false })}
					onOk={this.handleNewRecordParametersSelected} />
			</MaxAvailableSpaceContainer>
		)
	}
}

const mapStateToProps = state => {
	const survey = state.activeSurvey.survey
	const loggedUser = state.session.loggedUser
	const {id: loggedUserId} = loggedUser
	return {
		survey,
		surveyId: survey ? survey.id : null,
		surveyLanguage: state.activeSurvey.language,
		loggedUser,
		loggedUserId,
		userRoleInSurveyGroup: survey ? survey.userInGroupRole : null
	}
}

export default connect(mapStateToProps, { 
	deleteRecords, 
	startJobMonitor, 
	selectActiveSurveyLanguage,
	fetchRecordSummaries,
})(DataManagementPage)