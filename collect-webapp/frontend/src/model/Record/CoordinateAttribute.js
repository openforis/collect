import { Attribute } from './Attribute'

export class CoordinateAttribute extends Attribute {
  get value() {
    return super.value
  }

  set value(value) {
    if (value) {
      // Workaround: Coordinate field "srsId" matches field "srs" in the attribute
      const { srsId: srs, ...other } = value
      super.value = { ...other, srs }
    } else {
      super.value = null
    }
  }
}
