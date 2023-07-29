import Arrays from 'utils/Arrays'
import { AttributeDefinition } from './AttributeDefinition'

export class CoordinateAttributeDefinition extends AttributeDefinition {
  fieldsOrder
  showSrsField
  includeAltitudeField
  includeAccuracyField
  mandatoryFieldNames = [
    CoordinateAttributeDefinition.Fields.X,
    CoordinateAttributeDefinition.Fields.Y,
    CoordinateAttributeDefinition.Fields.SRS,
  ]

  static FieldsOrder = {
    SRS_X_Y: 'SRS_X_Y',
    SRS_Y_X: 'SRS_Y_X',
    X_Y_SRS: 'X_Y_SRS',
    Y_X_SRS: 'Y_X_SRS',
  }

  static Fields = {
    X: 'x',
    Y: 'y',
    SRS: 'srs',
    ALTITUDE: 'altitude',
    ACCURACY: 'accuracy',
  }

  get availableFieldNames() {
    const fields = []
    switch (this.fieldsOrder) {
      case CoordinateAttributeDefinition.FieldsOrder.SRS_X_Y:
        fields.push(
          CoordinateAttributeDefinition.Fields.SRS,
          CoordinateAttributeDefinition.Fields.X,
          CoordinateAttributeDefinition.Fields.Y
        )
        break
      case CoordinateAttributeDefinition.FieldsOrder.SRS_Y_X:
        fields.push(
          CoordinateAttributeDefinition.Fields.SRS,
          CoordinateAttributeDefinition.Fields.Y,
          CoordinateAttributeDefinition.Fields.X
        )
        break
      case CoordinateAttributeDefinition.FieldsOrder.X_Y_SRS:
        fields.push(
          CoordinateAttributeDefinition.Fields.X,
          CoordinateAttributeDefinition.Fields.Y,
          CoordinateAttributeDefinition.Fields.SRS
        )
        break
      case CoordinateAttributeDefinition.FieldsOrder.Y_X_SRS:
        fields.push(
          CoordinateAttributeDefinition.Fields.Y,
          CoordinateAttributeDefinition.Fields.X,
          CoordinateAttributeDefinition.Fields.SRS
        )
        break
      default:
        throw new Error(`Fields order not supported: ${this.fieldsOrder}`)
    }
    if (!this.showSrsField) {
      Arrays.deleteItem(CoordinateAttributeDefinition.Fields.SRS)(fields)
    }
    if (this.includeAltitudeField) {
      fields.push(CoordinateAttributeDefinition.Fields.ALTITUDE)
    }
    if (this.includeAccuracyField) {
      fields.push(CoordinateAttributeDefinition.Fields.ACCURACY)
    }
    return fields
  }
}
