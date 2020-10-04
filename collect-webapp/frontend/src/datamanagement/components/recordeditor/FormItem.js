import React, { Component } from 'react'
import { Label, Row, Col } from 'reactstrap'

import { FieldDefinition } from '../../../model/ui/FieldDefinition'
import { FieldsetDefinition } from '../../../model/ui/FieldsetDefinition'
import { MultipleFieldsetDefinition } from '../../../model/ui/MultipleFieldsetDefinition'
import { TableDefinition } from '../../../model/ui/TableDefinition'
import Fieldset from './Fieldset'
import MultipleFieldset from './MultipleFieldset'
import Table from './Table'
import FormItemFieldComponent from './FormItemFieldComponent'

export default class FormItem extends Component {
  render() {
    const { itemDef, parentEntity } = this.props

    if (itemDef instanceof FieldDefinition) {
      return (
        <Row>
          <Col style={{ maxWidth: '150px' }}>
            <Label>{itemDef.label}</Label>
          </Col>
          <Col>
            <FormItemFieldComponent itemDef={itemDef} parentEntity={parentEntity} />
          </Col>
        </Row>
      )
    } else if (itemDef instanceof FieldsetDefinition) {
      if (itemDef instanceof MultipleFieldsetDefinition) {
        return (
          <Row>
            <Col>
              <MultipleFieldset itemDef={itemDef} parentEntity={parentEntity} />
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
      return (
        <Row>
          <Col>
            <Table itemDef={itemDef} parentEntity={parentEntity} />
          </Col>
        </Row>
      )
    } else {
      return <div>ERROR</div>
    }
  }
}
