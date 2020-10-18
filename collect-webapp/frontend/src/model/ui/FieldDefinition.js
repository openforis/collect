import { UIModelObjectDefinition } from './UIModelObjectDefinition'

export class FieldDefinition extends UIModelObjectDefinition {
  attributeType
  attributeDefinitionId
  label
  column
  columnSpan
  row

  get attributeDefinition() {
    let survey = this.parent.uiConfiguration.survey
    return survey.schema.getDefinitionById(this.attributeDefinitionId)
  }

  get nodeDefinition() {
    return this.attributeDefinition
  }

  get nodeDefinitionId() {
    return this.attributeDefinitionId
  }
}
