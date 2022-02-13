import React from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table'
import { Button, Container, Row, Col } from 'reactstrap'

import { withNavigate } from 'common/hooks'
import AbstractItemsListPage from 'common/components/AbstractItemsListPage'
import * as UserGroupActions from 'actions/usergroups'
import Dialogs from 'common/components/Dialogs'
import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'

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
    dispatch: PropTypes.func.isRequired,
  }

  componentDidMount() {}

  handleRowDoubleClick(row) {
    this.navigateToItemEditView(row.id)
  }

  handleNewButtonClick() {
    this.navigateToItemEditView('new')
  }

  handleEditButtonClick() {
    this.navigateToItemEditView(this.state.editedItem.id)
  }

  navigateToItemEditView(itemId) {
    RouterUtils.navigateToUserGroupEditPage(this.props.navigate, itemId)
  }

  handleDeleteButtonClick() {
    const ids = this.state.selectedItemIds
    const confirmMessageKey =
      ids.length === 1 ? 'userGroup.delete.confirmDeleteSingleMessage' : 'userGroup.delete.confirmDeleteMultipleMessage'
    const confirmMessage = L.l(confirmMessageKey, [ids.length])
    Dialogs.confirm(
      L.l('userGroup.delete.confirmTitle'),
      confirmMessage,
      () => {
        const loggedUser = this.props.loggedUser
        this.props.dispatch(UserGroupActions.deleteUserGroups(loggedUser.id, ids))
      },
      null,
      { confirmButtonLabel: L.l('common.delete.label') }
    )
  }

  render() {
    const { userGroups } = this.props

    const editableUserGroups = userGroups.filter((g) => !g.systemDefined)

    return (
      <Container>
        <Row>
          <Col>
            <Button color="success" onClick={this.handleNewButtonClick}>
              New
            </Button>
            {this.state.editedItem && (
              <Button color={this.state.editedItem ? 'warning' : 'disabled'} onClick={this.handleEditButtonClick}>
                Edit
              </Button>
            )}
            {this.state.editedItem && (
              <Button color={this.state.editedItem ? 'danger' : 'disabled'} onClick={this.handleDeleteButtonClick}>
                <i className="fa fa-trash" aria-hidden="true" />
              </Button>
            )}
          </Col>
        </Row>
        <Row>
          <Col>
            <BootstrapTable
              data={editableUserGroups}
              className="user-groups-data-table"
              striped
              hover
              condensed
              selectRow={{
                mode: 'checkbox',
                clickToSelect: true,
                hideSelectionColumn: true,
                bgColor: 'lightBlue',
                onSelect: this.handleRowSelect,
                onSelectAll: this.handleAllRowsSelect,
                selected: this.state.selectedItemIds,
              }}
              options={{ onRowDoubleClick: this.handleRowDoubleClick }}
            >
              <TableHeaderColumn dataField="id" isKey hidden>
                Id
              </TableHeaderColumn>
              <TableHeaderColumn dataField="name">Name</TableHeaderColumn>
              <TableHeaderColumn dataField="label">Label</TableHeaderColumn>
              <TableHeaderColumn dataField="description">Description</TableHeaderColumn>
            </BootstrapTable>
          </Col>
        </Row>
      </Container>
    )
  }
}

const mapStateToProps = (state) => {
  const {
    isFetching: isFetchingUserGroups,
    lastUpdated: lastUpdatedUserGroups,
    items: userGroups,
  } = state.userGroups || {
    isFetchingUserGroups: true,
    userGroups: [],
  }
  return {
    loggedUser: state.session ? state.session.loggedUser : null,
    isFetchingUserGroups,
    lastUpdatedUserGroups,
    userGroups,
  }
}

export default connect(mapStateToProps)(withNavigate(UserGroupsPage))
