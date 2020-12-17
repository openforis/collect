import { AttributeDefinition } from './AttributeDefinition'

export class NumericAttributeDefinition extends AttributeDefinition {
  numericType
  precisions

  static NumericTypes = {
    INTEGER: 'INTEGER',
    REAL: 'REAL',
  }

  static DEFAULT_DECIMAL_DIGITS = 10

  isUnitVisible({ inTable }) {
    return this.precisions.length > 0 && !(inTable && this.precisions.length === 1)
  }

  isInteger() {
    return this.numericType === NumericAttributeDefinition.NumericTypes.INTEGER
  }

  getPrecision(unitId) {
    return this.precisions.find((precision) => precision.unitId === unitId)
  }

  getDecimalScale(unitId) {
    if (this.isInteger()) {
      return 0
    }
    const precision = this.getPrecision(unitId) || {}
    const { decimalDigits } = precision
    return decimalDigits === null || decimalDigits === undefined || Number.isNaN(decimalDigits)
      ? NumericAttributeDefinition.DEFAULT_DECIMAL_DIGITS
      : decimalDigits
  }

  getMaxLength(unitId) {
    return 10 + this.getDecimalScale(unitId)
  }
}
