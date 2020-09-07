import { Attribute, Entity } from './Record'

import {
  AttributeUpdatedEvent,
  EntityCreatedEvent,
  CodeAttributeUpdatedEvent,
  CoordinateAttributeUpdatedEvent,
  DateAttributeUpdatedEvent,
  TextAttributeUpdatedEvent,
  NodeRelevanceUpdatedEvent,
  NodeMinCountUpdatedEvent,
  NodeMinCountValidationUpdatedEvent,
  NodeMaxCountUpdatedEvent,
  NodeMaxCountValidationUpdatedEvent,
} from './event/RecordEvent'

import EventQueue from './event/EventQueue'

export class RecordUpdater {
  record = null

  constructor(record) {
    this.record = record

    this.handleRecordEventReceived = this.handleRecordEventReceived.bind(this)

    EventQueue.subscribe('recordEvent', this.handleRecordEventReceived)
  }

  destroy() {
    EventQueue.unsubscribe('recordEvent', this.handleRecordEventReceived)
  }

  handleRecordEventReceived(event) {
    const record = this.record
    if (record && record.id == event.recordId && record.step == event.recordStep) {
      const survey = record.survey
      const parentEntityId = event.parentEntityId
      const parentEntity = record.getNodeById(parentEntityId)
      const definition = survey.schema.getDefinitionById(Number(event.definitionId))
      const nodeId = Number(event.nodeId)
      const node = record.getNodeById(nodeId)

      if (event instanceof EntityCreatedEvent) {
        const newEntity = new Entity(record, definition, parentEntity)
        newEntity.id = nodeId
        parentEntity.addChild(newEntity)
      } else if (event instanceof AttributeUpdatedEvent) {
        let attr = node
        if (attr == null) {
          attr = new Attribute(record, definition, parentEntity)
          attr.id = nodeId
          parentEntity.addChild(attr)
        }
        this._setValueInAttribute(attr, event)
      } else if (event instanceof NodeRelevanceUpdatedEvent) {
        const childDefIndex = definition.getChildDefinitionIndexById(event.childDefinitionId)
        node.childrenRelevance[childDefIndex] = event.relevant
      } else if (event instanceof NodeMinCountUpdatedEvent) {
        const childDefIndex = definition.getChildDefinitionIndexById(event.childDefinitionId)
        node.childrenMinCount[childDefIndex] = event.count
      } else if (event instanceof NodeMaxCountUpdatedEvent) {
        const childDefIndex = definition.getChildDefinitionIndexById(event.childDefinitionId)
        node.childrenMaxCount[childDefIndex] = event.count
      } else if (event instanceof NodeMinCountValidationUpdatedEvent) {
        const childDefIndex = definition.getChildDefinitionIndexById(event.childDefinitionId)
        node.childrenMinCountValidation[childDefIndex] = event.flag
      } else if (event instanceof NodeMaxCountValidationUpdatedEvent) {
        const childDefIndex = definition.getChildDefinitionIndexById(event.childDefinitionId)
        node.childrenMaxCountValidation[childDefIndex] = event.flag
      }
    }
  }

  _setValueInAttribute(attr, event) {
    if (event instanceof CodeAttributeUpdatedEvent) {
      attr.setFieldValue(0, event.code)
      attr.setFieldValue(1, event.qualifier)
    } else if (event instanceof CoordinateAttributeUpdatedEvent) {
      attr.setFieldValue(0, event.x)
      attr.setFieldValue(1, event.y)
      attr.setFieldValue(2, event.srsId)
    } else if (event instanceof DateAttributeUpdatedEvent) {
      let date = event.date
      attr.setFieldValue(0, date.getFullYear())
      attr.setFieldValue(1, date.getMonth())
      attr.setFieldValue(2, date.getDay())
    } else if (event instanceof TextAttributeUpdatedEvent) {
      attr.setFieldValue(0, event.text)
    }
  }
}
