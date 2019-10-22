import React, { Component } from 'react'
import { Formik } from 'formik'
import { Alert, Button, Form, FormGroup, Row, Col, Progress } from 'reactstrap'
import { connect } from 'react-redux'

import Dropzone from 'common/components/Dropzone'
import Forms, { TextFormItem, SelectFormItem } from 'common/components/Forms'
import * as SurveyImportActions from 'surveydesigner/surveyImport/actions'
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'

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

    componentWillUnmount() {
        this.props.dispatch(SurveyImportActions.resetSurveyFileImport())
    }

    submit(values) {
        this.props.dispatch(SurveyImportActions.startSurveyFileImport(values.name, values.userGroupId))
    }

    handleFileDrop(file) {
        this.props.dispatch(SurveyImportActions.uploadSurveyFile(file))
    }

    render() {
        const { userGroups, error, handleSubmit, submitting,
            uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded, importingIntoExistingSurvey,
            surveyFileUploadError, surveyFileUploadErrorMessage } = this.props

        const userGroupOptions = [<option key="-1" value="">{L.l('forms.selectOne')}</option>].concat(userGroups.map(g => <option key={g.id} value={g.id}>{g.label}</option>))

        return (
            <Formik onSubmit={this.submit}>
                {({
                    values,
                    errors,
                    touched,
                    handleChange,
                    handleBlur,
                    handleSubmit,
                    isSubmitting,
                }) => (
                        <form onSubmit={handleSubmit}>
                            <FormGroup row>
                                <Col>
                                    {!uploadingSurveyFile &&
                                        <Dropzone
                                            acceptedFileTypes={SURVEY_IMPORT_ACCEPTED_FILE_TYPES}
                                            acceptedFileTypesDescription={L.l('survey.import.acceptedFileTypesDescription')}
                                            handleFileDrop={this.handleFileDrop}
                                            height="300px"
                                            fileToBeImportedPreview={surveyFileToBeImportedPreview} />
                                    }
                                    {uploadingSurveyFile &&
                                        <Progress animated value={100} />
                                    }
                                    {surveyFileUploadError &&
                                        <span className="error">{L.l(surveyFileUploadErrorMessage)}</span>
                                    }
                                </Col>
                            </FormGroup>
                            {surveyFileUploaded &&
                                <div className="animated fade-in">
                                    {importingIntoExistingSurvey &&
                                        <Alert color="warning">{L.l('survey.import.importingIntoExistingSurveyWarning')}</Alert>
                                    }
                                    <TextFormItem
                                        name="name"
                                        type="text"
                                        contentEditable={!importingIntoExistingSurvey}
                                        label={L.l('survey.identifier')}
                                        normalize={Forms.normalizeInternalName}
                                    />
                                    <TextFormItem
                                        name="userGroupId"
                                        contentEditable={!importingIntoExistingSurvey}
                                        label={L.l('survey.userGroup')}
                                        options={userGroupOptions}
                                    />
                                </div>
                            }
                            {errors && errors.length > 0 && <Alert color="danger">{errors}</Alert>}
                            <Row>
                                <Col xs={{ offset: 6 }}>
                                    <Button color="primary" type="submit" disabled={submitting}>{L.l('general.import')}</Button>
                                </Col>
                            </Row>
                        </form>
                    )}
            </Formik>
        )
    }
}

const mapStateToProps = state => {
    const { items: userGroups } = state.userGroups
    const { uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded, surveyBackupInfo,
        importingIntoExistingSurvey, surveyFileImported, importedSurveyId, surveyFileUploadError, surveyFileUploadErrorMessage } = state.surveyDesigner.surveyImport
    return {
        userGroups,
        uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded, surveyBackupInfo,
        importingIntoExistingSurvey, surveyFileImported, importedSurveyId, surveyFileUploadError, surveyFileUploadErrorMessage
    }
}

//SurveyImportForm = connect(mapStateToProps)(SurveyImportForm)

export default connect(mapStateToProps)(SurveyImportForm)
/*
export default reduxForm({
    form: 'surveyImportForm',
    asyncValidate,
    asyncBlurFields: ['name', 'userGroupId'],
    initialValues: {
        userGroupId: ''
    }
})(SurveyImportForm)
*/