import {
  CoordinateAttributeDefinition,
  FileAttributeDefinition,
  EntityDefinition,
  TaxonAttributeDefinition,
} from '../Survey'

import { Node } from './Node'
import { Attribute } from './Attribute'
import { CoordinateAttribute } from './CoordinateAttribute'
import { FileAttribute } from './FileAttribute'
import { TaxonAttribute } from './TaxonAttribute'

export class Entity extends Node {
  childrenByDefinitionId = {}
  childrenRelevanceByDefinitionId
  childrenMinCountByDefinitionId
  childrenMaxCountByDefinitionId
  childrenMinCountValidationByDefinitionId
  childrenMaxCountValidationByDefinitionId

  get summaryLabel() {
    const { definition } = this
    const keyDefs = definition.keyAttributeDefinitions
    const keyValuePairs = keyDefs.map((keyDef) => {
      const keyNodes = this.childrenByDefinitionId[keyDef.id]
      const keyNode = keyNodes && keyNodes.length ? keyNodes[0] : null
      const keyValue = keyNode ? keyNode.humanReadableValue : ''
      return `${keyDef.label}: ${keyValue}`
    })
    return `${definition.label} [${keyValuePairs.join(', ')}]`
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    this.childrenByDefinitionId = []
    for (var defIdStr in jsonObj.childrenByDefinitionId) {
      const defId = parseInt(defIdStr, 10)
      const def = this.record.survey.schema.getDefinitionById(defId)
      const childrenJsonObj = jsonObj.childrenByDefinitionId[defId]
      childrenJsonObj.forEach((childJsonObj) => {
        const child = this._createChildInstance(def)
        child.fillFromJSON(childJsonObj)
        this.addChild(child)
      })
    }
  }

  _createChildInstance(def) {
    let childClass
    if (def instanceof EntityDefinition) {
      childClass = Entity
    } else if (def instanceof CoordinateAttributeDefinition) {
      childClass = CoordinateAttribute
    } else if (def instanceof FileAttributeDefinition) {
      childClass = FileAttribute
    } else if (def instanceof TaxonAttributeDefinition) {
      childClass = TaxonAttribute
    } else {
      childClass = Attribute
    }
    return new childClass(this.record, def, this)
  }

  getChildrenByChildName(childName) {
    const childDef = this.definition.getChildDefinitionByName(childName)
    return this.getChildrenByDefinitionId(childDef.id)
  }

  getChildrenByDefinitionId(childDefId) {
    return this.childrenByDefinitionId[childDefId] || []
  }

  getDescendants(descendantDefIds) {
    let currentEntity = this
    let descendants
    const schema = this.record.survey.schema
    descendantDefIds.forEach((descendantDefId) => {
      descendants = currentEntity.getChildrenByDefinitionId(descendantDefId)
      const descendantDef = schema.getDefinitionById(descendantDefId)
      if (descendantDef instanceof EntityDefinition && descendantDef.single) {
        currentEntity = descendants[0]
      }
    })
    return descendants
  }

  getDescendantEntityClosestToNode(nodeDef) {
    const nodeDefAncestorIds = nodeDef.ancestorIds
    const nodeDefAncestorIdsUpToThis = nodeDefAncestorIds.slice(0, nodeDefAncestorIds.indexOf(this.definition.id))
    const descendantDefIds = [...nodeDefAncestorIdsUpToThis].reverse()
    const descendantEntities = descendantDefIds.length ? this.getDescendants(descendantDefIds) : null
    return descendantEntities && descendantEntities.length ? descendantEntities[0] : this
  }

  getSingleChild(defId) {
    const children = this.getChildrenByDefinitionId(defId)
    return children.length === 0 ? null : children[0]
  }

  addNewAttribute(attrDef) {
    const attr = this._createChildInstance(attrDef)
    this.addChild(attr)
    return attr
  }

  addChild(child) {
    let children = this.childrenByDefinitionId[child.definition.id]
    if (children == null) {
      children = []
      this.childrenByDefinitionId[child.definition.id] = children
    }
    children.push(child)
    child.index = children.length - 1
    child.path = child.calculatePath()
  }

  removeChild(child) {
    const children = this.childrenByDefinitionId[child.definition.id]
    if (children == null) {
      return
    }
    children.splice(child.index, 1)
    children.slice(child.index).forEach((child) => child.updatePath())
  }

  updatePath() {
    super.updatePath()
    Object.values(this.childrenByDefinitionId).forEach((children) => {
      children.forEach((child) => child.updatePath())
    })
  }
}
