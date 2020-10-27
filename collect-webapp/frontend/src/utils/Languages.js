import * as LanguagesKeys from './LanguagesKeys'
import Labels from './Labels'

export default class Languages {
  static STANDARDS = LanguagesKeys.STANDARDS

  static CODES_BY_STANDARD = {
    [LanguagesKeys.STANDARDS.ISO_639_1]: null,
    [LanguagesKeys.STANDARDS.ISO_639_3]: null,
  }

  static ITEMS_BY_STANDARD = {
    [LanguagesKeys.STANDARDS.ISO_639_1]: null,
    [LanguagesKeys.STANDARDS.ISO_639_3]: null,
  }

  static _getLabelsPrefix(standard) {
    return LanguagesKeys.LABELS_PREFIX_BY_LANGUAGE_CODE_STANDARD[standard]
  }

  static codes(standard = LanguagesKeys.STANDARDS.ISO_639_1) {
    let codes = Languages.CODES_BY_STANDARD[standard]
    if (!codes) {
      codes = Labels.keys(Languages._getLabelsPrefix(standard))
      Languages.CODES_BY_STANDARD[standard] = codes
    }
    return codes
  }

  static label(key, standard = LanguagesKeys.STANDARDS.ISO_639_1) {
    return Labels.label(`${Languages._getLabelsPrefix(standard)}.${key}`)
  }

  static items(standard = LanguagesKeys.STANDARDS.ISO_639_1) {
    let items = Languages.ITEMS_BY_STANDARD[standard]
    if (!items) {
      const langCodes = Languages.codes(standard)
      items = langCodes.map((code) => ({ code, label: Languages.label(code, standard) }))
      Languages.ITEMS_BY_STANDARD[standard] = items
    }
    return items
  }
}
