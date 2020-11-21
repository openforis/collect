import { AbstractNumericAttribute } from './AbstractNumericAttribute'

export class RangeAttribute extends AbstractNumericAttribute {
  get fromField() {
    return this.fields[0]
  }

  get toField() {
    return this.fields[1]
  }

  get unitField() {
    return this.fields[3]
  }

  get value() {
    return super.value
  }

  set value(value) {
    if (value) {
      // Workaround: Range value field "unitId" matches field "unit" in the attribute
      const { unitId: unit, ...other } = value
      super.value = { ...other, unit }
    } else {
      super.value = null
    }
  }
}
