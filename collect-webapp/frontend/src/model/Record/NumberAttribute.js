import { AbstractNumericAttribute } from './AbstractNumericAttribute'

export class NumberAttribute extends AbstractNumericAttribute {
  get valueField() {
    return this.fields[0]
  }

  get unitField() {
    return this.fields[1]
  }
}
