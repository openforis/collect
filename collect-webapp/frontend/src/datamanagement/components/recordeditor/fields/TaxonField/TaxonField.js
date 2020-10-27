import './TaxonField.css'

import React from 'react'
import { TextField } from '@material-ui/core'
import Autocomplete from '@material-ui/lab/Autocomplete'

import { TaxonAttributeDefinition } from 'model/Survey'

import Objects from 'utils/Objects'
import L from 'utils/Labels'
import Languages from 'utils/Languages'

import AbstractField from '../AbstractField'
import CompositeAttributeFormItem from '../CompositeAttributeFormItem'
import TaxonAutoCompleteField from './TaxonAutoCompleteField'

const LANG_CODE_STANDARD = Languages.STANDARDS.ISO_639_3

const ALL_AUTOCOMPLETE_FIELDS = [
  TaxonAttributeDefinition.FIELDS.FAMILY_CODE,
  TaxonAttributeDefinition.FIELDS.FAMILY_SCIENTIFIC_NAME,
  TaxonAttributeDefinition.FIELDS.CODE,
  TaxonAttributeDefinition.FIELDS.SCIENTIFIC_NAME,
  TaxonAttributeDefinition.FIELDS.VERNACULAR_NAME,
]
const ALL_LANGUAGE_FIELDS = [
  TaxonAttributeDefinition.FIELDS.LANGUAGE_CODE,
  TaxonAttributeDefinition.FIELDS.LANGUAGE_VARIETY,
]
const ALL_FIELDS = [...ALL_AUTOCOMPLETE_FIELDS, ...ALL_LANGUAGE_FIELDS]

export default class TaxonField extends AbstractField {
  LANG_CODES = Languages.codes(LANG_CODE_STANDARD)

  constructor() {
    super()

    this.onChangeField = this.onChangeField.bind(this)
    this.prepareValueUpdate = this.prepareValueUpdate.bind(this)
    this.undoValueUpdate = this.undoValueUpdate.bind(this)
  }

  onChangeField(field) {
    if (ALL_LANGUAGE_FIELDS.includes(field)) {
      return (langCode) => {
        const { value } = this.state
        const valueUpdated = { ...value, [field]: langCode }
        this.onAttributeUpdate({ value: valueUpdated, debounced: false })
      }
    } else {
      return (taxonOccurrence) => {
        const value = Objects.mapKeys({
          obj: taxonOccurrence,
          keysMapping: TaxonAttributeDefinition.FIELD_BY_VALUE_FIELD,
        })
        this.setState({ previousValue: null }, () => this.onAttributeUpdate({ value, debounced: false }))
      }
    }
  }

  prepareValueUpdate() {
    const { value, previousValue } = this.state
    if (!previousValue) {
      this.setState({ previousValue: value, value: null, dirty: true })
    }
  }

  undoValueUpdate() {
    const { previousValue } = this.state
    this.setState({ previousValue: null, value: previousValue, dirty: false })
  }

  render() {
    const { inTable, fieldDef, parentEntity } = this.props
    const { attributeDefinition } = fieldDef
    const { visibilityByField, showFamily } = attributeDefinition
    const { value = {} } = this.state
    const { code, vernacular_name: vernacularName } = value || {}

    const getLangLabel = (langCode) => Languages.label(langCode, LANG_CODE_STANDARD)
    const langOptions = Languages.items(LANG_CODE_STANDARD)

    const isFieldIncluded = (field) =>
      visibilityByField[field] &&
      (![TaxonAttributeDefinition.FIELDS.FAMILY_CODE, TaxonAttributeDefinition.FIELDS.FAMILY_SCIENTIFIC_NAME].includes(
        field
      ) ||
        showFamily)

    const fields = ALL_FIELDS.reduce((acc, field) => {
      if (isFieldIncluded(field)) {
        acc.push(field)
      }
      return acc
    }, [])

    const autocompleteFields = ALL_AUTOCOMPLETE_FIELDS.filter(isFieldIncluded)
    const languageFields = ALL_LANGUAGE_FIELDS.filter(isFieldIncluded)

    const inputFields = [
      ...autocompleteFields.map((field) => (
        <TaxonAutoCompleteField
          key={field}
          field={field}
          fieldDef={fieldDef}
          parentEntity={parentEntity}
          valueByFields={value}
          onInputChange={this.prepareValueUpdate}
          onDismiss={this.undoValueUpdate}
          onSelect={this.onChangeField(field)}
        />
      )),
      ...languageFields.map((field) => {
        const langCode = Objects.getProp(field)(value)
        const selectedOption = langCode ? { code: langCode, label: getLangLabel(langCode) } : null
        return (
          <Autocomplete
            key={field}
            className="taxon-autocomplete-language"
            style={{ width: 300 }}
            value={selectedOption}
            options={langOptions}
            getOptionSelected={(option) => option.code === langCode}
            getOptionLabel={(option) => `${option.label} (${option.code})`}
            renderInput={(params) => <TextField {...params} variant="outlined" />}
            onChange={(_, option) => this.onChangeField(field)(option.code)}
            disabled={!code || code !== 'UNL' || !vernacularName}
          />
        )
      }),
    ]

    return inTable
      ? inputFields
      : fields.map((field, index) => (
          <CompositeAttributeFormItem
            key={field}
            field={field}
            label={L.l(`dataManagement.dataEntry.taxonField.${field}`)}
            inputField={inputFields[index]}
            labelWidth={160}
          />
        ))
  }
}
