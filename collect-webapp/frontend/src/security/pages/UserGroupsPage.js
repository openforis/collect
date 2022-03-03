import React from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { Button, Container, Row, Col } from 'reactstrap'

import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'

import * as UserGroupActions from 'actions/usergroups'

import { withNavigate } from 'common/hooks'

import AbstractItemsListPage from 'common/components/AbstractItemsListPage'
import { DataGrid } from 'common/components/DataGrid'
import Dialogs from 'common/components/Dialogs'

const findById = (items) => (id) => items.find((u) => u.id === id)

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
            <DataGrid
              className="user-groups-data-grid"
              columns={[
                { field: 'id', hide: true },
                { field: 'name', headerName: 'Name', flex: 1 },
                { field: 'label', headerName: 'Label', flex: 2 },
                { field: 'description', headerName: 'Description', flex: 2 },
              ]}
              rows={editableUserGroups}
              onSelectedIdsChange={(selectedIds) =>
                this.handleItemsSelection(selectedIds.map(findById(editableUserGroups)))
              }
              onRowDoubleClick={({ row }) => this.handleRowDoubleClick(row)}
            />
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
