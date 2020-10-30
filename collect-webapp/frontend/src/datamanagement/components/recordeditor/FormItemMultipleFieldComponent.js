import './FormItemMultipleFieldComponent.css'
import React from 'react'
import { Button } from 'reactstrap'

import ServiceFactory from 'services/ServiceFactory'
import { AttributeCreatedEvent, AttributeDeletedEvent } from 'model/event/RecordEvent'

import DeleteIconButton from 'common/components/DeleteIconButton'
import FormItemFieldComponent from './FormItemFieldComponent'
import AbstractFormComponent from './AbstractFormComponent'

export default class FormItemMultipleFieldComponent extends AbstractFormComponent {
  commandService = ServiceFactory.commandService

  constructor() {
    super()

    this.state = {
      attributes: [],
    }

    this.extractAttributesFromProps = this.extractAttributesFromProps.bind(this)
    this.onAddButtonClick = this.onAddButtonClick.bind(this)
    this.onDeleteButtonClick = this.onDeleteButtonClick.bind(this)
  }

  componentDidMount() {
    super.componentDidMount()
    this.setState({ attributes: this.extractAttributesFromProps() })
  }

  extractAttributesFromProps() {
    const { itemDef, parentEntity } = this.props
    return parentEntity ? parentEntity.getChildrenByDefinitionId(itemDef.attributeDefinitionId) : []
  }

  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { parentEntity, itemDef } = this.props
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

    const attrDefId = itemDef.attributeDefinition.id
    const maxCount = parentEntity.childrenMaxCountByDefinitionId[attrDefId]

    const wrapperId = `form-item-multiple-field-component-${parentEntity.id}-attr-def-${attrDefId}`

    return (
      <div id={wrapperId} className="form-item-multiple-field-component form-item-wrapper">
        {attributes.map((attribute, index) => (
          <div key={`attribute-list-item-${index}`} className="attributes-list-item">
            <FormItemFieldComponent itemDef={itemDef} parentEntity={parentEntity} attribute={attribute} />
            <DeleteIconButton onClick={() => this.onDeleteButtonClick(attribute)} />
          </div>
        ))}
        {attributes.length < maxCount && (
          <Button color="success" onClick={this.onAddButtonClick}>
            Add
          </Button>
        )}
      </div>
    )
  }
}
