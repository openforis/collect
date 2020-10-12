import AbstractService from './AbstractService'
import { AttributeDefinition } from '../model/Survey'

export default class CommandService extends AbstractService {
  addAttribute(record, parentEntityId, attrDef) {
    const command = {
      surveyId: record.survey.id,
      recordId: record.id,
      recordStep: record.step,
      parentEntityId: parentEntityId,
      nodeDefId: attrDef.id,
    }

    return this.postJson('command/record/attribute/new', command)
  }

  updateAttribute(attribute, valueByField) {
    const { record, definition, parent } = attribute
    const { attributeType } = definition
    const numericType = attributeType === AttributeDefinition.Types.NUMBER ? definition.numericType : null

    const command = {
      surveyId: record.survey.id,
      recordId: record.id,
      recordStep: record.step,
      parentEntityPath: parent.path,
      nodeDefId: definition.id,
      nodePath: attribute.path,
      attributeType,
      numericType,
      valueByField,
    }
    return this.postJson('command/record/attribute', command)
  }

  addEntity(record, parentEntity, entityDef) {
    const command = {
      surveyId: record.survey.id,
      recordId: record.id,
      recordStep: record.step,
      parentEntityPath: parentEntity.path,
      nodeDefId: entityDef.id,
    }

    return this.postJson('command/record/entity', command)
  }

  _createDeleteNodeCommand(node) {
    const { record } = node
    return {
      surveyId: record.survey.id,
      recordId: record.id,
      recordStep: record.step,
      nodeDefId: node.definition.id,
      nodePath: node.path,
    }
  }
  deleteAttribute(node) {
    const command = this._createDeleteNodeCommand(node)
    return this.postJson('command/record/attribute/delete', command)
  }

  deleteEntity(node) {
    const command = this._createDeleteNodeCommand(node)
    return this.postJson('command/record/entity/delete', command)
  }
}
