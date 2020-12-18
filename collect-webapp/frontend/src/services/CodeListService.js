import AbstractService from './AbstractService'
import AsyncCache from 'model/AsyncCache'

const CacheType = {
  COUNT: 'count',
  ITEMS: 'items',
}

export default class CodeListService extends AbstractService {
  cachesByType = {}

  countAvailableItems({ surveyId, codeListId, versionId, ancestorCodes }) {
    const cache = this._getCache({ type: CacheType.COUNT, surveyId })
    return cache.getItem({
      fetchFunction: this._countAvailableItems.bind(this),
      params: { surveyId, codeListId, versionId, ancestorCodes },
    })
  }

  _countAvailableItems({ surveyId, codeListId, versionId, ancestorCodes }) {
    return this.postJson(`survey/${surveyId}/codelist/${codeListId}/validitems/count`, {
      versionId,
      ancestorCodes,
    })
  }

  loadAllAvailableItems({ surveyId, codeListId, versionId, language, ancestorCodes }) {
    const cache = this._getCache({ type: CacheType.ITEMS, surveyId })
    return cache.getItem({
      fetchFunction: this._loadAllAvailableItems.bind(this),
      params: { surveyId, codeListId, versionId, language, ancestorCodes },
    })
  }

  _loadAllAvailableItems({ surveyId, codeListId, versionId, language, ancestorCodes }) {
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

  invalidateCache({ surveyId }) {
    Object.values(this.cachesByType).forEach((caches) => {
      delete caches[surveyId]
    })
  }

  _getCache({ type, surveyId }) {
    let caches = this.cachesByType[type]
    if (!caches) {
      caches = {}
      this.cachesByType[type] = caches
    }
    let cache = caches[surveyId]
    if (!cache) {
      cache = new AsyncCache()
      caches[surveyId] = cache
    }
    return cache
  }
}
