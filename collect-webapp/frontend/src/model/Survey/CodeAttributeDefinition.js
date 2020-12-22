import { AttributeDefinition } from './AttributeDefinition'

export class CodeAttributeDefinition extends AttributeDefinition {
  codeListId
  parentCodeAttributeDefinitionId
  mandatoryFieldNames = ['code']
  itemsOrientation
  enumerator
  hasQualifiableItems
  layout
  showCode

  static Layouts = {
    RADIO: 'RADIO',
    DROPDOWN: 'DROPDOWN',
    TEXT: 'TEXT',
  }

  static ItemsOrientations = {
    VERTICAL: 'VERTICAL',
    HORIZONTAL: 'HORIZONTAL',
  }

  get parentCodeAttributeDefinition() {
    const id = this.parentCodeAttributeDefinitionId
    return id ? this.survey.schema.getDefinitionById(id) : null
  }

  get ancestorCodeAttributeDefinitionIds() {
    const ancestorIds = []
    const visitedIds = {}
    let currentParentAttrDef = this.parentCodeAttributeDefinition
    while (currentParentAttrDef && !visitedIds[currentParentAttrDef.id]) {
      ancestorIds.push(currentParentAttrDef.id)
      visitedIds[currentParentAttrDef.id] = true
      currentParentAttrDef = currentParentAttrDef.parentCodeAttributeDefinition
    }
    return ancestorIds
  }

  get levelIndex() {
    return this.ancestorCodeAttributeDefinitionIds.length
  }
}
