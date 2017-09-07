import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { withRouter } from 'react-router';
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import { Button, ButtonGroup, ButtonToolbar, Container, Row, Col } from 'reactstrap';

import AbstractItemsListPage from 'components/AbstractItemsListPage';
import { fetchUserGroups } from 'actions';

class UserGroupsPage extends AbstractItemsListPage {

	constructor(props) {
		super(props)
		this.handleRowDoubleClick = this.handleRowDoubleClick.bind(this)
		this.navigateToItemEditView = this.navigateToItemEditView.bind(this)
		this.handleEditButtonClick = this.handleEditButtonClick.bind(this)
	}

	static propTypes = {
		userGroups: PropTypes.array.isRequired,
		isFetchingUserGroups: PropTypes.bool.isRequired,
		lastUpdatedUserGroups: PropTypes.number,
		dispatch: PropTypes.func.isRequired
	}

	componentDidMount() {
	}

	handleRowDoubleClick(row) {
		this.navigateToItemEditView(row.id)
	}

	handleNewButtonClick() {
		this.navigateToItemEditView('new');
	}

	handleEditButtonClick() {
		this.navigateToItemEditView(this.state.editedItem.id)
	}

	navigateToItemEditView(itemId) {
		this.props.history.push('/usergroups/' + itemId)
	}

  	render() {
		const { userGroups } = this.props
	
	  	return (
			<Container>
				<Row>
					<Col>
						<Button color="success" onClick={this.handleNewButtonClick}>New</Button>
						<Button disabled={! this.state.editedItem} color={this.state.editedItem ? "warning": "disabled"} 
							onClick={this.handleEditButtonClick}>Edit</Button>
						<Button disabled={! this.state.editedItem} color={this.state.editedItem ? "danger": "disabled"} 
							onClick={this.handleDeleteButtonClick}>Delete</Button>
					</Col>
				</Row>
				<Row>
					<Col>
						<BootstrapTable
							data={userGroups}
							striped	hover	condensed
							selectRow={ {mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue', onSelect: this.handleRowSelect,
								selected: this.state.selectedItemIds} }
							options={{ onRowDoubleClick: this.handleRowDoubleClick}}
							>
							<TableHeaderColumn dataField="id" isKey hidden>Id</TableHeaderColumn>
							<TableHeaderColumn dataField="name">Name</TableHeaderColumn>
							<TableHeaderColumn dataField="label">Label</TableHeaderColumn>
							<TableHeaderColumn dataField="description">Description</TableHeaderColumn>
						</BootstrapTable>
					</Col>
				</Row>
			</Container>
	  );
  }
}


const mapStateToProps = state => {
  const {
    isFetching: isFetchingUserGroups,
    lastUpdated: lastUpdatedUserGroups,
    userGroups
  } = state.userGroups || {
    isFetchingUserGroups: true,
    userGroups: []
	}
	return {
    isFetchingUserGroups,
    lastUpdatedUserGroups,
		userGroups
	}
}

export default connect(mapStateToProps)(withRouter(UserGroupsPage));