import Serializable from './Serializable'
import { EntityDefinition } from './Survey'

export class Record extends Serializable {
  id
  survey
  step
  stepNumber
  rootEntity
  rootEntityKeys = []
  nodeById = []
  owner

  constructor(survey, jsonData) {
    super()
    this.survey = survey
    this.nodeById = []
    if (jsonData) {
      this.fillFromJSON(jsonData)
    }
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    let rootEntityDefId = parseInt(jsonObj.rootEntity.definitionId, 10)
    let rootEntityDef = this.survey.schema.getDefinitionById(rootEntityDefId)

    this.rootEntity = new Entity(this, rootEntityDef, null)
    this.rootEntity.fillFromJSON(jsonObj.rootEntity)
    this.index(this.rootEntity)
  }

  getNodeById(nodeId) {
    return this.nodeById[nodeId]
  }

  index(node) {
    this.nodeById[node.id] = node
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
}

export class Entity extends Node {
  childrenByDefinitionId = []
  childrenMinCount = []
  childrenMaxCount = []
  childrenMinCountValidation = []
  childrenMaxCountValidation = []

  get summaryLabel() {
    return 'Entity ' + this.id
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    let $this = this
    $this.record.index($this)

    this.childrenByDefinitionId = []
    for (var defIdStr in jsonObj.childrenByDefinitionId) {
      const defId = parseInt(defIdStr, 10)
      const def = this.record.survey.schema.getDefinitionById(defId)
      const childrenJsonObj = jsonObj.childrenByDefinitionId[defId]
      childrenJsonObj.forEach((childJsonObj) => {
        let child
        if (def instanceof EntityDefinition) {
          child = new Entity(this.record, def, this)
        } else {
          child = new Attribute(this.record, def, this)
        }
        child.fillFromJSON(childJsonObj)
        this.addChild(child)
      })
    }
  }

  getDescendants(descendantDefIds) {
    let currentEntity = this
    let descendants
    for (var descendantDefId in descendantDefIds) {
      descendants = currentEntity.childrenByDefinitionId[descendantDefId]
    }
    return descendants
  }

  getSingleChild(defId) {
    let children = this.childrenByDefinitionId[defId]
    return children === null || children.length === 0 ? null : children[0]
  }

  addChild(child) {
    let children = this.childrenByDefinitionId[child.definition.id]
    if (children == null) {
      children = []
      this.childrenByDefinitionId[child.definition.id] = children
    }
    children.push(child)
    this.record.index(child)
  }
}

export class Attribute extends Node {
  fields = []

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    this.fields = []
    jsonObj.fields.forEach((fieldJsonObj) => {
      let field = new Field()
      field.fillFromJSON(fieldJsonObj)
      this.fields.push(field)
    })
  }

  get allFieldsFilled() {
    for (var i = 0; i < this.fields.length; i++) {
      let field = this.fields[i]
      if (field.value == null) {
        return false
      }
    }
    return true
  }

  setFieldValue(fieldIdx, value) {
    if (this.fields == null) {
      this.fields = []
    }
    while (this.fields.length <= fieldIdx) {
      this.fields.push(new Field())
    }
    this.fields[fieldIdx].value = value
  }
}

export class Field extends Serializable {
  value = null
  remarks = null

  get intValue() {
    return this.value == null ? null : parseInt(this.value.toString(), 10)
  }
}
