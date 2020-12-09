import React, { useCallback } from 'react'
import { debounce } from 'throttle-debounce'

import ServiceFactory from 'services/ServiceFactory'
import Autocomplete from 'common/components/Autocomplete'
import Strings from 'utils/Strings'

import * as FieldsSizes from '../FieldsSizes'
import CodeFieldRadioItem from './CodeFieldRadioItem'
import CodeFieldItemLabel from './CodeFieldItemLabel'

const CodeFieldAutocomplete = (props) => {
  const {
    parentEntity,
    fieldDef,
    inTable,
    selectedItems,
    values,
    asynchronous,
    items,
    ancestorCodes,
    itemLabelFunction,
    onSelect,
    onChangeQualifier,
    width,
  } = props
  const { survey, record } = parentEntity
  const { id: surveyId, preferredLanguage: language } = survey
  const { attributeDefinition } = fieldDef
  const { versionId } = record
  const { calculated, codeListId } = attributeDefinition
  const readOnly = record.readOnly || calculated

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
      readOnly={readOnly}
      items={items}
      inputFieldWidth={width || FieldsSizes.getWidth({ fieldDef, inTable })}
      selectedItems={selectedItems}
      fetchFunction={fetchCodeItems({ surveyId, codeListId, versionId, language, ancestorCodes })}
      itemLabelFunction={itemLabelFunction}
      itemSelectedFunction={(item, value) => item.code === value.code}
      itemRenderFunction={(item) => <CodeFieldItemLabel item={item} attributeDefinition={attributeDefinition} />}
      onSelect={onSelect}
    />
  )
}

export default CodeFieldAutocomplete
