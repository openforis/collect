import AbstractService from './AbstractService'

export default class QueryService extends AbstractService {

    getQueryResult(surveyId, query, page, recordsPerPage, sortBy) {
        return this.postJson('survey/'+ surveyId + '/data/query', query)
    }
}