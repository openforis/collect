import React, { Component } from 'react';
import RecordDataTable from 'components/datamanagement/RecordDataTable';
import { Button, ButtonGroup, ButtonToolbar, Container, ButtonDropdown, 
	DropdownToggle, DropdownMenu, DropdownItem, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';

import ServiceFactory from 'services/ServiceFactory'
import Modals from 'components/Modals'

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
			//Modals.confirm('test', 'test', function() {
				this.recordService.delete(this.state.selectedItem).then(console.log('deleted'))
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
		if (isSelected) {
			this.handleItemSelected(row);
		} else {
			this.handleItemUnselected(row);
		}
	}

	handleItemSelected(item) {
		let newSelectedItems = this.state.selectedItems.concat([item]);
		this.handleItemsSelection(newSelectedItems)
	}

	handleItemUnselected(item) {
		let idx = this.state.selectedItems.indexOf(item);
		let newSelectedItems = this.state.selectedItems.slice(idx, 0);
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

	render() {
		if (!this.props.survey) {
			return <div>Select a survey first</div>
		}
		return (
			<Container>
				<Row className="justify-content-between">
					<Col sm={{size: 8}}>
						<Button color="info" onClick={this.handleNewButtonClick}>New</Button>
						<Button disabled={!this.state.selectedItem} color={this.state.selectedItem ? "success" : "disabled"}
							onClick={this.handleEditButtonClick}>Edit</Button>
						<Button disabled={!this.state.selectedItem} color={this.state.selectedItem ? "danger" : "disabled"}
							onClick={this.handleDeleteButtonClick}>Delete</Button>
					</Col>
					<Col sm={{size: 2}}>
						<ButtonDropdown isOpen={this.state.exportDropdownOpen} 
								toggle={() => this.setState({exportDropdownOpen: !this.state.exportDropdownOpen})}>
							<DropdownToggle color="primary" caret><span className="fa fa-download"/>Export</DropdownToggle>
							<DropdownMenu>
								<DropdownItem onClick={this.handleExportToCsvButtonClick}>to CSV</DropdownItem>
								<DropdownItem onClick={this.handleBackupButtonClick}>to Collect format</DropdownItem>
							</DropdownMenu>
						</ButtonDropdown>
					</Col>
					<Col sm={{size: 2}}>
						<ButtonDropdown isOpen={this.state.importDropdownOpen} 
								toggle={() => this.setState({importDropdownOpen: !this.state.importDropdownOpen})}>
							<DropdownToggle color="warning" caret><span className="fa fa-upload"/>Import</DropdownToggle>
							<DropdownMenu>
								<DropdownItem onClick={this.handleCsvImportButtonClick}>from CSV</DropdownItem>
								<DropdownItem onClick={this.handleBackupImportButtonClick}>from Collect format</DropdownItem>
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