import { AttributeDefinition } from './AttributeDefinition'
import { BooleanAttributeDefinition } from './BooleanAttributeDefinition'
import { CodeAttributeDefinition } from './CodeAttributeDefinition'
import { CoordinateAttributeDefinition } from './CoordinateAttributeDefinition'
import { FileAttributeDefinition } from './FileAttributeDefinition'
import { NodeDefinition } from './NodeDefinition'
import { NumberAttributeDefinition } from './NumberAttributeDefinition'
import { RangeAttributeDefinition } from './RangeAttributeDefinition'
import { TaxonAttributeDefinition } from './TaxonAttributeDefinition'
import { TextAttributeDefinition } from './TextAttributeDefinition'

const ATTRIBUTE_DEFINITION_CLASS_BY_TYPE = {
  [AttributeDefinition.Types.BOOLEAN]: BooleanAttributeDefinition,
  [AttributeDefinition.Types.CODE]: CodeAttributeDefinition,
  [AttributeDefinition.Types.COORDINATE]: CoordinateAttributeDefinition,
  [AttributeDefinition.Types.FILE]: FileAttributeDefinition,
  [AttributeDefinition.Types.NUMBER]: NumberAttributeDefinition,
  [AttributeDefinition.Types.RANGE]: RangeAttributeDefinition,
  [AttributeDefinition.Types.TAXON]: TaxonAttributeDefinition,
  [AttributeDefinition.Types.TEXT]: TextAttributeDefinition,
}

export class EntityDefinition extends NodeDefinition {
  children = []
  enumerated

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    this.children = jsonObj.children.map((nodeJsonObj) => {
      const { id, type, attributeType } = nodeJsonObj
      let nodeDef
      if (type === NodeDefinition.Types.ENTITY) {
        nodeDef = new EntityDefinition(id, this.survey, this)
      } else {
        const nodeDefClass = ATTRIBUTE_DEFINITION_CLASS_BY_TYPE[attributeType] || AttributeDefinition
        nodeDef = new nodeDefClass(id, this.survey, this)
      }
      nodeDef.fillFromJSON(nodeJsonObj)
      return nodeDef
    })
  }

  traverse(visitor) {
    const stack = []
    stack.push(this)
    while (stack.length > 0) {
      const def = stack.pop()
      visitor(def)
      if (def instanceof EntityDefinition) {
        stack.push(...def.children)
      }
    }
  }

  get keyAttributeDefinitions() {
    return this.findDefinitions((def) => def instanceof AttributeDefinition && def.key, true)
  }

  get attributeDefinitionsShownInRecordSummaryList() {
    return this.findDefinitions((def) => def instanceof AttributeDefinition && def.showInRecordSummaryList, true)
  }

  get qualifierAttributeDefinitions() {
    return this.findDefinitions((def) => def instanceof AttributeDefinition && def.qualifier, true)
  }

  visit(visitor, onlyInsideSingleEntities) {
    const queue = []

    this.children.forEach((child) => queue.push(child))

    while (queue.length > 0) {
      const item = queue.shift()
      visitor(item)
      if (item instanceof EntityDefinition && !(onlyInsideSingleEntities && item.multiple)) {
        item.children.forEach((child) => queue.push(child))
      }
    }
  }

  findDefinitions(predicate, onlyInsideSingleEntities) {
    const result = []
    this.visit(function (n) {
      if (predicate(n)) {
        result.push(n)
      }
    }, onlyInsideSingleEntities)
    return result
  }

  getChildDefinitionIndexById(childDefId) {
    return this.children.findIndex((childDef) => childDef.id === childDefId)
  }

  getChildDefinitionByName(childName) {
    return this.children.find((childDef) => childDef.name === childName)
  }
}
