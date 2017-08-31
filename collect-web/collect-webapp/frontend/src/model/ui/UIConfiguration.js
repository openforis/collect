import { Serializable } from '../Serializable';
import { TabSetDefinition } from './TabSetDefinition';
import { Survey } from '../Survey';

export class UIConfiguration extends Serializable {
    
    survey;
    tabSets;
    
    constructor(survey) {
        super();
        this.survey = survey;
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        this.tabSets = [];
        for (var i = 0; i < jsonObj.tabSets.length; i++) {
            var tabSetJsonObj = jsonObj.tabSets[i];
            var tabSet = new TabSetDefinition(tabSetJsonObj.id, this, null);
            tabSet.fillFromJSON(tabSetJsonObj);
            this.tabSets.push(tabSet);
        }
    }
    
    get mainTabSet() {
        return this.tabSets[0];
    }

    getTabSetByRootEntityDefinitionId(rootEntityDefinitionId) {
        return this.tabSets.find(tabSet => tabSet.rootEntityDefinitionId === rootEntityDefinitionId)
    }
}