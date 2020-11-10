import React from 'react'

import ServiceFactory from 'services/ServiceFactory'
import { CodeAttributeUpdatedEvent } from 'model/event/RecordEvent'
import { CodeFieldDefinition } from 'model/ui/CodeFieldDefinition'
import LoadingSpinnerSmall from 'common/components/LoadingSpinnerSmall'
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

    this.onInputChange = this.onInputChange.bind(this)
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
    const { layout } = fieldDef

    const nullCode = layout === CodeFieldDefinition.Layouts.DROPDOWN ? EMPTY_OPTION.value : ''
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

  onInputChange(event) {
    const { fieldDef } = this.props
    const { layout } = fieldDef
    const debounced = layout === CodeFieldDefinition.Layouts.TEXT
    const code = event.target.value
    const value = this.fromCodeToValue(code)
    this.onAttributeUpdate({ value, debounced })
  }

  onCodeListItemSelect(item) {
    const value = item ? this.fromCodeToValue(item.code) : null
    this.onAttributeUpdate({ value, debounced: false })
  }

  render() {
    const { fieldDef, parentEntity } = this.props
    const { items, selectedItems, asynchronous, loading } = this.state

    if (loading) {
      return <LoadingSpinnerSmall />
    }

    const { attributeDefinition, layout } = fieldDef

    return !asynchronous && layout === CodeFieldDefinition.Layouts.RADIO ? (
      <CodeFieldRadio
        parentEntity={parentEntity}
        attributeDefinition={attributeDefinition}
        selectedItems={selectedItems}
        items={items}
        onChange={this.onInputChange}
      />
    ) : (
      <CodeFieldAutocomplete
        parentEntity={parentEntity}
        fieldDef={fieldDef}
        asynchronous={asynchronous}
        items={items}
        selectedItems={selectedItems}
        onSelect={this.onCodeListItemSelect}
      />
    )
  }
}
