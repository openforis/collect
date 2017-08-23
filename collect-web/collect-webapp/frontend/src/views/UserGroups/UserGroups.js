import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import { Button, ButtonGroup, ButtonToolbar, Container, Row, Col } from 'reactstrap';
import ItemsList from '../../components/MasterDetail/ItemsList';
import UserGroupDetails from './UserGroupDetails';
import { fetchUserGroups } from '../../actions';

class UserGroups extends ItemsList {
	static propTypes = {
		userGroups: PropTypes.array.isRequired,
		isFetching: PropTypes.bool.isRequired,
		lastUpdated: PropTypes.number,
		dispatch: PropTypes.func.isRequired
	}

	componentDidMount() {
		this.props.dispatch(fetchUserGroups());
	}

	createNewItem() {
		return {id: null, name: '', label: '', description: '', visibilityCode: 'P', enabled: true};
	}

  render() {
		const { isFetching, lastUpdated, userGroups} = this.props
		
		let editedItemContainer = null;
		if (this.state.editedItem != null) {
			let editedItemForm = <UserGroupDetails userGroup={this.state.editedItem} />;
			let editedItemLegendText = this.state.editedItem.id == null ? 'New user group': 'Edit user group: ' + this.state.editedItem.label;
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
						<Button color="success" onClick={this.handleNewClick}>New</Button>
					</Col>
				</Row>
				<Row>
					<Col>
						<BootstrapTable
							data={this.props.userGroups}
							striped	hover	condensed
							selectRow={ {mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue', onSelect: this.handleRowSelect,
								selectedRows: this.state.selectedItemIds} }
							>
							<TableHeaderColumn dataField="id" isKey hidden>Id</TableHeaderColumn>
							<TableHeaderColumn dataField="name">Name</TableHeaderColumn>
							<TableHeaderColumn dataField="label">Label</TableHeaderColumn>
							<TableHeaderColumn dataField="description">Description</TableHeaderColumn>
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
    userGroups
  } = state.userGroups || {
    isFetching: true,
    userGroups: []
  }
  return {
    isFetching,
    lastUpdated,
    userGroups
  }
}

export default connect(mapStateToProps)(UserGroups);