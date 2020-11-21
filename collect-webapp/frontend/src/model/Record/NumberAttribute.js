import { AbstractNumericAttribute } from './AbstractNumericAttribute'

export class NumberAttribute extends AbstractNumericAttribute {
  get valueField() {
    return this.fields[0]
  }

  get unitField() {
    return this.fields[2]
  }

  get value() {
    return super.value
  }

  set value(value) {
    if (value) {
      // Workaround: Numeric value field "unitId" matches field "unit" in the attribute
      const { unitId: unit, ...other } = value
      super.value = { ...other, unit }
    } else {
      super.value = null
    }
  }
}
