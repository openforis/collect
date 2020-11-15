import React from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { Button, Container, Row, Col } from 'reactstrap';

import User from 'model/User'

import UserDetailsPage from './UserDetailsPage';
import AbstractItemsListPage from 'common/components/AbstractItemsListPage';
import * as UserActions from 'actions/users'
import Dialogs from 'common/components/Dialogs'
import L from 'utils/Labels'

const findById = items => id => items.find(u => u.id === id)

class UsersPage extends AbstractItemsListPage {

	static propTypes = {
		users: PropTypes.array.isRequired,
		isFetching: PropTypes.bool.isRequired,
		lastUpdated: PropTypes.number,
		dispatch: PropTypes.func.isRequired
	}

	componentWillReceiveProps(nextProps) {
		const { selectedItemIds } = this.state
		const { users } = nextProps
		if (selectedItemIds.length > 0 && users) {
			const newSelectedItemIds = selectedItemIds.filter(findById(users))
			const newSelectedItems = newSelectedItemIds.map(findById(users))
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

	checkCanDeleteUsers() {
		const { selectedItemIds } = this.state
		const { users, loggedUser } = this.props

		if (selectedItemIds.length === 0) {
			return false
		} else if (selectedItemIds.indexOf(loggedUser.id) >= 0) {
			Dialogs.alert(L.l('general.warning'), L.l('user.delete.cannotDeleteCurrentUser'))
			return false
		} else {
			const defaultAdminSelected = selectedItemIds.find(id => {
				const user = findById(users)(id)
				return user.username === User.DEFAULT_ADMIN_NAME
			})
			if (defaultAdminSelected) {
				Dialogs.alert(L.l('general.warning'), L.l('user.delete.cannotDeleteDefaultAdminUser'))
				return false
			} else {
				return true
			}
		}
	}

	handleDeleteButtonClick() {
		const { selectedItemIds } = this.state
		const { loggedUser, dispatch } = this.props

		const confirmMessageKey = selectedItemIds.length === 1 ? 'user.delete.confirmDeleteOneUserMessage': 'user.delete.confirmDeleteMultipleUsersMessage'

		if (this.checkCanDeleteUsers()) {
			Dialogs.confirm(L.l('user.delete.confirmTitle'), L.l(confirmMessageKey, [selectedItemIds.length]), () => {
				dispatch(UserActions.deleteUsers(loggedUser.id, selectedItemIds))
			}, null, {confirmButtonLabel: L.l('common.delete.label')})
		}
	}

  	render() {
		const { isFetching, users} = this.props
		const { selectedItemIds, editedItem } = this.state

		if (isFetching) {
			return <div>Loading...</div>
		}
		
		let editedItemContainer = null;
		if (editedItem != null) {
			const editedItemForm = <UserDetailsPage user={editedItem} />;
			const editedItemLegendText = editedItem.id == null ? 'New user': 'Edit user: ' + editedItem.username;
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
						{selectedItemIds.length > 0 &&
							<Button color="danger" onClick={this.handleDeleteButtonClick}><i className="fa fa-trash" aria-hidden="true" /></Button>
						}
					</Col>
				</Row>
				<Row>
					<Col>
						<BootstrapTable
							ref="table"
							data={users}
							className="users-data-table"
							striped hover selectRow={ {
								mode: 'checkbox', 
								clickToSelect: true, 
								hideSelectionColumn: true, 
								bgColor: 'lightBlue', 
								onSelectAll: this.handleAllRowsSelect,
								onSelect: this.handleRowSelect, 
								selected: selectedItemIds} 
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