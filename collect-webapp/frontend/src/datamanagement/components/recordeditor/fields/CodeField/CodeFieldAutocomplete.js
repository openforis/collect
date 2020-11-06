import React from 'react'
import { debounce } from 'throttle-debounce'

import ServiceFactory from 'services/ServiceFactory'
import AutocompleteAsync from 'common/components/AutocompleteAsync'

const fetchCodeItems = ({ surveyId, codeListId, versionId, language, ancestorCodes }) => ({
  searchString,
  onComplete,
}) => {
  const fetchCodeItemsDebounced = debounce(1000, false, async () => {
    const items = await ServiceFactory.codeListService.findAvailableItems({
      surveyId,
      codeListId,
      versionId,
      language,
      ancestorCodes,
      searchString,
    })
    onComplete(items)
  })

  return fetchCodeItemsDebounced
}

const CodeFieldAutocomplete = (props) => {
  const { parentEntity, fieldDef, selectedItem, onSelect } = props
  const { survey, record } = parentEntity
  const { attributeDefinition } = fieldDef

  const { versionId } = record
  const { codeListId } = attributeDefinition
  const language = null //TODO
  const surveyId = survey.id
  const ancestorCodes = []

  return (
    <AutocompleteAsync
      inputFieldWidth={300}
      selectedItem={selectedItem}
      fetchFunction={fetchCodeItems({ surveyId, codeListId, versionId, language, ancestorCodes })}
      optionLabelFunction={(item) => `${item.code}${item.label ? ` - ${item.label}` : ''}`}
      optionSelectedFunction={(item, value) => item.code === value.code}
      onSelect={onSelect}
    />
  )
}

export default CodeFieldAutocomplete
