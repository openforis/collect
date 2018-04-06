import React, { Component } from 'react';
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, DropdownItem, DropdownToggle, DropdownMenu,
    Form, FormFeedback, FormGroup, Label, Input, Row, Col, UncontrolledButtonDropdown } from 'reactstrap';

import MaxAvailableSpaceContainer from 'components/MaxAvailableSpaceContainer';
import Dialogs from 'components/Dialogs';
import Containers from 'components/Containers';
import * as Formatters from 'components/datatable/formatters';
import CheckedIconFormatter from 'components/datatable/CheckedIconFormatter'
import UserGroupColumnEditor from 'components/surveydesigner/UserGroupColumnEditor';
import ServiceFactory from 'services/ServiceFactory';
import L from 'utils/Labels';
import Arrays from 'utils/Arrays';
import RouterUtils from 'utils/RouterUtils';
import * as SurveyActions  from 'actions/surveys';

class SurveysListPage extends Component {

    constructor(props) {
        super(props)

        this.state = {
            selectedSurvey: null,
            selectedSurveys: [],
            selectedSurveyIds: []
        }

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
        this.handleWindowResize = this.handleWindowResize.bind(this)
        this.updateTableHeight = this.updateTableHeight.bind(this)
        this.handleRowClick = this.handleRowClick.bind(this)
        this.handleFilterChange = this.handleFilterChange.bind(this)
    }

    componentDidMount() {
        this.handleWindowResize();
        window.addEventListener("resize", this.handleWindowResize);
    }

    componentWillUnmount() {
        window.removeEventListener("resize", this.handleWindowResize);
    }

    handleWindowResize() {
        this.updateTableHeight()
    }

    updateTableHeight() {
        const mainContainer = this.refs['survey-list-container']
        Containers.extendTableHeightToMaxAvailable(mainContainer, 50)
    }
    
    handleCellEdit(row, fieldName, value) {
		if (fieldName === 'userGroupId') {
			const surveyId = row.id
            const newUserGroupId = value.userGroupId
            const loggedUserId = this.props.loggedUser.id
            this.props.dispatch(SurveyActions.changeUserGroup(surveyId, newUserGroupId, loggedUserId))
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
                }, null, {confirmButtonLabel: L.l('global.delete')})
            }
        }, null, {confirmButtonLabel: L.l('global.delete')})
    }

    performSurveyDelete(survey) {
        this.props.dispatch(SurveyActions.deleteSurvey(survey))
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
        const survey = this.state.selectedSurvey
        const $this = this
        const confirmMessage = L.l('survey.publish.confirmMessage', survey.name)
        Dialogs.confirm(L.l('survey.publish.confirmTitle', survey.name), confirmMessage, function() {
            ServiceFactory.surveyService.publish(survey.id).then(s => {
                Dialogs.alert(L.l('survey.publish.successDialog.title'), 
                    L.l('survey.publish.successDialog.message', survey.name))
                $this.resetSelection()
            })
        }, null, {confirmButtonLabel: L.l('survey.publish')})
    }

    handleUnpublishButtonClick() {
        const survey = this.state.selectedSurvey
        const $this = this
        const confirmMessage = L.l('survey.unpublish.confirmMessage', survey.name)
        Dialogs.confirm(L.l('survey.unpublish.confirmTitle', survey.name), confirmMessage, function() {
            const surveyId = survey.temporary ? survey.publishedId : survey.id
            ServiceFactory.surveyService.unpublish(surveyId).then(s => {
                Dialogs.alert(L.l('survey.unpublish.successDialog.title'), 
                    L.l('survey.unpublish.successDialog.message', survey.name))
                $this.resetSelection()
            })
        }, null, {confirmButtonLabel: L.l('survey.unpublish')})
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
        const { surveySummaries, userGroups, loggedUser } = this.props
        if (surveySummaries === null || userGroups === null) {
            return <div>Loading...</div>
        }
        //update user group with cached one
        surveySummaries.forEach(s => {
            const userGroup = userGroups.find(u => u.id === s.userGroupId)
            s.userGroup = userGroup //update userGroup with cached one in UI
        })
        
        const groupedByUriSummaries = Arrays.groupBy(surveySummaries, 'uri')
        const surveyUris = Object.keys(groupedByUriSummaries)
        const combinedSummaries = surveyUris.map(uri => {
            const tempAndPublished = groupedByUriSummaries[uri]
            if (tempAndPublished.length === 1) {
                return tempAndPublished[0]
            } else {
                const publishedSurvey = tempAndPublished.filter(s => !s.temporary)[0]
                const tempSurvey = tempAndPublished.filter(s => s.temporary)[0]
                const merged = {...tempSurvey, 
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

                if (loggedUser.canChangeSurveyUserGroup(survey)) {
                    return <span>
                            <i className="fa fa-pencil" aria-hidden="true" ></i>
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
            switch(cell) {
                case "COLLECT_EARTH":
                    return <span className="logo small collect-earth" />
                case "COLLECT_DESKTOP":
                default:
                    return <span className="logo small collect-desktop" />
            }
        }

        function publishedIconFormatter(cell, row) {
            return <CheckedIconFormatter checked={row.published && (!row.temporary || row.publishedId)} />
        }

        const nonEditableRows = combinedSummaries.filter(s => loggedUser.canChangeSurveyUserGroup(s))

        return (
            <MaxAvailableSpaceContainer ref="survey-list-container">
                <Row className="action-bar justify-content-between">
					<Col sm={3}>
						<Button color="info" onClick={this.handleNewButtonClick}>{L.l('general.new')}</Button>
					</Col>
                    {selectedSurvey &&
                        <Col sm={1}>
                            <Button color="success" onClick={this.handleEditButtonClick}>
                                <i class="fa fa-pencil" aria-hidden="true"></i>{L.l('global.edit')}
                            </Button>
                        </Col>
                    }
                    {selectedSurvey &&
                        <Col sm={1}>
                            <Button color="primary" onClick={this.handleExportButtonClick}>
                                <i class="fa fa-download" aria-hidden="true"></i>{L.l('global.export')}
                            </Button>
                        </Col>
                    }
                    {selectedSurvey &&
                        <Col sm={2}>
                            <UncontrolledButtonDropdown>
                                <DropdownToggle caret color="warning">
                                    <i class="fa fa-wrench" aria-hidden="true"></i>{L.l('global.advancedFunctions')}
                                </DropdownToggle>
                                <DropdownMenu>
                                    {selectedSurvey && selectedSurvey.temporary &&
                                        <DropdownItem bgColor="warning" color="warning" onClick={this.handlePublishButtonClick}><i class="fa fa-check-circle" aria-hidden="true"></i>{L.l('survey.publish')}</DropdownItem>
                                    }
                                    {selectedSurvey && selectedSurvey.temporary &&
                                        <DropdownItem divider/>
                                    }
                                    {selectedSurvey && selectedSurvey.published && (!selectedSurvey.temporary || selectedSurvey.publishedId) &&
                                        <DropdownItem color="warning" onClick={this.handleUnpublishButtonClick}><i class="fa fa-ban" aria-hidden="true"></i>{L.l('survey.unpublish')}</DropdownItem>
                                    }
                                    <DropdownItem color="primary" onClick={this.handleCloneButtonClick}><i class="fa fa-clone" aria-hidden="true"></i>{L.l('survey.clone')}</DropdownItem>
                                    <DropdownItem divider/>
                                    <DropdownItem color="danger" onClick={this.handleDeleteButtonClick}><i className="fa fa-trash"/>{L.l('global.delete')}</DropdownItem>
                                </DropdownMenu>
                            </UncontrolledButtonDropdown>
                        </Col>
                    }
                </Row>
                <BootstrapTable
                    data={combinedSummaries}
                    striped hover condensed
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
                    <TableHeaderColumn key="name" dataField="name" editable={false} filter={{type: 'TextFilter'}} dataSort width="200">{L.l('survey.name')}</TableHeaderColumn>
                    <TableHeaderColumn key="projectName" dataField="projectName" editable={false} filter={{type: 'TextFilter'}} dataSort width="250">{L.l('survey.projectName')}</TableHeaderColumn>
                    <TableHeaderColumn key="modifiedDate" dataField="modifiedDate" editable={false} dataFormat={Formatters.dateTimeFormatter}
				        dataAlign="center" width="90" editable={false} dataSort>{L.l('survey.lastModified')}</TableHeaderColumn>
                    <TableHeaderColumn key="target" dataField="target" dataFormat={targetFormatter}
				        dataAlign="center" width="60" editable={false} dataSort>{L.l('survey.target')}</TableHeaderColumn>
                    <TableHeaderColumn key="temporary" dataField="temporary" dataFormat={Formatters.checkedIconFormatter}
				        dataAlign="center" width="80" editable={false} dataSort>{L.l('survey.unpublishedChanges')}</TableHeaderColumn>
                    <TableHeaderColumn key="published" dataField="published" dataFormat={publishedIconFormatter}
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
	return {
		survey: state.preferredSurvey ? state.preferredSurvey.survey : null,
		users: state.users ? state.users.users : null,
		userGroups: state.userGroups ? state.userGroups.items : null,
		loggedUser: state.session ? state.session.loggedUser : null,
		surveySummaries: state.surveySummaries ? state.surveySummaries.items : null
	}
}

export default connect(mapStateToProps)(SurveysListPage)
    