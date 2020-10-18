import Serializable from './Serializable'
import { CoordinateAttributeDefinition, EntityDefinition } from './Survey'

export class Record extends Serializable {
  id
  survey
  step
  stepNumber
  rootEntity
  rootEntityKeys = []
  owner

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
}

export class Node extends Serializable {
  record
  parent
  definition
  id

  constructor(record, definition, parent) {
    super()
    this.record = record
    this.definition = definition
    this.parent = parent
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)
  }

  calculatePath() {
    const { definition, parent, index } = this

    return (
      (parent ? parent.path : '') +
      '/' +
      definition.name +
      (definition.multiple && parent ? '[' + (index + 1) + ']' : '')
    )
  }

  calculateIndex() {
    return this.parent.childrenByDefinitionId[this.definition.id].indexOf(this)
  }

  updatePath() {
    this.index = this.calculateIndex()
    this.path = this.calculatePath()
  }
}

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
        let childClass
        if (def instanceof EntityDefinition) {
          childClass = Entity
        } else if (def instanceof CoordinateAttributeDefinition) {
          childClass = CoordinateAttribute
        } else {
          childClass = Attribute
        }
        const child = new childClass(this.record, def, this)
        child.fillFromJSON(childJsonObj)
        this.addChild(child)
      })
    }
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
    const entity = descendantDefIds.length ? this.getDescendants(descendantDefIds)[0] : this
    return entity
  }

  getSingleChild(defId) {
    const children = this.getChildrenByDefinitionId(defId)
    return children.length === 0 ? null : children[0]
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

export class Attribute extends Node {
  fields = []

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    this.fields = []
    jsonObj.fields.forEach((fieldJsonObj) => {
      const field = new Field()
      field.fillFromJSON(fieldJsonObj)
      this.fields.push(field)
    })
  }

  isAllFieldsEmpty() {
    if (!this.fields) {
      return true
    }
    return this.fields.every((field) => !field.value)
  }

  isEmpty() {
    if (!this.fields) {
      return true
    }
    const fields = this.fields
    const mandatoryFieldNames = this.definition.mandatoryFieldNames
    if (!mandatoryFieldNames) {
      return false
    }
    return this.definition.fieldNames.some(
      (fieldName, index) => mandatoryFieldNames.includes(fieldName) && fields[index].value === null
    )
  }

  set value(value) {
    if (this.fields == null) {
      this.fields = []
    }
    const fields = this.fields
    this.definition.fieldNames.forEach((fieldName, index) => {
      let field = fields[index]
      if (!field) {
        field = new Field()
        fields.push(field)
      }
      field.value = value ? value[fieldName] : null
    })
  }

  get value() {
    const $this = this
    return $this.isAllFieldsEmpty()
      ? null
      : $this.definition.fieldNames.reduce((valueAcc, fieldName, index) => {
          valueAcc[fieldName] = $this.fields[index].value
          return valueAcc
        }, {})
  }

  get humanReadableValue() {
    return this.fields && this.fields.length ? this.fields[0].value || '' : ''
  }
}

export class CoordinateAttribute extends Attribute {
  get value() {
    return super.value
  }

  set value(value) {
    // Workaround: Coordinate field "srsId" matches field "srs" in the attribute
    const { srsId: srs, ...other } = value
    super.value = { ...other, srs }
  }
}

export class Field extends Serializable {
  value = null
  remarks = null

  get intValue() {
    return this.value == null ? null : parseInt(this.value.toString(), 10)
  }
}
