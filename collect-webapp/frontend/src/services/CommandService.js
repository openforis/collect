import AbstractService from './AbstractService'
import { RecordEventWrapper } from '../model/event/RecordEvent'
import EventQueue from '../model/event/EventQueue'

export default class CommandService extends AbstractService {
  constructor() {
    super()
    this._handleEventResponse = this._handleEventResponse.bind(this)
  }

  addAttribute(record, parentEntityId, attrDef) {
    const command = {
      surveyId: record.survey.id,
      recordId: record.id,
      parentEntityId: parentEntityId,
      nodeDefId: attrDef.id,
    }

    return this.postJson('command/record/attribute/new', command).then(this._handleEventResponse)
  }

  updateAttribute(attribute, attributeType, valueByField) {
    const { record, definition, parent } = attribute

    const command = {
      surveyId: record.survey.id,
      recordId: record.id,
      recordStep: record.step,
      parentEntityPath: parent.getPath(),
      nodeDefId: definition.id,
      nodePath: attribute.getPath(),
      attributeType,
      valueByField,
    }
    return this.postJson('command/record/attribute', command).then(this._handleEventResponse)
  }

  addEntity(record, parentEntity, entityDef) {
    const command = {
      surveyId: record.survey.id,
      recordId: record.id,
      parentEntityPath: parentEntity.path,
      nodeDefId: entityDef.id,
    }

    return this.postJson('command/record/entity', command).then(this._handleEventResponse)
  }

  _handleEventResponse(res) {
    res.forEach((eventJsonObj) => {
      const eventWrapper = new RecordEventWrapper(eventJsonObj)
      EventQueue.publish('recordEvent', eventWrapper.event)
    })
  }
}
