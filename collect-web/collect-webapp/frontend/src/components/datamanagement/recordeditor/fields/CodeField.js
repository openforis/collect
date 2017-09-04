import React, { Component, PropTypes } from 'react'
import classnames from 'classnames';
import { Label, Input, FormGroup } from 'reactstrap';
import ServiceFactory from 'services/ServiceFactory'
import AbstractField from './AbstractField'

export default class CodeField extends AbstractField {

    constructor(props) {
        super(props)

        this.state = {
            value: '',
            items: []
        }

        this.handleInputChange = this.handleInputChange.bind(this)
    }

    componentDidMount() {
        const parentEntity = this.props.parentEntity
        if (parentEntity) {
            this.handleParentEntityChanged(parentEntity)
        }
    }

    componentWillReceiveProps(nextProps) {
        const parentEntity = nextProps.parentEntity
        if (parentEntity) {
            this.handleParentEntityChanged(parentEntity)
        }
    }

    handleParentEntityChanged(parentEntity) {
        this.loadCodeListItems(parentEntity)
        const attr = this.getSingleAttribute(parentEntity)
        const codeVal = attr.fields[0].value
        this.updateStateValue(codeVal)
    }

    updateStateValue(value) {
        if (value == null) {
            //set empty option
            const layout = this.props.fieldDef.layout
            
            switch(layout) {
            case 'DROPDOWN':
                value = '-1'
                break;
            default:
                value = ''
            }
        }
        this.setState({...this.state, value: value})
    }

    loadCodeListItems(parentEntity) {
        const attr = this.getSingleAttribute(parentEntity)
        if (attr) {
            ServiceFactory.codeListService.findAvailableItems(parentEntity, attr.definition)
                .then(items => this.setState({...this.state, items: items}))
        }
    }

    handleAttributeUpdatedEvent(event) {
        super.handleAttributeUpdatedEvent(event)
        this.updateStateValue(event.code)
    }

    handleInputChange(event) {
        const attr = this.getSingleAttribute()
        if (attr) {
            const val = event.target.value
            this.updateStateValue(val)
            ServiceFactory.commandService.updateAttribute(attr, 'CODE', {
                code: val
            })
        }
    }

    render() {
        const parentEntity = this.props.parentEntity
        const fieldDef = this.props.fieldDef
        if (! parentEntity || ! fieldDef) {
            return <div>Loading...</div>
        }
        
        const EMPTY_OPTION = <option key="-1" value="-1">--- Select one ---</option>
        
        const attrDef = fieldDef.attributeDefinition
        const layout = this.props.fieldDef.layout
        
        switch(layout) {
        case 'DROPDOWN':
            let options = [EMPTY_OPTION]
                .concat(this.state.items.map(item => <option key={item.code} value={item.code}>{item.label}</option>))
            return (
                <Input type="select" onChange={this.handleInputChange} value={this.state.value}>
                    {options}
                </Input>
            )
        case 'RADIO':
            let radioBoxes = this.state.items.map(item => 
                <FormGroup check key={item.code}>
                    <Label check>
                        <Input type="radio" 
                            name={'code_group_' + parentEntity.id + '_' + attrDef.id}
                            value={item.code}
                            checked={this.state.value == item.code}
                            onChange={this.handleInputChange} />{' '}
                        {item.label}
                    </Label>
                </FormGroup>
            )
            return <div>{radioBoxes}</div>
        default:
            return <Input value={this.state.value} onChange={this.handleInputChange} style={{maxWidth: '100px'}} />
        }
    }
}