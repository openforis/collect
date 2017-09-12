import AbstractService from './AbstractService';
import { Record } from '../model/Record';

export default class RecordService extends AbstractService {

    fetchRecordSummaries(surveyId, recordsPerPage, page) {
        return this.get('survey/' + surveyId + '/data/records/summary.json', {
            maxNumberOfRows: recordsPerPage,
            offset: (page - 1) * recordsPerPage,
            //sorting: state.sorting,
            //filtering: state.filtering
        });
    }

    fetchSurveyId(recordId) {
        return this.get('data/records/' + recordId + '/surveyId')
    }

    fetchById(survey, recordId) {
        return this.get('survey/' + survey.id + '/data/records/' + recordId).then(res => new Record(survey, res))
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