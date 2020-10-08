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
  static createTabsFromJSON(jsonArrObj, parentUIModelObject) {
    var tabs = []
    for (var i = 0; i < jsonArrObj.length; i++) {
      var itemJsonObj = jsonArrObj[i]
      var item = new TabDefinition(itemJsonObj.id, parentUIModelObject)
      item.fillFromJSON(itemJsonObj)
      tabs.push(item)
    }
    return tabs
  }

  static createItemsFromJSON(jsonObj, parentUIModelObject) {
    const items = []
    for (var i = 0; i < jsonObj.length; i++) {
      const itemJsonObj = jsonObj[i]
      const { type, attributeType, id } = itemJsonObj
      const formItemClass = getFormItemClass(type, attributeType)
      if (formItemClass) {
        const item = new formItemClass(id, parentUIModelObject)
        item.fillFromJSON(itemJsonObj)
        items.push(item)
      }
    }
    return items
  }
}
