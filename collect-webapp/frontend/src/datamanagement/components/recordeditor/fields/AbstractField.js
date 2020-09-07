import { Component } from 'react'
import { debounce } from 'throttle-debounce'

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

    this.attributeUpdatedDebounced = null
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

  onAttributeUpdate({ value, debounced = true }) {
    const { fieldDef } = this.props

    this.setState({ value, dirty: true })

    const attrType = fieldDef.attributeDefinition.attributeType
    const attr = this.getSingleAttribute()
    if (this.attributeUpdatedDebounced) {
      this.attributeUpdatedDebounced.cancel()
    }
    this.attributeUpdatedDebounced = debounce(debounced ? 1000 : 0, false, () =>
      ServiceFactory.commandService.updateAttribute(attr, attrType, value)
    )
    this.attributeUpdatedDebounced()
  }

  handleRecordEventReceived(event) {
    const { fieldDef, parentEntity } = this.props
    if (!parentEntity) {
      return
    }
    if (event instanceof AttributeUpdatedEvent) {
      if (
        event.recordId === parentEntity.record.id &&
        event.recordStep === parentEntity.record.step &&
        Number(event.parentEntityId) === parentEntity.id &&
        Number(event.definitionId) === fieldDef.attributeDefinitionId
      ) {
        this.handleAttributeUpdatedEvent(event)
      }
    }
  }

  handleAttributeUpdatedEvent(event) {
    const value = this.extractValueFromAttributeUpdateEvent(event)
    this.setState({ dirty: false, value })
  }

  extractValueFromAttributeUpdateEvent(event) {}
}
