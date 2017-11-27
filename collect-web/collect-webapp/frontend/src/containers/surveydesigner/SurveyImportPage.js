import React, { Component } from 'react';
import { Button, Col, Container, Row } from 'reactstrap';
import { connect } from 'react-redux';
import L from 'utils/Labels'

import SurveyImportForm from './SurveyImportForm';

class SurveyImportPage extends Component {
    constructor(props) {
        super(props)
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.surveyFileImported) {
            //navigate to survey edit
            this.props.history.push('/surveydesigner/surveys/' + nextProps.importedSurveyId)
        }
    }

    render() {
        return (
            <Container fluid>
                <Row>
                    <Col md={{ size: 6, offset: 3 }}>
                        <SurveyImportForm style={{width: "500px"}} />
                    </Col>
                </Row>
            </Container>
        )
    }
}

const mapStateToProps = state => {
    const { items: userGroups } = state.userGroups
    const { uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded, surveyBackupInfo, 
        importingIntoExistingSurvey, surveyFileImported, importedSurveyId, surveyFileUploadError, surveyFileUploadErrorMessage } = state.surveys
    return {
        userGroups, 
        uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded, surveyBackupInfo, 
        importingIntoExistingSurvey, surveyFileImported, importedSurveyId, surveyFileUploadError, surveyFileUploadErrorMessage
    }
}

export default connect(mapStateToProps)(SurveyImportPage)