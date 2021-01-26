import React from 'react'

import { TaxonAttributeDefinition } from 'model/Survey'

const widthByField = {
  [TaxonAttributeDefinition.Fields.CODE]: '150px',
  [TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME]: '300px',
  [TaxonAttributeDefinition.Fields.VERNACULAR_NAME]: '200px',
  [TaxonAttributeDefinition.Fields.LANGUAGE_CODE]: '50px',
  // [TaxonAttributeDefinition.Fields.LANGUAGE_VARIETY]: '50px',
}

const TaxonAutoCompleteDialogItem = (props) => {
  const { taxonOccurrence, attributeDefinition } = props
  const { availableFieldNames } = attributeDefinition

  const visibleFields = Object.keys(widthByField).filter((field) => availableFieldNames.includes(field))
  const gridTemplateColumns = visibleFields.map((field) => widthByField[field]).join(' ')

  return (
    <div className="taxon-autocomplete-dialog-item" style={{ gridTemplateColumns }}>
      {visibleFields.map((field) => {
        const valueFieldName = TaxonAttributeDefinition.ValueFieldByField[field]
        const fieldValue = taxonOccurrence[valueFieldName]
        return <div>{fieldValue}</div>
      })}
    </div>
  )
}

export default TaxonAutoCompleteDialogItem
