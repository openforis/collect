import React, { Component } from 'react';
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormFeedback, FormGroup, Label, Input, Row, Col, Modal, ModalHeader, ModalBody, ModalFooter } from 'reactstrap';

import * as Formatters from 'components/datatable/formatters';
import UserGroupColumnEditor from 'components/surveydesigner/UserGroupColumnEditor';
import ServiceFactory from 'services/ServiceFactory';
import L from 'utils/Labels';
import RouterUtils from 'utils/RouterUtils';
import * as SurveyActions  from 'actions/surveys';

class SurveysListPage extends Component {

    constructor(props) {
        super(props)

        this.state = {

        }

        this.handleCellEdit = this.handleCellEdit.bind(this)
        this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
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
    
    render() {
        if (this.props.surveySummaries == null) {
            return <div>Loading...</div>
        }
        const userGroups = this.props.userGroups

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

        return (
            <Container fluid>
                <Row className="justify-content-between">
					<Col sm={{size: 4}}>
						<Button color="info" onClick={this.handleNewButtonClick}>{L.l('general.new')}</Button>
					</Col>
                </Row>
                <BootstrapTable
                    data={this.props.surveySummaries}
                    striped hover condensed
                    height="100%"
                    selectRow={{
                        mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue',
                        selected: this.props.selectedItemIds
                    }}
                    cellEdit={{ mode: 'click', blurToSave: true }}
                    options={{
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
                    <TableHeaderColumn key="published" dataField="published" dataFormat={Formatters.checkedIconFormatter}
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
    