import React from 'react'
import Autocomplete from 'common/components/Autocomplete'
import { debounce } from 'throttle-debounce'

import ServiceFactory from 'services/ServiceFactory'
import { TaxonAttributeDefinition } from 'model/Survey'
import Objects from 'utils/Objects'

import TaxonAutoCompleteDialogItem from './TaxonAutoCompleteDialogItem'
import * as FieldsSizes from '../FieldsSizes'

const fetchTaxa = ({ surveyId, fieldDef, queryField }) => ({ searchString, onComplete }) => {
  return debounce(1000, false, async () => {
    const { attributeDefinition } = fieldDef
    const { allowUnlisted, highestRank, includeUniqueVernacularName, showFamily, taxonomyName } = attributeDefinition

    const query = {
      field: queryField,
      searchString,
      parameters: { highestRank, includeUniqueVernacularName, includeAncestorTaxons: showFamily },
    }
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

  const onTaxonSelected = (taxonOccurrence, inputValue) => {
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
      asynchronous
      inputFieldValue={initialInputValue}
      inputFieldWidth={FieldsSizes.TaxonFieldWidths[field]}
      selectedItem={selectedTaxonOccurrence}
      fetchFunction={fetchTaxa({ surveyId, fieldDef, queryField })}
      itemLabelFunction={Objects.getProp(valueField, '')}
      itemSelectedFunction={(item, value) => item.code === value.code}
      itemRenderFunction={(taxonOccurrence) => (
        <TaxonAutoCompleteDialogItem taxonOccurrence={taxonOccurrence} showFamily={showFamily} />
      )}
      onSelect={onTaxonSelected}
      onDismiss={onDismiss}
    />
  )
}

export default TaxonAutoCompleteField
