import { UIModelObjectDefinition } from './UIModelObjectDefinition'
import { TabContainers } from './TabContainers'

export class TabDefinition extends UIModelObjectDefinition {
  items = []
  tabs = []
  label
  totalColumns
  totalRows

  constructor(id, parent) {
    super(id, parent)
    this.items = []
    this.tabs = []
  }

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj, { skipFields: ['children'] })
    this.tabs = TabContainers.createTabsFromJSON({ json: jsonObj.tabs, parent: this })
    this.items = TabContainers.createItemsFromJSON({ json: jsonObj.children, parent: this })
  }

  isInVersion(version) {
    const innerTabsInVersion = this.tabs.filter((tab) => tab.isInVersion(version))
    const itemsInVersion = this.items.filter((item) => item.isInVersion(version))
    return innerTabsInVersion.length > 0 || itemsInVersion.length > 0
  }
}
