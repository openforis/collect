export default class Objects {
  static compare(a, b) {
    if (a === null && b === null) {
      return 0
    } else if (a === null) {
      return -1
    } else if (b === null) {
      return 1
    } else {
      if (typeof a === 'string' && typeof b === 'string') {
        return a.localeCompare(b)
      } else {
        return a === b ? 0 : a > b ? 1 : -1
      }
    }
  }

  static isNullOrUndefined(obj) {
    return obj === null || typeof obj === 'undefined'
  }

  static isNotNullOrUndefined(obj) {
    return !Objects.isNullOrUndefined(obj)
  }

  static isNotEmpty(obj) {
    return typeof obj === 'object' && Object.keys(obj).length > 0
  }

  static isEmpty(obj) {
    return !Objects.isNotEmpty(obj)
  }

  static defaultIfNull(obj, defaultVal) {
    if (Objects.isNullOrUndefined(obj)) {
      return defaultVal
    } else {
      return obj
    }
  }

  static matchesKeyValuePairs(obj, keyValuePairs) {
    let matching = true
    Object.keys(keyValuePairs).forEach((key) => {
      if (obj[key] !== keyValuePairs[key]) {
        matching = false
      }
    })
    return matching
  }

  static mapKeys({ obj, keysMapping }) {
    return obj
      ? Object.entries(obj).reduce((acc, [key, value]) => {
          const newKey = keysMapping[key]
          if (newKey) {
            acc[newKey] = value
          }
          return acc
        }, {})
      : null
  }

  static getProp = (prop, defaultTo = null) => (obj) => {
    const val = obj ? obj[prop] : null
    return Objects.isNullOrUndefined(val) ? defaultTo : val
  }
}
