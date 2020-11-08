import Serializable from '../Serializable'
import { Entity } from './Entity'

export class Record extends Serializable {
  id
  survey
  step
  stepNumber
  rootEntity
  rootEntityKeys = []
  owner
  version

  constructor(survey, jsonData) {
    super()
    this.survey = survey
    if (jsonData) {
      this.fillFromJSON(jsonData)
    }
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    const rootEntityDefId = Number(jsonObj.rootEntity.definitionId)
    const rootEntityDef = this.survey.schema.getDefinitionById(rootEntityDefId)

    this.rootEntity = new Entity(this, rootEntityDef, null)
    this.rootEntity.fillFromJSON(jsonObj.rootEntity)
  }

  getNodeByPath(path) {
    const pathParts = path.split('/').slice(2)
    let currentNode = this.rootEntity
    pathParts.forEach((pathPart) => {
      if (currentNode) {
        const pathPartMatch = pathPart.match(/(\w+)(\[(\d+)\])?/)
        const nodeName = pathPartMatch[1]
        const position = pathPartMatch[3] || 1
        const currentNodeChildren = currentNode.getChildrenByChildName(nodeName)
        currentNode = position > 0 && position <= currentNodeChildren.length ? currentNodeChildren[position - 1] : null
      }
    })
    return currentNode
  }

  get ownerId() {
    return this.owner ? this.owner.id : null
  }

  get versionId() {
    return this.version ? this.version.id : null
  }

  getParentCodeAttribute({ parentEntity, attributeDefinition }) {
    const { parentCodeAttributeDefinition } = attributeDefinition
    if (!parentCodeAttributeDefinition) {
      return null
    }
    const parentCodeAttrDefParentId = parentCodeAttributeDefinition.parent.id

    const parentCodeAttrParent = parentEntity.getAncestorByDefinitionId(parentCodeAttrDefParentId)
    if (parentCodeAttrParent) {
      const parentCodeAttrs = parentCodeAttrParent.getChildrenByDefinitionId(parentCodeAttributeDefinition.id)
      if (parentCodeAttrs && parentCodeAttrs.length === 1) {
        return parentCodeAttrs[0]
      }
    }
    return null
  }

  getAncestorCodeValues({ parentEntity, attributeDefinition }) {
    const ancestorCodeAttributes = []

    let currentCodeAttrDef = attributeDefinition
    let currentParentEntity = parentEntity
    let currentParentCodeAttr = null
    do {
      currentParentCodeAttr = this.getParentCodeAttribute({
        parentEntity: currentParentEntity,
        attributeDefinition: currentCodeAttrDef,
      })
      if (currentParentCodeAttr && !currentParentCodeAttr.isEmpty()) {
        ancestorCodeAttributes.unshift(currentParentCodeAttr)
        currentParentEntity = currentParentCodeAttr.parent
        currentCodeAttrDef = currentParentCodeAttr.definition
      }
    } while (currentParentCodeAttr && currentCodeAttrDef.levelIndex > 0)

    return ancestorCodeAttributes.length === attributeDefinition.levelIndex
      ? ancestorCodeAttributes.map((attr) => attr.value.code)
      : null
  }
}
