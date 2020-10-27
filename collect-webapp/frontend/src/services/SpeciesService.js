import AbstractService from './AbstractService'

export default class SpeciesService extends AbstractService {
  findTaxa({ surveyId, taxonomyName, query }) {
    return this.postJson(`survey/${surveyId}/taxonomy/${taxonomyName}/query`, query)
  }
}
