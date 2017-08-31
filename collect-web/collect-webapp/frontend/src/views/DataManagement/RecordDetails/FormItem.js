import React, { Component, PropTypes } from 'react'
import { FieldDefinition } from '../../../model/ui/FieldDefinition'
import { FieldsetDefinition } from '../../../model/ui/FieldsetDefinition'
import { MultipleFieldsetDefinition } from '../../../model/ui/MultipleFieldsetDefinition'
import { TableDefinition } from '../../../model/ui/TableDefinition'

export default class FormItem extends Component {

    constructor(props) {
        super(props)
    }

    render() {
        let itemDef = this.props.itemDef

        return <div>TODO</div>
        /*
        if (itemDef instanceof FieldDefinition) {
            return <div>Field</div>
        } else if (itemDef instanceof FieldsetDefinition) {
            if (itemDef instanceof MultipleFieldsetDefinition) {
                return "MULTIPLE_FIELDSET";
            } else {
                return "SINGLE_FIELDSET";
            }
        } else if (itemDef instanceof TableDefinition) {
            return "TABLE";
        } else {
            return null; //ERROR
        }
        */
    }
}