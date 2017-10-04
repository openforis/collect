import React, { Component } from 'react';
import RecordDataTable from 'components/datamanagement/RecordDataTable';
import { Button, ButtonGroup, ButtonToolbar, Container, Row, Col } from 'reactstrap';
import { connect } from 'react-redux'

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
		this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
		this.handleImportButtonClick = this.handleImportButtonClick.bind(this)
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

	handleExportButtonClick() {
		this.props.history.push('/datamanagement/export')
	}

	handleImportButtonClick() {
		this.props.history.push('/datamanagement/import/backup')
	}

	render() {
		if (!this.props.survey) {
			return <div>Select a survey first</div>
		}
		return (
			<Container>
				<Row>
					<Col>
						<Button color="info" onClick={this.handleNewButtonClick}>New</Button>
						<Button disabled={!this.state.selectedItem} color={this.state.selectedItem ? "success" : "disabled"}
							onClick={this.handleEditButtonClick}>Edit</Button>
						<Button disabled={!this.state.selectedItem} color={this.state.selectedItem ? "danger" : "disabled"}
							onClick={this.handleDeleteButtonClick}>Delete</Button>
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
				<Row>
					<Col>
						<Button color="info" onClick={this.handleExportButtonClick}>Export</Button>
					</Col>
					<Col>
						<Button color="info" onClick={this.handleImportButtonClick}>Import</Button>
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