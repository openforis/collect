export default class Strings {
  static replaceAll(target, search, replacement) {
    return target.replace(new RegExp(search, 'g'), replacement)
  }

  static trimToEmpty(value) {
    return value === null ? '' : value.toString().trim()
  }

  static isBlank(value) {
    return value === null || value === undefined || value.trim().length === 0
  }

  static isNotBlank(value) {
    return value !== null && value.trim().length > 0
  }

  static compare(a, b) {
    return a > b ? 1 : b > a ? -1 : 0
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
}
