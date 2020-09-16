import Serializable from './Serializable'
import { UIConfiguration } from './ui/UIConfiguration'

export class SurveyObject extends Serializable {
  id

  constructor(id) {
    super()
    this.id = id
  }
}

export class Survey extends Serializable {
  id
  name
  uri
  schema
  modelVersions = []
  codeLists = []
  units = []
  uiConfiguration
  temporary
  published
  publishedId
  languages = []

  constructor(jsonData) {
    super()
    if (jsonData) {
      this.fillFromJSON(jsonData)
    }
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    this.codeLists = jsonObj.codeLists.map((codeListJsonObj) => {
      const codeList = new CodeList(this)
      codeList.fillFromJSON(codeListJsonObj)
      return codeList
    })
    this.units = jsonObj.units.map((unitJsonObj) => {
      const unit = new Unit(this)
      unit.fillFromJSON(unitJsonObj)
      return unit
    })
    this.languages = jsonObj.languages
    this.modelVersions = jsonObj.modelVersions
    this.schema = new Schema(this)
    this.schema.fillFromJSON(jsonObj.schema)
    this.uiConfiguration = new UIConfiguration(this)
    this.uiConfiguration.fillFromJSON(jsonObj.uiConfiguration)
  }
}

export class CodeList extends Serializable {
  survey
  id
  name
  items = []
  hierarchycal

  constructor(survey) {
    super()
    this.survey = survey
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    this.items = jsonObj.items.map((itemJsonObj) => {
      return new CodeListItem(itemJsonObj)
    })
  }
}

export class CodeListItem extends Serializable {
  code
  label
  color
}

export class Unit extends Serializable {
  id
  conversionFactor
  abbreviation
  label

  constructor(survey) {
    super()
    this.survey = survey
  }
}

export class Schema extends Serializable {
  survey
  rootEntities = []
  definitions = [] //cache

  constructor(survey) {
    super()
    this.survey = survey
    this.definitions = []
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    let $this = this

    this.rootEntities = jsonObj.rootEntities.map((rootEntityJsonObj) => {
      var rootEntity = new EntityDefinition(rootEntityJsonObj.id, this.survey, null)
      rootEntity.fillFromJSON(rootEntityJsonObj)
      rootEntity.traverse(function (nodeDef) {
        $this.definitions[nodeDef.id] = nodeDef
      })
      return rootEntity
    })
  }

  get firstRootEntityDefinition() {
    return this.rootEntities[0]
  }

  getDefinitionById(id) {
    return this.definitions[id]
  }
}

export class NodeDefinition extends SurveyObject {
  survey
  parent
  name
  label
  multiple

  constructor(id, survey, parent) {
    super(id)
    this.survey = survey
    this.parent = parent
  }

  get rootEntity() {
    let currentParent = this.parent
    while (currentParent.parent != null) {
      currentParent = currentParent.parent
    }
    return currentParent
  }
}

export class EntityDefinition extends NodeDefinition {
  children = []

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    this.children = jsonObj.children.map((nodeJsonObj) => {
      let nodeDef
      if (nodeJsonObj.type === 'ENTITY') {
        nodeDef = new EntityDefinition(nodeJsonObj.id, this.survey, this)
      } else {
        switch (nodeJsonObj.attributeType) {
          case 'CODE':
            nodeDef = new CodeAttributeDefinition(nodeJsonObj.id, this.survey, this)
            break
          case 'NUMERIC':
            nodeDef = new NumericAttributeDefinition(nodeJsonObj.id, this.survey, this)
            break
          default:
            nodeDef = new AttributeDefinition(nodeJsonObj.id, this.survey, this)
        }
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

export class AttributeDefinition extends NodeDefinition {
  key
  attributeType

  static Types = {
    BOOLEAN: 'BOOLEAN',
    CODE: 'CODE',
    COORDINATE: 'COORDINATE',
    NUMBER: 'NUMBER',
    TEXT: 'TEXT',
  }
}

export class CodeAttributeDefinition extends AttributeDefinition {
  codeListId
  parentCodeAttributeDefinitionId
}

export class NumericAttributeDefinition extends AttributeDefinition {
  numericType
  precisions
}
