import AbstractService from './AbstractService'
import { Record } from 'model/Record'
import RecordSummary from 'model/RecordSummary'
import Workflow from 'model/Workflow'

export default class RecordService extends AbstractService {
  fetchRecordSummaries({ surveyId, rootEntityName, userId, filterOptions, sortFields = null, fullSummary = false }) {
    return this.get('survey/' + surveyId + '/data/records/summary', {
      rootEntityName,
      userId: userId,
      maxNumberOfRows: filterOptions.recordsPerPage,
      offset: filterOptions.page * filterOptions.recordsPerPage,
      keyValues: filterOptions.keyValues,
      summaryValues: filterOptions.summaryValues,
      ownerIds: filterOptions.ownerIds,
      sortFields,
      fullSummary,
      includeOwners: true,
    }).then((res) => {
      return { ...res, records: res.records.map((r) => new RecordSummary(r)) }
    })
  }

  fetchSurveyId(recordId) {
    return this.get(`data/records/${recordId}/surveyId`)
  }

  fetchById(survey, recordId, lock = false) {
    return this.get('survey/' + survey.id + '/data/records/' + recordId, { lock }).then(
      (res) => new Record(survey, res)
    )
  }

  releaseLock(surveyId, recordId) {
    return this.post(`survey/${surveyId}/data/records/releaselock/${recordId}`)
  }

  loadRecordsStats(survey) {
    return this.get('survey/' + survey.id + '/data/records/stats').then((res) => {
      return res === null
        ? null
        : {
            period: res.period === null ? null : [new Date(res.period[0]), new Date(res.period[1])],
            dailyStats: res.dailyStats,
            monthlyStats: res.monthlyStats,
            yearlyStats: res.yearlyStats,
          }
    })
  }

  createRecord({ surveyId, rootEntityName = null, versionId = null, step = Workflow.STEPS.entry, preview = false }) {
    return this.postJson('survey/' + surveyId + '/data/records', {
      rootEntityName,
      versionId,
      step,
      preview,
    })
  }

  startCSVDataExport(surveyId, parameters) {
    return this.postJson('survey/' + surveyId + '/data/records/startcsvexport', parameters)
  }

  downloadCSVDataExportResult(surveyId) {
    this.downloadFile(this.BASE_URL + 'survey/' + surveyId + '/data/records/csvexportresult.zip')
  }

  exportRecordToExcel(record) {
    const { survey, id: recordId, step } = record
    const { id: surveyId } = survey
    const stepNumber = Workflow.getStepNumber(step)
    this.downloadFile(
      `${this.BASE_URL}survey/${surveyId}/data/records/${recordId}/steps/${stepNumber}/content/xlsx/data.zip`
    )
  }

  exportRecordToCollectFormat(record) {
    const { survey, id: recordId } = record
    const { id: surveyId } = survey
    this.downloadFile(`${this.BASE_URL}survey/${surveyId}/data/records/${recordId}/content/collect/data.collect-data`)
  }

  startBackupDataExport(surveyId, parameters) {
    return this.postJson('survey/' + surveyId + '/data/records/startbackupexport', parameters)
  }

  getBackupDataExportJob(surveyId) {
    return this.get('survey/' + surveyId + '/data/records/backupexportjob')
  }

  downloadBackupDataExportResult(surveyId) {
    this.downloadFile(this.BASE_URL + 'survey/' + surveyId + '/data/records/exportresult.collect-data')
  }

  startValidationReport(surveyId, parameters) {
    return this.postJson('survey/' + surveyId + '/data/records/validationreport', parameters)
  }

  downloadValidationReportResult(surveyId) {
    this.downloadFile(this.BASE_URL + 'survey/' + surveyId + '/data/records/validationreport.csv')
  }

  updateOwner(record, owner) {
    return this.postJson('survey/' + record.surveyId + '/data/update/records/' + record.id, {
      ownerId: owner == null ? null : owner.id,
    })
  }

  delete(surveyId, userId, recordIds) {
    return super.delete('survey/' + surveyId + '/data/records', {
      userId,
      recordIds,
    })
  }

  generateBackupDataImportSummary(survey, rootEntityName, file, onError, onProgress) {
    return this.postFile(
      'survey/' + survey.id + '/data/import/records/summary',
      file,
      {
        rootEntityName,
      },
      onError,
      onProgress
    )
  }

  loadBackupDataImportSummary(survey) {
    return this.get('survey/' + survey.id + '/data/import/records/summary')
  }

  startBackupDataImportFromSummary(survey, entryIdsToImport, validateRecords) {
    return this.postFormData('survey/' + survey.id + '/data/import/records', {
      entryIdsToImport: entryIdsToImport,
      validateRecords: validateRecords,
    })
  }

  startCsvDataImport(
    survey,
    rootEntityName,
    file,
    importType,
    steps,
    entityDefinitionId,
    validateRecords,
    deleteEntitiesBeforeImport,
    newRecordVersionName
  ) {
    return this.postFormData('survey/' + survey.id + '/data/csvimport/records', {
      rootEntityName,
      file: file,
      importType,
      steps,
      entityDefinitionId,
      validateRecords,
      deleteEntitiesBeforeImport,
      newRecordVersionName,
    })
  }

  loadCsvDataImportStatus(survey) {
    return this.get('survey/' + survey.id + '/data/csvimport/records')
  }

  startRecordMoveJob(surveyId, fromStep, promote) {
    return this.post('survey/' + surveyId + '/data/move/records', {
      fromStep,
      promote,
    })
  }

  promoteRecord({ surveyId, recordId }) {
    return this.post(`survey/${surveyId}/data/records/promote/${recordId}`)
  }

  demoteRecord({ surveyId, recordId }) {
    return this.post(`survey/${surveyId}/data/records/demote/${recordId}`)
  }
}
