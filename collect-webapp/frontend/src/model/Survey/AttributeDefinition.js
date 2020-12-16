import { NodeDefinition } from './NodeDefinition'

export class AttributeDefinition extends NodeDefinition {
  key
  attributeType
  fieldNames
  fieldLabels
  mandatoryFieldNames

  static Types = {
    BOOLEAN: 'BOOLEAN',
    CODE: 'CODE',
    COORDINATE: 'COORDINATE',
    DATE: 'DATE',
    FILE: 'FILE',
    NUMBER: 'NUMBER',
    RANGE: 'RANGE',
    TAXON: 'TAXON',
    TEXT: 'TEXT',
    TIME: 'TIME',
  }

  getFieldLabel(fieldName) {
    const index = this.fieldNames.indexOf(fieldName)
    return this.fieldLabels[index]
  }

  /**
   * Visible field names in order
   */
  get availableFieldNames() {
    return this.fieldnames
  }
}
