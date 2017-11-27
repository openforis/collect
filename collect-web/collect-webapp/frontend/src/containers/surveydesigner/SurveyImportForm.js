import React, { Component } from 'react';
import { Field, SubmissionError, reduxForm } from 'redux-form'
import {
    Alert, Button, ButtonGroup, ButtonToolbar, Container, ButtonDropdown,
    DropdownToggle, DropdownMenu, DropdownItem, Form, FormFeedback, FormGroup, Label, Input, Row, Col, Progress
} from 'reactstrap';
import Dropzone from 'react-dropzone';
import { connect } from 'react-redux';

import Forms, { FormItem } from 'components/Forms'
import * as SurveysActions from 'actions/surveys';
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'
import Strings from 'utils/Strings'

const SURVEY_IMPORT_ACCEPTED_FILE_TYPES = ".collect,.collect-backup,.cep,.xml"

const asyncValidate = (values /*, dispatch */) => {
    return ServiceFactory.surveyService.validateSurveyImport(values.name, values.userGroupId).then(r => {
        Forms.handleValidationResponse(r)
    })
}

class SurveyImportForm extends Component {

    constructor(props) {
        super(props)

        this.submit = this.submit.bind(this)
        this.handleFileDrop = this.handleFileDrop.bind(this)
        
    }

    submit(values) {
        this.props.dispatch(SurveysActions.startSurveyFileImport(values.name, values.userGroupId))
    }

    handleFileDrop(files) {
        const file = files[0]
        this.props.dispatch(SurveysActions.uploadSurveyFile(file))
    }

    render() {
        const { userGroups, error, handleSubmit, pristine, reset, submitting,
            uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded, importingIntoExistingSurvey,
            surveyFileUploadError, surveyFileUploadErrorMessage, surveyBackupInfo } = this.props
        const acceptedFileTypesDescription = L.l('survey.import.acceptedFileTypesDescription')
        const userGroupOptions = [<option key="-1" value="">{L.l('forms.selectOne')}</option>].concat(userGroups.map(g => <option key={g.id} value={g.id}>{g.label}</option>))
        
        return (
            <Form onSubmit={handleSubmit(this.submit)}>
                <FormGroup row>
                    <Label xs={2} for="file">{L.l('survey.import.file')}:</Label>
                    <Col xs={10}>
                        {uploadingSurveyFile && 
                            <Progress animated value={100} />
                        }
                        {! uploadingSurveyFile && 
                            <Dropzone accept={SURVEY_IMPORT_ACCEPTED_FILE_TYPES} onDrop={(files) => this.handleFileDrop(files)} style={{
                                width: '100%', height: '200px', 
                                borderWidth: '2px', borderColor: 'rgb(102, 102, 102)', 
                                borderStyle: 'dashed', borderRadius: '5px'
                                }}>
                                {surveyFileToBeImportedPreview ?
                                    <p style={{fontSize: '2em', textAlign: 'center'}}><span className="checked large" />{this.props.surveyFileToBeImportedPreview}</p>
                                    : <p>{L.l('forms.fileDropMessage', acceptedFileTypesDescription)}</p>
                                }
                            </Dropzone>
                        }
                        { surveyFileUploadError &&
                            <span className="error">{L.l(surveyFileUploadErrorMessage)}</span> 
                        }
                    </Col>
                </FormGroup>
                {surveyFileUploaded &&
                    <div>
                        {importingIntoExistingSurvey &&
                            <Alert color="warning">{L.l('survey.import.importingIntoExistingSurveyWarning')}</Alert>
                        }
                        <Field
                            className="animated fade-in"
                            name="name"
                            type="text"
                            component={Forms.renderFormItemInputField}
                            contentEditable={!importingIntoExistingSurvey}
                            label={L.l('survey.identifier')}
                            normalize={Forms.normalizeInternalName}
                        />
                        <Field
                            className="animated fade-in"
                            name="userGroupId"
                            component={Forms.renderFormItemSelect}
                            contentEditable={!importingIntoExistingSurvey}
                            label={L.l('survey.userGroup')}
                            options={userGroupOptions}
                        />
                    </div>
                }
                {error && <Alert color="danger">{error}</Alert>}
                <Row>
                    <Col xs={{offset: 5}}>
                        <Button color="primary" type="submit" disabled={submitting}>{L.l('general.import')}</Button>
                    </Col>
                </Row>
            </Form>
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

SurveyImportForm = connect(mapStateToProps)(SurveyImportForm)

export default reduxForm({
    form: 'surveyImportForm',
    asyncValidate,
    asyncBlurFields: ['name', 'userGroupId'],
    initialValues: {
        userGroupId: ''
    }
})(SurveyImportForm)