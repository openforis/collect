import Objects from './Objects'
import Strings from './Strings'

export default class Numbers {
  static toString = (value) => (Objects.isNullOrUndefined(value) ? '' : String(value))

  static toNumber = (value) => (Strings.isBlank(value) ? null : Number(value))
}
