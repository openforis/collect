import { NumericAttributeDefinition } from './NumericAttributeDefinition'

export class RangeAttributeDefinition extends NumericAttributeDefinition {
  mandatoryFieldNames = ['from', 'to']
}
