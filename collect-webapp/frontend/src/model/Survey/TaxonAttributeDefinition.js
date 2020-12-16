import { AttributeDefinition } from './AttributeDefinition'

export class TaxonAttributeDefinition extends AttributeDefinition {
  static QueryFields = {
    CODE: 'CODE',
    SCIENTIFIC_NAME: 'SCIENTIFIC_NAME',
    VERNACULAR_NAME: 'VERNACULAR_NAME',
    FAMILY_CODE: 'FAMILY_CODE',
    FAMILY_SCIENTIFIC_NAME: 'FAMILY_SCIENTIFIC_NAME',
  }

  static Fields = {
    CODE: 'code',
    SCIENTIFIC_NAME: 'scientific_name',
    VERNACULAR_NAME: 'vernacular_name',
    LANGUAGE_CODE: 'language_code',
    LANGUAGE_VARIETY: 'language_variety',
    FAMILY_CODE: 'family_code',
    FAMILY_SCIENTIFIC_NAME: 'family_scientific_name',
  }

  static QueryFieldByField = {
    [TaxonAttributeDefinition.Fields.CODE]: TaxonAttributeDefinition.QueryFields.CODE,
    [TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME]: TaxonAttributeDefinition.QueryFields.SCIENTIFIC_NAME,
    [TaxonAttributeDefinition.Fields.VERNACULAR_NAME]: TaxonAttributeDefinition.QueryFields.VERNACULAR_NAME,
    [TaxonAttributeDefinition.Fields.FAMILY_CODE]: TaxonAttributeDefinition.QueryFields.FAMILY_CODE,
    [TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME]:
      TaxonAttributeDefinition.QueryFields.FAMILY_SCIENTIFIC_NAME,
  }

  static ValueFields = {
    CODE: 'code',
    SCIENTIFIC_NAME: 'scientificName',
    VERNACULAR_NAME: 'vernacularName',
    LANGUAGE_CODE: 'languageCode',
    LANGUAGE_VARIETY: 'languageVariety',
    FAMILY_CODE: 'familyCode',
    FAMILY_SCIENTIFIC_NAME: 'familyScientificName',
  }

  static ValueFieldByField = {
    [TaxonAttributeDefinition.Fields.CODE]: TaxonAttributeDefinition.ValueFields.CODE,
    [TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME]: TaxonAttributeDefinition.ValueFields.SCIENTIFIC_NAME,
    [TaxonAttributeDefinition.Fields.VERNACULAR_NAME]: TaxonAttributeDefinition.ValueFields.VERNACULAR_NAME,
    [TaxonAttributeDefinition.Fields.LANGUAGE_CODE]: TaxonAttributeDefinition.ValueFields.LANGUAGE_CODE,
    [TaxonAttributeDefinition.Fields.LANGUAGE_VARIETY]: TaxonAttributeDefinition.ValueFields.LANGUAGE_VARIETY,
    [TaxonAttributeDefinition.Fields.FAMILY_CODE]: TaxonAttributeDefinition.ValueFields.FAMILY_CODE,
    [TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME]:
      TaxonAttributeDefinition.ValueFields.FAMILY_SCIENTIFIC_NAME,
  }

  static FieldByValueField = {
    [TaxonAttributeDefinition.ValueFields.CODE]: TaxonAttributeDefinition.Fields.CODE,
    [TaxonAttributeDefinition.ValueFields.SCIENTIFIC_NAME]: TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME,
    [TaxonAttributeDefinition.ValueFields.VERNACULAR_NAME]: TaxonAttributeDefinition.Fields.VERNACULAR_NAME,
    [TaxonAttributeDefinition.ValueFields.LANGUAGE_CODE]: TaxonAttributeDefinition.Fields.LANGUAGE_CODE,
    [TaxonAttributeDefinition.ValueFields.LANGUAGE_VARIETY]: TaxonAttributeDefinition.Fields.LANGUAGE_VARIETY,
    [TaxonAttributeDefinition.ValueFields.FAMILY_CODE]: TaxonAttributeDefinition.Fields.FAMILY_CODE,
    [TaxonAttributeDefinition.ValueFields.FAMILY_SCIENTIFIC_NAME]:
      TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME,
  }

  static AUTOCOMPLETE_FIELDS = [
    TaxonAttributeDefinition.Fields.FAMILY_CODE,
    TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME,
    TaxonAttributeDefinition.Fields.CODE,
    TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME,
    TaxonAttributeDefinition.Fields.VERNACULAR_NAME,
  ]

  static LANGUAGE_FIELDS = [
    TaxonAttributeDefinition.Fields.LANGUAGE_CODE,
    //TaxonAttributeDefinition.Fields.LANGUAGE_VARIETY,
  ]

  static ALL_FIELDS_IN_ORDER = [
    ...TaxonAttributeDefinition.AUTOCOMPLETE_FIELDS,
    ...TaxonAttributeDefinition.LANGUAGE_FIELDS,
  ]

  static isAutocompleteField(field) {
    return TaxonAttributeDefinition.AUTOCOMPLETE_FIELDS.includes(field)
  }

  static isLanguageField(field) {
    return TaxonAttributeDefinition.LANGUAGE_FIELDS.includes(field)
  }

  taxonomyName
  highestRank
  codeVisible
  scientificNameVisible
  vernacularNameVisible
  languageCodeVisible
  languageVarietyVisible
  showFamily
  includeUniqueVernacularName
  allowUnlisted

  _isFieldAvailable = (field) =>
    this.visibilityByField[field] &&
    (![TaxonAttributeDefinition.Fields.FAMILY_CODE, TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME].includes(
      field
    ) ||
      this.showFamily)

  get availableFieldNames() {
    return TaxonAttributeDefinition.ALL_FIELDS_IN_ORDER.reduce((acc, field) => {
      if (this._isFieldAvailable(field)) {
        acc.push(field)
      }
      return acc
    }, [])
  }
}
