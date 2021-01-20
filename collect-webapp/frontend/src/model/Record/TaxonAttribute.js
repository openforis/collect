import { Attribute } from './Attribute'
import { TaxonAttributeDefinition } from '../Survey'

import Strings from 'utils/Strings'
import Objects from 'utils/Objects'

export class TaxonAttribute extends Attribute {
  // get value() {
  //   const val = super.value
  //   if (val === null) {
  //     return null
  //   }
  //   const { code, scientific_name, vernacular_name, language_code, language_variety } = val
  //   return {
  //     code,
  //     scientificName: scientific_name,
  //     vernacularName: vernacular_name,
  //     languageCode: language_code,
  //     languageVariety: language_variety,
  //   }
  // }

  get value() {
    return super.value
  }

  get humanReadableValue() {
    if (this.fields && this.fields.length) {
      const { availableFieldNames } = this.definition
      const fieldValues = []
      // show code only if visible
      if (availableFieldNames.includes(TaxonAttributeDefinition.Fields.CODE)) {
        fieldValues.push(this.fields[0].value)
      }
      // always show scientific name
      fieldValues.push(this.fields[1].value)

      return fieldValues.filter((v) => Strings.isNotBlank(v)).join(' - ')
    } else {
      return ''
    }
  }

  set value(value) {
    if (value) {
      const valueByFields = Objects.mapKeys({ obj: value, keysMapping: TaxonAttributeDefinition.FieldByValueField })
      super.value = valueByFields
    } else {
      super.value = null
    }
  }
}
