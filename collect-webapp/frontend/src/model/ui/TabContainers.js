import { AttributeDefinition } from 'model/Survey'
import { CodeFieldDefinition } from './CodeFieldDefinition'
import { FieldDefinition } from './FieldDefinition'
import { FieldsetDefinition } from './FieldsetDefinition'
import { MultipleFieldsetDefinition } from './MultipleFieldsetDefinition'
import { TabDefinition } from './TabDefinition'
import { TableDefinition } from './TableDefinition'
import FormItemTypes from './FormItemTypes'

const getFormItemClass = (itemType, attributeType) => {
  switch (itemType) {
    case FormItemTypes.FIELD:
      return attributeType === AttributeDefinition.Types.CODE ? CodeFieldDefinition : FieldDefinition
    case FormItemTypes.FIELDSET:
      return FieldsetDefinition
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
      const { type, attributeType, id } = itemJsonObj
      const formItemClass = getFormItemClass(type, attributeType)
      if (formItemClass) {
        const item = new formItemClass(id, parent)
        item.fillFromJSON(itemJsonObj)
        itemsAcc.push(item)
      }
      return itemsAcc
    }, [])
  }
}
