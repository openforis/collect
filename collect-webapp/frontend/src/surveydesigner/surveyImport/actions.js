import * as Forms from 'common/components/Forms'

import * as JobActions from 'actions/job'
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'

export const UPLOADING_SURVEY_FILE = 'UPLOADING_SURVEY_FILE'
export const SURVEY_FILE_UPLOAD_ERROR = 'SURVEY_FILE_UPLOAD_ERROR'
export const SURVEY_FILE_UPLOADED = 'SURVEY_FILE_UPLOADED'
export const SURVEY_FILE_IMPORT_STARTING = 'SURVEY_FILE_IMPORT_STARTING'
export const SURVEY_FILE_IMPORT_STARTED = 'SURVEY_FILE_IMPORT_STARTED'
export const SURVEY_FILE_IMPORTED = 'SURVEY_FILE_IMPORTED'
export const SURVEY_FILE_IMPORT_ERROR = 'SURVEY_FILE_IMPORT_ERROR'
export const SURVEY_FILE_IMPORT_RESET = 'SURVEY_FILE_IMPORT_RESET'

export const uploadSurveyFile = (file) => (dispatch) => {
  dispatch(uploadingSurveyFile(file))

  return ServiceFactory.surveyService
    .uploadSurveyFile(file)
    .then((res) => {
      if (res.statusError) {
        Forms.handleValidationResponse(res)
        dispatch(surveyFileUploadError(res.errorMessage))
      } else {
        const { surveyBackupInfo, importingIntoExistingSurvey, existingSurveyUserGroupId } = res.objects
        dispatch(surveyFileUploaded(surveyBackupInfo, importingIntoExistingSurvey, existingSurveyUserGroupId))
      }
    })
    .catch((error) => {
      dispatch(surveyFileUploadError(L.l('survey.import.error.errorUploadingFile', error.toString())))
    })
}

const uploadingSurveyFile = (file) => ({
  type: UPLOADING_SURVEY_FILE,
  file,
  filePreview: file.name,
})

const surveyFileUploadError = (errorMessage) => ({
  type: SURVEY_FILE_UPLOAD_ERROR,
  errorMessage,
})

const surveyFileUploaded = (surveyBackupInfo, importingIntoExistingSurvey, existingSurveyUserGroupId) => ({
  type: SURVEY_FILE_UPLOADED,
  surveyBackupInfo,
  importingIntoExistingSurvey,
  existingSurveyUserGroupId,
})

export const startSurveyFileImport = (surveyName, userGroupId) => (dispatch) => {
  dispatch(surveyFileImportStarting(surveyName, userGroupId))

  return ServiceFactory.surveyService.startSurveyFileImport(surveyName, userGroupId).then((res) => {
    if (res.statusError) {
      Forms.handleValidationResponse(res)
      dispatch(surveyFileImportError(res.errorMessage, res.objects.errors))
    } else {
      const job = res.object
      dispatch(
        JobActions.startJobMonitor({
          jobId: job.id,
          handleJobCompleted: () => {
            ServiceFactory.surveyService.fetchSurveyImportStatus().then((importJob) => {
              const importedSurveyId = importJob.surveyId
              dispatch(surveyFileImported(importedSurveyId))
            })
          },
        })
      )
      dispatch(surveyFileImportStarted())
    }
  })
}

const surveyFileImportStarting = (surveyName, userGroupId) => ({
  type: SURVEY_FILE_IMPORT_STARTING,
  surveyName,
  userGroupId,
})

const surveyFileImportStarted = (importJob) => ({
  type: SURVEY_FILE_IMPORT_STARTED,
  importJob,
})

const surveyFileImportError = (errorMessage, fieldErrors) => ({
  type: SURVEY_FILE_IMPORT_ERROR,
  errorMessage,
  fieldErrors,
})

export const surveyFileImported = (importedSurveyId) => ({
  type: SURVEY_FILE_IMPORTED,
  importedSurveyId,
})

export const resetSurveyFileImport = () => ({
  type: SURVEY_FILE_IMPORT_RESET,
})
