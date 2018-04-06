import AbstractService from './AbstractService';
import { Record } from 'model/Record';
import RecordSummary from 'model/RecordSummary';

export default class RecordService extends AbstractService {

    fetchRecordSummaries(surveyId, rootEntityName, userId, filterOptions, sortFields, fullSummary=false) {
        return this.get('survey/' + surveyId + '/data/records/summary', {
            rootEntityName: rootEntityName,
            userId: userId,
            maxNumberOfRows: filterOptions.recordsPerPage,
            offset: (filterOptions.page - 1) * filterOptions.recordsPerPage,
            keyValues: filterOptions.keyValues,
            summaryValues: filterOptions.summaryValues,
            ownerIds: filterOptions.ownerIds,
            sortFields: sortFields,
            fullSummary: fullSummary,
            includeOwners: true
        }).then(res => {return {...res, records: res.records.map(r => new RecordSummary(r))}});
    }

    fetchSurveyId(recordId) {
        return this.get('data/records/' + recordId + '/surveyId')
    }

    fetchById(survey, recordId) {
        return this.get('survey/' + survey.id + '/data/records/' + recordId).then(res => new Record(survey, res))
    }

    loadRecordsStats(survey) {
        return this.get('survey/' + survey.id + '/data/records/stats').then(res => {
            return res === null ? null : {
                period: res.period === null ? null : [new Date(res.period[0]), new Date(res.period[1])],
                dailyStats: res.dailyStats,
                monthlyStats: res.monthlyStats,
                yearlyStats: res.yearlyStats
            }
        })
    }

    createRecord(surveyId, rootEntityName = null, versionName = null) {
        return this.postJson('survey/' + surveyId + '/data/records', {
            rootEntityName: rootEntityName,
            versionName: versionName
        })
    }

    startCSVDataExport(surveyId, parameters) {
        return this.postJson('survey/' + surveyId + '/data/records/startcsvexport', parameters)
    }

    downloadCSVDataExportResult(surveyId) {
        this.downloadFile(this.BASE_URL + 'survey/' + surveyId + '/data/records/csvexportresult.zip')
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
            ownerId: owner == null ? null : owner.id
        })
    }

    delete(surveyId, userId, recordIds) {
        return super.delete('survey/' + surveyId + '/data/records', {
            userId: userId,
            recordIds: recordIds
        })
    }

    generateBackupDataImportSummary(survey, rootEntityName, file) {
        return this.postFormData('survey/' + survey.id + '/data/import/records/summary', {
            rootEntityName: rootEntityName,
            file: file
        })
    }

    loadBackupDataImportSummary(survey) {
        return this.get('survey/' + survey.id + '/data/import/records/summary')
    }

    startBackupDataImportFromSummary(survey, entryIdsToImport, validateRecords) {
        return this.postFormData('survey/' + survey.id + '/data/import/records', {
            entryIdsToImport: entryIdsToImport,
            validateRecords: validateRecords
        })
    }

    startCsvDataImport(survey, rootEntityName, file, importType, steps, entityDefinitionId, 
            validateRecords, deleteEntitiesBeforeImport, newRecordVersionName) {
        return this.postFormData('survey/' + survey.id + '/data/csvimport/records', {
            rootEntityName: rootEntityName,
            file: file,
            importType: importType,
            steps: steps,
            entityDefinitionId: entityDefinitionId,
            validateRecords: validateRecords,
            deleteEntitiesBeforeImport: deleteEntitiesBeforeImport,
            newRecordVersionName: newRecordVersionName
        })
    }

    loadCsvDataImportStatus(survey) {
        return this.get('survey/' + survey.id + '/data/csvimport/records')
    }

    startRecordMoveJob(surveyId, fromStep, promote) {
        return this.post('survey/' + surveyId + '/data/move/records', {
            fromStep: fromStep,
            promote: promote
        })
    }
}