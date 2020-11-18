import { CodeFieldDefinition } from 'model/ui/CodeFieldDefinition'
import {
  AttributeDefinition,
  CoordinateAttributeDefinition,
  TaxonAttributeDefinition,
  TextAttributeDefinition,
} from 'model/Survey'

export const COORDINATE_FIELD_WIDTH = 150
export const COORDINATE_FIELD_WIDTH_PX = `${COORDINATE_FIELD_WIDTH}px`

export const TaxonFieldWidths = {
  [TaxonAttributeDefinition.Fields.FAMILY_CODE]: 200,
  [TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME]: 380,
  [TaxonAttributeDefinition.Fields.CODE]: 200,
  [TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME]: 380,
  [TaxonAttributeDefinition.Fields.VERNACULAR_NAME]: 380,
  [TaxonAttributeDefinition.Fields.LANGUAGE_CODE]: 100,
  [TaxonAttributeDefinition.Fields.LANGUAGE_VARIETY]: 100,
}

export const TaxonFormFieldLabelWidth = 160

const WIDTH_CALCULATORS_BY_ATTRIBUTE_TYPE = {
  [AttributeDefinition.Types.BOOLEAN]: () => 50,
  [AttributeDefinition.Types.CODE]: ({ fieldDef }) => {
    const { layout } = fieldDef
    switch (layout) {
      case CodeFieldDefinition.Layouts.RADIO:
        return 200
      case CodeFieldDefinition.Layouts.DROPDOWN:
      case CodeFieldDefinition.Layouts.TEXT:
      default:
        return 200
    }
  },
  [AttributeDefinition.Types.COORDINATE]: ({ fieldDef, inTable }) => {
    const { attributeDefinition } = fieldDef
    const { availableFieldNames } = attributeDefinition

    return inTable ? availableFieldNames.length * COORDINATE_FIELD_WIDTH : COORDINATE_FIELD_WIDTH
  },
  [AttributeDefinition.Types.DATE]: () => 180,
  [AttributeDefinition.Types.FILE]: () => 200,
  [AttributeDefinition.Types.NUMBER]: ({ fieldDef, inTable }) => {
    const { attributeDefinition: attrDef } = fieldDef
    const precisions = attrDef.precisions
    const hasPrecisions = precisions.length > 0
    const unitVisible = hasPrecisions && (!inTable || precisions.length > 1)
    return unitVisible ? 150 : 120
  },
  [AttributeDefinition.Types.TAXON]: ({ fieldDef, inTable }) => {
    const { attributeDefinition } = fieldDef
    const { availableFieldNames } = attributeDefinition

    return inTable
      ? availableFieldNames.reduce((widthAcc, field) => widthAcc + TaxonFieldWidths[field], 0)
      : TaxonFieldWidths[TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME] + TaxonFormFieldLabelWidth
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

export const getWidthPx = ({ fieldDef, inTable }) => `${getWidth({ fieldDef, inTable })}px`

export const getFieldWidth = ({ fieldDef, fieldName }) => {
  const { attributeDefinition } = fieldDef
  if (attributeDefinition instanceof CoordinateAttributeDefinition) {
    return COORDINATE_FIELD_WIDTH
  }
  if (attributeDefinition instanceof TaxonAttributeDefinition) {
    return TaxonFieldWidths[fieldName]
  }
  return 100
}

export const getFieldWidthPx = ({ fieldDef, fieldName }) => `${getFieldWidth({ fieldDef, fieldName })}px`
