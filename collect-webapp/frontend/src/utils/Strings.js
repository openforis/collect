export default class Strings {
  static replaceAll(target, search, replacement) {
    return target.replace(new RegExp(search, 'g'), replacement)
  }

  static trimToEmpty(value) {
    return value === null || value === undefined ? '' : value.toString().trim()
  }

  static isBlank(value) {
    return Strings.trimToEmpty(value).length === 0
  }

  static isNotBlank(value) {
    return Strings.trimToEmpty(value).length > 0
  }

  static compare(a, b) {
    return a > b ? 1 : b > a ? -1 : 0
  }

  static equalsIgnoreCase(a, b) {
    if (a === null && b === null) return true
    if (a === null) return false
    if (b === null) return false
    return a.localeCompare(b, undefined, { sensitivity: 'accent' }) === 0
  }

  static join(values, separator) {
    if (!separator) {
      separator = ', '
    }
    let result = ''
    for (let i = 0; i < values.length; i++) {
      result += values[i]
      if (i < values.length - 1) {
        result += separator
      }
    }
    return result
  }

  /**
   * Appends the specified value to a string only if the string doesn't end with that value already.
   *
   * @param {!string} string - String to update.
   * @param {!string} value - Value to append.
   * @returns {string} - The updated string.
   */
  static appendIfMissing(string, value) {
    return string.endsWith(value) ? string : `${string}${value}`
  }
}
