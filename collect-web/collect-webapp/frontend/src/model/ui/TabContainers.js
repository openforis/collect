import { CodeFieldDefinition } from './CodeFieldDefinition';
import { FieldDefinition } from './FieldDefinition';
import { FieldsetDefinition } from './FieldsetDefinition';
import { MultipleFieldsetDefinition } from './MultipleFieldsetDefinition';
import { TabDefinition } from './TabDefinition';
import { TableDefinition } from './TableDefinition';

export class TabContainers {
	
    static createTabsFromJSON(jsonArrObj, parentUIModelObject) {
        var tabs = [];
        for (var i = 0; i < jsonArrObj.length; i++) {
            var itemJsonObj = jsonArrObj[i];
            var item = new TabDefinition(itemJsonObj.id, parentUIModelObject);
            item.fillFromJSON(itemJsonObj);
            tabs.push(item);
        }
        return tabs;
    }
    
    static createItemsFromJSON(jsonObj, parentUIModelObject) {
        let items = [];
        for (var i = 0; i < jsonObj.length; i++) {
            var itemJsonObj = jsonObj[i];
            var item;
            switch(itemJsonObj.type) {
            case 'FIELD':
                if (itemJsonObj.attributeType === 'CODE') {
                    item = new CodeFieldDefinition(itemJsonObj.id, parentUIModelObject);
                } else {
                    item = new FieldDefinition(itemJsonObj.id, parentUIModelObject);
                }
                break;
            case 'FIELDSET':
                item = new FieldsetDefinition(itemJsonObj.id, parentUIModelObject);
                break;
            case 'MULTIPLE_FIELDSET':
                item = new MultipleFieldsetDefinition(itemJsonObj.id, parentUIModelObject);
                break;
            case 'TABLE':
                item = new TableDefinition(itemJsonObj.jd, parentUIModelObject);
                break;
            default:
                item = null;
            }
            if (item != null) {
                item.fillFromJSON(itemJsonObj);
                items.push(item);
            }
        }
        return items;
    }
}