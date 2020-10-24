import React, { useEffect, useState } from 'react'
import Autocomplete from '@material-ui/lab/Autocomplete'
import { TextField } from '@material-ui/core'

import ServiceFactory from 'services/ServiceFactory'
import { TaxonAttributeDefinition } from 'model/Survey'

import FieldLoadingSpinner from '../FieldLoadingSpinner'
import AbstractField from '../AbstractField'
import CompositeAttributeFormItem from '../CompositeAttributeFormItem'
import Objects from '../../../../../utils/Objects'

const AutoCompleteField = (props) => {
  const { parentEntity, fieldDef, queryField, field, valueByFields, width, onChange } = props

  const valueField = TaxonAttributeDefinition.VALUE_FIELD_BY_FIELD[field]
  const initialInputValue = valueByFields ? valueByFields[field] : ''
  console.log('===field', field)
  console.log('----initialInputValue', initialInputValue)
  const selectedTaxonOccurrence = Objects.mapKeys({
    obj: valueByFields,
    keysMapping: TaxonAttributeDefinition.VALUE_FIELD_BY_FIELD,
  })
  const highestRank = 'FAMILY'
  const includeUniqueVernacularName = false
  const includeAncestorTaxons = false

  const surveyId = parentEntity.survey.id
  const taxonomyName = fieldDef.attributeDefinition.taxonomyName

  const [open, setOpen] = useState(false)
  const [taxonOccurrences, setTaxonOccurrences] = useState([])
  const [noResultsFound, setNoResultsFound] = useState(false)
  const [searchString, setSearchString] = useState(initialInputValue)

  const loading = open && !noResultsFound && taxonOccurrences.length === 0

  useEffect(() => {
    let active = true

    if (!loading) {
      return undefined
    }

    ;(async () => {
      const query = {
        field: queryField,
        searchString,
        parameters: { highestRank, includeUniqueVernacularName, includeAncestorTaxons },
      }
      const taxa = await ServiceFactory.speciesService.findTaxa({ surveyId, taxonomyName, query })

      if (active) {
        setTaxonOccurrences(taxa)
        setNoResultsFound(taxa.length === 0)
      }
    })()

    return () => {
      active = false
    }
  }, [loading, searchString])

  useEffect(() => {
    if (!open) {
      setTaxonOccurrences([])
      setNoResultsFound(false)
    }
  }, [open])

  useEffect(() => {
    if (open) {
      setTaxonOccurrences([])
      setNoResultsFound(false)
    }
  }, [searchString])

  const gridTemplateColumns = '100px 200px 200px 50px 50px'

  return (
    <Autocomplete
      style={{ width }}
      open={open}
      openOnFocus={false}
      onOpen={() => {
        setOpen(true)
      }}
      onClose={() => {
        setOpen(false)
      }}
      value={selectedTaxonOccurrence}
      inputValue={searchString}
      onChange={(_, taxonOccurrence) => onChange(taxonOccurrence)}
      onInputChange={(_, value) => setSearchString(value)}
      getOptionLabel={Objects.getProp(valueField, '')}
      getOptionSelected={(taxonOccurrence, value) => taxonOccurrence.code === value.code}
      options={taxonOccurrences}
      loading={loading}
      renderInput={(params) => (
        <TextField
          {...params}
          variant="outlined"
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading && <FieldLoadingSpinner />}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
      renderOption={(taxonOccurrence) => {
        const { code, scientificName, vernacularName, languageCode, languageVariety } = taxonOccurrence
        return (
          <div style={{ display: 'grid', gridTemplateColumns }}>
            <div>{code}</div>
            <div>{scientificName}</div>
            <div>{vernacularName}</div>
            <div>{languageCode}</div>
            <div>{languageVariety}</div>
          </div>
        )
      }}
    />
  )
}

export default class TaxonField extends AbstractField {
  constructor() {
    super()

    this.onChangeField = this.onChangeField.bind(this)
  }

  onChangeField(field) {
    return (taxonOccurrence) => {
      const value = Objects.mapKeys({
        obj: taxonOccurrence,
        keysMapping: TaxonAttributeDefinition.FIELD_BY_VALUE_FIELD,
      })
      this.onAttributeUpdate({ value })
    }
  }

  render() {
    const { inTable, fieldDef, parentEntity } = this.props
    const { dirty, value = {} } = this.state
    const { code, scientificName, vernacularName, languageCode, languageVariety } = value || {}

    const codeField = (
      <AutoCompleteField
        key={TaxonAttributeDefinition.FIELDS.CODE}
        field={TaxonAttributeDefinition.FIELDS.CODE}
        queryField={TaxonAttributeDefinition.QUERY_FIELDS.CODE}
        fieldDef={fieldDef}
        parentEntity={parentEntity}
        valueByFields={value}
        onChange={this.onChangeField(TaxonAttributeDefinition.FIELDS.CODE)}
      />
    )

    const scientificNameField = (
      <AutoCompleteField
        key={TaxonAttributeDefinition.FIELDS.SCIENTIFIC_NAME}
        field={TaxonAttributeDefinition.FIELDS.SCIENTIFIC_NAME}
        queryField={TaxonAttributeDefinition.QUERY_FIELDS.SCIENTIFIC_NAME}
        fieldDef={fieldDef}
        parentEntity={parentEntity}
        valueByFields={value}
        onChange={this.onChangeField(TaxonAttributeDefinition.FIELDS.SCIENTIFIC_NAME)}
      />
    )

    const vernacularNameField = (
      <AutoCompleteField
        key={TaxonAttributeDefinition.FIELDS.VERNACULAR_NAME}
        field={TaxonAttributeDefinition.FIELDS.VERNACULAR_NAME}
        queryField={TaxonAttributeDefinition.QUERY_FIELDS.VERNACULAR_NAME}
        fieldDef={fieldDef}
        parentEntity={parentEntity}
        valueByFields={value}
        onChange={this.onChangeField(TaxonAttributeDefinition.FIELDS.VERNACULAR_NAME)}
      />
    )

    const getInternalContent = () => {
      let internalParts = null
      if (inTable) {
        internalParts = [codeField, scientificNameField, vernacularNameField]
      } else {
        const labelWidth = 100
        const codeFieldLabel = 'Code'
        const codeFormItem = (
          <CompositeAttributeFormItem
            key={TaxonAttributeDefinition.FIELDS.CODE}
            field={TaxonAttributeDefinition.FIELDS.CODE}
            label={codeFieldLabel}
            inputField={codeField}
            labelWidth={labelWidth}
          />
        )
        const scientificNameFieldLabel = 'Scientific name'
        const scientificNameFormItem = (
          <CompositeAttributeFormItem
            key={TaxonAttributeDefinition.FIELDS.SCIENTIFIC_NAME}
            field={TaxonAttributeDefinition.FIELDS.SCIENTIFIC_NAME}
            label={scientificNameFieldLabel}
            inputField={scientificNameField}
            labelWidth={labelWidth}
          />
        )
        const vernacularNameFieldLabel = 'Vernacular name'
        const vernacularNameFormItem = (
          <CompositeAttributeFormItem
            key={TaxonAttributeDefinition.FIELDS.VERNACULAR_NAME}
            field={TaxonAttributeDefinition.FIELDS.VERNACULAR_NAME}
            label={vernacularNameFieldLabel}
            inputField={vernacularNameField}
            labelWidth={labelWidth}
          />
        )
        internalParts = [codeFormItem, scientificNameFormItem, vernacularNameFormItem]
      }
      return internalParts
    }

    return getInternalContent()
  }
}
