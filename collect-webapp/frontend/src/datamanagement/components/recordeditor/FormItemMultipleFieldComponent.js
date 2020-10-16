import './FormItemMultipleFieldComponent.css'
import React from 'react'
import { Button } from 'reactstrap'
import ServiceFactory from 'services/ServiceFactory'
import EventQueue from 'model/event/EventQueue'
import { AttributeCreatedEvent, AttributeDeletedEvent, RecordEvent } from 'model/event/RecordEvent'
import FormItemFieldComponent from './FormItemFieldComponent'
import DeleteNodeButton from './DeleteNodeButton'

export default class FormItemMultipleFieldComponent extends React.Component {
  commandService = ServiceFactory.commandService

  constructor() {
    super()

    this.state = {
      attributes: [],
    }

    this.handleRecordEventReceived = this.handleRecordEventReceived.bind(this)
    this.extractAttributesFromProps = this.extractAttributesFromProps.bind(this)
    this.onAddButtonClick = this.onAddButtonClick.bind(this)
    this.onDeleteButtonClick = this.onDeleteButtonClick.bind(this)
  }

  componentDidMount() {
    EventQueue.subscribe(RecordEvent.TYPE, this.handleRecordEventReceived)

    this.setState({ attributes: this.extractAttributesFromProps() })
  }

  componentWillUnmount() {
    EventQueue.unsubscribe(RecordEvent.TYPE, this.handleRecordEventReceived)
  }

  extractAttributesFromProps() {
    const { itemDef, parentEntity } = this.props
    return parentEntity ? parentEntity.getChildrenByDefinitionId(itemDef.attributeDefinitionId) : []
  }

  handleRecordEventReceived(event) {
    const { parentEntity, itemDef } = this.props
    if (!parentEntity) {
      return
    }
    if (
      (event instanceof AttributeCreatedEvent || event instanceof AttributeDeletedEvent) &&
      event.isRelativeToNodes({ parentEntity, nodeDefId: itemDef.attributeDefinitionId })
    ) {
      this.setState({ attributes: this.extractAttributesFromProps() })
    }
  }

  onAddButtonClick = () => {
    const { itemDef, parentEntity } = this.props
    const { attributeDefinition: attributeDef } = itemDef
    this.commandService.addAttribute({ parentEntity, attributeDef })
  }

  onDeleteButtonClick = (attribute) => {
    this.commandService.deleteAttribute(attribute)
  }

  render() {
    const { itemDef, parentEntity } = this.props
    const { attributes } = this.state

    if (!parentEntity) {
      return null
    }

    return (
      <div className="form-item-multiple-field-component">
        {attributes.map((attribute) => (
          <div className="attributes-list">
            <FormItemFieldComponent itemDef={itemDef} parentEntity={parentEntity} attribute={attribute} />
            <DeleteNodeButton onClick={() => this.onDeleteButtonClick(attribute)} />
          </div>
        ))}
        <Button color="success" onClick={this.onAddButtonClick}>
          Add
        </Button>
      </div>
    )
  }
}
