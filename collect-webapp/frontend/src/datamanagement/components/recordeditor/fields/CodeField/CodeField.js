import React from 'react'

import ServiceFactory from 'services/ServiceFactory'
import { CodeAttributeUpdatedEvent } from 'model/event/RecordEvent'
import { CodeFieldDefinition } from 'model/ui/CodeFieldDefinition'
import AbstractSingleAttributeField from '../AbstractSingleAttributeField'
import CodeFieldRadio from './CodeFieldRadio'
import CodeFieldAutocomplete from './CodeFieldAutocomplete'

const MAX_ITEMS = 100

const EMPTY_OPTION = (
  <option key="-1" value="-1">
    --- Select one ---
  </option>
)

export default class CodeField extends AbstractSingleAttributeField {
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
    const { parentEntity, fieldDef } = this.props
    const { attributeDefinition } = fieldDef

    if (parentEntity) {
      const { record } = parentEntity

      const ancestorCodes = record.getAncestorCodeValues({
        contextEntity: parentEntity,
        attributeDefinition,
      })

      this.setState({ loading: true, ancestorCodes }, () => this.loadCodeListItems())
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
    const { ancestorCodes } = this.state
    const attr = this.getAttribute()
    if (attr) {
      const { definition, survey, record } = attr
      const { versionId } = record
      const { codeListId, levelIndex } = definition

      if (levelIndex === 0 || (ancestorCodes && ancestorCodes.length >= levelIndex)) {
        const value = this.extractValueFromProps()

        const count = await ServiceFactory.codeListService.countAvailableItems({
          surveyId: survey.id,
          codeListId,
          versionId,
          ancestorCodes,
        })

        const asynchronous = count > MAX_ITEMS
        const items = asynchronous
          ? null
          : await ServiceFactory.codeListService.loadAllAvailableItems({
              surveyId: survey.id,
              codeListId,
              versionId,
              ancestorCodes,
            })
        this.setState({ loading: false, value, asynchronous, items })
      }
    }
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
    const { items, value, asynchronous, loading } = this.state
    const { code } = value || {}

    const selectedItem = code ? { code } : null

    if (loading) {
      return <div>Loading...</div>
    }

    const { attributeDefinition, layout } = fieldDef

    return !asynchronous && layout === CodeFieldDefinition.Layouts.RADIO ? (
      <CodeFieldRadio
        parentEntity={parentEntity}
        attributeDefinition={attributeDefinition}
        selectedItem={items.find((itm) => itm.code === code)}
        items={items}
        onChange={this.onInputChange}
      />
    ) : (
      <CodeFieldAutocomplete
        parentEntity={parentEntity}
        fieldDef={fieldDef}
        asynchronous={asynchronous}
        items={items}
        selectedItem={selectedItem}
        onSelect={this.onCodeListItemSelect}
      />
    )
  }
}
