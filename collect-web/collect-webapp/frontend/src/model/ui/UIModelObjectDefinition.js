import Serializable from '../Serializable';

export class UIModelObjectDefinition extends Serializable {
    parent;
    id;
    hidden;
    
    constructor(id, parent) {
        super();
        this.id = id;
        this.parent = parent;
    }
    
    get rootTabSet() {
        let currentObj = this;
        while (currentObj.parent != null) {
            currentObj = currentObj.parent;
        }
        return currentObj;
    }
    
    get uiConfiguration() {
        return this.rootTabSet.uiConfiguration;
    }
    
    get survey() {
        return this.rootTabSet.survey;
    }

}
