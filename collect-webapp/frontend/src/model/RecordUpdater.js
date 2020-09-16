import { Attribute, Entity } from './Record'

import {
  AttributeUpdatedEvent,
  EntityCreatedEvent,
  BooleanAttributeUpdatedEvent,
  CodeAttributeUpdatedEvent,
  CoordinateAttributeUpdatedEvent,
  DateAttributeUpdatedEvent,
  DoubleAttributeUpdatedEvent,
  IntegerAttributeUpdatedEvent,
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
      const definition = survey.schema.getDefinitionById(Number(event.definitionId))
      const parentEntity = event.parentEntityPath ? record.getNodeByPath(event.parentEntityPath) : record.rootEntity
      const node = record.getNodeByPath(event.nodePath)

      if (event instanceof EntityCreatedEvent) {
        const newEntity = new Entity(record, definition, parentEntity)
        newEntity.childrenRelevanceByDefinitionId = event.childrenRelevanceByDefinitionId
        newEntity.childrenMinCountByDefinitionId = event.childrenMinCountByDefinitionId
        newEntity.childrenMaxCountByDefinitionId = event.childrenMaxCountByDefinitionId
        newEntity.childrenMinCountValidationByDefinitionId = event.childrenMinCountValidationByDefinitionId
        newEntity.childrenMaxCountValidationByDefinitionId = event.childrenMaxCountValidationByDefinitionId
        parentEntity.addChild(newEntity)
      } else if (event instanceof AttributeUpdatedEvent) {
        let attr = node
        if (attr == null) {
          attr = new Attribute(record, definition, parentEntity)
          parentEntity.addChild(attr)
        }
        this._setValueInAttribute(attr, event)
        attr.validationResults = event.validationResults
      } else if (event instanceof NodeRelevanceUpdatedEvent) {
        node.childrenRelevanceByDefinitionId[event.childDefinitionId] = event.relevant
      } else if (event instanceof NodeMinCountUpdatedEvent) {
        node.childrenMinCountByDefinitionId[event.childDefinitionId] = event.count
      } else if (event instanceof NodeMaxCountUpdatedEvent) {
        node.childrenMaxCountByDefinitionId[event.childDefinitionId] = event.count
      } else if (event instanceof NodeMinCountValidationUpdatedEvent) {
        node.childrenMinCountValidationByDefinitionId[event.childDefinitionId] = event.flag
      } else if (event instanceof NodeMaxCountValidationUpdatedEvent) {
        node.childrenMaxCountValidationByDefinitionId[event.childDefinitionId] = event.flag
      }
    }
  }

  _setValueInAttribute(attr, event) {
    if (event instanceof BooleanAttributeUpdatedEvent) {
      attr.setFieldValue(0, event.value)
    } else if (event instanceof CodeAttributeUpdatedEvent) {
      attr.setFieldValue(0, event.code)
      attr.setFieldValue(1, event.qualifier)
    } else if (event instanceof CoordinateAttributeUpdatedEvent) {
      attr.setFieldValue(0, event.x)
      attr.setFieldValue(1, event.y)
      attr.setFieldValue(2, event.srsId)
    } else if (event instanceof DateAttributeUpdatedEvent) {
      attr.setFieldValue(0, event.year)
      attr.setFieldValue(1, event.month)
      attr.setFieldValue(2, event.day)
    } else if (event instanceof IntegerAttributeUpdatedEvent || event instanceof DoubleAttributeUpdatedEvent) {
      attr.setFieldValue(0, event.value)
      attr.setFieldValue(1, event.unitId)
    } else if (event instanceof TextAttributeUpdatedEvent) {
      attr.setFieldValue(0, event.text)
    }
  }
}
