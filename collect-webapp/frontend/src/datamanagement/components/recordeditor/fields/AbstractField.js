import { Component } from 'react'

import { AttributeUpdatedEvent } from 'model/event/RecordEvent'
import EventQueue from 'model/event/EventQueue'
import ServiceFactory from '../../../../services/ServiceFactory'

export default class AbstractField extends Component {
  constructor(props) {
    super(props)

    this.state = {
      value: '',
      dirty: false,
    }

    this.handleRecordEventReceived = this.handleRecordEventReceived.bind(this)
    this.onAttributeUpdate = this.onAttributeUpdate.bind(this)

    EventQueue.subscribe('recordEvent', this.handleRecordEventReceived)
  }

  componentDidMount() {
    this.setState({ value: this.extractValueFromProps() })
  }

  componentWillUnmount() {
    EventQueue.unsubscribe('recordEvent', this.handleRecordEventReceived)
  }

  extractValueFromProps() {
    return null
  }

  getSingleAttribute(parentEntity) {
    if (!parentEntity) {
      parentEntity = this.props.parentEntity
    }
    if (parentEntity) {
      const { fieldDef } = this.props
      const attrDef = fieldDef.attributeDefinition
      if (attrDef.multiple) {
        throw new Error('Expected single attribute, found multiple: ' + attrDef.name)
      } else {
        const attribute = parentEntity.getSingleChild(attrDef.id)
        return attribute
      }
    }
  }

  onAttributeUpdate({ value }) {
    const { fieldDef } = this.props

    this.setState({ value, dirty: true })

    const attrType = fieldDef.attributeDefinition.attributeType
    const attr = this.getSingleAttribute()
    ServiceFactory.commandService.updateAttribute(attr, attrType, value)
  }

  handleRecordEventReceived(event) {
    const { fieldDef, parentEntity } = this.props
    if (!parentEntity) {
      return
    }
    if (event instanceof AttributeUpdatedEvent) {
      const record = parentEntity.record
      if (record && record.id === event.recordId && record.step === event.recordStep) {
        const parentEntityId = event.parentEntityId
        const attrDefId = fieldDef.attributeDefinitionId
        if (parentEntityId === parentEntity.id && event.definitionId === attrDefId) {
          this.handleAttributeUpdatedEvent(event)
        }
      }
    }
  }

  handleAttributeUpdatedEvent(event) {}
}
