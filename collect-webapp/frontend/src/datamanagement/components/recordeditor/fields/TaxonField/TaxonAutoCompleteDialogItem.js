import React from 'react'

const TaxonAutoCompleteDialogItem = (props) => {
  const { taxonOccurrence, showFamily } = props
  const gridTemplateColumns = `${showFamily ? '150px 300px ' : ''}150px 300px 200px 50px 50px`

  const { code, scientificName, vernacularName, languageCode, languageVariety } = taxonOccurrence
  return (
    <div className="taxon-autocomplete-dialog-item" style={{ gridTemplateColumns }}>
      <div>{code}</div>
      <div>{scientificName}</div>
      <div>{vernacularName}</div>
      <div>{languageCode}</div>
      <div>{languageVariety}</div>
    </div>
  )
}

export default TaxonAutoCompleteDialogItem
