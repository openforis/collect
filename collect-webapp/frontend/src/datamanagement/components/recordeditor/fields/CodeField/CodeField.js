import './CodeField.css'

import React from 'react'
import { InputLabel } from '@material-ui/core'

import ServiceFactory from 'services/ServiceFactory'
import { CodeAttributeUpdatedEvent } from 'model/event/RecordEvent'
import { CodeFieldDefinition } from 'model/ui/CodeFieldDefinition'

import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'

import Arrays from 'utils/Arrays'
import Strings from 'utils/Strings'

import AbstractField from '../AbstractField'
import * as FieldsSizes from '../FieldsSizes'
import CodeFieldRadio from './CodeFieldRadio'
import CodeFieldAutocomplete from './CodeFieldAutocomplete'
import CodeFieldItemLabel, { itemLabelFunction } from './CodeFieldItemLabel'
import CodeFieldText from './CodeFieldText'

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
      values: [],
      items: [],
      loading: true,
      asynchronous: false,
      ancestorCodes: null,
      parentCodeMissing: false,
    }

    this.onCodeListItemSelect = this.onCodeListItemSelect.bind(this)
    this.onChangeQualifier = this.onChangeQualifier.bind(this)
  }

  componentDidMount() {
    super.componentDidMount()
    this.onParentEntityChange()
  }

  onParentEntityChange() {
    super.onParentEntityChange()
    this.updateItemsState()
  }

  async updateStateFromProps() {
    const { ancestorCodes, asynchronous, items } = this.state
    const { parentEntity, fieldDef } = this.props

    const values = this.extractValuesFromProps()
    const valuesNotEmpty = values.filter((value) => Strings.isNotBlank(value.code))

    let selectedItems = null
    if (asynchronous) {
      const { survey, record } = parentEntity
      const { id: surveyId } = survey
      const { versionId } = record
      const { attributeDefinition } = fieldDef
      const { codeListId } = attributeDefinition

      const selectedItemsFetched = await Promise.all(
        valuesNotEmpty.map(async (value) => {
          if (Strings.isBlank(value.code)) {
            return null
          }
          try {
            return await ServiceFactory.codeListService.loadItem({
              surveyId,
              codeListId,
              versionId,
              ancestorCodes,
              code: value.code,
            })
          } catch (e) {
            return null
          }
        })
      )
      selectedItems = selectedItemsFetched.map((item, index) => (item ? item : valuesNotEmpty[index]))
    } else {
      selectedItems = valuesNotEmpty.map((value) => {
        const item = items.find((item) => item.code === value.code)
        return item ? item : value
      })
    }
    this.setState({ loading: false, selectedItems, values })
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
      this.updateItemsState()
    }
  }

  async loadAllAvailableItems() {
    const { parentEntity, fieldDef } = this.props
    const { survey, record } = parentEntity
    const { id: surveyId } = survey
    const { versionId } = record
    const { attributeDefinition } = fieldDef
    const { codeListId } = attributeDefinition

    const ancestorCodes = record.getAncestorCodeValues({
      contextEntity: parentEntity,
      attributeDefinition,
    })

    return await ServiceFactory.codeListService.loadAllAvailableItems({
      surveyId,
      codeListId,
      versionId,
      ancestorCodes,
      language: survey.preferredLanguage,
    })
  }

  fromCodeToValue(code) {
    const { fieldDef } = this.props
    const { attributeDefinition } = fieldDef
    const { layout } = attributeDefinition

    const nullCode = [CodeFieldDefinition.Layouts.DROPDOWN].includes(layout) ? EMPTY_OPTION.value : ''
    const codeUpdated = code === null ? nullCode : code

    return { code: codeUpdated }
  }

  updateItemsState() {
    this.setState({ loading: true, items: [] }, async () => {
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

      if (levelIndex === 0 || (ancestorCodes && ancestorCodes.length === levelIndex)) {
        const count = await ServiceFactory.codeListService.countAvailableItems({
          surveyId,
          codeListId,
          versionId,
          ancestorCodes,
        })

        const asynchronous = count > MAX_ITEMS
        const items = asynchronous ? null : await this.loadAllAvailableItems()
        this.setState({ asynchronous, ancestorCodes, parentCodeMissing: false, items }, () =>
          this.updateStateFromProps()
        )
      } else {
        this.setState({ asynchronous: false, ancestorCodes, parentCodeMissing: true, items: [], loading: false })
      }
    })
  }

  onChangeQualifier({ code, qualifier }) {
    const { parentEntity, fieldDef } = this.props
    const { values } = this.state
    const { attributeDefinition } = fieldDef

    const valuesUpdated = [...values]
    const valueIndex = values.findIndex((value) => value.code === code)
    const value = values[valueIndex]
    valuesUpdated[valueIndex] = { ...value, qualifier }

    this.updateWithDebounce({
      state: { values: valuesUpdated },
      updateFn: () =>
        ServiceFactory.commandService.updateMultipleAttribute({
          parentEntity,
          attributeDefinition,
          valuesByField: valuesUpdated,
        }),
    })
  }

  onCodeListItemSelect({ item, selected = true }) {
    const { fieldDef } = this.props
    const { selectedItems } = this.state
    const { attributeDefinition } = fieldDef
    const { multiple } = attributeDefinition

    const code = item?.code

    if (multiple) {
      const selectedItemsUpdated = code
        ? selected
          ? [...selectedItems, item] // add item
          : selectedItems.filter((itm) => itm.code !== code) // remove item
        : []
      this.onCodeListItemsSelect(selectedItemsUpdated)
    } else {
      const value = this.fromCodeToValue(selected ? code : null)
      this.updateValue({ value, debounced: false })
    }
  }

  onCodeListItemsSelect(items) {
    const { parentEntity, fieldDef } = this.props
    const { values } = this.state
    const { attributeDefinition } = fieldDef

    const selectedItemsUpdated = items
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

    this.updateWithDebounce({
      state: { values: valuesUpdated, selectedItems: selectedItemsUpdated },
      debounced: false,
      updateFn: () =>
        ServiceFactory.commandService.updateMultipleAttribute({
          parentEntity,
          attributeDefinition,
          valuesByField: valuesUpdated,
        }),
    })
  }

  render() {
    const { fieldDef, inTable, parentEntity } = this.props
    const { items, selectedItems, asynchronous, loading, ancestorCodes, values } = this.state

    if (loading) {
      return <LoadingSpinnerSmall />
    }

    const { attributeDefinition } = fieldDef
    const { layout, multiple, enumerator, hasQualifiableItems } = attributeDefinition

    if (enumerator) {
      return (
        <InputLabel style={{ width: FieldsSizes.getWidth({ fieldDef, textAlign: 'left' }) }}>
          <CodeFieldItemLabel item={Arrays.head(selectedItems)} attributeDefinition={attributeDefinition} />
        </InputLabel>
      )
    }

    if (
      (multiple && (asynchronous || inTable || layout !== CodeFieldDefinition.Layouts.RADIO)) ||
      (inTable && hasQualifiableItems)
    ) {
      return (
        <CodeFieldText
          asynchronous={asynchronous}
          parentEntity={parentEntity}
          ancestorCodes={ancestorCodes}
          attributeDefinition={attributeDefinition}
          fieldDef={fieldDef}
          itemLabelFunction={itemLabelFunction(attributeDefinition)}
          items={items}
          onChange={this.onCodeListItemSelect}
          onChangeQualifier={this.onChangeQualifier}
          selectedItems={selectedItems}
          values={values}
        />
      )
    }

    if (!inTable && !asynchronous && layout === CodeFieldDefinition.Layouts.RADIO) {
      return (
        <CodeFieldRadio
          parentEntity={parentEntity}
          attributeDefinition={attributeDefinition}
          values={values}
          items={items}
          onChange={this.onCodeListItemSelect}
          onChangeQualifier={this.onChangeQualifier}
        />
      )
    }

    return (
      <CodeFieldAutocomplete
        parentEntity={parentEntity}
        fieldDef={fieldDef}
        asynchronous={asynchronous}
        inTable={inTable}
        items={items}
        selectedItems={selectedItems}
        values={values}
        ancestorCodes={ancestorCodes}
        itemLabelFunction={itemLabelFunction(attributeDefinition)}
        onSelect={(item) => this.onCodeListItemSelect({ item, selected: Boolean(item) })}
        onChangeQualifier={this.onChangeQualifier}
      />
    )
  }
}
