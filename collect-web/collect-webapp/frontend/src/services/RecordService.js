import AbstractService from './AbstractService';
import { Record } from '../model/Record';

export default class RecordService extends AbstractService {

    fetchRecordSummaries(surveyId, recordsPerPage, page, rootEntityName, keyValues, sortFields) {
        return this.get('survey/' + surveyId + '/data/records/summary', {
            maxNumberOfRows: recordsPerPage,
            offset: (page - 1) * recordsPerPage,
            rootEntityName: rootEntityName,
            keyValues: keyValues,
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

    updateOwner(record, owner) {
        return this.patchJson('survey/' + record.surveyId + '/data/records/' + record.id, {
            ownerId: owner == null ? null : owner.id
        })
    }
}