import T from 'i18n-react'
import Constants from 'Constants'
import Arrays from 'utils/Arrays'
import BrowserUtils from 'utils/BrowserUtils'
import * as LanguagesKeys from './LanguagesKeys'

export default class Labels {
  static SUPPORTED_LANG_CODES = ['en']
  static DEFAULT_LANG_CODE = 'en'
  static _ALL_TEXTS = {}

  static initialize(callback) {
    const browserLangCode = BrowserUtils.determineBrowserLanguageCode()
    const langCode = Arrays.contains(Labels.SUPPORTED_LANG_CODES, browserLangCode)
      ? browserLangCode
      : Labels.DEFAULT_LANG_CODE
    Labels.loadLabels(langCode, callback)
  }

  static loadLabels(langCode, callback) {
    const labelsFileLoaded = function (texts) {
      Object.assign(Labels._ALL_TEXTS, texts)

      const nextFilePrefix = filePrefixes.pop()
      if (nextFilePrefix) {
        fetchLabelFile(nextFilePrefix, langCode, labelsFileLoaded)
      } else {
        T.setTexts(Labels._ALL_TEXTS)
        callback()
      }
    }

    const fetchLabelFile = function (filePrefix, langCode, callback) {
      fetch(Constants.BASE_ASSETS_URL + 'locales/' + filePrefix + langCode + '.json?_v=' + Constants.APP_VERSION)
        .then((res) => res.json())
        .then((texts) => {
          callback(texts)
        })
    }

    const filePrefixes = ['labels', ...Object.values(LanguagesKeys.LABELS_PREFIX_BY_LANGUAGE_CODE_STANDARD)].map(
      (key) => `${key}_`
    )
    const nextFilePrefix = filePrefixes.pop()
    fetchLabelFile(nextFilePrefix, langCode, labelsFileLoaded)
  }

  static label(key, params) {
    const parameters = params ? (params instanceof Array ? params : [params]) : null
    return T.translate(key, parameters)
  }

  static keys(prefix) {
    return Object.keys(Labels._ALL_TEXTS[prefix])
  }

  static l(key, parameters) {
    return Labels.label(key, parameters)
  }
}
