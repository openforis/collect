import AbstractService from './AbstractService'

export default class QueryService extends AbstractService {

    getQueryResult(surveyId, query, page, recordsPerPage, sortBy) {
        return this.get('survey/'+ surveyId + '/data/query', query)
    }
}