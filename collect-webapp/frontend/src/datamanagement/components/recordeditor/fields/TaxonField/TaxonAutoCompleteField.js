import React from 'react'
import Autocomplete from 'common/components/Autocomplete'
import { debounce } from 'throttle-debounce'

import ServiceFactory from 'services/ServiceFactory'
import { TaxonAttributeDefinition } from 'model/Survey'
import Arrays from 'utils/Arrays'
import Objects from 'utils/Objects'

import TaxonAutoCompleteDialogItem from './TaxonAutoCompleteDialogItem'
import * as FieldsSizes from '../FieldsSizes'

const fetchTaxa =
  ({ surveyId, fieldDef, queryField }) =>
  ({ searchString, onComplete }) => {
    return debounce(1000, async () => {
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
  const { parentEntity, fieldDef, field, valueByFields, onInputChange, onSelect, onDismiss, readOnly } = props

  const { attributeDefinition } = fieldDef

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
      taxonOccurrenceUpdated[valueField] = inputValue.trim()
    }
    onSelect(taxonOccurrenceUpdated)
  }

  return (
    <Autocomplete
      asynchronous
      inputValue={initialInputValue}
      inputFieldWidth={FieldsSizes.TaxonFieldWidths[field]}
      selectedItems={Arrays.singleton(selectedTaxonOccurrence)}
      fetchFunction={fetchTaxa({ surveyId, fieldDef, queryField })}
      isItemEqualToValue={({ item, value }) => item.code === value.code}
      itemLabelFunction={Objects.getProp(valueField, '')}
      itemRenderFunction={({ item: taxonOccurrence }) => (
        <TaxonAutoCompleteDialogItem attributeDefinition={attributeDefinition} taxonOccurrence={taxonOccurrence} />
      )}
      onInputChange={onInputChange}
      onSelect={onTaxonSelected}
      onDismiss={onDismiss}
      readOnly={readOnly}
      popUpWidthContentBased
    />
  )
}

export default TaxonAutoCompleteField
