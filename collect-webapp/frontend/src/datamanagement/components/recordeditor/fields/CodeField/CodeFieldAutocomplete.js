import React, { useCallback } from 'react'
import { debounce } from 'throttle-debounce'

import ServiceFactory from 'services/ServiceFactory'
import Autocomplete from 'common/components/Autocomplete'
import * as FieldsSizes from '../FieldsSizes'
import Strings from 'utils/Strings'

const CodeFieldAutocomplete = (props) => {
  const {
    parentEntity,
    fieldDef,
    inTable,
    selectedItems,
    asynchronous,
    items,
    ancestorCodes,
    itemLabelFunction,
    onSelect,
  } = props
  const { survey, record } = parentEntity
  const { attributeDefinition } = fieldDef
  const { versionId } = record
  const { codeListId, multiple } = attributeDefinition

  const language = survey.defaultLanguage // TODO
  const surveyId = survey.id

  const fetchCodeItems = useCallback(
    ({ surveyId, codeListId, versionId, language, ancestorCodes }) => ({ searchString, onComplete }) =>
      debounce(Strings.isBlank(searchString) ? 0 : 1000, false, async () => {
        const items = await ServiceFactory.codeListService.findAvailableItems({
          surveyId,
          codeListId,
          versionId,
          language,
          ancestorCodes,
          searchString,
        })
        onComplete(items)
      }),
    [surveyId, codeListId, versionId, language, ancestorCodes]
  )

  return (
    <Autocomplete
      asynchronous={asynchronous}
      multiple={multiple}
      items={items}
      inputFieldWidth={FieldsSizes.getWidth({ fieldDef, inTable })}
      selectedItems={selectedItems}
      fetchFunction={fetchCodeItems({ surveyId, codeListId, versionId, language, ancestorCodes })}
      itemLabelFunction={itemLabelFunction}
      itemSelectedFunction={(item, value) => item.code === value.code}
      itemRenderFunction={(item) => <div title={item.description}>{itemLabelFunction(item)}</div>}
      onSelect={onSelect}
    />
  )
}

export default CodeFieldAutocomplete
