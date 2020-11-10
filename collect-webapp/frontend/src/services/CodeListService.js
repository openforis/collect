import AbstractService from './AbstractService'

export default class CodeListService extends AbstractService {
  countAvailableItems({ surveyId, codeListId, versionId, ancestorCodes }) {
    return this.postJson(`survey/${surveyId}/codelist/${codeListId}/validitems/count`, {
      versionId,
      ancestorCodes,
    })
  }

  loadAllAvailableItems({ surveyId, codeListId, versionId, ancestorCodes }) {
    return this.postJson(`survey/${surveyId}/codelist/${codeListId}/validitems`, {
      versionId,
      ancestorCodes,
    })
  }

  findAvailableItems({ surveyId, codeListId, versionId, language, ancestorCodes, searchString }) {
    return this.postJson(`survey/${surveyId}/codelist/${codeListId}/finditems`, {
      versionId,
      ancestorCodes,
      language,
      searchString,
    })
  }

  loadItem({ surveyId, codeListId, versionId, ancestorCodes, code }) {
    return this.postJson(`survey/${surveyId}/codelist/${codeListId}/item`, {
      versionId,
      ancestorCodes,
      code,
    })
  }
}
