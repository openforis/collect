export default class BrowserUtils {

    static determineBrowserLanguageCode() {
        const locale = navigator.language || (navigator.languages && navigator.languages[0])
            || navigator.userLanguage
        const langCode = locale.length > 2 ? 
            locale.indexOf('_') > 0 ? locale.split('_')[0]
                : locale.split('-')[0]
            : locale
        return langCode
    }
}

