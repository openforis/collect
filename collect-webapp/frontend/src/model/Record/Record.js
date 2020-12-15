import Arrays from 'utils/Arrays'

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
  readOnly
  errorsMissingValues
  errorsInvalidValues
  warnings
  warningsMissingValues

  constructor(survey, jsonData) {
    super()
    this.survey = survey
    if (jsonData) {
      this.fillFromJSON(jsonData)
    }
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj, { skipFields: ['missing', 'missingErrors', 'missingWarnings'] })
    this.errorsMissingValues = jsonObj.missingErrors
    this.errorsInvalidValues = jsonObj.errors
    this.warningsMissingValues = jsonObj.missingWarnings

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

  get errors() {
    return this.errorsMissingValues || 0 + this.errorsInvalidValues || 0
  }

  get validationSummary() {
    return {
      errors: this.errors,
      errorsMissingValues: this.errorsMissingValues,
      warnings: this.warnings,
      warningsMissingValues: this.warningsMissingValues,
    }
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

  getCommonParentEntity({ contextEntity, attributeDefinition }) {
    const { definition: contextEntityDef } = contextEntity
    const contextAncestorIds = contextEntityDef.ancestorAndSelfIds
    const { ancestorIds: attrDefAncestorIds } = attributeDefinition
    const commonHierarchyDefIds = Arrays.intersect(contextAncestorIds, attrDefAncestorIds)
    const commonEntityDefId = Arrays.head(commonHierarchyDefIds)
    return commonEntityDefId === contextEntityDef.id
      ? contextEntity
      : contextEntity.getAncestorByDefinitionId(commonEntityDefId)
  }

  getAncestorCodeAttributesPath({ contextEntity, attributeDefinition }) {
    let currentContextEntity = contextEntity
    let currentAttrDef = attributeDefinition.parentCodeAttributeDefinition
    const ancestorCodePaths = []

    while (currentAttrDef && currentContextEntity) {
      const commonParent = this.getCommonParentEntity({
        contextEntity: currentContextEntity,
        attributeDefinition: currentAttrDef,
      })
      const ancestorCodeParentEntity = commonParent && commonParent.getDescendantEntityClosestToNode(currentAttrDef)
      if (ancestorCodeParentEntity) {
        ancestorCodePaths.unshift(`${ancestorCodeParentEntity.path}/${currentAttrDef.name}`)
      }
      currentAttrDef = currentAttrDef.parentCodeAttributeDefinition
      currentContextEntity = ancestorCodeParentEntity
    }
    return ancestorCodePaths.length === attributeDefinition.levelIndex ? ancestorCodePaths : null
  }

  getAncestorCodeValues({ contextEntity, attributeDefinition }) {
    const codeAttributePaths = this.getAncestorCodeAttributesPath({ contextEntity, attributeDefinition })
    return codeAttributePaths
      ? codeAttributePaths.map((path) => {
          const attr = this.getNodeByPath(path)
          return attr && !attr.isEmpty() ? attr.value.code : null
        })
      : null
  }

  traverse(visitor) {
    visitor(this.rootEntity)
    this.rootEntity.visitDescendants(visitor)
  }
}
