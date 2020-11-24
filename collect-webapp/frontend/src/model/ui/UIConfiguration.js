import Serializable from '../Serializable'
import { TabSetDefinition } from './TabSetDefinition'

export class UIConfiguration extends Serializable {
  survey
  tabSets

  constructor(survey) {
    super()
    this.survey = survey
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)
    this.tabSets = jsonObj.tabSets.reduce((acc, tabSetJsonObj) => {
      var tabSet = new TabSetDefinition(tabSetJsonObj.id, this, null)
      tabSet.fillFromJSON(tabSetJsonObj)
      acc.push(tabSet)
      return acc
    }, [])
  }

  get mainTabSet() {
    return this.tabSets[0]
  }

  getTabSetByRootEntityDefinitionId(rootEntityDefinitionId) {
    return this.tabSets.find((tabSet) => tabSet.rootEntityDefinitionId === rootEntityDefinitionId)
  }
}
