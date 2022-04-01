import './SurveysListPage.scss'

import React from 'react'
import { connect } from 'react-redux'
import {
  Button,
  DropdownItem,
  DropdownToggle,
  DropdownMenu,
  Row,
  Col,
  UncontrolledButtonDropdown,
  UncontrolledTooltip,
} from 'reactstrap'

import { withNavigate } from 'common/hooks'
import MaxAvailableSpaceContainer from 'common/components/MaxAvailableSpaceContainer'
import Dialogs from 'common/components/Dialogs'
import { DataGrid, DataGridCellRenderers, DataGridValueFormatters } from 'common/components'
import UserGroupColumnEditor from 'common/components/surveydesigner/UserGroupColumnEditor'
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'
import Arrays from 'utils/Arrays'
import RouterUtils from 'utils/RouterUtils'
import { changeUserGroup, publishSurvey, unpublishSurvey, deleteSurvey } from 'actions/surveys'
import SurveyValidationResultDialog from '../components/SurveyValidationResultDialog'

class SurveysListPage extends React.Component {
  constructor() {
    super()

    this.state = {
      selectedSurvey: null,
      selectedSurveys: [],
      selectedSurveyIds: [],
    }

    this.wrapperRef = React.createRef()

    this.handleUserGroupIdUpdate = this.handleUserGroupIdUpdate.bind(this)
    this.handleCloneButtonClick = this.handleCloneButtonClick.bind(this)
    this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
    this.handleEditButtonClick = this.handleEditButtonClick.bind(this)
    this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
    this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
    this.handlePublishButtonClick = this.handlePublishButtonClick.bind(this)
    this.handleRowDoubleClick = this.handleRowDoubleClick.bind(this)
    this.handleSurveysSelection = this.handleSurveysSelection.bind(this)
    this.handleUnpublishButtonClick = this.handleUnpublishButtonClick.bind(this)
    this.performSurveyDelete = this.performSurveyDelete.bind(this)
    this.resetSelection = this.resetSelection.bind(this)
  }

  handleUserGroupIdUpdate({ row, userGroupId }) {
    const { changeUserGroup, loggedUser } = this.props
    changeUserGroup(row, userGroupId, loggedUser.id)
  }

  handleNewButtonClick() {
    RouterUtils.navigateToNewSurveyPage(this.props.navigate)
  }

  handleRowDoubleClick(surveySummary) {
    if (surveySummary.temporary) {
      RouterUtils.navigateToSurveyEditPage(this.props.navigate, surveySummary.id)
    } else {
      ServiceFactory.surveyService.createTemporarySurvey(surveySummary.id).then((res) => {
        const newSurveyId = res.object
        RouterUtils.navigateToSurveyEditPage(this.props.navigate, newSurveyId)
      })
    }
  }

  handleEditButtonClick() {
    this.handleRowDoubleClick(this.state.selectedSurvey)
  }

  handleDeleteButtonClick() {
    const survey = this.state.selectedSurvey

    Dialogs.confirm(
      L.l('survey.delete.confirm.title'),
      L.l('survey.delete.confirm.message', survey.name),
      () => {
        if (survey.temporary && !survey.publishedId) {
          this.performSurveyDelete(survey)
        } else {
          Dialogs.confirm(
            L.l('survey.delete.published.confirm.title'),
            L.l('survey.delete.published.confirm.message', survey.name),
            () => {
              this.performSurveyDelete(survey)
            },
            null,
            { confirmButtonLabel: L.l('common.delete.label') }
          )
        }
      },
      null,
      { confirmButtonLabel: L.l('common.delete.label') }
    )
  }

  performSurveyDelete(survey) {
    const { deleteSurvey } = this.props
    deleteSurvey(survey)
    this.resetSelection()
  }

  resetSelection() {
    this.setState({
      selectedSurvey: null,
      selectedSurveys: [],
      selectedSurveyIds: [],
    })
  }

  handleExportButtonClick() {
    RouterUtils.navigateToSurveyExportPage(this.props.navigate, this.state.selectedSurvey.id)
  }

  handlePublishButtonClick() {
    const { publishSurvey } = this.props
    const { selectedSurvey: survey } = this.state
    const $this = this
    const confirmMessage = L.l('survey.publish.confirmMessage', survey.name)
    Dialogs.confirm(
      L.l('survey.publish.confirmTitle', survey.name),
      confirmMessage,
      () => {
        publishSurvey(survey, false, () => {
          $this.resetSelection()
        })
      },
      null,
      { confirmButtonLabel: L.l('survey.publish') }
    )
  }

  handleUnpublishButtonClick() {
    const { unpublishSurvey } = this.props
    const { selectedSurvey: survey } = this.state
    const $this = this
    const confirmMessage = L.l('survey.unpublish.confirmMessage', survey.name)
    Dialogs.confirm(
      L.l('survey.unpublish.confirmTitle', survey.name),
      confirmMessage,
      function () {
        unpublishSurvey(survey)
        $this.resetSelection()
      },
      null,
      { confirmButtonLabel: L.l('survey.unpublish') }
    )
  }

  handleCloneButtonClick() {
    RouterUtils.navigateToSurveyClonePage(this.props.navigate, this.state.selectedSurvey.name)
  }

  handleSurveysSelection(newSelectedSurveys) {
    this.setState({
      selectedSurvey: Arrays.uniqueItemOrNull(newSelectedSurveys),
      selectedSurveyIds: newSelectedSurveys.map((item) => item.id),
      selectedSurveys: newSelectedSurveys,
    })
  }

  render() {
    const { surveys, userGroups, loggedUser, validationResultShown } = this.props
    const { selectedSurvey } = this.state

    if (surveys === null) {
      return <div>Loading...</div>
    }
    const groupedByUriSummaries = Arrays.groupBy(surveys, 'uri')
    const surveyUris = Object.keys(groupedByUriSummaries)
    const combinedSummaries = surveyUris.map((uri) => {
      const tempAndPublished = groupedByUriSummaries[uri]
      if (tempAndPublished.length === 1) {
        return tempAndPublished[0]
      } else {
        const publishedSurvey = tempAndPublished.filter((s) => !s.temporary)[0]
        const tempSurvey = tempAndPublished.filter((s) => s.temporary)[0]
        const merged = {
          ...tempSurvey,
          published: true,
          publishedId: publishedSurvey.id,
        }
        return merged
      }
    })

    const userGroupCellRenderer = ({ value: userGroupId, row: survey }) => {
      if (!userGroupId) {
        return ''
      }
      const userGroupLabel = survey.userGroup.label

      if (loggedUser.canChangeSurveyUserGroup(survey.userInGroupRole)) {
        return (
          <span>
            <i className="fa fa-edit" aria-hidden="true"></i>
            &nbsp;
            {userGroupLabel}
          </span>
        )
      } else {
        return userGroupLabel
      }
    }

    const targetCellRenderer = ({ value, row }) => {
      let logoClass, logoTooltip
      switch (value) {
        case 'COLLECT_EARTH':
          logoClass = 'collect-earth'
          logoTooltip = 'Collect Earth'
          break
        case 'COLLECT_DESKTOP':
        default:
          logoClass = 'collect-desktop'
          logoTooltip = 'Collect Desktop'
      }
      const logoElId = 'survey_target_icon_' + row.id
      return (
        <span>
          <span className={'logo small ' + logoClass} id={logoElId} />
          <UncontrolledTooltip placement="right" target={logoElId}>
            {logoTooltip}
          </UncontrolledTooltip>
        </span>
      )
    }

    return (
      <MaxAvailableSpaceContainer ref={this.wrapperRef}>
        {validationResultShown && <SurveyValidationResultDialog />}
        <Row className="action-bar justify-content-between">
          <Col sm={3}>
            <Button color="info" onClick={this.handleNewButtonClick}>
              {L.l('common.new')}
            </Button>
          </Col>
          {selectedSurvey && (
            <Col sm={1}>
              <Button color="success" onClick={this.handleEditButtonClick}>
                <i className="fa fa-edit" aria-hidden="true"></i>
                {L.l('global.edit')}
              </Button>
            </Col>
          )}
          {selectedSurvey && (
            <Col sm={1}>
              <Button color="primary" onClick={this.handleExportButtonClick}>
                <i className="fa fa-download" aria-hidden="true"></i>
                {L.l('global.export')}
              </Button>
            </Col>
          )}
          {selectedSurvey && (
            <Col sm={2}>
              <UncontrolledButtonDropdown>
                <DropdownToggle caret color="warning">
                  <i className="fa fa-wrench" aria-hidden="true"></i>
                  {L.l('global.advancedFunctions')}
                </DropdownToggle>
                <DropdownMenu>
                  {selectedSurvey && selectedSurvey.temporary && (
                    <DropdownItem color="warning" onClick={this.handlePublishButtonClick}>
                      <i className="fa fa-check-circle" aria-hidden="true"></i>
                      {L.l('survey.publish')}
                    </DropdownItem>
                  )}
                  {selectedSurvey && selectedSurvey.temporary && <DropdownItem divider />}
                  {selectedSurvey && selectedSurvey.published && (
                    <DropdownItem color="warning" onClick={this.handleUnpublishButtonClick}>
                      <i className="fa fa-ban" aria-hidden="true"></i>
                      {L.l('survey.unpublish')}
                    </DropdownItem>
                  )}
                  <DropdownItem color="primary" onClick={this.handleCloneButtonClick}>
                    <i className="fa fa-clone" aria-hidden="true"></i>
                    {L.l('survey.clone')}
                  </DropdownItem>
                  <DropdownItem divider />
                  <DropdownItem color="danger" onClick={this.handleDeleteButtonClick}>
                    <i className="fa fa-trash" />
                    {L.l('common.delete.label')}
                  </DropdownItem>
                </DropdownMenu>
              </UncontrolledButtonDropdown>
            </Col>
          )}
        </Row>
        <DataGrid
          className="surveys-list"
          columns={[
            {
              field: 'name',
              headerName: 'survey.name',
              sortable: true,
              flex: 1,
              quickSearch: true,
            },
            {
              field: 'projectName',
              headerName: 'survey.projectName',
              sortable: true,
              flex: 1.5,
              quickSearch: true,
            },
            {
              field: 'modifiedDate',
              headerName: 'survey.lastModified',
              sortable: true,
              valueFormatter: DataGridValueFormatters.dateTime,
              width: 150,
              align: 'center',
            },
            {
              field: 'target',
              headerName: 'survey.target',
              sortable: true,
              renderCell: targetCellRenderer,
              width: 120,
              align: 'center',
            },
            {
              field: 'temporary',
              headerName: 'survey.unpublishedChanges',
              sortable: true,
              renderCell: DataGridCellRenderers.bool,
              width: 150,
              type: 'boolean',
            },
            {
              field: 'published',
              headerName: 'survey.published',
              sortable: true,
              renderCell: DataGridCellRenderers.bool,
              width: 120,
              type: 'boolean',
            },
            {
              field: 'userGroupId',
              headerName: 'survey.userGroup',
              sortable: true,
              renderCell: userGroupCellRenderer,
              editable: true,
              renderEditCell: ({ api, field, id, row }) => (
                <UserGroupColumnEditor
                  onUpdate={({ userGroupId }) => {
                    this.handleUserGroupIdUpdate({ row, userGroupId })
                    // close cell editor
                    api.setCellMode(id, field, 'view')
                  }}
                  row={row}
                  userGroups={userGroups}
                />
              ),
              width: 200,
            },
          ]}
          disableMultipleSelection
          isCellEditable={({ row: survey }) => loggedUser.canChangeSurveyUserGroup(survey.userInGroupRole)}
          onSelectedIdsChange={(selectedIds) =>
            this.handleSurveysSelection(
              selectedIds.map((selectedId) => combinedSummaries.find((s) => s.id === selectedId))
            )
          }
          onRowDoubleClick={({ row }) => this.handleRowDoubleClick(row)}
          rows={combinedSummaries}
        />
      </MaxAvailableSpaceContainer>
    )
  }
}

const mapStateToProps = (state) => {
  const surveysListState = state.surveyDesigner.surveysList
  const surveys = surveysListState ? surveysListState.items : null
  const userGroups = state.userGroups ? state.userGroups.items : null

  //update user group with cached one
  if (surveys && userGroups) {
    surveys.forEach((s) => {
      s.userGroup = userGroups.find((u) => u.id === s.userGroupId)
    })
  }

  return {
    survey: state.activeSurvey ? state.activeSurvey.survey : null,
    userGroups,
    loggedUser: state.session ? state.session.loggedUser : null,
    surveys,
    validationResultShown: surveysListState.validationResultShown,
  }
}

export default connect(mapStateToProps, {
  publishSurvey,
  unpublishSurvey,
  deleteSurvey,
  changeUserGroup,
})(withNavigate(SurveysListPage))
