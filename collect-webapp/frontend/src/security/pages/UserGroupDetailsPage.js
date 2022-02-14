import React from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import {
  Alert,
  Button,
  Container,
  Row,
  Col,
  Form,
  FormGroup,
  Label,
  Input,
  FormFeedback,
  UncontrolledTooltip,
} from 'reactstrap'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table'

import User from '../../model/User'
import * as UserGroupActions from 'actions/usergroups'
import { withNavigate, withParams } from 'common/hooks'
import AbstractItemDetailsPage from 'common/components/AbstractItemDetailsPage'
import UserGroupService from 'services/UserGroupService'
import UserRoleDropdownEditor from '../components/UserRoleDropdownEditor'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'

class UserGroupDetailsPage extends AbstractItemDetailsPage {
  constructor(props) {
    super(props)

    this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this)
    this.handleAvailableUsersRowSelect = this.handleAvailableUsersRowSelect.bind(this)
    this.handleUsersInGroupRowSelect = this.handleUsersInGroupRowSelect.bind(this)
    this.handleAddSelectedUsersToGroup = this.handleAddSelectedUsersToGroup.bind(this)
    this.handleRemoveSelectedUsersToGroup = this.handleRemoveSelectedUsersToGroup.bind(this)
    this.handleSelectedUsersCellEdit = this.handleSelectedUsersCellEdit.bind(this)

    this.state = {
      ready: false,
    }
  }

  static propTypes = {
    users: PropTypes.array.isRequired,
    isFetchingUsers: PropTypes.bool.isRequired,
    lastUpdatedUsers: PropTypes.number,
    dispatch: PropTypes.func.isRequired,
  }

  userGroupService = new UserGroupService()

  updateStateFromProps(props) {
    if (!props.isUserGroupsInitialized || props.isFetchingUserGroups) {
      this.setState({
        ready: false,
      })
    } else {
      const loggedUser = this.props.loggedUser
      let idParam = props.params.id
      let userGroup
      if (idParam === 'new') {
        userGroup = {
          id: null,
          name: '',
          label: '',
          description: '',
          visibilityCode: 'P',
          enabled: true,
          parentId: null,
          qualifierName: '',
          qualifierValue: '',
          users: [
            {
              userId: loggedUser.id,
              username: loggedUser.username,
              userEnabled: loggedUser.enabled,
              userRole: loggedUser.role,
              role: User.ROLE_IN_GROUP.OWNER,
              joinStatus: User.USER_GROUP_JOIN_STATUS.ACCEPTED,
            },
          ],
        }
      } else {
        let userGroupId = parseInt(idParam, 10)
        userGroup = props.userGroups.find((group) => group.id === userGroupId)
      }
      if (!this.state.ready || this.state.id !== userGroup.id) {
        this.setState({
          ready: true,
          editedUserGroup: userGroup,
          newItem: !userGroup.id,
          id: userGroup.id,
          name: userGroup.name,
          label: userGroup.label,
          description: userGroup.description,
          visibilityCode: userGroup.visibilityCode,
          enabled: userGroup.enabled,
          parentId: userGroup.parentId,
          qualifierName: userGroup.qualifierName,
          qualifierValue: userGroup.qualifierValue,
          errorFeedback: [],
          alertMessageOpen: false,
          availableUsers: this.getAvailableUsers(props.users, userGroup.users),
          usersInGroup: userGroup.users.map((uig) => {
            const user = this.props.users.find((u) => u.id === uig.userId)
            return { ...uig, username: user.username, userRole: user.role }
          }),
          selectedAvailableUsers: [],
          selectedAvailableUsersIds: [],
          selectedUsersInGroup: [],
          selectedUsersInGroupIds: [],
          newUserRoleCode: User.ROLE_IN_GROUP.OPERATOR,
        })
      }
    }
  }

  getAvailableUsers(allUsers, groupUsers) {
    return allUsers.filter((user) => {
      let groupUser = groupUsers.find((u) => {
        return u.userId === user.id
      })
      return groupUser === undefined
    })
  }

  extractFormObject() {
    return {
      id: this.state.id,
      name: this.state.name,
      label: this.state.label,
      description: this.state.description,
      visibilityCode: this.state.visibilityCode,
      parentId: this.state.parentId,
      enabled: this.state.enabled,
      qualifierName: this.state.qualifierName,
      qualifierValue: this.state.qualifierValue,
      users: this.state.usersInGroup,
    }
  }

  handleSaveBtnClick() {
    let formObject = this.extractFormObject()
    this.userGroupService.save(formObject).then(this.updateStateFromResponse)
  }

  handleAvailableUsersRowSelect(user, isSelected, e) {
    const newSelectedUsers = Arrays.addOrRemoveItem(this.state.selectedAvailableUsers, user, !isSelected)
    this.setState({
      ...this.state,
      selectedAvailableUsers: newSelectedUsers,
      selectedAvailableUsersIds: newSelectedUsers.map((u) => u.id),
    })
  }

  handleUsersInGroupRowSelect(user, isSelected, e) {
    const newSelectedUsersInGroup = Arrays.addOrRemoveItem(this.state.selectedUsersInGroup, user, !isSelected)
    this.setState({
      ...this.state,
      selectedUsersInGroup: newSelectedUsersInGroup,
      selectedUsersInGroupIds: newSelectedUsersInGroup.map((u) => u.userId),
    })
  }

  handleAddSelectedUsersToGroup() {
    const selectedUsers = this.state.selectedAvailableUsers
    const role = this.state.newUserRoleCode
    const usersInGroupToAdd = selectedUsers.map((user) => {
      return {
        userId: user.id,
        username: user.username,
        userRole: user.role,
        userEnabled: user.enabled,
        role: role,
        joinStatus: User.USER_GROUP_JOIN_STATUS.ACCEPTED,
      }
    })
    const newAvailableUsers = this.state.availableUsers.filter((u) => selectedUsers.indexOf(u) < 0)
    const newUsersInGroup = this.state.usersInGroup.concat(usersInGroupToAdd)
    Arrays.sort(newUsersInGroup, 'username')

    this.setState({
      ...this.state,
      usersInGroup: newUsersInGroup,
      availableUsers: newAvailableUsers,
      selectedAvailableUsers: [],
      selectedAvailableUsersIds: [],
    })
  }

  handleRemoveSelectedUsersToGroup() {
    let selectedUsers = this.state.selectedUsersInGroup.map((userInGroup) => {
      const user = this.props.users.find((u) => u.id === userInGroup.userId)
      return {
        id: userInGroup.userId,
        username: user.username,
        role: user.role,
      }
    })
    const notSelectedUsersInGroup = this.state.usersInGroup.filter(
      (u) => this.state.selectedUsersInGroup.indexOf(u) < 0
    )
    const newUsersInGroup = notSelectedUsersInGroup
    const newAvailableUsers = this.state.availableUsers.concat(selectedUsers)
    Arrays.sort(newAvailableUsers, 'username')

    this.setState({
      ...this.state,
      selectedUsersInGroup: [],
      selectedUsersInGroupIds: [],
      availableUsers: newAvailableUsers,
      usersInGroup: newUsersInGroup,
    })
  }

  updateStateFromResponse(res) {
    super.updateStateFromResponse(res)
    if (res.statusOk) {
      const wasNewItem = this.state.newItem
      if (wasNewItem) {
        this.props.dispatch(UserGroupActions.receiveUserGroup(res.form))
        const itemId = res.form.id
        RouterUtils.navigateToUserGroupEditPage(this.props.navigate, itemId)
      }
    }
  }

  handleSelectedUsersCellEdit(row, cellName, cellValue) {
    console.log(cellValue)
    console.log(this.state.usersInGroup)
  }

  render() {
    if (!this.state.ready) {
      return <div>Loading...</div>
    }
    const loggedUserRole = this.props.loggedUser.role
    const roles = Object.keys(User.ROLE_IN_GROUP)
    const availableRoles = roles.filter((r) => {
      switch (loggedUserRole) {
        case User.ROLE.ADMIN:
          return true
        case User.ROLE.DESIGN:
        case User.ROLE.ANALYSIS:
        case User.ROLE.CLEANSING:
          return r !== User.ROLE_IN_GROUP.OWNER
        case User.ROLE.ENTRY:
        case User.ROLE.ENTRY_LIMITED:
          return r === User.ROLE_IN_GROUP.OPERATOR || r === User.ROLE_IN_GROUP.VIEWER
        default:
          return false
      }
    })
    const joinStata = Object.keys(User.USER_GROUP_JOIN_STATUS)

    const createRoleEditor = (onUpdate, props) => <UserRoleDropdownEditor onUpdate={onUpdate} {...props} />

    const isNotDescendantOf = function (group1, group2) {
      return true
    }
    const editedUserGroup = this.state.editedUserGroup
    const ownerId = 0 //this.state.usersInGroup.find(u => u.role == 'OWNER').userId

    const availableParentGroups = this.props.userGroups.filter((group) => {
      return (
        group.id !== editedUserGroup.id && (editedUserGroup.id === null || isNotDescendantOf(group, editedUserGroup))
      )
    })
    const parentGroupOptions = [
      <option key="0" value="">
        ---
      </option>,
    ].concat(
      availableParentGroups.map((group) => (
        <option key={group.id} value={group.id}>
          {group.label}
        </option>
      ))
    )

    return (
      <div>
        <Form>
          <FormGroup row color={this.getFieldState('name')}>
            <Label for="name" sm={2}>
              Name
            </Label>
            <Col sm={10}>
              <Input
                type="text"
                name="name"
                id="name"
                value={this.state.name}
                state={this.getFieldState('name')}
                onChange={(event) => this.setState({ ...this.state, name: event.target.value })}
              />
              {this.state.errorFeedback['name'] && <FormFeedback>{this.state.errorFeedback['name']}</FormFeedback>}
            </Col>
          </FormGroup>
          <FormGroup row color={this.getFieldState('label')}>
            <Label for="label" sm={2}>
              Label
            </Label>
            <Col sm={10}>
              <Input
                type="text"
                name="label"
                id="label"
                value={this.state.label}
                state={this.getFieldState('label')}
                onChange={(event) => this.setState({ ...this.state, label: event.target.value })}
              />
              {this.state.errorFeedback['label'] && <FormFeedback>{this.state.errorFeedback['label']}</FormFeedback>}
            </Col>
          </FormGroup>
          <FormGroup row color={this.getFieldState('description')}>
            <Label for="description" sm={2}>
              Description
            </Label>
            <Col sm={10}>
              <Input
                type="text"
                name="description"
                id="description"
                value={this.state.description}
                state={this.getFieldState('description')}
                onChange={(event) => this.setState({ ...this.state, description: event.target.value })}
              />
              {this.state.errorFeedback['description'] && (
                <FormFeedback>{this.state.errorFeedback['description']}</FormFeedback>
              )}
            </Col>
          </FormGroup>
          <FormGroup row color={this.getFieldState('visibilityCode')}>
            <Label for="visibilityCode" sm={2}>
              Visibility
            </Label>
            <Col sm={10}>
              <FormGroup check>
                <Label check>
                  <Input
                    type="radio"
                    value="P"
                    name="visibilityCode"
                    id="visibilityCodePublic"
                    checked={this.state.visibilityCode === 'P'}
                    state={this.getFieldState('visibilityCode')}
                    onChange={(event) => this.setState({ ...this.state, visibilityCode: event.target.value })}
                  />
                  Public
                </Label>
                <span style={{ display: 'inline-block', width: '40px' }}></span>
                <Label check>
                  <Input
                    type="radio"
                    value="N"
                    name="visibilityCode"
                    id="visibilityCodePrivate"
                    checked={this.state.visibilityCode === 'N'}
                    state={this.getFieldState('visibilityCode')}
                    onChange={(event) => this.setState({ ...this.state, visibilityCode: event.target.value })}
                  />
                  Private
                </Label>
              </FormGroup>
              {this.state.errorFeedback['visibilityCode'] && (
                <FormFeedback>{this.state.errorFeedback['visibilityCode']}</FormFeedback>
              )}
            </Col>
          </FormGroup>
          <FormGroup row color={this.getFieldState('parentId')}>
            <Label for="parentGroupSelect" sm={2}>
              Parent Group
            </Label>
            <Col sm={10}>
              <Input
                type="select"
                name="parentId"
                id="parentGroupSelect"
                state={this.getFieldState('parentId')}
                value={this.state.parentId ? this.state.parentId : ''}
                onChange={(event) => this.setState({ ...this.state, parentId: event.target.value })}
              >
                {parentGroupOptions}
              </Input>
              {this.state.errorFeedback['parentId'] && (
                <FormFeedback>{this.state.errorFeedback['parentId']}</FormFeedback>
              )}
            </Col>
          </FormGroup>
          <FormGroup row color={this.getFieldState('enabled')}>
            <Label for="enabled" sm={2}>
              Enabled
            </Label>
            <Col sm={{ size: 10 }}>
              <FormGroup check>
                <Label check>
                  <Input
                    type="checkbox"
                    name="enabled"
                    id="enabled"
                    checked={this.state.enabled}
                    state={this.getFieldState('enabled')}
                    onChange={(event) => this.setState({ ...this.state, enabled: event.target.checked })}
                  />
                  {this.state.errorFeedback['enabled'] && (
                    <FormFeedback>{this.state.errorFeedback['enabled']}</FormFeedback>
                  )}
                </Label>
              </FormGroup>
            </Col>
          </FormGroup>
          <fieldset className="secondary">
            <legend>
              {L.l('userGroup.qualifiers.heading')}{' '}
              <i id="qualifiersHeading" className="info fa fa-info-circle" aria-hidden="true"></i>
            </legend>
            <UncontrolledTooltip placement="right" className="info" autohide={false} target="qualifiersHeading">
              {L.l('userGroup.qualifiers.info')}
            </UncontrolledTooltip>
            <Container fluid>
              <FormGroup md={6} row color={this.getFieldState('qualifierName')}>
                <Label for="qualifierName" sm={2}>
                  {L.l('userGroup.qualifier.name')}
                </Label>
                <Col sm={10}>
                  <Input
                    type="text"
                    name="qualifierName"
                    id="qualifierName"
                    value={this.state.qualifierName}
                    state={this.getFieldState('qualifierName')}
                    onChange={(event) => this.setState({ ...this.state, qualifierName: event.target.value })}
                  />
                  {this.state.errorFeedback['qualifierName'] && (
                    <FormFeedback>{this.state.errorFeedback['qualifierName']}</FormFeedback>
                  )}
                </Col>
              </FormGroup>
              <FormGroup md={6} row color={this.getFieldState('qualifierValue')}>
                <Label for="qualifierValue" sm={2}>
                  {L.l('userGroup.qualifier.value')}
                </Label>
                <Col sm={10}>
                  <Input
                    type="text"
                    name="qualifierValue"
                    id="qualifierValue"
                    value={this.state.qualifierValue}
                    state={this.getFieldState('qualifierValue')}
                    onChange={(event) => this.setState({ ...this.state, qualifierValue: event.target.value })}
                  />
                  {this.state.errorFeedback['qualifierValue'] && (
                    <FormFeedback>{this.state.errorFeedback['qualifierValue']}</FormFeedback>
                  )}
                </Col>
              </FormGroup>
            </Container>
          </fieldset>
          <Row>
            <Col sm="7">
              <fieldset className="secondary">
                <legend>Add/Remove Users</legend>
                <Row>
                  <Col sm="8">
                    <BootstrapTable
                      data={this.state.availableUsers}
                      striped
                      hover
                      condensed
                      height="200"
                      selectRow={{
                        mode: 'checkbox',
                        clickToSelect: true,
                        hideSelectionColumn: true,
                        bgColor: 'lightBlue',
                        onSelect: this.handleAvailableUsersRowSelect,
                        selected: this.state.selectedAvailableUsersIds,
                      }}
                    >
                      <TableHeaderColumn dataField="id" isKey hidden>
                        Id
                      </TableHeaderColumn>
                      <TableHeaderColumn dataField="username">Username</TableHeaderColumn>
                      <TableHeaderColumn dataField="role" width="100">
                        Role
                      </TableHeaderColumn>
                    </BootstrapTable>
                  </Col>
                  <Col sm="4">
                    {this.state.selectedAvailableUsers.length > 0 ? (
                      <Row>
                        <Col sm="10">
                          <Input
                            type="select"
                            name="newUserRole"
                            id="newUserRoleSelect"
                            onChange={(event) => this.setState({ ...this.state, newUserRoleCode: event.target.value })}
                            value={this.state.newUserRoleCode}
                          >
                            {availableRoles.map((role) => (
                              <option key={role} value={role}>
                                {role}
                              </option>
                            ))}
                          </Input>
                        </Col>
                        <Col sm="2">
                          <Button onClick={this.handleAddSelectedUsersToGroup}>&gt;</Button>
                        </Col>
                      </Row>
                    ) : (
                      ''
                    )}
                    {this.state.selectedUsersInGroup.length > 0 ? (
                      <Row>
                        <Col sm={{ size: 2, offset: 6 }}>
                          <Button onClick={this.handleRemoveSelectedUsersToGroup}>&lt;</Button>
                        </Col>
                      </Row>
                    ) : (
                      ''
                    )}
                  </Col>
                </Row>
              </fieldset>
            </Col>
            <Col sm="5">
              <fieldset className="secondary">
                <legend>Users in Group</legend>
                <BootstrapTable
                  data={this.state.usersInGroup}
                  striped
                  hover
                  height="200"
                  cellEdit={{ mode: 'click', afterSaveCell: this.handleSelectedUsersCellEdit }}
                  selectRow={{
                    mode: 'checkbox',
                    clickToSelect: true,
                    hideSelectionColumn: true,
                    bgColor: 'lightBlue',
                    onSelect: this.handleUsersInGroupRowSelect,
                    selected: this.state.selectedUsersInGroupIds,
                    unselectable: [ownerId],
                  }}
                >
                  <TableHeaderColumn dataField="userId" isKey hidden>
                    Id
                  </TableHeaderColumn>
                  <TableHeaderColumn dataField="username" editable={false}>
                    Username
                  </TableHeaderColumn>
                  <TableHeaderColumn
                    dataField="role"
                    width="140"
                    customEditor={{ getElement: createRoleEditor, customEditorParameters: { roles: roles } }}
                  >
                    Role
                  </TableHeaderColumn>
                  <TableHeaderColumn
                    dataField="joinStatus"
                    editable={{ type: 'select', options: { values: joinStata } }}
                    width="120"
                  >
                    Status
                  </TableHeaderColumn>
                </BootstrapTable>
                {this.state.errorFeedback['users'] && <FormFeedback>{this.state.errorFeedback['users']}</FormFeedback>}
              </fieldset>
            </Col>
          </Row>
          <Row>
            <Col md="12">
              <Alert color={this.state.alertMessageColor} isOpen={this.state.alertMessageOpen}>
                {this.state.alertMessageText}
              </Alert>
            </Col>
          </Row>
          <FormGroup check row>
            <Col sm={{ size: 2, offset: 5 }}>
              <Button color="primary" onClick={this.handleSaveBtnClick}>
                {L.l('global.save')}
              </Button>
            </Col>
          </FormGroup>
        </Form>
      </div>
    )
  }
}

const mapStateToProps = (state) => {
  const {
    initialized: isUserGroupsInitialized,
    isFetching: isFetchingUserGroups,
    lastUpdated: lastUpdatedUserGroups,
    items: userGroups,
  } = state.userGroups || {
    isUserGroupsInitialized: false,
    isFetchingUserGroups: true,
    userGroups: [],
  }
  const {
    isFetching: isFetchingUsers,
    lastUpdated: lastUpdatedUsers,
    users,
  } = state.users || {
    isFetchingUsers: true,
    users: [],
  }
  const { loggedUser } = state.session

  return {
    loggedUser,
    isFetchingUsers,
    lastUpdatedUsers,
    users,
    isUserGroupsInitialized,
    isFetchingUserGroups,
    lastUpdatedUserGroups,
    userGroups,
  }
}

export default connect(mapStateToProps)(withNavigate(withParams(UserGroupDetailsPage)))
