import React from 'react'

import ServiceFactory from 'services/ServiceFactory'
import { CodeAttributeUpdatedEvent } from 'model/event/RecordEvent'
import { CodeFieldDefinition } from 'model/ui/CodeFieldDefinition'
import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
import Arrays from 'utils/Arrays'
import Strings from 'utils/Strings'
import AbstractField from '../AbstractField'
import CodeFieldRadio from './CodeFieldRadio'
import CodeFieldAutocomplete from './CodeFieldAutocomplete'

const MAX_ITEMS = 100

const EMPTY_OPTION = (
  <option key="-1" value="-1">
    --- Select one ---
  </option>
)

export default class CodeField extends AbstractField {
  constructor(props) {
    super(props)

    this.state = {
      ...this.state,
      value: { code: '' },
      items: [],
      loading: true,
      asynchronous: false,
      ancestorCodes: null,
    }

    this.onCodeListItemSelect = this.onCodeListItemSelect.bind(this)
  }

  componentDidMount() {
    super.componentDidMount()
    this.onParentEntityChange()
  }

  componentDidUpdate(prevProps) {
    const { parentEntity } = this.props
    const { parentEntity: prevParentEntity } = prevProps

    if (prevParentEntity !== parentEntity) {
      this.onParentEntityChange()
    }
  }

  fromCodeToValue(code) {
    const { fieldDef } = this.props
    const { attributeDefinition } = fieldDef
    const { layout } = attributeDefinition

    const nullCode = [CodeFieldDefinition.Layouts.DROPDOWN].includes(layout) ? EMPTY_OPTION.value : ''
    const codeUpdated = code === null ? nullCode : code

    return { code: codeUpdated }
  }

  onParentEntityChange() {
    const { parentEntity } = this.props

    if (parentEntity) {
      this.setState({ loading: true, items: [] }, () => this.loadCodeListItems())
    }
  }

  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { fieldDef } = this.props
    const { attributeDefinition } = fieldDef
    const { ancestorCodeAttributeDefinitionIds } = attributeDefinition

    if (
      ancestorCodeAttributeDefinitionIds.length > 0 &&
      event instanceof CodeAttributeUpdatedEvent &&
      ancestorCodeAttributeDefinitionIds.includes(Number(event.definitionId))
    ) {
      this.onParentEntityChange()
    }
  }

  async loadCodeListItems() {
    const { parentEntity, fieldDef } = this.props
    const { survey, record } = parentEntity
    const { id: surveyId } = survey
    const { versionId } = record
    const { attributeDefinition } = fieldDef
    const { codeListId, levelIndex } = attributeDefinition

    const ancestorCodes = record.getAncestorCodeValues({
      contextEntity: parentEntity,
      attributeDefinition,
    })

    if (levelIndex === 0 || (ancestorCodes && ancestorCodes.length == levelIndex)) {
      const count = await ServiceFactory.codeListService.countAvailableItems({
        surveyId,
        codeListId,
        versionId,
        ancestorCodes,
      })

      const asynchronous = count > MAX_ITEMS
      const items = asynchronous
        ? null
        : await ServiceFactory.codeListService.loadAllAvailableItems({
            surveyId,
            codeListId,
            versionId,
            ancestorCodes,
          })

      this.setState({ asynchronous, items, ancestorCodes }, () => this.updateStateFromProps())
    } else {
      this.setState({ loading: false, asynchronous: false, items: [] })
    }
  }

  async updateStateFromProps() {
    const { ancestorCodes, asynchronous, items } = this.state
    const { parentEntity, fieldDef } = this.props

    const values = this.extractValuesFromProps()

    let selectedItems = null
    if (asynchronous) {
      const { survey, record } = parentEntity
      const { id: surveyId } = survey
      const { versionId } = record
      const { attributeDefinition } = fieldDef
      const { codeListId } = attributeDefinition

      const selectedItemsFetched = await Promise.all(
        values.map((value) =>
          ServiceFactory.codeListService.loadItem({
            surveyId,
            codeListId,
            versionId,
            ancestorCodes,
            code: value.code,
          })
        )
      )
      selectedItems = selectedItemsFetched.map((item, index) => (item ? item : values[index]))
    } else {
      selectedItems = values.map((value) => {
        const item = items.find((item) => item.code === value.code)
        return item ? item : value
      })
    }
    this.setState({ loading: false, selectedItems })
  }

  onCodeListItemSelect(item, selected = true) {
    const { fieldDef } = this.props
    const { selectedItems } = this.state
    const { attributeDefinition } = fieldDef
    const { multiple } = attributeDefinition

    const itemCode = item?.code

    if (multiple) {
      const selectedItemsUpdated = itemCode
        ? selected
          ? [...selectedItems, item] // add item
          : selectedItems.filter((itm) => itm.code !== itemCode) // remove item
        : []
      const values = this.extractValuesFromProps()
      const valuesUpdated = itemCode
        ? selected
          ? [...values, this.fromCodeToValue(itemCode)] // add value
          : values.filter((value) => value.code !== itemCode) // remove value
        : []
      this._updateValues({ selectedItems: selectedItemsUpdated, valuesByField: valuesUpdated })
    } else {
      const code = selected ? itemCode : null
      const value = this.fromCodeToValue(code)
      this.updateValue({ value, debounced: false })
    }
  }

  onCodeListItemsSelect(items) {
    const selectedItemsUpdated = items
    const values = this.extractValuesFromProps()
    const selectedCodesPrev = values.map((value) => value.code)
    const selectedCodesCurrent = items.map((item) => item.code)
    const selectedCodesAdded = Arrays.difference(selectedCodesCurrent, selectedCodesPrev)
    const selectedCodesRemoved = Arrays.difference(selectedCodesPrev, selectedCodesCurrent)
    let valuesUpdated = [...values]
    valuesUpdated = selectedCodesRemoved.reduce(
      (valuesAcc, codeRemoved) => valuesAcc.filter((item) => item.code !== codeRemoved),
      valuesUpdated
    )
    valuesUpdated = selectedCodesAdded.reduce(
      (valuesAcc, codeAdded) => [...valuesAcc, this.fromCodeToValue(codeAdded)],
      valuesUpdated
    )
    this._updateValues({ selectedItems: selectedItemsUpdated, valuesByField: valuesUpdated })
  }

  _updateValues({ selectedItems, valuesByField }) {
    const { parentEntity, fieldDef } = this.props
    const { attributeDefinition } = fieldDef

    this.updateWithDebounce({
      state: { selectedItems },
      debounced: false,
      updateFn: () =>
        ServiceFactory.commandService.updateMultipleAttribute({
          parentEntity,
          attributeDefinition,
          valuesByField,
        }),
    })
  }

  render() {
    const { fieldDef, parentEntity } = this.props
    const { items, selectedItems, asynchronous, loading, ancestorCodes } = this.state

    if (loading) {
      return <LoadingSpinnerSmall />
    }

    const { attributeDefinition } = fieldDef
    const { layout, showCode, multiple } = attributeDefinition

    const itemLabelFunction = (item) => {
      const parts = []
      if (showCode || Strings.isBlank(item.label)) {
        parts.push(item.code)
      }
      if (Strings.isNotBlank(item.label)) {
        parts.push(item.label)
      }
      return parts.join(' - ')
    }

    return !asynchronous && layout === CodeFieldDefinition.Layouts.RADIO ? (
      <CodeFieldRadio
        parentEntity={parentEntity}
        attributeDefinition={attributeDefinition}
        selectedItems={selectedItems}
        items={items}
        itemLabelFunction={itemLabelFunction}
        onChange={this.onCodeListItemSelect}
      />
    ) : (
      <CodeFieldAutocomplete
        parentEntity={parentEntity}
        fieldDef={fieldDef}
        asynchronous={asynchronous}
        items={items}
        selectedItems={selectedItems}
        ancestorCodes={ancestorCodes}
        itemLabelFunction={itemLabelFunction}
        onSelect={(selection) =>
          multiple ? this.onCodeListItemsSelect(selection) : this.onCodeListItemSelect(selection, true)
        }
      />
    )
  }
}
