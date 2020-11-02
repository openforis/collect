import React from 'react'
import { Label, Input, FormGroup } from 'reactstrap'
import ServiceFactory from 'services/ServiceFactory'
import { CodeFieldDefinition } from 'model/ui/CodeFieldDefinition'
import AbstractSingleAttributeField from './AbstractSingleAttributeField'

export default class CodeField extends AbstractSingleAttributeField {
  constructor(props) {
    super(props)

    this.state = {
      ...this.state,
      value: { code: '' },
      items: [],
    }

    this.handleInputChange = this.handleInputChange.bind(this)
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
    let codeUpdated = null
    if (code === null) {
      const layout = this.props.fieldDef.layout
      codeUpdated = layout === 'DROPDOWN' ? '-1' : ''
    } else {
      codeUpdated = code
    }
    return { code: codeUpdated }
  }

  handleParentEntityChanged() {
    const { parentEntity } = this.props

    if (parentEntity) {
      this.loadCodeListItems(parentEntity)
      const value = this.extractValueFromProps()
      this.setState({ value })
    }
  }

  loadCodeListItems(parentEntity) {
    const attr = this.getAttribute(parentEntity)
    if (attr) {
      ServiceFactory.codeListService
        .findAvailableItems(parentEntity, attr.definition)
        .then((items) => this.setState({ items }))
    }
  }

  handleInputChange(event) {
    const { fieldDef } = this.props
    const { layout } = fieldDef
    const debounced = layout === 'TEXT'
    const code = event.target.value
    const value = this.fromCodeToValue(code)
    this.onAttributeUpdate({ value, debounced })
  }

  render() {
    const { fieldDef, parentEntity } = this.props
    const { items, value } = this.state
    const { code } = value || {}

    const codeText = code || ''

    if (!parentEntity || !fieldDef) {
      return <div>Loading...</div>
    }

    const attrDef = fieldDef.attributeDefinition
    const layout = fieldDef.layout

    switch (layout) {
      case CodeFieldDefinition.Layouts.DROPDOWN:
        const EMPTY_OPTION = (
          <option key="-1" value="-1">
            --- Select one ---
          </option>
        )
        return (
          <Input type="select" onChange={this.handleInputChange} value={codeText}>
            {[EMPTY_OPTION].concat(
              items.map((item) => (
                <option key={item.code} value={item.code}>
                  {item.label}
                </option>
              ))
            )}
          </Input>
        )
      case CodeFieldDefinition.Layouts.RADIO:
        return (
          <div>
            {items.map((item) => (
              <FormGroup check key={item.code}>
                <Label check>
                  <Input
                    type="radio"
                    name={'code_group_' + parentEntity.id + '_' + attrDef.id}
                    value={item.code}
                    checked={item.code === code}
                    onChange={this.handleInputChange}
                  />{' '}
                  {item.label}
                </Label>
              </FormGroup>
            ))}
          </div>
        )
      default:
        return <Input value={codeText} onChange={this.handleInputChange} style={{ maxWidth: '100px' }} />
    }
  }
}
