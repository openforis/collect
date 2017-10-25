import React, { Component } from 'react';
import RecordDataTable from 'components/datamanagement/RecordDataTable';
import { Button, ButtonGroup, ButtonToolbar, Container, ButtonDropdown, 
	DropdownToggle, DropdownMenu, DropdownItem, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';

import * as Actions from 'actions';
import ServiceFactory from 'services/ServiceFactory'
import Modals from 'components/Modals'
import Arrays from 'utils/Arrays'

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
		if (window.confirm('Delete?'))  {
			const $this = this
			//Modals.confirm('test', 'test', function() {
				this.recordService.delete(this.state.selectedItem).then(response => {
					$this.props.dispatch(Actions.recordDeleted($this.state.selectedItem));
					$this.setState({selectedItem: null, selectedItemIds: []})
					$this.forceUpdate()
				})
			//})
		}
	}

	navigateToItemEditView(itemId) {
		this.props.history.push('/datamanagement/' + itemId)
	}

	handleRowDoubleClick(record) {
		this.navigateToItemEditView(record.id)
	}

	handleRowSelect(row, isSelected, e) {
		const newSelectedItems = Arrays.addOrRemoveItem(this.state.selectedItems, row, !isSelected)
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
		this.props.dispatch(Actions.closeJobMonitor())
	}
	
	render() {
		if (!this.props.survey) {
			return <div>Select a survey first</div>
		}
		return (
			<Container>
				<Row className="justify-content-between">
					<Col sm={{size: 4}}>
						<Button color="info" onClick={this.handleNewButtonClick}>New</Button>
						<Button disabled={!this.state.selectedItem} color={this.state.selectedItem ? "success" : "disabled"}
							onClick={this.handleEditButtonClick}>Edit</Button>
						<Button disabled={!this.state.selectedItem} color={this.state.selectedItem ? "danger" : "disabled"}
							onClick={this.handleDeleteButtonClick}>Delete</Button>
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
						<ButtonDropdown isOpen={this.state.importDropdownOpen} 
								toggle={() => this.setState({importDropdownOpen: !this.state.importDropdownOpen})}>
							<DropdownToggle color="warning" caret><span className="fa fa-upload"/>Import</DropdownToggle>
							<DropdownMenu>
								<DropdownItem onClick={this.handleCsvImportButtonClick}><i className="fa fa-file-excel-o" aria-hidden="true"></i> from CSV</DropdownItem>
								<DropdownItem onClick={this.handleBackupImportButtonClick}><i className="fa fa-file-code-o" aria-hidden="true"></i> from Collect format</DropdownItem>
							</DropdownMenu>
						</ButtonDropdown>
					</Col>
				</Row>
				<Row>
					<Col>
						<RecordDataTable 
							selectedItemIds={this.state.selectedItemIds}
							handlRowDoubleClick={this.handleRowDoubleClick}
							handleRowSelect={this.handleRowSelect}
							handleRowDoubleClick={this.handleRowDoubleClick} />
					</Col>
				</Row>
			</Container>
		);
	}
}

const mapStateToProps = state => {
	return {
		survey: state.preferredSurvey ? state.preferredSurvey.survey : null
	}
}

export default connect(mapStateToProps)(DataManagementPage)