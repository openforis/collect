import React from 'react'
import { Label, Input, FormGroup } from 'reactstrap'
import ServiceFactory from 'services/ServiceFactory'
import AbstractField from './AbstractField'

export default class CodeField extends AbstractField {
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

  handleParentEntityChanged() {
    const { parentEntity } = this.props

    if (parentEntity) {
      this.loadCodeListItems(parentEntity)
      const attr = this.getSingleAttribute(parentEntity)
      const codeVal = attr.fields[0].value
      this.updateStateValue(codeVal)
    }
  }

  updateStateValue(code) {
    if (code == null) {
      //set empty option
      const layout = this.props.fieldDef.layout

      switch (layout) {
        case 'DROPDOWN':
          code = '-1'
          break
        default:
          code = ''
      }
    }
    this.setState({ value: { code } })
  }

  loadCodeListItems(parentEntity) {
    const attr = this.getSingleAttribute(parentEntity)
    if (attr) {
      ServiceFactory.codeListService
        .findAvailableItems(parentEntity, attr.definition)
        .then((items) => this.setState({ items: items }))
    }
  }

  handleAttributeUpdatedEvent(event) {
    super.handleAttributeUpdatedEvent(event)
    this.updateStateValue(event.code)
    this.setState({ dirty: false })
  }

  handleInputChange(event) {
    const attr = this.getSingleAttribute()
    if (attr) {
      this.updateStateValue(event.target.value)
      if (event.target.type != 'text') {
        this.sendAttributeUpdateCommand()
      }
    }
  }

  render() {
    const { fieldDef, parentEntity } = this.props
    const { items, value } = this.state
    const { code } = value

    if (!parentEntity || !fieldDef) {
      return <div>Loading...</div>
    }

    const EMPTY_OPTION = (
      <option key="-1" value="-1">
        --- Select one ---
      </option>
    )

    const attrDef = fieldDef.attributeDefinition
    const layout = fieldDef.layout

    switch (layout) {
      case 'DROPDOWN':
        let options = [EMPTY_OPTION].concat(
          items.map((item) => (
            <option key={item.code} value={item.code}>
              {item.label}
            </option>
          ))
        )
        return (
          <Input type="select" onChange={this.handleInputChange} value={value}>
            {options}
          </Input>
        )
      case 'RADIO':
        let radioBoxes = items.map((item) => (
          <FormGroup check key={item.code}>
            <Label check>
              <Input
                type="radio"
                name={'code_group_' + parentEntity.id + '_' + attrDef.id}
                value={item.code}
                checked={value == item.code}
                onChange={this.handleInputChange}
              />{' '}
              {item.label}
            </Label>
          </FormGroup>
        ))
        return <div>{radioBoxes}</div>
      default:
        return (
          <Input
            value={code}
            onChange={this.handleInputChange}
            onBlur={this.sendAttributeUpdateCommand}
            style={{ maxWidth: '100px' }}
          />
        )
    }
  }
}
