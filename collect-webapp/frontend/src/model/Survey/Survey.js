import Serializable from '../Serializable'
import { UIConfiguration } from '../ui/UIConfiguration'
import { CodeList } from './CodeList'
import { EntityDefinition } from './EntityDefinition'
import { SpatialReferenceSystem } from './SpatialReferenceSystem'
import { Unit } from './Unit'

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
  preferredLanguage

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

  getVersionById(versionId) {
    return this.modelVersions.find((version) => version.id === versionId)
  }
}

class Schema extends Serializable {
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
      rootEntity.traverse((nodeDef) => {
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
