import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import { Button, ButtonGroup, ButtonToolbar, Container, Row, Col } from 'reactstrap';

import { fetchUsers } from '../../actions';
import UserDetails from './UserDetails';

class Users extends Component {

	state = { 
		selectedUsers: [],
		editedUser: null
	};

	constructor( props ) {
		super( props );

		this.handleRowSelect = this.handleRowSelect.bind(this);
		this.handleNewClick = this.handleNewClick.bind(this);
	}

	static propTypes = {
			users: PropTypes.array.isRequired,
			isFetching: PropTypes.bool.isRequired,
			lastUpdated: PropTypes.number,
			dispatch: PropTypes.func.isRequired
	}

	componentDidMount() {
		this.props.dispatch(fetchUsers());
	}

	handleRowSelect(row, isSelected, e) {
		if (isSelected) {
			this.setState(this.addSelectedUser(row));
		} else {
			this.setState(this.removeSelectedUser(row));
		}
	}

	addSelectedUser(user) {
		let newSelectedUsers = this.state.selectedUsers.concat([user]);
		return { ...this.state, 
			selectedUsers: newSelectedUsers,
			editedUser: this.getUniqueItemOrNull(newSelectedUsers)
		}
	}

	removeSelectedUser(user) {
		let idx = this.state.selectedUsers.indexOf(user);
		let newSelectedUsers = this.state.selectedUsers.slice(idx, 0);
		return { ...this.state, 
			selectedUsers: newSelectedUsers,
			editedUser: this.getUniqueItemOrNull(newSelectedUsers)
		}
	}

	getUniqueItemOrNull(items) {
		return items.length === 1 ? items[0] : null;
	}
	
	handleNewClick() {
		this.setState({...this.state, editedUser: {}})
	}

  	render() {
		const { isFetching, lastUpdated, users} = this.props
		
		let editedUserForm = null;
		if (this.state.editedUser != null) {
			editedUserForm = 
				<fieldset>
					<legend>Edit user: {this.state.editedUser.username}</legend>
					<UserDetails user={this.state.editedUser} />
				</fieldset>
		}
		return (
			<Container>
				<Row>
					<Col>
						<Button color="success" onClick={this.handleNewClick}>New</Button>
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
							selectRow={ {mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue', onSelect: this.handleRowSelect} }
							>
							<TableHeaderColumn dataField="id" isKey hidden dataAlign="center">Id</TableHeaderColumn>
							<TableHeaderColumn dataField="username">Username</TableHeaderColumn>
							<TableHeaderColumn dataField="enabled">Enabled</TableHeaderColumn>
						</BootstrapTable>
					</Col>
					<Col>
						{editedUserForm}
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

export default connect(mapStateToProps)(Users);