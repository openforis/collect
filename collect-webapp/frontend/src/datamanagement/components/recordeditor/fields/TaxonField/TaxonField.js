import './TaxonField.css'

import React from 'react'

import { TaxonAttributeDefinition } from 'model/Survey'

import Arrays from 'utils/Arrays'
import Objects from 'utils/Objects'
import L from 'utils/Labels'
import Languages from 'utils/Languages'

import Autocomplete from 'common/components/Autocomplete'
import AbstractField from '../AbstractField'
import CompositeAttributeFormItem from '../CompositeAttributeFormItem'
import TaxonAutoCompleteField from './TaxonAutoCompleteField'
import * as FieldsSizes from '../FieldsSizes'

const LANG_CODE_STANDARD = Languages.STANDARDS.ISO_639_3

export default class TaxonField extends AbstractField {
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
        this.updateValue({ value: valueUpdated, debounced: false })
      }
    } else {
      return (taxonOccurrence) => {
        const value = Objects.mapKeys({
          obj: taxonOccurrence,
          keysMapping: TaxonAttributeDefinition.FieldByValueField,
        })
        this.setState({ previousValue: null }, () => this.updateValue({ value, debounced: false }))
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
        return (
          <Autocomplete
            key={field}
            className="taxon-autocomplete-language"
            asynchronous={false}
            disabled={!code || code !== 'UNL' || !vernacularName}
            items={langOptions}
            inputFieldWidth={FieldsSizes.TaxonFieldWidths[field]}
            selectedItems={Arrays.singleton(selectedOption)}
            itemLabelFunction={(option) => `${option.code} - ${option.label}`}
            itemSelectedFunction={(item) => item.code === langCode}
            onSelect={(option) => this.onChangeField(field)(option.code)}
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
