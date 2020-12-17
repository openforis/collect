import Objects from './Objects'
import Strings from './Strings'

export default class Numbers {
  static toString = (value) => (Objects.isNullOrUndefined(value) ? '' : String(value))

  static toNumber = (value) => (Strings.isBlank(value) ? null : Number(value))

  static countDecimals = (value) => {
    if (!value || Math.floor(value) === value) {
      return 0
    }
    const parts = value.toString().split('.')
    return parts.length > 1 ? parts[1].length : 0
  }
}
