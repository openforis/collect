import React, { Component, PropTypes } from 'react'
import classnames from 'classnames';

import { AttributeUpdatedEvent } from 'model/event/RecordEvent'
import EventQueue from 'model/event/EventQueue'

export default class AbstractField extends Component {

    constructor(props) {
        super(props)

        this.handleRecordEventReceived = this.handleRecordEventReceived.bind(this)

        EventQueue.subscribe('recordEvent', this.handleRecordEventReceived)
    }

    getSingleAttribute(parentEntity) {
        if (! parentEntity) {
            parentEntity = this.props.parentEntity
        }
        if (parentEntity) {
            let fieldDef = this.props.fieldDef
            let attrDef = fieldDef.attributeDefinition
            if (attrDef.multiple) {
                throw new Error('Expected single attribute, found multiple: ' + attrDef.name)
            } else {
                let attribute = parentEntity.getSingleChild(attrDef.id)
                return attribute;
            }
        }
    }

    handleRecordEventReceived(event) {
        let parentEntity = this.props.parentEntity
        if (! parentEntity) {
            return
        }
        if (event instanceof AttributeUpdatedEvent) {
            let record = parentEntity.record
            if (record && record.id == event.recordId && record.step == event.recordStep) {
                let survey = record.survey;
                let parentEntityId = event.parentEntityId;
                let attrDefId = this.props.fieldDef.attributeDefinitionId
                if (parentEntityId == parentEntity.id && event.definitionId == attrDefId) {
                    this.handleAttributeUpdatedEvent(event)
                }
            }
        }
    }

    handleAttributeUpdatedEvent(event) {
        
    }

    componentWillUnmount() {
        EventQueue.unsubscribe('recordEvent', this.handleRecordEventReceived)
    }

    
}
    