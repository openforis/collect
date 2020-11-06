import React, { useEffect, useState } from 'react'
import Autocomplete from '@material-ui/lab/Autocomplete'
import { TextField } from '@material-ui/core'
import { debounce } from 'throttle-debounce'

import ServiceFactory from 'services/ServiceFactory'
import { TaxonAttributeDefinition } from 'model/Survey'
import Objects from 'utils/Objects'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import TaxonAutoCompleteDialogItem from './TaxonAutoCompleteDialogItem'
import * as FieldsSizes from '../FieldsSizes'

const fetchTaxa = async ({ surveyId, fieldDef, queryField, searchString, onComplete }) => {
  const { attributeDefinition } = fieldDef
  const { allowUnlisted, highestRank, includeUniqueVernacularName, showFamily, taxonomyName } = attributeDefinition

  const query = {
    field: queryField,
    searchString,
    parameters: { highestRank, includeUniqueVernacularName, includeAncestorTaxons: showFamily },
  }
  const fetchTaxaDebounced = debounce(1000, false, async () => {
    const taxa = await ServiceFactory.speciesService.findTaxa({ surveyId, taxonomyName, query })
    if (taxa.length === 0 && allowUnlisted) {
      taxa.push(
        {
          [TaxonAttributeDefinition.ValueFields.CODE]: 'UNK',
          [TaxonAttributeDefinition.ValueFields.SCIENTIFIC_NAME]: 'Unknown',
        },
        {
          [TaxonAttributeDefinition.ValueFields.CODE]: 'UNL',
          [TaxonAttributeDefinition.ValueFields.SCIENTIFIC_NAME]: 'Unlisted',
        }
      )
    }
    onComplete(taxa)
  })
  fetchTaxaDebounced()

  return fetchTaxaDebounced
}

const TaxonAutoCompleteField = (props) => {
  const { parentEntity, fieldDef, field, valueByFields, onInputChange: onInputChangeProps, onSelect, onDismiss } = props

  const { attributeDefinition } = fieldDef
  const { showFamily } = attributeDefinition

  const queryField = TaxonAttributeDefinition.QueryFieldByField[field]

  const surveyId = parentEntity.survey.id

  const valueField = TaxonAttributeDefinition.ValueFieldByField[field]
  const initialInputValue = Objects.getProp(field, '')(valueByFields)
  const selectedTaxonOccurrence = Objects.mapKeys({
    obj: valueByFields,
    keysMapping: TaxonAttributeDefinition.ValueFieldByField,
  })

  const [state, setStateInternal] = useState({
    open: false,
    loading: false,
    taxonOccurrences: [],
    inputValue: initialInputValue,
    fetchTaxaDebounced: null,
  })
  const setState = (stateUpdated) => setStateInternal({ ...state, ...stateUpdated })

  const { open, loading, taxonOccurrences, inputValue, fetchTaxaDebounced } = state

  // fetch taxa on "open" and "inputValue" change
  useEffect(() => {
    if (!loading) {
      return undefined
    }

    let active = true // prevents rendering of an unmounted component

    ;(async () => {
      if (fetchTaxaDebounced) {
        fetchTaxaDebounced.cancel()
      }
      const fetchDataDebouncedNew = await fetchTaxa({
        surveyId,
        fieldDef,
        queryField,
        searchString: inputValue,
        onComplete: (taxa) => {
          if (active) {
            setState({ taxonOccurrences: taxa, loading: false })
          }
        },
      })
      setState({ fetchTaxaDebounced: fetchDataDebouncedNew })
    })()

    return () => {
      active = false
    }
  }, [loading, inputValue])

  // set input initial value on "initialInputValue" change (if dialog not open)
  useEffect(() => {
    if (!open) {
      setState({ inputValue: initialInputValue })
    }
  }, [initialInputValue])

  // on dialog open, trigger loading
  useEffect(() => {
    const stateUpdated = { loading: open }
    if (!open) {
      stateUpdated.taxonOccurrences = []
    }
    setState(stateUpdated)
  }, [open])

  // on input value change re-fetch taxa
  useEffect(() => {
    if (open) {
      setState({ taxonOccurrences: [], loading: true })
    }
  }, [inputValue])

  const onInputChange = (_event, value, reason) => {
    if (reason === 'input') {
      onInputChangeProps(value)
    }
    setState({ inputValue: value })
  }

  const onOpen = () => {
    setState({ open: true })
  }

  const onClose = (_, reason) => {
    setState({ open: false })
    if (['escape', 'blur'].includes(reason)) {
      onDismiss()
    }
  }

  const onTaxonSelected = (taxonOccurrence) => {
    const taxonOccurrenceUpdated = taxonOccurrence
    if (
      taxonOccurrence &&
      [
        TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME,
        TaxonAttributeDefinition.Fields.VERNACULAR_NAME,
        TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME,
      ].includes(field) &&
      ['UNK', 'UNL'].includes(taxonOccurrence.code)
    ) {
      taxonOccurrenceUpdated[valueField] = inputValue
    }
    onSelect(taxonOccurrenceUpdated)
  }

  return (
    <Autocomplete
      open={open}
      openOnFocus={false}
      onOpen={onOpen}
      onClose={onClose}
      value={selectedTaxonOccurrence}
      inputValue={inputValue}
      onChange={(_, taxonOccurrence) => onTaxonSelected(taxonOccurrence)}
      onInputChange={onInputChange}
      getOptionLabel={Objects.getProp(valueField, '')}
      getOptionSelected={(taxonOccurrence, value) => taxonOccurrence.code === value.code}
      options={taxonOccurrences}
      filterOptions={() => taxonOccurrences}
      loading={loading}
      renderInput={(params) => (
        <TextField
          {...params}
          fullWidth={false}
          style={{ width: `${FieldsSizes.TaxonFieldWidths[field]}px` }}
          variant="outlined"
          InputProps={{
            ...params.InputProps,
            endAdornment: (
              <>
                {loading && <LoadingSpinnerSmall />}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
      renderOption={(taxonOccurrence) => (
        <TaxonAutoCompleteDialogItem taxonOccurrence={taxonOccurrence} showFamily={showFamily} />
      )}
    />
  )
}

export default TaxonAutoCompleteField
