import AbstractService from './AbstractService'
import { Survey } from '../model/Survey'

export default class SurveyService extends AbstractService {
  fetchById(surveyId, langCode = 'en') {
    const preferredLanguage = langCode || 'en'
    return this.get(`survey/${surveyId}?langCode=${preferredLanguage}&includeCodeListValues=false`).then((res) => {
      const survey = new Survey(res)
      survey.preferredLanguage = preferredLanguage
      return survey
    })
  }

  createNewSurvey(surveyFormObj) {
    return this.post('survey', surveyFormObj)
  }

  validateSurveyCreation(surveyFormObj) {
    return this.post('survey/validatecreation', surveyFormObj)
  }

  changeUserGroup(surveyName, userGroupId, loggedUserId) {
    return this.post(`survey/${surveyName}/changeusergroup`, {
      userGroupId,
      loggedUserId,
    })
  }

  createTemporarySurvey(publishedSurveyId) {
    return this.post('survey/cloneintotemporary/' + publishedSurveyId)
  }

  fetchAllSummaries() {
    return this.get('survey', {
      includeTemporary: true,
    })
  }

  uploadSurveyFile(file) {
    return this.postFormData('survey/prepareimport', {
      file: file,
    })
  }

  validateSurveyImport(name, userGroupId) {
    return this.post('survey/validateimport', {
      name,
      userGroupId,
    })
  }

  startSurveyFileImport(name, userGroupId) {
    return this.post('survey/startimport', {
      name,
      userGroupId,
    })
  }

  fetchSurveyImportStatus() {
    return this.get('survey/importstatus')
  }

  startExport({
    surveyId,
    surveyUri,
    surveyType,
    outputFormat,
    languageCode,
    skipValidation,
    includeData,
    rdbDialect,
    rdbTargetSchemaName,
    rdbDateTimeFormat,
  }) {
    return this.post('survey/export/' + surveyId, {
      surveyId,
      surveyUri,
      surveyType,
      outputFormat,
      languageCode,
      skipValidation,
      includeData,
      rdbDialect,
      rdbTargetSchemaName,
      rdbDateTimeFormat,
    })
  }

  downloadExportResult(surveyId) {
    return this.downloadFile(this.BASE_URL + 'survey/export/' + surveyId + '/result')
  }

  publish(surveyId, ignoreWarnings = false) {
    return this.post('survey/publish/' + surveyId, {
      ignoreWarnings,
    })
  }

  unpublish(surveyId) {
    return this.post('survey/unpublish/' + surveyId)
  }

  delete(surveyId) {
    return this.post('survey/delete/' + surveyId)
  }

  startClone(originalSurveyName, originalSurveyType, newSurveyName) {
    return this.post('survey/clone', {
      originalSurveyName: originalSurveyName,
      originalSurveyType: originalSurveyType,
      newSurveyName: newSurveyName,
    })
  }

  getClonedSurveyId() {
    return this.get('survey/cloned/id')
  }

  validateClone(originalSurveyName, originalSurveyType, newSurveyName) {
    return this.post('survey/validate/clone', {
      originalSurveyName: originalSurveyName,
      originalSurveyType: originalSurveyType,
      newSurveyName: newSurveyName,
    })
  }
}
