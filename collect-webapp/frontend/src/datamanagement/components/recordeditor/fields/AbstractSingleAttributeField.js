import { debounce } from 'throttle-debounce'

import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'
import ServiceFactory from 'services/ServiceFactory'

import AbstractFormComponent from '../AbstractFormComponent'

export default class AbstractSingleAttributeField extends AbstractFormComponent {
  constructor() {
    super()

    this.state = {
      dirty: false,
      value: null,
      errors: null,
      warnings: null,
    }

    this.attributeUpdatedDebounced = null
    this.onAttributeUpdate = this.onAttributeUpdate.bind(this)
  }

  componentDidMount() {
    super.componentDidMount()
    this.updateStateFromProps()
  }

  updateStateFromProps() {
    const value = this.extractValueFromProps()
    this.setState({ dirty: false, value })
  }

  extractValueFromProps() {
    const attr = this.getAttribute()
    return attr ? attr.value : null
  }

  getAttribute(parentEntityParam) {
    const { parentEntity: parentEntityProps, fieldDef, attribute: attributeParam } = this.props
    const parentEntity = parentEntityParam || parentEntityProps
    if (parentEntity) {
      const attrDef = fieldDef.attributeDefinition
      return attrDef.multiple ? attributeParam : parentEntity.getSingleChild(attrDef.id)
    }
  }

  onAttributeUpdate({ value, debounced = true }) {
    this.setState({ value, dirty: true })

    const attr = this.getAttribute()
    if (this.attributeUpdatedDebounced) {
      this.attributeUpdatedDebounced.cancel()
    }
    this.attributeUpdatedDebounced = debounce(debounced ? 1000 : 0, false, () =>
      ServiceFactory.commandService.updateAttribute(attr, value)
    )
    this.attributeUpdatedDebounced()
  }

  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { fieldDef, parentEntity } = this.props
    if (
      event instanceof AttributeValueUpdatedEvent &&
      event.isRelativeToNodes({ parentEntity, nodeDefId: fieldDef.attributeDefinitionId })
    ) {
      this.handleAttributeUpdatedEvent(event)
    }
  }

  handleAttributeUpdatedEvent(_) {
    this.updateStateFromProps()
  }
}
