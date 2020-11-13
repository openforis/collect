import AbstractService from './AbstractService'

export default class CodeListService extends AbstractService {
  countAvailableItems({ surveyId, codeListId, versionId, ancestorCodes }) {
    return this.postJson(`survey/${surveyId}/codelist/${codeListId}/validitems/count`, {
      versionId,
      ancestorCodes,
    })
  }

  loadAllAvailableItems({ surveyId, codeListId, versionId, language, ancestorCodes }) {
    return this.postJson(`survey/${surveyId}/codelist/${codeListId}/validitems`, {
      versionId,
      language,
      ancestorCodes,
    })
  }

  findAvailableItems({ surveyId, codeListId, versionId, language, ancestorCodes, searchString }) {
    return this.postJson(`survey/${surveyId}/codelist/${codeListId}/finditems`, {
      versionId,
      language,
      ancestorCodes,
      searchString,
    })
  }

  loadItem({ surveyId, codeListId, versionId, language, ancestorCodes, code }) {
    return this.postJson(`survey/${surveyId}/codelist/${codeListId}/item`, {
      versionId,
      language,
      ancestorCodes,
      searchString: code,
    })
  }
}
