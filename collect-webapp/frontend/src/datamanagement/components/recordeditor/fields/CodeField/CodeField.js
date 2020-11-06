import React from 'react'
import { Input } from 'reactstrap'

import ServiceFactory from 'services/ServiceFactory'
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
    }

    this.onInputChange = this.onInputChange.bind(this)
    this.onCodeListItemSelect = this.onCodeListItemSelect.bind(this)
  }

  componentDidMount() {
    this.handleParentEntityChanged()
  }

  componentDidUpdate(prevProps) {
    const { parentEntity } = this.props
    const { parentEntity: prevParentEntity } = prevProps
    if (prevParentEntity !== parentEntity) {
      this.handleParentEntityChanged()
    }
  }

  fromCodeToValue(code) {
    const { fieldDef } = this.props
    const { layout } = fieldDef

    const nullCode = layout === CodeFieldDefinition.Layouts.DROPDOWN ? EMPTY_OPTION.value : ''
    const codeUpdated = code === null ? nullCode : code

    return { code: codeUpdated }
  }

  handleParentEntityChanged() {
    const { parentEntity } = this.props

    if (parentEntity) {
      this.setState({ loading: true })
      this.loadCodeListItems(parentEntity)
    }
  }

  async loadCodeListItems(parentEntity) {
    const attr = this.getAttribute(parentEntity)
    if (attr) {
      const { definition, survey, record } = attr
      const { versionId } = record
      const { codeListId } = definition
      const ancestorCodes = []
      const value = this.extractValueFromProps()

      const count = await ServiceFactory.codeListService.countAvailableItems({
        surveyId: survey.id,
        codeListId,
        versionId,
        ancestorCodes,
      })

      const asynchronous = count > MAX_ITEMS
      let items = null
      if (!asynchronous) {
        items = await ServiceFactory.codeListService.loadAllAvailableItems({
          surveyId: survey.id,
          codeListId,
          versionId,
          ancestorCodes,
        })
      }
      this.setState({ loading: false, value, asynchronous, items })
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

    if (!asynchronous && layout === CodeFieldDefinition.Layouts.RADIO) {
      return (
        <CodeFieldRadio
          parentEntity={parentEntity}
          attributeDefinition={attributeDefinition}
          selectedCode={code}
          items={items}
          onChange={this.onInputChange}
        />
      )
    } else {
      return (
        <CodeFieldAutocomplete
          parentEntity={parentEntity}
          fieldDef={fieldDef}
          selectedItem={selectedItem}
          onSelect={this.onCodeListItemSelect}
        />
      )
    }
  }
}
