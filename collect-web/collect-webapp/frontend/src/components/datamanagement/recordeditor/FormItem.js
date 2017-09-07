import React, { Component, PropTypes } from 'react'
import { Label, Row, Col } from 'reactstrap';

import { FieldDefinition } from '../../../model/ui/FieldDefinition'
import { FieldsetDefinition } from '../../../model/ui/FieldsetDefinition'
import { MultipleFieldsetDefinition } from '../../../model/ui/MultipleFieldsetDefinition'
import { TableDefinition } from '../../../model/ui/TableDefinition'
import Fieldset from './Fieldset' 
import MultipleFieldset from './MultipleFieldset' 
import BooleanField from './fields/BooleanField'
import CodeField from './fields/CodeField'

export default class FormItem extends Component {

    constructor(props) {
        super(props)
    }

    _createField(itemDef) {
        let attrDef = itemDef.attributeDefinition
        switch(attrDef.attributeType) {
        case 'BOOLEAN':
            return <BooleanField fieldDef={itemDef} parentEntity={this.props.parentEntity} />
        case 'CODE':
            return <CodeField fieldDef={itemDef} parentEntity={this.props.parentEntity} />
        default:
            return <div>Field type {attrDef.attributeType} to be implemented</div>
        }
    }

    render() {
        let itemDef = this.props.itemDef
        
        if (itemDef instanceof FieldDefinition) {
            return (
                <Row>
                    <Col style={{maxWidth: '150px'}}>
                        <Label>{itemDef.label}</Label>
                    </Col>
                    <Col>
                        {this._createField(itemDef)}
                    </Col>
                </Row>
            )
        } else if (itemDef instanceof FieldsetDefinition) {
            if (itemDef instanceof MultipleFieldsetDefinition) {
                return (
                    <Row>
                        <Col>
                            <MultipleFieldset fieldsetDef={itemDef} parentEntity={this.props.parentEntity} />
                        </Col>
                    </Row>
                )
            } else {
                return (
                    <Row>
                        <Col>
                            <Fieldset fieldsetDef={itemDef} parentEntity={this.props.parentEntity} />
                        </Col>
                    </Row>
                )
            }
        } else if (itemDef instanceof TableDefinition) {
            return <div>Table</div>;
        } else {
            return <div>ERROR</div>;
        }
        
    }
}