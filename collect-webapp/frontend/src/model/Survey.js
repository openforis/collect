import Serializable from './Serializable'
import { UIConfiguration } from './ui/UIConfiguration'
import Arrays from 'utils/Arrays'

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
  spatialReferenceSystems = []
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
    this.spatialReferenceSystems = jsonObj.spatialReferenceSystems.map((srsJsonObj) => {
      const srs = new SpatialReferenceSystem(this)
      srs.fillFromJSON(srsJsonObj)
      return srs
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

export class SpatialReferenceSystem extends Serializable {
  id
  label
  description

  constructor(survey) {
    super()
    this.survey = survey
  }
}

export class Schema extends Serializable {
  survey
  rootEntities = []
  definitions = {} //cache

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

  static Types = {
    ENTITY: 'ENTITY',
    ATTRIBUTE: 'ATTRIBUTE',
  }

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

  visitAncestors(visitor) {
    let currentParent = this.parent
    while (currentParent != null) {
      visitor(currentParent)
      currentParent = currentParent.parent
    }
  }

  get ancestorIds() {
    const ancestorIds = []
    this.visitAncestors((ancestor) => ancestorIds.push(ancestor.id))
    return ancestorIds
  }

  get ancestorAndSelfIds() {
    return [...this.ancestorIds, this.id]
  }

  get single() {
    return !this.multiple
  }
}

export class EntityDefinition extends NodeDefinition {
  children = []

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)

    this.children = jsonObj.children.map((nodeJsonObj) => {
      const { id, type, attributeType } = nodeJsonObj
      let nodeDef
      if (type === NodeDefinition.Types.ENTITY) {
        nodeDef = new EntityDefinition(id, this.survey, this)
      } else {
        const nodeDefClass = attributeDefinitionClassByType[attributeType] || AttributeDefinition
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

export class AttributeDefinition extends NodeDefinition {
  key
  attributeType
  fieldNames
  fieldLabels
  mandatoryFieldNames

  static Types = {
    BOOLEAN: 'BOOLEAN',
    CODE: 'CODE',
    COORDINATE: 'COORDINATE',
    DATE: 'DATE',
    FILE: 'FILE',
    NUMBER: 'NUMBER',
    TAXON: 'TAXON',
    TEXT: 'TEXT',
    TIME: 'TIME',
  }

  getFieldLabel(fieldName) {
    const index = this.fieldNames.indexOf(fieldName)
    return this.fieldLabels[index]
  }

  /**
   * Visible field names in order
   */
  get availableFieldNames() {
    return this.fieldnames
  }
}

export class CodeAttributeDefinition extends AttributeDefinition {
  codeListId
  parentCodeAttributeDefinitionId
  mandatoryFieldNames = ['code']
  itemsOrientation

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
    const visitedIds = []
    let currentParentAttrDef = this.parentCodeAttributeDefinition
    while (currentParentAttrDef && !visitedIds.includes(currentParentAttrDef.id)) {
      ancestorIds.push(currentParentAttrDef.id)
      visitedIds.push(currentParentAttrDef.id)
      const nextParentAttrDef = currentParentAttrDef.parentCodeAttributeDefinition
      currentParentAttrDef = nextParentAttrDef
    }
    return ancestorIds
  }

  get levelIndex() {
    return this.ancestorCodeAttributeDefinitionIds.length
  }
}

export class CoordinateAttributeDefinition extends AttributeDefinition {
  fieldsOrder
  showSrsField
  includeAltitudeField
  includeAccuracyField

  static FieldsOrder = {
    SRS_X_Y: 'SRS_X_Y',
    SRS_Y_X: 'SRS_Y_X',
    X_Y_SRS: 'X_Y_SRS',
    Y_X_SRS: 'Y_X_SRS',
  }

  static Fields = {
    X: 'x',
    Y: 'y',
    SRS: 'srs',
    ALTITUDE: 'altitude',
    ACCURACY: 'accuracy',
  }

  get availableFieldNames() {
    const fields = []
    switch (this.fieldsOrder) {
      case CoordinateAttributeDefinition.FieldsOrder.SRS_X_Y:
        fields.push(
          CoordinateAttributeDefinition.Fields.SRS,
          CoordinateAttributeDefinition.Fields.X,
          CoordinateAttributeDefinition.Fields.Y
        )
        break
      case CoordinateAttributeDefinition.FieldsOrder.SRS_Y_X:
        fields.push(
          CoordinateAttributeDefinition.Fields.SRS,
          CoordinateAttributeDefinition.Fields.Y,
          CoordinateAttributeDefinition.Fields.X
        )
        break
      case CoordinateAttributeDefinition.FieldsOrder.X_Y_SRS:
        fields.push(
          CoordinateAttributeDefinition.Fields.X,
          CoordinateAttributeDefinition.Fields.Y,
          CoordinateAttributeDefinition.Fields.SRS
        )
        break
      case CoordinateAttributeDefinition.FieldsOrder.Y_X_SRS:
        fields.push(
          CoordinateAttributeDefinition.Fields.Y,
          CoordinateAttributeDefinition.Fields.X,
          CoordinateAttributeDefinition.Fields.SRS
        )
        break
      default:
        throw new Error(`Fields order not supported: ${this.fieldsOrder}`)
    }
    if (!this.showSrsField) {
      Arrays.deleteItem(CoordinateAttributeDefinition.Fields.SRS)(fields)
    }
    if (this.includeAltitudeField) {
      fields.push(CoordinateAttributeDefinition.Fields.ALTITUDE)
    }
    if (this.includeAccuracyField) {
      fields.push(CoordinateAttributeDefinition.Fields.ACCURACY)
    }
    return fields
  }
}

export class TaxonAttributeDefinition extends AttributeDefinition {
  static QueryFields = {
    CODE: 'CODE',
    SCIENTIFIC_NAME: 'SCIENTIFIC_NAME',
    VERNACULAR_NAME: 'VERNACULAR_NAME',
    FAMILY_CODE: 'FAMILY_CODE',
    FAMILY_SCIENTIFIC_NAME: 'FAMILY_SCIENTIFIC_NAME',
  }

  static Fields = {
    CODE: 'code',
    SCIENTIFIC_NAME: 'scientific_name',
    VERNACULAR_NAME: 'vernacular_name',
    LANGUAGE_CODE: 'language_code',
    LANGUAGE_VARIETY: 'language_variety',
    FAMILY_CODE: 'family_code',
    FAMILY_SCIENTIFIC_NAME: 'family_scientific_name',
  }

  static QueryFieldByField = {
    [TaxonAttributeDefinition.Fields.CODE]: TaxonAttributeDefinition.QueryFields.CODE,
    [TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME]: TaxonAttributeDefinition.QueryFields.SCIENTIFIC_NAME,
    [TaxonAttributeDefinition.Fields.VERNACULAR_NAME]: TaxonAttributeDefinition.QueryFields.VERNACULAR_NAME,
    [TaxonAttributeDefinition.Fields.FAMILY_CODE]: TaxonAttributeDefinition.QueryFields.FAMILY_CODE,
    [TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME]:
      TaxonAttributeDefinition.QueryFields.FAMILY_SCIENTIFIC_NAME,
  }

  static ValueFields = {
    CODE: 'code',
    SCIENTIFIC_NAME: 'scientificName',
    VERNACULAR_NAME: 'vernacularName',
    LANGUAGE_CODE: 'languageCode',
    LANGUAGE_VARIETY: 'languageVariety',
    FAMILY_CODE: 'familyCode',
    FAMILY_SCIENTIFIC_NAME: 'familyScientificName',
  }

  static ValueFieldByField = {
    [TaxonAttributeDefinition.Fields.CODE]: TaxonAttributeDefinition.ValueFields.CODE,
    [TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME]: TaxonAttributeDefinition.ValueFields.SCIENTIFIC_NAME,
    [TaxonAttributeDefinition.Fields.VERNACULAR_NAME]: TaxonAttributeDefinition.ValueFields.VERNACULAR_NAME,
    [TaxonAttributeDefinition.Fields.LANGUAGE_CODE]: TaxonAttributeDefinition.ValueFields.LANGUAGE_CODE,
    [TaxonAttributeDefinition.Fields.LANGUAGE_VARIETY]: TaxonAttributeDefinition.ValueFields.LANGUAGE_VARIETY,
    [TaxonAttributeDefinition.Fields.FAMILY_CODE]: TaxonAttributeDefinition.ValueFields.FAMILY_CODE,
    [TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME]:
      TaxonAttributeDefinition.ValueFields.FAMILY_SCIENTIFIC_NAME,
  }

  static FieldByValueField = {
    [TaxonAttributeDefinition.ValueFields.CODE]: TaxonAttributeDefinition.Fields.CODE,
    [TaxonAttributeDefinition.ValueFields.SCIENTIFIC_NAME]: TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME,
    [TaxonAttributeDefinition.ValueFields.VERNACULAR_NAME]: TaxonAttributeDefinition.Fields.VERNACULAR_NAME,
    [TaxonAttributeDefinition.ValueFields.LANGUAGE_CODE]: TaxonAttributeDefinition.Fields.LANGUAGE_CODE,
    [TaxonAttributeDefinition.ValueFields.LANGUAGE_VARIETY]: TaxonAttributeDefinition.Fields.LANGUAGE_VARIETY,
    [TaxonAttributeDefinition.ValueFields.FAMILY_CODE]: TaxonAttributeDefinition.Fields.FAMILY_CODE,
    [TaxonAttributeDefinition.ValueFields.FAMILY_SCIENTIFIC_NAME]:
      TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME,
  }

  static AUTOCOMPLETE_FIELDS = [
    TaxonAttributeDefinition.Fields.FAMILY_CODE,
    TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME,
    TaxonAttributeDefinition.Fields.CODE,
    TaxonAttributeDefinition.Fields.SCIENTIFIC_NAME,
    TaxonAttributeDefinition.Fields.VERNACULAR_NAME,
  ]

  static LANGUAGE_FIELDS = [
    TaxonAttributeDefinition.Fields.LANGUAGE_CODE,
    //TaxonAttributeDefinition.Fields.LANGUAGE_VARIETY,
  ]

  static ALL_FIELDS_IN_ORDER = [
    ...TaxonAttributeDefinition.AUTOCOMPLETE_FIELDS,
    ...TaxonAttributeDefinition.LANGUAGE_FIELDS,
  ]

  static isAutocompleteField(field) {
    return TaxonAttributeDefinition.AUTOCOMPLETE_FIELDS.includes(field)
  }

  static isLanguageField(field) {
    return TaxonAttributeDefinition.LANGUAGE_FIELDS.includes(field)
  }

  taxonomyName
  highestRank
  codeVisible
  scientificNameVisible
  vernacularNameVisible
  languageCodeVisible
  languageVarietyVisible
  showFamily
  includeUniqueVernacularName
  allowUnlisted

  _isFieldAvailable = (field) =>
    this.visibilityByField[field] &&
    (![TaxonAttributeDefinition.Fields.FAMILY_CODE, TaxonAttributeDefinition.Fields.FAMILY_SCIENTIFIC_NAME].includes(
      field
    ) ||
      this.showFamily)

  get availableFieldNames() {
    return TaxonAttributeDefinition.ALL_FIELDS_IN_ORDER.reduce((acc, field) => {
      if (this._isFieldAvailable(field)) {
        acc.push(field)
      }
      return acc
    }, [])
  }
}

export class FileAttributeDefinition extends AttributeDefinition {
  fileType
  extensions
  maxSize

  static FileTypes = {
    AUDIO: 'AUDIO',
    DOCUMENT: 'DOCUMENT',
    IMAGE: 'IMAGE',
    VIDEO: 'VIDEO',
  }
}

export class NumericAttributeDefinition extends AttributeDefinition {
  numericType
  precisions
  mandatoryFieldNames = ['value']
}

export class TextAttributeDefinition extends AttributeDefinition {
  textType

  static TextTypes = {
    SHORT: 'SHORT',
    MEMO: 'MEMO',
  }
}

const attributeDefinitionClassByType = {
  [AttributeDefinition.Types.CODE]: CodeAttributeDefinition,
  [AttributeDefinition.Types.COORDINATE]: CoordinateAttributeDefinition,
  [AttributeDefinition.Types.FILE]: FileAttributeDefinition,
  [AttributeDefinition.Types.NUMBER]: NumericAttributeDefinition,
  [AttributeDefinition.Types.TAXON]: TaxonAttributeDefinition,
}
