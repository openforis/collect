import React, { Component } from 'react';
import { Field, SubmissionError, reduxForm } from 'redux-form'
import {
    Alert, Button, ButtonGroup, ButtonToolbar, Container, ButtonDropdown,
    DropdownToggle, DropdownMenu, DropdownItem, Form, FormFeedback, FormGroup, Label, Input, Row, Col, Progress
} from 'reactstrap';
import Dropzone from 'react-dropzone';
import { connect } from 'react-redux';

import Forms from 'components/Forms'
import * as SurveysActions from 'actions/surveys';
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'
import Strings from 'utils/Strings'

const SURVEY_IMPORT_ACCEPTED_FILE_TYPES = ".collect,.collect-backup,.cep,.xml"

const asyncValidate = (values /*, dispatch */) => {
    return ServiceFactory.surveyService.validateSurveyImport(values.name).then(r => {
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
    }

    handleFileDrop(files) {
        const file = files[0]
        this.props.dispatch(SurveysActions.uploadSurveyFile(file))
    }

    render() {
        const { userGroups, error, handleSubmit, pristine, reset, submitting } = this.props
        const acceptedFileTypesDescription = L.l('survey.import.acceptedFileTypesDescription')
        const userGroupOptions = [<option key="-1" value="">{L.l('forms.selectOne')}</option>].concat(userGroups.map(g => <option key={g.id} value={g.id}>{g.label}</option>))
        
        return (
            <Form onSubmit={handleSubmit(this.submit)}>
                <FormGroup row>
                    <Label for="file">{L.l('survey.import.file')}:</Label>
                    <Col sm={10}>
                        {this.props.uploadingSurveyFile && 
                            <Progress animated value={100} />
                        }
                        {! this.props.uploadingSurveyFile && 
                            <Dropzone accept={SURVEY_IMPORT_ACCEPTED_FILE_TYPES} onDrop={(files) => this.handleFileDrop(files)} style={{
                                width: '100%', height: '200px', 
                                borderWidth: '2px', borderColor: 'rgb(102, 102, 102)', 
                                borderStyle: 'dashed', borderRadius: '5px'
                                }}>
                                {this.props.surveyFileToBeImportedPreview ?
                                    <p style={{fontSize: '2em', textAlign: 'center'}}><span className="checked large" />{this.props.surveyFileToBeImportedPreview}</p>
                                    : <p>{L.l('forms.fileDropMessage', acceptedFileTypesDescription)}</p>
                                }
                            </Dropzone>
                        }
                    </Col>
                </FormGroup>
                    <div>
                        <FormGroup row>
                            <Label for="name">{L.l('survey.name')}:</Label>
                            <Col sm={10}>
                                {this.props.importingIntoExistingSurvey && <Label>{this.props.surveyBackupInfo.name}</Label>}
                                {!this.props.importingIntoExistingSurvey && 
                                    <Field
                                        name="name"
                                        type="text"
                                        label={L.l('survey.name')}
                                        normalize={Forms.normalizeInternalName}
                                    />
                                }
                            </Col>
                        </FormGroup>
                        <Field
                            name="userGroupId"
                            component={Forms.renderSelect}
                            label={L.l('survey.userGroup')}
                            options={userGroupOptions}
                        />
                    </div>
                }
            </Form>
        )
    }
}

const mapStateToProps = state => {
    const { items: userGroups } = state.userGroups
    const { uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded, surveyBackupInfo, importingIntoExistingSurvey } = state.surveys
    return {
        userGroups, 
        uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded, surveyBackupInfo, importingIntoExistingSurvey
    }
}

SurveyImportForm = connect(mapStateToProps)(SurveyImportForm)

export default reduxForm({
    form: 'surveyImportForm',
    asyncValidate,
    asyncBlurFields: ['name', 'userGroupId'],
    initialValues: {
        name: '',
        templateType: 'BLANK',
        defaultLanguageCode: 'en',
        userGroupId: ''
    }
})(SurveyImportForm)