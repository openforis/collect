import { AttributeDefinition } from './AttributeDefinition'

export class NumericAttributeDefinition extends AttributeDefinition {
  numericType
  precisions

  static NumericTypes = {
    INTEGER: 'INTEGER',
    REAL: 'REAL',
  }

  isUnitVisible({ inTable }) {
    return this.precisions.length > 0 && !(inTable && this.precisions.length === 1)
  }

  isInteger() {
    return this.numericType === NumericAttributeDefinition.NumericTypes.INTEGER
  }
}
