import React, { Component, PropTypes } from 'react'
import classnames from 'classnames';
import { Label, Input } from 'reactstrap';
import ServiceFactory from '../../../../services/ServiceFactory'

export default class CodeField extends Component {

    constructor(props) {
        super(props)

        this.state = {
            value: '',
            items: []
        }

        this.handleInputChange = this.handleInputChange.bind(this)
    }

    componentWillReceiveProps(nextProps) {
        let parentEntity = nextProps.parentEntity
        if (parentEntity) {
            let fieldDef = nextProps.fieldDef
            let attrDef = fieldDef.attributeDefinition
            if (attrDef.multiple) {
                //TODO
            } else {
                let attribute = parentEntity.getSingleChild(attrDef.id)
                let codeFieldValue = attribute.fields[0].value
                this.setState({...this.state, value: codeFieldValue})

                ServiceFactory.codeListService.findAvailableItems(parentEntity, attrDef)
                    .then(items => this.setState({...this.state, items: items}))
            }
        }
    }

    handleInputChange(event) {
        this.setState({...this.state, value: event.target.value})
    }

    render() {
        let parentEntity = this.props.parentEntity
        let fieldDef = this.props.fieldDef
        if (! parentEntity || ! fieldDef) {
            return <div>Loading...</div>
        }
        let attrDef = fieldDef.attributeDefinition
        let layout = this.props.fieldDef.layout;
        
        switch(layout) {
        case 'DROPDOWN':
            let options = this.state.items.map(item => <option key={item.code} value={item.code}>{item.label}</option>)
            return <Input type="select" onChange={this.handleInputChange}>{options}</Input>
        case 'RADIO':
            let radioBoxes = this.state.items.map(item => 
                <Input type="radiobox" name={'code_group_' + parentEntity.id + '_' + attrDef.id}
                    value={item.code}
                    onChange={this.handleInputChange} />)
            return <div>{radioBoxes}</div>
        default:
            return <Input value={this.state.value} onChange={this.handleInputChange} />
        }
    }
}