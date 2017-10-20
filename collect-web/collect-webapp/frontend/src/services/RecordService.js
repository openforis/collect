import AbstractService from './AbstractService';
import { Record } from '../model/Record';

export default class RecordService extends AbstractService {

    fetchRecordSummaries(surveyId, rootEntityName, userId, filterOptions, sortFields) {
        return this.get('survey/' + surveyId + '/data/records/summary', {
            rootEntityName: rootEntityName,
            userId: userId,
            maxNumberOfRows: filterOptions.recordsPerPage,
            offset: (filterOptions.page - 1) * filterOptions.recordsPerPage,
            keyValues: filterOptions.keyValues,
            sortFields: sortFields
        });
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

    downloadBackupDataExportResult(surveyId) {
        this.downloadFile(this.BASE_URL + 'survey/' + surveyId + '/data/records/exportresult.collect-data')
    }

    updateOwner(record, owner) {
        return this.patchJson('survey/' + record.surveyId + '/data/records/' + record.id, {
            ownerId: owner == null ? null : owner.id
        })
    }

    delete(record) {
        return super.delete('survey/' + record.surveyId + '/data/records/' + record.id)
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
}