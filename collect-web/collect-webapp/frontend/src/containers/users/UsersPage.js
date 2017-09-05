import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import { Button, ButtonGroup, ButtonToolbar, Container, Row, Col } from 'reactstrap';

import { fetchUsers } from 'actions';
import UserDetailsPage from './UserDetailsPage';
import AbstractItemsListPage from 'components/AbstractItemsListPage';

class UsersPage extends AbstractItemsListPage {

	static propTypes = {
		users: PropTypes.array.isRequired,
		isFetching: PropTypes.bool.isRequired,
		lastUpdated: PropTypes.number,
		dispatch: PropTypes.func.isRequired
	}

	componentDidMount() {
		this.props.dispatch(fetchUsers());
	}

	createNewItem() {
		return {id: null, username: '', enabled: true, role: '-1'};
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
					</Col>
				</Row>
				<Row>
					<Col>
						<BootstrapTable
							ref="table"
							data={this.props.users}
							striped
							hover
							condensed
							selectRow={ 
								{mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue', onSelect: this.handleRowSelect,
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
    users
  }
}

export default connect(mapStateToProps)(UsersPage);