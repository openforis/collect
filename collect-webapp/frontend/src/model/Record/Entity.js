import { AttributeDefinition, EntityDefinition } from '../Survey'

import { Node } from './Node'
import { Attribute } from './Attribute'
import { CoordinateAttribute } from './CoordinateAttribute'
import { FileAttribute } from './FileAttribute'
import { TaxonAttribute } from './TaxonAttribute'
import { RangeAttribute } from './RangeAttribute'
import { NumberAttribute } from './NumberAttribute'

const attributeClassByType = {
  [AttributeDefinition.Types.COORDINATE]: CoordinateAttribute,
  [AttributeDefinition.Types.FILE]: FileAttribute,
  [AttributeDefinition.Types.NUMBER]: NumberAttribute,
  [AttributeDefinition.Types.RANGE]: RangeAttribute,
  [AttributeDefinition.Types.TAXON]: TaxonAttribute,
}

export class Entity extends Node {
  childrenByDefinitionId = {}
  childrenRelevanceByDefinitionId
  childrenMinCountByDefinitionId
  childrenMaxCountByDefinitionId
  childrenMinCountValidationByDefinitionId
  childrenMaxCountValidationByDefinitionId

  get keyNodes() {
    const { definition } = this
    const { keyAttributeDefinitions } = definition

    return keyAttributeDefinitions.reduce((keyNodesAcc, keyDef) => {
      const keyNodeParent = this.getDescendantEntityClosestToNode(keyDef)
      const keyNodes = keyNodeParent?.getChildrenByDefinitionId(keyDef.id)
      const keyNode = keyNodes?.length > 0 ? keyNodes[0] : null
      keyNodesAcc.push(keyNode)
      return keyNodesAcc
    }, [])
  }

  get summaryValues() {
    return this.keyNodes
      .filter((keyNode) => !!keyNode)
      .map((keyNode) => keyNode.humanReadableValue)
      .join('-')
  }

  get summaryLabel() {
    const keyNodes = this.keyNodes
    if (keyNodes.length > 0) {
      const keyValuePairs = this.keyNodes.map((keyNode) => {
        const keyValue = keyNode ? keyNode.humanReadableValue : ''
        return keyNode ? keyValue : ''
      })
      return `${keyValuePairs.join(', ')}`
    } else {
      // show position if key nodes are missing
      return this.index + 1
    }
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

  _getChildInstanceClass(def) {
    if (def instanceof EntityDefinition) {
      return Entity
    }
    return attributeClassByType[def.attributeType] || Attribute
  }
  _createChildInstance(def) {
    const childClass = this._getChildInstanceClass(def)
    return new childClass(this.record, def, this)
  }

  getChildrenByChildName(childName) {
    const childDef = this.definition.getChildDefinitionByName(childName)
    return this.getChildrenByDefinitionId(childDef.id)
  }

  getChildrenByDefinitionId(childDefId) {
    return this.childrenByDefinitionId[childDefId] || []
  }

  _getDescendantParentDefIdsByNodeDefinition(nodeDef) {
    const nodeDefAncestorIds = nodeDef.ancestorIds
    const nodeDefAncestorIdsUpToThis = nodeDefAncestorIds.slice(0, nodeDefAncestorIds.indexOf(this.definition.id))
    return [...nodeDefAncestorIdsUpToThis].reverse()
  }

  _getDescendantsByNodeDefHierarchy(descendantDefIds) {
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
    if (nodeDef.parent.id === this.definition.id) {
      return this
    }
    const descendantEntities = this.getDescendantsByNodeDefinition(nodeDef.parent)
    return descendantEntities && descendantEntities.length ? descendantEntities[0] : this
  }

  getDescendantsByNodeDefinition(nodeDef) {
    const descendantParentDefIds = this._getDescendantParentDefIdsByNodeDefinition(nodeDef)
    const descendantDefIds = [...descendantParentDefIds, nodeDef.id]
    return descendantDefIds.length ? this._getDescendantsByNodeDefHierarchy(descendantDefIds) : []
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

  visitDescendants(visitor) {
    this.definition.children.forEach((childDef) => {
      const children = this.getChildrenByDefinitionId(childDef.id)
      children.forEach((child) => {
        visitor(child)
        if (child instanceof Entity) {
          child.visitDescendants(visitor)
        }
      })
    })
  }

  _hasDescendant(predicate) {
    return this.definition.children.some((childDef) => {
      const children = this.getChildrenByDefinitionId(childDef.id)
      return children.some((child) => {
        if (predicate(child)) {
          return true
        }
        if (child instanceof Entity) {
          return child._hasDescendant(predicate)
        }
        return false
      })
    })
  }

  _hasDescendantNodePointers({ nodeDefinition, predicate }) {
    const descendantParentDefIds = this._getDescendantParentDefIdsByNodeDefinition(nodeDefinition)
    const parentNodes =
      descendantParentDefIds.length > 0 ? this._getDescendantsByNodeDefHierarchy(descendantParentDefIds) : [this]
    return parentNodes.some((parentNode) => predicate(parentNode, nodeDefinition))
  }

  hasSomeDescendantNotEmpty({ nodeDefinition }) {
    return this.getDescendantsByNodeDefinition(nodeDefinition).some((node) => !node.isEmpty())
  }

  hasSomeDescendantRelevant({ nodeDefinition }) {
    return this._hasDescendantNodePointers({
      nodeDefinition,
      predicate: (parentNode, nodeDef) => parentNode.isChildRelevant(nodeDef),
    })
  }

  isEmpty() {
    return !this._hasDescendant((descendant) => (!descendant.isEmpty()))
  }

  isChildRelevant(childDef) {
    const relevant = this.childrenRelevanceByDefinitionId[childDef.id]
    return relevant !== false // relevance true by default
  }
}
