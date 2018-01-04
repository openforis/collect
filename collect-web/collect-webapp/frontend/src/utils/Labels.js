import T from 'i18n-react';
import Constants from 'Constants'
import Arrays from 'utils/Arrays'

export default class Labels {

    static SUPPORTED_LANG_CODES = ['en']
    static DEFAULT_LANG_CODE = 'en'
    static _ALL_TEXTS = {}

    static initialize(callback) {
        const browserLangCode = Labels._determineBrowserLanguageCode()
        const langCode = Arrays.contains(Labels.SUPPORTED_LANG_CODES, browserLangCode) ? 
            browserLangCode : Labels.DEFAULT_LANG_CODE
        Labels.loadLabels(langCode, callback)
    }

    static _determineBrowserLanguageCode() {
        const locale = navigator.language || (navigator.languages && navigator.languages[0])
            || navigator.userLanguage
        const langCode = locale.length > 2 ? 
            locale.indexOf('_') > 0 ? locale.split('_')[0]
                : locale.split('-')[0]
            : locale
        return langCode
    }

    static loadLabels(langCode, callback) {
        const labelsFileLoaded = function(texts) {
            Object.assign(Labels._ALL_TEXTS, texts)
            
            let nextFilePrefix = filePrefixes.pop()
            if (nextFilePrefix) {
                fetchLabelFile(Labels._ALL_TEXTS, nextFilePrefix, langCode, labelsFileLoaded)
            } else {
                T.setTexts(Labels._ALL_TEXTS)
                callback()
            }
        }

        const fetchLabelFile = function(allTexts, filePrefix, langCode, callback) {
            fetch(Constants.BASE_ASSETS_URL + "locales/" + filePrefix + langCode + ".json?_v=" + Constants.APP_VERSION)
                .then(res => res.json())
                .then(texts => {
                    callback(texts)
                })
        }

        const filePrefixes = ['labels_', 'languages_']
        let nextFilePrefix = filePrefixes.pop()
        fetchLabelFile(Labels._ALL_TEXTS, nextFilePrefix, langCode, labelsFileLoaded)
    }


    static label(key, params) {
        const parameters = params ? 
                params instanceof Array ? params 
                : [params] 
            : null
        return T.translate(key, parameters)
    }

    static keys(prefix) {
        return Object.keys(Labels._ALL_TEXTS[prefix])
    }

    static l(key, parameters) {
        return Labels.label(key, parameters)
    }
}