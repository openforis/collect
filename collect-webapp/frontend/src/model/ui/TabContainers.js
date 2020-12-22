import { FieldDefinition } from './FieldDefinition'
import { FieldsetDefinition } from './FieldsetDefinition'
import { MultipleFieldsetDefinition } from './MultipleFieldsetDefinition'
import { MultipleFieldDefinition } from './MultipleFieldDefinition'
import { TabDefinition } from './TabDefinition'
import { TableDefinition } from './TableDefinition'
import FormItemTypes from './FormItemTypes'

const getFormItemClass = (itemType) => {
  switch (itemType) {
    case FormItemTypes.FIELD:
      return FieldDefinition
    case FormItemTypes.FIELDSET:
      return FieldsetDefinition
    case FormItemTypes.MULTIPLE_FIELD:
      return MultipleFieldDefinition
    case FormItemTypes.MULTIPLE_FIELDSET:
      return MultipleFieldsetDefinition
    case FormItemTypes.TABLE:
      return TableDefinition
    default:
      return null
  }
}

export class TabContainers {
  static createTabsFromJSON({ json = [], parent }) {
    return json.reduce((tabsAcc, itemJsonObj) => {
      var tab = new TabDefinition(itemJsonObj.id, parent)
      tab.fillFromJSON(itemJsonObj)
      tabsAcc.push(tab)
      return tabsAcc
    }, [])
  }

  static createItemsFromJSON({ json = [], parent }) {
    return json.reduce((itemsAcc, itemJsonObj) => {
      const { type, id } = itemJsonObj
      const formItemClass = getFormItemClass(type)
      if (formItemClass) {
        const item = new formItemClass(id, parent)
        item.fillFromJSON(itemJsonObj)
        itemsAcc.push(item)
      }
      return itemsAcc
    }, [])
  }
}
