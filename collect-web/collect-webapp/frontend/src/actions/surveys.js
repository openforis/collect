import { change } from 'redux-form';

import Forms from 'components/Forms';
import Dialogs from 'components/Dialogs';

import ServiceFactory from 'services/ServiceFactory';

import L from 'utils/Labels';

import * as JobActions from 'actions/job';

export const REQUEST_SURVEY_SUMMARIES = 'REQUEST_SURVEY_SUMMARIES'
export const RECEIVE_SURVEY_SUMMARIES = 'RECEIVE_SURVEY_SUMMARIES'
export const INVALIDATE_SURVEY_SUMMARIES = 'INVALIDATE_SURVEY_SUMMARIES'
export const CHANGE_SURVEY_USER_GROUP = 'CHANGE_SURVEY_USER_GROUP'
export const REQUEST_SURVEY_USER_GROUP_CHANGE = 'REQUEST_SURVEY_USER_GROUP_CHANGE'
export const SURVEY_USER_GROUP_CHANGED = 'SURVEY_USER_GROUP_CHANGED'
export const REQUEST_CREATE_NEW_SURVEY = 'REQUEST_CREATE_NEW_SURVEY'
export const NEW_SURVEY_CREATED = 'NEW_SURVEY_CREATED'
export const SURVEY_CREATION_ERROR = 'SURVEY_CREATION_ERROR'
export const RESET_NEW_SURVEY_FORM = 'RESET_NEW_SURVEY_FORM'
export const UPLOADING_SURVEY_FILE = 'UPLOADING_SURVEY_FILE'
export const SURVEY_FILE_UPLOAD_ERROR = 'SURVEY_FILE_UPLOAD_ERROR'
export const SURVEY_FILE_UPLOADED = 'SURVEY_FILE_UPLOADED'
export const SURVEY_FILE_IMPORT_STARTING = 'SURVEY_FILE_IMPORT_STARTING'
export const SURVEY_FILE_IMPORT_STARTED = 'SURVEY_FILE_IMPORT_STARTED'
export const SURVEY_FILE_IMPORTED = 'SURVEY_FILE_IMPORTED'
export const SURVEY_FILE_IMPORT_ERROR = 'SURVEY_FILE_IMPORT_ERROR'
export const SURVEY_FILE_IMPORT_RESET = 'SURVEY_FILE_IMPORT_RESET'
export const SURVEY_CREATED = 'SURVEY_CREATED'
export const SURVEY_UPDATED = 'SURVEY_UPDATED'
export const SURVEY_DELETED = 'SURVEY_DELETED'

const SURVEY_IMPORT_FORM_NAME = 'surveyImportForm'

const dispatchSurveyUpdated = (dispatch, surveySummary) =>
    dispatch({ type: SURVEY_UPDATED, surveySummary })

function requestSurveySummaries() {
    return {
        type: REQUEST_SURVEY_SUMMARIES
    }
}

function receiveSurveySummaries(json) {
    return {
        type: RECEIVE_SURVEY_SUMMARIES,
        summaries: json.map(s => s), //TODO map into Survey object
        receivedAt: Date.now()
    }
}

export function fetchSurveySummaries() {
    return function (dispatch) {
        dispatch(requestSurveySummaries())
        try {
            ServiceFactory.surveyService.fetchAllSummaries().then(json =>
                dispatch(receiveSurveySummaries(json))
            )
        } catch (e) { }
    }
}

export const publishSurvey = (survey) =>
    dispatch => {
        ServiceFactory.surveyService.publish(survey.id)
            .then(s => {
                Dialogs.alert(L.l('survey.publish.successDialog.title'),
                    L.l('survey.publish.successDialog.message', survey.name))
                //survey update managed by WebSocket
                //dispatchSurveyUpdated(dispatch, s)
            })
    }

export const unpublishSurvey = survey =>
    dispatch => {
        const surveyId = survey.temporary ? survey.publishedId : survey.id
        ServiceFactory.surveyService.unpublish(surveyId).then(s => {
            Dialogs.alert(L.l('survey.unpublish.successDialog.title'),
                L.l('survey.unpublish.successDialog.message', survey.name))
            //survey update managed by WebSocket
            //dispatchSurveyUpdated(dispatch, s)
        })

    }

function requestUserGroupChange() {
    return {
        type: REQUEST_SURVEY_USER_GROUP_CHANGE
    }
}

export function changeUserGroup(surveySummary, newUserGroupId, loggedUserId) {
    return function (dispatch) {
        dispatch(requestUserGroupChange())
        ServiceFactory.surveyService.changeUserGroup(surveySummary.name, newUserGroupId, loggedUserId)
            .then(s => {
                //survey update managed by WebSocket
                //dispatchSurveyUpdated(dispatch, s)
            })
    }
}

export function invalidateSurveySummaries(summaries) {
    return {
        type: INVALIDATE_SURVEY_SUMMARIES,
        summaries
    }
}

function requestCreateNewSurvey(name, template, defaultLanguageCode, userGroupId) {
    return {
        type: REQUEST_CREATE_NEW_SURVEY,
        name: name,
        template: template,
        defaultLanguageCode: defaultLanguageCode,
        userGroupId: userGroupId
    }
}

function surveyCreationError(errors) {
    return {
        type: SURVEY_CREATION_ERROR,
        errors: errors
    }
}

export function createNewSurvey(name, template, defaultLanguageCode, userGroupId) {
    return function (dispatch) {
        dispatch(requestCreateNewSurvey(name, template, defaultLanguageCode, userGroupId))
        return ServiceFactory.surveyService.createNewSurvey(name, template, defaultLanguageCode, userGroupId).then(res => {
            if (res.statusError) {
                Forms.handleValidationResponse(res)
                dispatch(surveyCreationError(res.objects.errors))
            } else {
                dispatch(newSurveyCreated(res.object))
            }
        })
    }
}

export function newSurveyCreated(newSurveySummary) {
    return {
        type: NEW_SURVEY_CREATED,
        newSurveySummary
    }
}

export function resetNewSurveyForm() {
    return {
        type: RESET_NEW_SURVEY_FORM
    }
}

export function uploadSurveyFile(file) {
    return function (dispatch) {
        dispatch(uploadingSurveyFile(file))
        return ServiceFactory.surveyService.uploadSurveyFile(file).then(res => {
            if (res.statusError) {
                Forms.handleValidationResponse(res)
                dispatch(surveyFileUploadError(res.errorMessage))
            } else {
                const backupInfo = res.objects.surveyBackupInfo
                const importingIntoExistingSurvey = res.objects.importingIntoExistingSurvey
                dispatch(change(SURVEY_IMPORT_FORM_NAME, 'name', backupInfo.surveyName))
                if (importingIntoExistingSurvey) {
                    dispatch(change(SURVEY_IMPORT_FORM_NAME, 'userGroupId', res.objects.existingSurveyUserGroupId))
                }
                dispatch(surveyFileUploaded(backupInfo, importingIntoExistingSurvey))
            }
        })
    }
}

function uploadingSurveyFile(file) {
    return {
        type: UPLOADING_SURVEY_FILE,
        file: file,
        filePreview: file.name
    }
}

function surveyFileUploadError(errorMessage) {
    return {
        type: SURVEY_FILE_UPLOAD_ERROR,
        errorMessage: errorMessage
    }
}

function surveyFileUploaded(surveyBackupInfo, importingIntoExistingSurvey) {
    return {
        type: SURVEY_FILE_UPLOADED,
        surveyBackupInfo: surveyBackupInfo,
        importingIntoExistingSurvey: importingIntoExistingSurvey
    }
}

export function startSurveyFileImport(surveyName, userGroupId) {
    return function (dispatch) {
        dispatch(surveyFileImportStarting(surveyName, userGroupId))
        return ServiceFactory.surveyService.startSurveyFileImport(surveyName, userGroupId).then(res => {
            if (res.statusError) {
                Forms.handleValidationResponse(res)
                dispatch(surveyFileImportError(res.errorMessage, res.objects.errors))
            } else {
                const job = res.object
                dispatch(JobActions.startJobMonitor({
                    jobId: job.id,
                    handleJobCompleted: () => {
                        ServiceFactory.surveyService.fetchSurveyImportStatus().then(importJob => {
                            const importedSurveyId = importJob.surveyId
                            dispatch(surveyFileImported(importedSurveyId))
                        })
                    }
                }))
                dispatch(surveyFileImportStarted())
            }
        })
    }
}

function surveyFileImportStarting(surveyName, userGroupId) {
    return {
        type: SURVEY_FILE_IMPORT_STARTING,
        surveyName: surveyName,
        userGroupId: userGroupId
    }
}

function surveyFileImportStarted(importJob) {
    return {
        type: SURVEY_FILE_IMPORT_STARTED,
        importJob: importJob
    }
}

function surveyFileImportError(errorMessage, fieldErrors) {
    return {
        type: SURVEY_FILE_IMPORT_ERROR,
        errorMessage: errorMessage,
        fieldErrors: fieldErrors
    }
}

export function surveyFileImported(importedSurveyId) {
    return {
        type: SURVEY_FILE_IMPORTED,
        importedSurveyId: importedSurveyId
    }
}

export function resetSurveyFileImport() {
    return {
        type: SURVEY_FILE_IMPORT_RESET
    }
}

export function deleteSurvey(survey) {
    return function (dispatch) {
        ServiceFactory.surveyService.delete(survey.id).then(r => {
            Dialogs.alert(L.l('survey.delete.success.title'), L.l('survey.delete.success.message', survey.name))
            dispatch(surveyDeleted(survey))
        })
    }
}

export function surveyCreated(surveySummary) {
    return {
        type: SURVEY_CREATED,
        surveySummary
    }
}

export function surveyUpdated(surveySummary) {
    return {
        type: SURVEY_UPDATED,
        surveySummary
    }
}

export function surveyDeleted(surveySummary) {
    return {
        type: SURVEY_DELETED,
        surveySummary
    }
}