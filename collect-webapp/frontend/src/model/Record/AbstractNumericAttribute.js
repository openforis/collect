import { Attribute } from './Attribute'

export class AbstractNumericAttribute extends Attribute {
  get unitField() {
    // to be extended by subclasses
    return null
  }
}
