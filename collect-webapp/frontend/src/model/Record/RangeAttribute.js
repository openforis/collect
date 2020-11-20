import { AbstractNumericAttribute } from './AbstractNumericAttribute'

export class RangeAttribute extends AbstractNumericAttribute {
  get fromField() {
    return this.fields[0]
  }

  get toField() {
    return this.fields[1]
  }

  get unitField() {
    return this.fields[2]
  }
}
