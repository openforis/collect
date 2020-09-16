import React, { Component } from 'react'
import { Label, Row, Col } from 'reactstrap'

import { AttributeDefinition } from '../../../model/Survey'
import { FieldDefinition } from '../../../model/ui/FieldDefinition'
import { FieldsetDefinition } from '../../../model/ui/FieldsetDefinition'
import { MultipleFieldsetDefinition } from '../../../model/ui/MultipleFieldsetDefinition'
import { TableDefinition } from '../../../model/ui/TableDefinition'
import Fieldset from './Fieldset'
import MultipleFieldset from './MultipleFieldset'
import BooleanField from './fields/BooleanField'
import CodeField from './fields/CodeField'
import NumberField from './fields/NumberField'
import TextField from './fields/TextField'

const FIELD_COMPONENTS_BY_TYPE = {
  [AttributeDefinition.Types.BOOLEAN]: BooleanField,
  [AttributeDefinition.Types.CODE]: CodeField,
  [AttributeDefinition.Types.NUMBER]: NumberField,
  [AttributeDefinition.Types.TEXT]: TextField,
}

export default class FormItem extends Component {
  _createField(itemDef) {
    const { parentEntity } = this.props
    const attrDef = itemDef.attributeDefinition
    const component = FIELD_COMPONENTS_BY_TYPE[attrDef.attributeType]
    return component ? (
      React.createElement(component, { fieldDef: itemDef, parentEntity })
    ) : (
      <div>Field type {attrDef.attributeType} to be implemented</div>
    )
  }

  render() {
    const { itemDef, parentEntity } = this.props

    if (itemDef instanceof FieldDefinition) {
      return (
        <Row>
          <Col style={{ maxWidth: '150px' }}>
            <Label>{itemDef.label}</Label>
          </Col>
          <Col>{this._createField(itemDef)}</Col>
        </Row>
      )
    } else if (itemDef instanceof FieldsetDefinition) {
      if (itemDef instanceof MultipleFieldsetDefinition) {
        return (
          <Row>
            <Col>
              <MultipleFieldset fieldsetDef={itemDef} parentEntity={parentEntity} />
            </Col>
          </Row>
        )
      } else {
        return (
          <Row>
            <Col>
              <Fieldset fieldsetDef={itemDef} parentEntity={parentEntity} />
            </Col>
          </Row>
        )
      }
    } else if (itemDef instanceof TableDefinition) {
      return <div>Table</div>
    } else {
      return <div>ERROR</div>
    }
  }
}
