import './TaxonField.css'

import React from 'react'
import { TextField } from '@material-ui/core'
import Autocomplete from '@material-ui/lab/Autocomplete'

import { TaxonAttributeDefinition } from 'model/Survey'

import Objects from 'utils/Objects'
import L from 'utils/Labels'
import Languages from 'utils/Languages'

import AbstractSingleAttributeField from '../AbstractSingleAttributeField'
import CompositeAttributeFormItem from '../CompositeAttributeFormItem'
import TaxonAutoCompleteField from './TaxonAutoCompleteField'
import * as FieldsSizes from '../FieldsSizes'

const LANG_CODE_STANDARD = Languages.STANDARDS.ISO_639_3

export default class TaxonField extends AbstractSingleAttributeField {
  LANG_CODES = Languages.codes(LANG_CODE_STANDARD)

  constructor() {
    super()

    this.onChangeField = this.onChangeField.bind(this)
    this.prepareValueUpdate = this.prepareValueUpdate.bind(this)
    this.undoValueUpdate = this.undoValueUpdate.bind(this)
  }

  onChangeField(field) {
    if (TaxonAttributeDefinition.isLanguageField(field)) {
      return (langCode) => {
        const { value } = this.state
        const valueUpdated = { ...value, [field]: langCode }
        this.onAttributeUpdate({ value: valueUpdated, debounced: false })
      }
    } else {
      return (taxonOccurrence) => {
        const value = Objects.mapKeys({
          obj: taxonOccurrence,
          keysMapping: TaxonAttributeDefinition.FieldByValueField,
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
    const { availableFieldNames } = attributeDefinition
    const { value = {} } = this.state
    const { code, vernacular_name: vernacularName } = value || {}

    const getLangLabel = (langCode) => Languages.label(langCode, LANG_CODE_STANDARD)
    const langOptions = Languages.items(LANG_CODE_STANDARD)

    const inputFields = availableFieldNames.map((field) => {
      if (TaxonAttributeDefinition.isAutocompleteField(field)) {
        return (
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
        )
      } else {
        const langCode = Objects.getProp(field)(value)
        const selectedOption = langCode ? { code: langCode, label: getLangLabel(langCode) } : null
        const width = FieldsSizes.TaxonFieldWidths[field]
        return (
          <Autocomplete
            key={field}
            className="taxon-autocomplete-language"
            style={{ width: `${width}px` }}
            value={selectedOption}
            options={langOptions}
            getOptionSelected={(option) => option.code === langCode}
            getOptionLabel={(option) => `${option.code} - ${option.label}`}
            renderInput={(params) => <TextField {...params} variant="outlined" />}
            onChange={(_, option) => this.onChangeField(field)(option.code)}
            disabled={!code || code !== 'UNL' || !vernacularName}
          />
        )
      }
    })

    return inTable ? (
      <div style={{ display: 'flex' }}>{inputFields}</div>
    ) : (
      availableFieldNames.map((field, index) => (
        <CompositeAttributeFormItem
          key={field}
          field={field}
          label={L.l(`dataManagement.dataEntry.attribute.taxon.${field}`)}
          inputField={inputFields[index]}
          labelWidth={FieldsSizes.TaxonFormFieldLabelWidth}
        />
      ))
    )
  }
}
