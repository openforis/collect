import './CodeFieldAutocomplete.css'

import React, { useCallback } from 'react'
import classNames from 'classnames'
import { debounce } from 'throttle-debounce'

import ServiceFactory from 'services/ServiceFactory'
import Autocomplete from 'common/components/Autocomplete'
import Strings from 'utils/Strings'
import Arrays from 'utils/Arrays'

import * as FieldsSizes from '../FieldsSizes'
import CodeFieldItemLabel from './CodeFieldItemLabel'
import CodeFieldQualifier from './CodeFieldQualifier'

const CodeFieldAutocomplete = (props) => {
  const {
    readOnly,
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
  const { codeListId } = attributeDefinition

  const fetchCodeItems = useCallback(
    ({ surveyId, codeListId, versionId, language, ancestorCodes }) =>
      ({ searchString, onComplete }) =>
        debounce(Strings.isBlank(searchString) ? 0 : 1000, false, async () => {
          const availableItems = await ServiceFactory.codeListService.findAvailableItems({
            surveyId,
            codeListId,
            versionId,
            language,
            ancestorCodes,
            searchString,
          })
          onComplete(availableItems)
        }),
    [surveyId, codeListId, versionId, language, ancestorCodes]
  )

  const selectedItem = Arrays.head(selectedItems)
  const qualifiableItemSelected = selectedItem && selectedItem.qualifiable

  return (
    <div className={classNames('code-field-autocomplete-wrapper', { qualifiable: qualifiableItemSelected })}>
      <Autocomplete
        asynchronous={asynchronous}
        readOnly={readOnly}
        items={items}
        inputFieldWidth={width || FieldsSizes.getWidth({ fieldDef, inTable })}
        selectedItems={selectedItems}
        fetchFunction={fetchCodeItems({ surveyId, codeListId, versionId, language, ancestorCodes })}
        isItemEqualToValue={({ item, value }) => item.code === value.code}
        itemLabelFunction={itemLabelFunction}
        itemRenderFunction={({ item }) => <CodeFieldItemLabel item={item} attributeDefinition={attributeDefinition} />}
        onSelect={onSelect}
      />
      {!inTable && qualifiableItemSelected && (
        <CodeFieldQualifier
          code={selectedItem.code}
          qualifier={values[0].qualifier}
          onChangeQualifier={onChangeQualifier}
          readOnly={readOnly}
        />
      )}
    </div>
  )
}

export default CodeFieldAutocomplete
