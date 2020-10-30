import { CodeFieldDefinition } from 'model/ui/CodeFieldDefinition'
import { AttributeDefinition, TaxonAttributeDefinition, TextAttributeDefinition } from 'model/Survey'

export const COORDINATE_FIELD_WIDTH = 100
export const COORDINATE_FIELD_WIDTH_PX = `${COORDINATE_FIELD_WIDTH}px`

export const TaxonFieldWidths = {
  [TaxonAttributeDefinition.Fields.FAMILY_CODE]: 200,
  [TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME]: 400,
  [TaxonAttributeDefinition.Fields.CODE]: 200,
  [TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME]: 400,
  [TaxonAttributeDefinition.Fields.VERNACULAR_NAME]: 400,
  [TaxonAttributeDefinition.Fields.LANGUAGE_CODE]: 100,
  [TaxonAttributeDefinition.Fields.LANGUAGE_VARIETY]: 100,
}

const WIDTH_CALCULATORS_BY_ATTRIBUTE_TYPE = {
  [AttributeDefinition.Types.BOOLEAN]: () => 50,
  [AttributeDefinition.Types.CODE]: ({ fieldDef }) => {
    const { layout } = fieldDef
    switch (layout) {
      case CodeFieldDefinition.Layouts.DROPDOWN:
        return 200
      case CodeFieldDefinition.Layouts.RADIO:
        return 200
      default:
        return 100
    }
  },
  [AttributeDefinition.Types.COORDINATE]: ({ fieldDef, inTable }) => {
    const { attributeDefinition: attrDef } = fieldDef

    if (inTable) {
      const { showSrsField, includeAltitudeField, includeAccuracyField } = attrDef
      const partsCount = 2 + Number(showSrsField) + Number(includeAltitudeField) + Number(includeAccuracyField)
      return partsCount * COORDINATE_FIELD_WIDTH
    } else {
      return COORDINATE_FIELD_WIDTH
    }
  },
  [AttributeDefinition.Types.DATE]: () => 180,
  [AttributeDefinition.Types.FILE]: () => 200,
  [AttributeDefinition.Types.NUMBER]: ({ fieldDef }) => {
    const { attributeDefinition: attrDef } = fieldDef
    const precisions = attrDef.precisions
    const hasPrecisions = precisions.length > 0
    return hasPrecisions ? 150 : 120
  },
  [AttributeDefinition.Types.TAXON]: ({ fieldDef, inTable }) => {
    const { attributeDefinition: attrDef } = fieldDef
    const { visibilityByField, showFamily } = attrDef

    return inTable
      ? Object.values(TaxonAttributeDefinition.Fields).reduce((widthAcc, field) => {
          const visible = [
            TaxonAttributeDefinition.Fields.FAMILY_CODE,
            TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME,
          ].includes(field)
            ? showFamily
            : visibilityByField[field]
          const width = visible ? TaxonFieldWidths[field] : 0
          return widthAcc + width
        }, 0)
      : TaxonFieldWidths[TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME]
  },
  [AttributeDefinition.Types.TEXT]: ({ fieldDef }) => {
    const { attributeDefinition: attrDef } = fieldDef
    const { textType } = attrDef
    return textType === TextAttributeDefinition.Types.SHORT ? 200 : 300
  },
  [AttributeDefinition.Types.TIME]: () => 130,
}

export const getWidth = ({ fieldDef, inTable }) => {
  const { attributeDefinition } = fieldDef
  const widthCalculator = WIDTH_CALCULATORS_BY_ATTRIBUTE_TYPE[attributeDefinition.attributeType]
  return widthCalculator({ fieldDef, inTable })
}
