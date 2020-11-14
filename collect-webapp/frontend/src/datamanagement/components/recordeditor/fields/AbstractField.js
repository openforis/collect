import { debounce } from 'throttle-debounce'

import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'
import ServiceFactory from 'services/ServiceFactory'

import AbstractFormComponent from '../AbstractFormComponent'

export default class AbstractField extends AbstractFormComponent {
  constructor() {
    super()

    this.state = {
      dirty: false,
      value: null,
      errors: null,
      warnings: null,
    }

    this._updateAttributeValueDebounced = null
    this.updateValue = this.updateValue.bind(this)
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

  extractValuesFromProps() {
    const attrs = this.getAttributes()
    return attrs ? attrs.map((attr) => attr.value).filter((value) => !!value) : []
  }

  getAttribute() {
    const { fieldDef, attribute: attributeParam } = this.props
    if (fieldDef.attributeDefinition.multiple) {
      return attributeParam
    }
    const attributes = this.getAttributes()
    return attributes[0]
  }

  getAttributes() {
    const { parentEntity, fieldDef } = this.props
    const { attributeDefinitionId } = fieldDef
    return parentEntity ? parentEntity.getChildrenByDefinitionId(attributeDefinitionId) : []
  }

  updateValue({ value, debounced = true }) {
    this.updateWithDebounce({
      state: { value },
      debounced,
      updateFn: () =>
        ServiceFactory.commandService.updateAttribute({ attribute: this.getAttribute(), valueByField: value }),
    })
  }

  updateWithDebounce({ state, updateFn, debounced = true }) {
    this.setState({ ...state, dirty: true })

    if (this._updateAttributeValueDebounced) {
      this._updateAttributeValueDebounced.cancel()
    }
    this._updateAttributeValueDebounced = debounce(debounced ? 1000 : 0, false, updateFn)
    this._updateAttributeValueDebounced()
  }

  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { fieldDef, parentEntity } = this.props
    if (
      event instanceof AttributeValueUpdatedEvent &&
      event.isRelativeToNodes({ parentEntity, nodeDefId: fieldDef.attributeDefinitionId })
    ) {
      this.onAttributeUpdatedEvent(event)
    }
  }

  onAttributeUpdatedEvent(_) {
    this.updateStateFromProps()
  }
}
