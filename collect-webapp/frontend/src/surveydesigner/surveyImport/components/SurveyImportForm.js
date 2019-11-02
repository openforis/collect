import React, { useEffect } from 'react'
import { compose } from "redux"
import { connect } from 'react-redux'
import { withFormik } from 'formik'
import { Alert, Form, FormGroup, Row, Col, Progress } from 'reactstrap'

import Dropzone from 'common/components/Dropzone'
import { TextFormItem, SelectFormItem, SubmitButton, normalizeInternalName, asyncValidate } from 'common/components/Forms'
import { startSurveyFileImport, resetSurveyFileImport, uploadSurveyFile } from 'surveydesigner/surveyImport/actions'
import ServiceFactory from 'services/ServiceFactory'
import User from 'model/User'
import L from 'utils/Labels'

const SURVEY_IMPORT_ACCEPTED_FILE_TYPES = ".collect,.collect-backup,.cep,.xml"

const SurveyImportForm = props => {
    const {
        userGroups,
        handleSubmit, handleChange,
        uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded, importingIntoExistingSurvey,
        surveyFileUploadError, surveyFileUploadErrorMessage,
        resetSurveyFileImport, uploadSurveyFile
    } = props

    useEffect(() => {
        return () => resetSurveyFileImport()
    }, [])

    const userGroupOptions = [<option key="-1" value="">{L.l('forms.selectOne')}</option>].concat(userGroups.map(g => <option key={g.id} value={g.id}>{g.label}</option>))

    return (
        <Form onSubmit={handleSubmit}>
            <FormGroup row>
                <Col>
                    {!uploadingSurveyFile &&
                        <Dropzone
                            acceptedFileTypes={SURVEY_IMPORT_ACCEPTED_FILE_TYPES}
                            acceptedFileTypesDescription={L.l('survey.import.acceptedFileTypesDescription')}
                            handleFileDrop={file => uploadSurveyFile(file)}
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
                        readOnly={importingIntoExistingSurvey}
                        label={L.l('survey.identifier')}
                        {...props}
                        handleChange={e => {
                            e.target.value = normalizeInternalName(e.target.value)
                            handleChange(e)
                        }}
                    />
                    <SelectFormItem
                        name="userGroupId"
                        disabled={importingIntoExistingSurvey}
                        label={L.l('survey.userGroup')}
                        {...props}
                    >{userGroupOptions}</SelectFormItem>
                    <Row>
                        <Col xs={{ offset: 6 }}>
                            <SubmitButton {...props}>{L.l('general.import')}</SubmitButton>
                        </Col>
                    </Row>
                </div>
            }

        </Form>
    )
}


const mapStateToProps = state => {
    const { items: userGroups } = state.userGroups
    const {
        uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded,
        surveyBackupInfo, importingIntoExistingSurvey, existingSurveyUserGroupId,
        surveyFileImported, importedSurveyId, surveyFileUploadError, surveyFileUploadErrorMessage
    } = state.surveyDesigner.surveyImport
    return {
        userGroups,
        uploadingSurveyFile, surveyFileToBeImportedPreview, surveyFileUploaded, surveyBackupInfo,
        importingIntoExistingSurvey, surveyFileImported, importedSurveyId, existingSurveyUserGroupId,
        surveyFileUploadError, surveyFileUploadErrorMessage
    }
}

const mapPropsToValues = props => {
    const { surveyBackupInfo, importingIntoExistingSurvey, existingSurveyUserGroupId, userGroups } = props
    const defaultPublicGroup = userGroups.find(userGroup => userGroup.name === User.DEFAULT_PUBLIC_GROUP_NAME)
    return {
        name: surveyBackupInfo ? normalizeInternalName(surveyBackupInfo.surveyName) : '',
        userGroupId: importingIntoExistingSurvey ? existingSurveyUserGroupId : defaultPublicGroup ? defaultPublicGroup.id : ''
    }
}

export default compose(
    connect(mapStateToProps, { startSurveyFileImport, resetSurveyFileImport, uploadSurveyFile }),
    withFormik({
        enableReinitialize: true,
        mapPropsToValues,
        validate: values => asyncValidate(ServiceFactory.surveyService.validateSurveyImport.bind(ServiceFactory.surveyService))(values.name, values.userGroupId),
        handleSubmit: (values, { props }) => props.startSurveyFileImport(values.name, values.userGroupId)
    })
)(SurveyImportForm)