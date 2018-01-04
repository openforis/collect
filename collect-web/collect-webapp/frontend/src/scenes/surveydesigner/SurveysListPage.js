import React, { Component } from 'react';
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormFeedback, FormGroup, Label, Input, Row, Col } from 'reactstrap';

import Dialogs from 'components/Dialogs';
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
        this.handleRowDoubleClick = this.handleRowDoubleClick.bind(this)
        this.handleRowSelect = this.handleRowSelect.bind(this)
        this.handleSurveysSelection = this.handleSurveysSelection.bind(this)
        this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
        this.handleEditButtonClick = this.handleEditButtonClick.bind(this)
        this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
        this.handlePublishButtonClick = this.handlePublishButtonClick.bind(this)
        this.handleUnpublishButtonClick = this.handleUnpublishButtonClick.bind(this)
        this.handleCloneButtonClick = this.handleCloneButtonClick.bind(this)
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
        RouterUtils.navigateToSurveyEditPage(this.props.history, surveySummary.id)
    }

    handleEditButtonClick() {
        this.handleRowDoubleClick(this.state.selectedSurvey)
    }

    handleExportButtonClick() {
        RouterUtils.navigateToSurveyExportPage(this.props.history, this.state.selectedSurvey.id)
    }

    handlePublishButtonClick() {
        const survey = this.state.selectedSurvey
        const confirmMessage = L.l('survey.publish.confirmMessage', survey.name)
        Dialogs.confirm(L.l('survey.publish.confirmTitle', survey.name), confirmMessage, function() {
            ServiceFactory.surveyService.publish(survey.id).then(s => {
                Dialogs.alert(L.l('survey.publish.successDialog.title'), 
                    L.l('survey.publish.successDialog.message', survey.name))
            })
        }, null, {confirmButtonLabel: L.l('survey.publish')})
    }

    handleUnpublishButtonClick() {
        const survey = this.state.selectedSurvey
        const confirmMessage = L.l('survey.unpublish.confirmMessage', survey.name)
        Dialogs.confirm(L.l('survey.unpublish.confirmTitle', survey.name), confirmMessage, function() {
            ServiceFactory.surveyService.unpublish(survey.id).then(s => {
                Dialogs.alert(L.l('survey.unpublish.successDialog.title'), 
                    L.l('survey.unpublish.successDialog.message', survey.name))
            })
        }, null, {confirmButtonLabel: L.l('survey.unpublish')})
    }
    
    handleCloneButtonClick() {
        RouterUtils.navigateToSurveyClonePage(this.props.history, this.state.selectedSurvey.name)
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
    
    render() {
        const { surveySummaries, userGroups } = this.props
        if (surveySummaries === null || userGroups === null) {
            return <div>Loading...</div>
        }

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
            if (cell) {
                return userGroups.find(u => u.id === cell).label
            } else {
                return ''
            }
        }
         
        function targetFormatter(cell, row) {
            switch(cell) {
                case "COLLECT_EARTH":
                    return <span style={{display: "inline-block", width: "30px", height: "30px", backgroundSize: "30px",
                        backgroundImage: "url(/assets/img/of_collect_earth_logo_simple.png)"}} />
                case "COLLECT_DESKTOP":
                default:
                    return <span style={{display: "inline-block", width: "30px", height: "30px", backgroundSize: "30px", 
                        backgroundImage: "url(/assets/img/of_collect_logo_simple.png)"}} />
            }
        }

        function publishedIconFormatter(cell, row) {
            return <CheckedIconFormatter checked={row.published && (!row.temporary || row.publishedId)} />
        }

        return (
            <Container fluid>
                <Row className="justify-content-between">
					<Col sm={4}>
						<Button color="info" onClick={this.handleNewButtonClick}>{L.l('general.new')}</Button>
					</Col>
                    {selectedSurvey &&
                        <div>
                            <Button color="success" onClick={this.handleEditButtonClick}>Edit</Button>
                            <Button color="primary" onClick={this.handleExportButtonClick}>Export</Button>
                            <Button color="danger" onClick={this.handleDeleteButtonClick}><i className="fa fa-trash"/></Button>
                            {selectedSurvey && selectedSurvey.temporary &&
                                <Button color="warning" onClick={this.handlePublishButtonClick}>Publish</Button>
                            }
                            {selectedSurvey && selectedSurvey.published && (!selectedSurvey.temporary || selectedSurvey.publishedId) &&
                                <Button color="warning" onClick={this.handleUnpublishButtonClick}>Unpublish</Button>
                            }
                            <Button color="primary" onClick={this.handleCloneButtonClick}>Clone</Button>
                        </div>
                    }
                </Row>
                <BootstrapTable
                    data={combinedSummaries}
                    striped hover condensed
                    height="100%"
                    selectRow={{
                        mode: 'radio',  // single select
                        clickToSelect: true, 
                        hideSelectionColumn: true, 
                        bgColor: 'lightBlue',
                        onSelect: this.handleRowSelect,
                        selected: this.state.selectedSurveyIds
                    }}
                    cellEdit={{ mode: 'click', blurToSave: true }}
                    options={{
                        onRowDoubleClick: this.handleRowDoubleClick,
                        onCellEdit: this.handleCellEdit
                    }}
                >
                    <TableHeaderColumn key="id" dataField="id" isKey hidden dataAlign="center">Id</TableHeaderColumn>
                    <TableHeaderColumn key="name" dataField="name" editable={false} filter={{type: 'TextFilter'}} dataSort>{L.l('survey.name')}</TableHeaderColumn>
                    <TableHeaderColumn key="projectName" dataField="projectName" editable={false} filter={{type: 'TextFilter'}} dataSort>{L.l('survey.name')}</TableHeaderColumn>
                    <TableHeaderColumn key="modifiedDate" dataField="modifiedDate" editable={false} dataFormat={Formatters.dateTimeFormatter}
				        dataAlign="center" width="150" editable={false} dataSort>{L.l('survey.lastModified')}</TableHeaderColumn>
                    <TableHeaderColumn key="target" dataField="target" dataFormat={targetFormatter}
				        dataAlign="center" width="80" editable={false} dataSort>{L.l('survey.target')}</TableHeaderColumn>
                    <TableHeaderColumn key="temporary" dataField="temporary" dataFormat={Formatters.checkedIconFormatter}
				        dataAlign="center" width="80" editable={false} dataSort>{L.l('survey.unpublishedChanges')}</TableHeaderColumn>
                    <TableHeaderColumn key="published" dataField="published" dataFormat={publishedIconFormatter}
				        dataAlign="center" width="80" editable={false} dataSort>{L.l('survey.published')}</TableHeaderColumn>
                    <TableHeaderColumn key="userGroupId" dataField="userGroupId" dataFormat={userGroupFormatter}
                        customEditor={{ getElement: createUserGroupEditor, customEditorParameters: { userGroups: userGroups } }}
                        dataAlign="center" width="150" dataSort>{L.l('survey.userGroup')}</TableHeaderColumn>
                </BootstrapTable>
            </Container>
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
    