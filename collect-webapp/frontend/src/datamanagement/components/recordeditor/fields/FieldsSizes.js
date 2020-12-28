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
  [AttributeDefinition.Types.BOOLEAN]: () => 100,
  [AttributeDefinition.Types.CODE]: ({ inTable }) => (inTable ? 200 : 300),
  [AttributeDefinition.Types.COORDINATE]: ({ fieldDef, inTable }) => {
    const { attributeDefinition } = fieldDef
    const { availableFieldNames } = attributeDefinition

    return inTable ? availableFieldNames.length * COORDINATE_FIELD_WIDTH : COORDINATE_FIELD_WIDTH
  },
  [AttributeDefinition.Types.DATE]: () => 180,
  [AttributeDefinition.Types.FILE]: ({ inTable }) => (inTable ? 200 : 400),
  [AttributeDefinition.Types.NUMBER]: ({ fieldDef, inTable }) => {
    const { attributeDefinition } = fieldDef
    return 120 + (attributeDefinition.isUnitVisible({ inTable }) ? 30 : 0)
  },
  [AttributeDefinition.Types.RANGE]: ({ fieldDef, inTable }) => {
    const { attributeDefinition } = fieldDef
    return attributeDefinition.isUnitVisible({ inTable }) ? 270 : 240
  },
  [AttributeDefinition.Types.TAXON]: ({ fieldDef, inTable }) => {
    const { attributeDefinition } = fieldDef
    const { availableFieldNames } = attributeDefinition

    return inTable
      ? availableFieldNames.reduce((widthAcc, field) => widthAcc + TaxonFieldWidths[field], 0)
      : TaxonFieldWidths[TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME] + TaxonFormFieldLabelWidth
  },
  [AttributeDefinition.Types.TEXT]: ({ fieldDef, inTable }) => {
    const { attributeDefinition: attrDef } = fieldDef
    const { textType } = attrDef
    switch (textType) {
      case TextAttributeDefinition.TextTypes.MEMO:
        return inTable ? 300 : 500
      default:
        return inTable ? 200 : 300
    }
  },
  [AttributeDefinition.Types.TIME]: () => 130,
}

export const getWidth = ({ fieldDef, inTable }) => {
  const { attributeDefinition } = fieldDef
  const { width = 0 } = attributeDefinition
  const widthCalculator = WIDTH_CALCULATORS_BY_ATTRIBUTE_TYPE[attributeDefinition.attributeType]
  const minWidth = widthCalculator({ fieldDef, inTable })
  return Math.max(minWidth, width)
}

export const getWidthPx = ({ fieldDef, inTable }) => `${getWidth({ fieldDef, inTable })}px`

export const getFormItemWidth = ({ fieldDef, inTable }) => {
  const { attributeDefinition } = fieldDef
  const { labelWidth = 200 } = attributeDefinition
  return getWidth({ fieldDef, inTable }) + labelWidth
}

export const getFormItemWidthPx = ({ fieldDef, inTable }) => `${getFormItemWidth({ fieldDef, inTable })}px`

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
