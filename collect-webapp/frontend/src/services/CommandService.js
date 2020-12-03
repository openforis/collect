import AbstractService from './AbstractService'
import { AttributeDefinition } from '../model/Survey'

export default class CommandService extends AbstractService {
  addAttribute({ parentEntity, attributeDef }) {
    const { survey, record } = parentEntity
    const { preferredLanguage } = survey
    const command = {
      surveyId: record.survey.id,
      recordId: record.id,
      recordStep: record.step,
      parentEntityPath: parentEntity.path,
      nodeDefId: attributeDef.id,
      preferredLanguage,
    }

    return this.postJson('command/record/attribute/new', command)
  }

  updateAttribute({ attribute, valueByField }) {
    const { survey, record, definition, parent } = attribute
    const { attributeType } = definition
    const { preferredLanguage } = survey

    const numericType = [AttributeDefinition.Types.NUMBER, AttributeDefinition.Types.RANGE].includes(attributeType)
      ? definition.numericType
      : null

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
      preferredLanguage,
    }
    return this.postJson('command/record/attribute', command)
  }

  updateMultipleAttribute({ parentEntity, attributeDefinition, valuesByField }) {
    const { survey, record } = parentEntity
    const { attributeType, id: attributeDefinitionId } = attributeDefinition
    const { preferredLanguage } = survey

    const command = {
      surveyId: record.survey.id,
      recordId: record.id,
      recordStep: record.step,
      parentEntityPath: parentEntity.path,
      nodeDefId: attributeDefinitionId,
      attributeType,
      valuesByField,
      preferredLanguage,
    }
    return this.postJson('command/record/attribute', command)
  }

  updateAttributeFile({ attribute, file }) {
    const { survey, record, definition, parent } = attribute
    const { attributeType } = definition
    const { preferredLanguage } = survey

    const command = {
      surveyId: record.survey.id,
      recordId: record.id,
      recordStep: record.step,
      parentEntityPath: parent.path,
      nodeDefId: definition.id,
      nodePath: attribute.path,
      attributeType,
      preferredLanguage,
    }
    return this.postFormData('command/record/attribute/file', { file, command: JSON.stringify(command) })
  }

  deleteAttributeFile({ fileAttribute }) {
    const command = this._createDeleteNodeCommand({ node: fileAttribute })
    return this.postJson('command/record/attribute/file/delete', command)
  }

  addEntity({ record, parentEntity, entityDef }) {
    const { survey } = record
    const { preferredLanguage } = survey

    const command = {
      surveyId: record.survey.id,
      recordId: record.id,
      recordStep: record.step,
      parentEntityPath: parentEntity.path,
      nodeDefId: entityDef.id,
      preferredLanguage,
    }

    return this.postJson('command/record/entity', command)
  }

  _createDeleteNodeCommand({ node }) {
    const { survey, record } = node
    const { preferredLanguage } = survey
    return {
      surveyId: record.survey.id,
      recordId: record.id,
      recordStep: record.step,
      nodeDefId: node.definition.id,
      nodePath: node.path,
      preferredLanguage,
    }
  }

  deleteAttribute({ attribute }) {
    const command = this._createDeleteNodeCommand({ node: attribute })
    return this.postJson('command/record/attribute/delete', command)
  }

  deleteEntity({ entity }) {
    const command = this._createDeleteNodeCommand({ node: entity })
    return this.postJson('command/record/entity/delete', command)
  }
}
