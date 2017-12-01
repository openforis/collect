import { TabContainers } from './TabContainers';
import { UIModelObjectDefinition } from './UIModelObjectDefinition';

export class TabSetDefinition extends UIModelObjectDefinition {

    _uiConfiguration;
    rootEntityDefinitionId;
    items = [];
    tabs = [];
    totalColumns;
    totalRows;

    constructor(id, uiConfiguration, parent) {
        super(id, parent);
        this._uiConfiguration = uiConfiguration;
        this.tabs = [];
        this.items = [];
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        this.tabs = TabContainers.createTabsFromJSON(jsonObj.tabs, this);
        this.items = TabContainers.createItemsFromJSON(jsonObj.children, this);
    }
    
    get uiConfiguration() {
        return this._uiConfiguration;
    }
    
    get survey() {
        return this.uiConfiguration.survey;
    }

}