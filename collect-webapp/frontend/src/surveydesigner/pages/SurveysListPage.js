import React from 'react';
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import {
    Button, DropdownItem, DropdownToggle, DropdownMenu,
    Row, Col, UncontrolledButtonDropdown, UncontrolledTooltip
} from 'reactstrap';

import MaxAvailableSpaceContainer from 'common/components/MaxAvailableSpaceContainer';
import TableResizeOnWindowResizeComponent from 'common/components/TableResizeOnWindowResizeComponent';
import Dialogs from 'common/components/Dialogs';
import * as Formatters from 'common/components/datatable/formatters';
import UserGroupColumnEditor from 'common/components/surveydesigner/UserGroupColumnEditor';
import ServiceFactory from 'services/ServiceFactory';
import L from 'utils/Labels';
import Arrays from 'utils/Arrays';
import RouterUtils from 'utils/RouterUtils';
import { changeUserGroup, publishSurvey, unpublishSurvey, deleteSurvey } from 'actions/surveys';
import SurveyValidationResultDialog from '../components/SurveyValidationResultDialog';

class SurveysListPage extends React.Component {

    constructor() {
        super()

        this.state = {
            selectedSurvey: null,
            selectedSurveys: [],
            selectedSurveyIds: []
        }

        this.wrapperRef = React.createRef()

        this.handleCellEdit = this.handleCellEdit.bind(this)
        this.handleCloneButtonClick = this.handleCloneButtonClick.bind(this)
        this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
        this.handleEditButtonClick = this.handleEditButtonClick.bind(this)
        this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
        this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
        this.handlePublishButtonClick = this.handlePublishButtonClick.bind(this)
        this.handleRowDoubleClick = this.handleRowDoubleClick.bind(this)
        this.handleRowSelect = this.handleRowSelect.bind(this)
        this.handleSurveysSelection = this.handleSurveysSelection.bind(this)
        this.handleUnpublishButtonClick = this.handleUnpublishButtonClick.bind(this)
        this.performSurveyDelete = this.performSurveyDelete.bind(this)
        this.resetSelection = this.resetSelection.bind(this)
        this.handleRowClick = this.handleRowClick.bind(this)
        this.handleFilterChange = this.handleFilterChange.bind(this)
    }

    handleCellEdit(row, fieldName, value) {
        if (fieldName === 'userGroupId') {
            const { changeUserGroup, loggedUser } = this.props
            changeUserGroup(row, value.userGroupId, loggedUser.id)
        }
    }

    handleNewButtonClick() {
        RouterUtils.navigateToNewSurveyPage(this.props.history)
    }

    handleRowDoubleClick(surveySummary) {
        if (surveySummary.temporary) {
            RouterUtils.navigateToSurveyEditPage(this.props.history, surveySummary.id)
        } else {
            ServiceFactory.surveyService.createTemporarySurvey(surveySummary.id).then(res => {
                const newSurveyId = res.object
                RouterUtils.navigateToSurveyEditPage(this.props.history, newSurveyId)
            })
        }
    }

    handleEditButtonClick() {
        this.handleRowDoubleClick(this.state.selectedSurvey)
    }

    handleDeleteButtonClick() {
        const survey = this.state.selectedSurvey

        Dialogs.confirm(L.l('survey.delete.confirm.title'), L.l('survey.delete.confirm.message', survey.name), () => {
            if (survey.temporary && !survey.publishedId) {
                this.performSurveyDelete(survey)
            } else {
                Dialogs.confirm(L.l('survey.delete.published.confirm.title'),
                    L.l('survey.delete.published.confirm.message', survey.name), () => {
                        this.performSurveyDelete(survey)
                    }, null, { confirmButtonLabel: L.l('common.delete.label') })
            }
        }, null, { confirmButtonLabel: L.l('common.delete.label') })
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
            selectedSurveyIds: []
        })
    }

    handleExportButtonClick() {
        RouterUtils.navigateToSurveyExportPage(this.props.history, this.state.selectedSurvey.id)
    }

    handlePublishButtonClick() {
        const { publishSurvey } = this.props
        const { selectedSurvey: survey } = this.state
        const $this = this
        const confirmMessage = L.l('survey.publish.confirmMessage', survey.name)
        Dialogs.confirm(L.l('survey.publish.confirmTitle', survey.name), confirmMessage, function () {
            const loadingDialog = Dialogs.showLoadingDialog()
            publishSurvey(survey, false , () => {
                $this.resetSelection()
                loadingDialog.close()
            })
        }, null, { confirmButtonLabel: L.l('survey.publish') })
    }

    handleUnpublishButtonClick() {
        const { unpublishSurvey } = this.props
        const { selectedSurvey: survey } = this.state
        const $this = this
        const confirmMessage = L.l('survey.unpublish.confirmMessage', survey.name)
        Dialogs.confirm(L.l('survey.unpublish.confirmTitle', survey.name), confirmMessage, function () {
            unpublishSurvey(survey)
            $this.resetSelection()
        }, null, { confirmButtonLabel: L.l('survey.unpublish') })
    }

    handleCloneButtonClick() {
        RouterUtils.navigateToSurveyClonePage(this.props.history, this.state.selectedSurvey.name)
    }

    handleRowClick(row) {
        this.handleRowSelect(row, true)
    }

    handleRowSelect(row, isSelected, e) {
        const newSelectedSurveys = isSelected ? [row] : []
        this.handleSurveysSelection(newSelectedSurveys)
    }

    handleSurveysSelection(newSelectedSurveys) {
        this.setState({
            ...this.state,
            selectedSurvey: Arrays.uniqueItemOrNull(newSelectedSurveys),
            selectedSurveyIds: newSelectedSurveys.map(item => item.id),
            selectedSurveys: newSelectedSurveys
        })
    }

    handleFilterChange(filterObj) {
        this.resetSelection()
    }

    render() {
        const { surveys, userGroups, loggedUser, validationResultShown } = this.props

        if (surveys === null) {
            return <div>Loading...</div>
        }
        const groupedByUriSummaries = Arrays.groupBy(surveys, 'uri')
        const surveyUris = Object.keys(groupedByUriSummaries)
        const combinedSummaries = surveyUris.map(uri => {
            const tempAndPublished = groupedByUriSummaries[uri]
            if (tempAndPublished.length === 1) {
                return tempAndPublished[0]
            } else {
                const publishedSurvey = tempAndPublished.filter(s => !s.temporary)[0]
                const tempSurvey = tempAndPublished.filter(s => s.temporary)[0]
                const merged = {
                    ...tempSurvey,
                    published: true,
                    publishedId: publishedSurvey.id
                }
                return merged
            }
        })

        const selectedSurvey = this.state.selectedSurvey

        const createUserGroupEditor = (onUpdate, props) => (<UserGroupColumnEditor onUpdate={onUpdate} {...props} />);

        function userGroupFormatter(cell, row) {
            const userGroupId = cell
            const survey = row

            if (userGroupId) {
                const userGroupLabel = survey.userGroup.label

                if (loggedUser.canChangeSurveyUserGroup(survey.userInGroupRole)) {
                    return <span>
                        <i className="fa fa-edit" aria-hidden="true" ></i>
                        &nbsp;
                            {userGroupLabel}
                    </span>
                } else {
                    return userGroupLabel
                }
            } else {
                return ''
            }
        }

        function targetFormatter(cell, row) {
            let logoClass, logoTooltip
            switch (cell) {
                case "COLLECT_EARTH":
                    logoClass = 'collect-earth'
                    logoTooltip = 'Collect Earth'
                    break
                case "COLLECT_DESKTOP":
                default:
                    logoClass = 'collect-desktop'
                    logoTooltip = 'Collect Desktop'
            }
            let logoElId = 'survey_target_icon_' + row.id
            return <span>
                <span className={'logo small ' + logoClass} id={logoElId} />
                <UncontrolledTooltip placement="right" target={logoElId}>{logoTooltip}</UncontrolledTooltip>
            </span>

        }

        const nonEditableRows = combinedSummaries.filter(s => loggedUser.canChangeSurveyUserGroup(s.userInGroupRole))

        return (
            <MaxAvailableSpaceContainer ref={this.wrapperRef}>
                <TableResizeOnWindowResizeComponent wrapperRef={this.wrapperRef} margin={108} />
                {
                    validationResultShown &&
                    <SurveyValidationResultDialog />
                }
                <Row className="action-bar justify-content-between">
                    <Col sm={3}>
                        <Button color="info" onClick={this.handleNewButtonClick}>{L.l('common.new')}</Button>
                    </Col>
                    {selectedSurvey &&
                        <Col sm={1}>
                            <Button color="success" onClick={this.handleEditButtonClick}>
                                <i className="fa fa-edit" aria-hidden="true"></i>{L.l('global.edit')}
                            </Button>
                        </Col>
                    }
                    {selectedSurvey &&
                        <Col sm={1}>
                            <Button color="primary" onClick={this.handleExportButtonClick}>
                                <i className="fa fa-download" aria-hidden="true"></i>{L.l('global.export')}
                            </Button>
                        </Col>
                    }
                    {selectedSurvey &&
                        <Col sm={2}>
                            <UncontrolledButtonDropdown>
                                <DropdownToggle caret color="warning">
                                    <i className="fa fa-wrench" aria-hidden="true"></i>{L.l('global.advancedFunctions')}
                                </DropdownToggle>
                                <DropdownMenu>
                                    {selectedSurvey && selectedSurvey.temporary &&
                                        <DropdownItem color="warning" onClick={this.handlePublishButtonClick}><i className="fa fa-check-circle" aria-hidden="true"></i>{L.l('survey.publish')}</DropdownItem>
                                    }
                                    {selectedSurvey && selectedSurvey.temporary &&
                                        <DropdownItem divider />
                                    }
                                    {selectedSurvey && selectedSurvey.published &&
                                        <DropdownItem color="warning" onClick={this.handleUnpublishButtonClick}><i className="fa fa-ban" aria-hidden="true"></i>{L.l('survey.unpublish')}</DropdownItem>
                                    }
                                    <DropdownItem color="primary" onClick={this.handleCloneButtonClick}><i className="fa fa-clone" aria-hidden="true"></i>{L.l('survey.clone')}</DropdownItem>
                                    <DropdownItem divider />
                                    <DropdownItem color="danger" onClick={this.handleDeleteButtonClick}><i className="fa fa-trash" />{L.l('common.delete.label')}</DropdownItem>
                                </DropdownMenu>
                            </UncontrolledButtonDropdown>
                        </Col>
                    }
                </Row>
                <BootstrapTable
                    data={combinedSummaries}
                    striped hover condensed pagination
                    selectRow={{
                        mode: 'radio',  // single select
                        bgColor: 'lightBlue',
                        hideSelectColumn: true,
                        onSelect: this.handleRowSelect,
                        selected: this.state.selectedSurveyIds
                    }}
                    cellEdit={{
                        mode: 'click',
                        blurToSave: true,
                        nonEditableRows: () => nonEditableRows
                    }}
                    options={{
                        onRowClick: this.handleRowClick,
                        onRowDoubleClick: this.handleRowDoubleClick,
                        onCellEdit: this.handleCellEdit,
                        onFilterChange: this.handleFilterChange
                    }}
                >
                    <TableHeaderColumn key="id" dataField="id" isKey hidden dataAlign="center">Id</TableHeaderColumn>
                    <TableHeaderColumn key="name" dataField="name" editable={false} filter={{ type: 'TextFilter' }} dataSort width="200">{L.l('survey.name')}</TableHeaderColumn>
                    <TableHeaderColumn key="projectName" dataField="projectName" editable={false} filter={{ type: 'TextFilter' }} dataSort width="250">{L.l('survey.projectName')}</TableHeaderColumn>
                    <TableHeaderColumn key="modifiedDate" dataField="modifiedDate" dataFormat={Formatters.dateTimeFormatter}
                        dataAlign="center" width="90" editable={false} dataSort>{L.l('survey.lastModified')}</TableHeaderColumn>
                    <TableHeaderColumn key="target" dataField="target" dataFormat={targetFormatter}
                        dataAlign="center" width="60" editable={false} dataSort>{L.l('survey.target')}</TableHeaderColumn>
                    <TableHeaderColumn key="temporary" dataField="temporary" dataFormat={Formatters.checkedIconFormatter}
                        dataAlign="center" width="80" editable={false} dataSort>{L.l('survey.unpublishedChanges')}</TableHeaderColumn>
                    <TableHeaderColumn key="published" dataField="published" dataFormat={Formatters.checkedIconFormatter}
                        dataAlign="center" width="80" editable={false} dataSort>{L.l('survey.published')}</TableHeaderColumn>
                    <TableHeaderColumn key="userGroupId" dataField="userGroupId" dataFormat={userGroupFormatter}
                        customEditor={{ getElement: createUserGroupEditor, customEditorParameters: { userGroups: userGroups } }}
                        dataAlign="left" width="150" dataSort>{L.l('survey.userGroup')}</TableHeaderColumn>
                </BootstrapTable>
            </MaxAvailableSpaceContainer>
        )
    }
}

const mapStateToProps = state => {
    const surveysListState = state.surveyDesigner.surveysList
    const surveys = surveysListState ? surveysListState.items : null
    const userGroups = state.userGroups ? state.userGroups.items : null

    //update user group with cached one
    if (surveys && userGroups) {
        surveys.forEach(s => {
            s.userGroup = userGroups.find(u => u.id === s.userGroupId)
        })
    }

    return {
        survey: state.activeSurvey ? state.activeSurvey.survey : null,
        userGroups,
        loggedUser: state.session ? state.session.loggedUser : null,
        surveys,
        validationResultShown: surveysListState.validationResultShown
    }
}

export default connect(mapStateToProps, {
    publishSurvey, unpublishSurvey, deleteSurvey, changeUserGroup
})(SurveysListPage)
