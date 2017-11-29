import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import { Button, ButtonGroup, ButtonToolbar, Container, Row, Col } from 'reactstrap';

import UserDetailsPage from './UserDetailsPage';
import AbstractItemsListPage from 'components/AbstractItemsListPage';
import * as UserActions from 'actions/users'

class UsersPage extends AbstractItemsListPage {

	static propTypes = {
		users: PropTypes.array.isRequired,
		isFetching: PropTypes.bool.isRequired,
		lastUpdated: PropTypes.number,
		dispatch: PropTypes.func.isRequired
	}

	componentDidMount() {
	}

	componentWillReceiveProps(nextProps) {
		if (this.state.selectedItemIds.length > 0 && nextProps.users) {
			const newSelectedItemIds = this.state.selectedItemIds.filter(id => nextProps.users.find(u => u.id === id))
			const newSelectedItems = newSelectedItemIds.map(id => nextProps.users.find(u => u.id === id))
			const newEditedItem = newSelectedItems.length === 1 ? newSelectedItems[0] : null
			this.setState({
				selectedItemIds: newSelectedItemIds,
				selectedItems: newSelectedItems,
				editedItem: newEditedItem
			})
		}
	}

	createNewItem() {
		return {id: null, username: '', enabled: true, role: 'ENTRY'};
	}

	handleDeleteButtonClick() {
		const ids = this.state.selectedItemIds
		if (window.confirm('Delete the selected ' + ids.length + ' user(s)?')) {
			const loggedUser = this.props.loggedUser
			this.props.dispatch(UserActions.deleteUsers(loggedUser.id, ids))
		}
	}

  	render() {
		const { isFetching, lastUpdated, users} = this.props
		
		let editedItemContainer = null;
		if (this.state.editedItem != null) {
			let editedItemForm = <UserDetailsPage user={this.state.editedItem} />;
			let editedItemLegendText = this.state.editedItem.id == null ? 'New user': 'Edit user: ' + this.state.editedItem.username;
			editedItemContainer = 
				<fieldset>
					<legend>{editedItemLegendText}</legend>
					{editedItemForm}
				</fieldset>
		}
		return (
			<Container>
				<Row>
					<Col>
						<Button color="success" onClick={this.handleNewButtonClick}>New</Button>
						{' '}
						{this.state.selectedItemIds.length > 0 &&
							<Button color="danger" onClick={this.handleDeleteButtonClick}><i className="fa fa-trash" aria-hidden="true" /></Button>
						}
					</Col>
				</Row>
				<Row>
					<Col>
						<BootstrapTable
							ref="table"
							data={this.props.users}
							striped hover selectRow={ {
								mode: 'checkbox', 
								clickToSelect: true, 
								hideSelectionColumn: true, 
								bgColor: 'lightBlue', 
								onSelectAll: this.handleAllRowsSelect,
								onSelect: this.handleRowSelect, 
								selected: this.state.selectedItemIds} 
							}
							>
							<TableHeaderColumn dataField="id" isKey hidden dataAlign="center">Id</TableHeaderColumn>
							<TableHeaderColumn dataField="username">Username</TableHeaderColumn>
							<TableHeaderColumn dataField="enabled">Enabled</TableHeaderColumn>
							<TableHeaderColumn dataField="role">Role</TableHeaderColumn>
						</BootstrapTable>
					</Col>
					<Col>
						{editedItemContainer}
					</Col>
				</Row>
	    	</Container>
		);
  	}
}

const mapStateToProps = state => {
  const {
    isFetching,
    lastUpdated,
    users
  } = state.users || {
    isFetching: true,
    users: []
  }
  return {
    isFetching,
    lastUpdated,
	users,
	loggedUser: state.session ? state.session.loggedUser : null,
  }
}

export default connect(mapStateToProps)(UsersPage);