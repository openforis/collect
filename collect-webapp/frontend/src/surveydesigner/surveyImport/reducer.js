import {
    UPLOADING_SURVEY_FILE, SURVEY_FILE_UPLOADED, SURVEY_FILE_UPLOAD_ERROR,
    SURVEY_FILE_IMPORT_STARTED, SURVEY_FILE_IMPORT_ERROR, SURVEY_FILE_IMPORTED, SURVEY_FILE_IMPORT_RESET
} from './actions'

const SURVEY_FILE_IMPORT_BASE_STATE = {
    uploadingSurveyFile: false,
    surveyFileToBeImported: null,
    surveyFileToBeImportedPreview: null,        
    surveyFileUploaded: false,
    surveyFileUploadError: false,
    surveyFileUploadErrorMessage: null,
    importingIntoExistingSurvey: false,
    surveyBackupInfo: null,
    surveyFileImportStarted: false,
    surveyFileImported: false,
    importedSurveyId: null,
    surveyFileImportErrorMessage: null,
    surveyFileImportFieldErrors: []
}

export default function (
    state = SURVEY_FILE_IMPORT_BASE_STATE,
    action
) {
    switch (action.type) {
        case UPLOADING_SURVEY_FILE:
            return Object.assign({}, state, {
                uploadingSurveyFile: true,
                surveyFileToBeImported: action.file,
                surveyFileToBeImportedPreview: action.filePreview,
                surveyFileUploaded: false,
                surveyBackupInfo: null,
                surveyFileImported: false,
                surveyFileImportErrorMessage: null,
                surveyFileImportFieldErrors: [],
                surveyFileImportStarted: false
            })
        case SURVEY_FILE_UPLOADED:
            return Object.assign({}, state, {
                uploadingSurveyFile: false,
                surveyFileUploaded: true,
                surveyBackupInfo: action.surveyBackupInfo,
                importingIntoExistingSurvey: action.importingIntoExistingSurvey,
                existingSurveyUserGroupId: action.existingSurveyUserGroupId,
            })
        case SURVEY_FILE_UPLOAD_ERROR:
            return Object.assign({}, state, {
                uploadingSurveyFile: false,
                surveyFileUploadError: true,
                surveyFileUploadErrorMessage: action.errorMessage
            })
        case SURVEY_FILE_IMPORT_STARTED:
            return Object.assign({}, state, {
                surveyFileImportStarted: true
            })
        case SURVEY_FILE_IMPORT_ERROR:
            return Object.assign({}, state, {
                surveyFileImportErrorMessage: action.errorMessage,
                surveyFileImportFieldErrors: action.fieldErrors
            })
        case SURVEY_FILE_IMPORTED:
            return Object.assign({}, state, {
                surveyFileImportStarted: false,
                surveyFileImported: true,
                importedSurveyId: action.importedSurveyId
            })
        case SURVEY_FILE_IMPORT_RESET:
            return Object.assign({}, state, SURVEY_FILE_IMPORT_BASE_STATE)
        default:
            return state
        }
    }