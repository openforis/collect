//import { change } from 'react-final-form'

import Forms from 'common/components/Forms'

import * as JobActions from 'actions/job'
import ServiceFactory from 'services/ServiceFactory'

export const UPLOADING_SURVEY_FILE = 'UPLOADING_SURVEY_FILE'
export const SURVEY_FILE_UPLOAD_ERROR = 'SURVEY_FILE_UPLOAD_ERROR'
export const SURVEY_FILE_UPLOADED = 'SURVEY_FILE_UPLOADED'
export const SURVEY_FILE_IMPORT_STARTING = 'SURVEY_FILE_IMPORT_STARTING'
export const SURVEY_FILE_IMPORT_STARTED = 'SURVEY_FILE_IMPORT_STARTED'
export const SURVEY_FILE_IMPORTED = 'SURVEY_FILE_IMPORTED'
export const SURVEY_FILE_IMPORT_ERROR = 'SURVEY_FILE_IMPORT_ERROR'
export const SURVEY_FILE_IMPORT_RESET = 'SURVEY_FILE_IMPORT_RESET'

const SURVEY_IMPORT_FORM_NAME = 'surveyImportForm'

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
                //dispatch(change(SURVEY_IMPORT_FORM_NAME, 'name', backupInfo.surveyName))
                if (importingIntoExistingSurvey) {
                    //dispatch(change(SURVEY_IMPORT_FORM_NAME, 'userGroupId', res.objects.existingSurveyUserGroupId))
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