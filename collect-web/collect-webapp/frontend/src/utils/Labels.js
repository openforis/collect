import T from 'i18n-react';
import Constants from 'utils/Constants'
import Arrays from 'utils/Arrays'

export default class Labels {

    static SUPPORTED_LANG_CODES = ['en']
    static DEFAULT_LANG_CODE = 'en'

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
        fetch("/locales/labels_" + langCode + ".json?_v=" + Constants.APP_VERSION)
            .then(res => res.json())
            .then(texts => {
                T.setTexts(texts)
                callback()
            }
        )
    }

    static label(key, parameters) {
        return T.translate(key, parameters)
    }

    static l(key, parameters) {
        return Labels.label(key, parameters)
    }
}