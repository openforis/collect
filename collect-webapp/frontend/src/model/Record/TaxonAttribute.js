import Objects from '../../utils/Objects'
import { TaxonAttributeDefinition } from '../Survey'

import { Attribute } from './Attribute'

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

  set value(value) {
    if (value) {
      const valueByFields = Objects.mapKeys({ obj: value, keysMapping: TaxonAttributeDefinition.FieldByValueField })
      super.value = valueByFields
    } else {
      super.value = null
    }
  }
}
