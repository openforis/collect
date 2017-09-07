import Serializable from './Serializable';
import { UIConfiguration } from './ui/UIConfiguration';

export class SurveyObject extends Serializable {
    id;
    
    constructor(id) {
        super();
        this.id = id;
    }
}    

export class Survey extends Serializable {
    id;
    name;
    uri;
    schema;
    codeLists = [];
    uiConfiguration;
    
    constructor(jsonData) {
        super();
        if (jsonData) {
            this.fillFromJSON(jsonData)
        }
    }

    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        
        this.codeLists = [];
        for (var i = 0; i < jsonObj.codeLists.length; i++) {
            var codeListJsonObj = jsonObj.codeLists[i];
            var codeList = new CodeList(this);
            codeList.fillFromJSON(codeListJsonObj);
            this.codeLists.push(codeList);
        }
        this.schema = new Schema(this);
        this.schema.fillFromJSON(jsonObj.schema);
        this.uiConfiguration = new UIConfiguration(this);
        this.uiConfiguration.fillFromJSON(jsonObj.uiConfiguration);
    }
}

export class CodeList extends Serializable {
    survey;
    id;
    name;
    items = [];
    hierarchycal;
    
    constructor(survey) {
        super();
        this.survey = survey;
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        
        this.items = [];
        for (var i = 0; i < jsonObj.items.length; i++) {
            let itemJsonObj = jsonObj.items[i];
            let codeListItem = new CodeListItem();
            codeListItem.fillFromJSON(itemJsonObj);
            this.items.push(codeListItem);
        }
    }
}

export class CodeListItem extends Serializable {
    
    code;
    label;
    color;
    
    constructor() {
        super();
    }
}

export class Schema extends Serializable {
    survey;
    rootEntities = [];
    definitions = []; //cache
    
    constructor(survey) {
        super();
        this.survey = survey;
        this.definitions = [];
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        
        let $this = this;
        
        this.rootEntities = [];
        for (var i = 0; i < jsonObj.rootEntities.length; i++) {
            var rootEntityJsonObj = jsonObj.rootEntities[i];
            var rootEntity = new EntityDefinition(rootEntityJsonObj.id, this.survey, null);
            rootEntity.fillFromJSON(rootEntityJsonObj);
            this.rootEntities.push(rootEntity);
            rootEntity.traverse(function(nodeDef) {
                $this.definitions[nodeDef.id] = nodeDef;
            });
        }
    }
    
    get defaultRootEntity() {
        return this.rootEntities[0];
    }
    
    getDefinitionById(id) {
        return this.definitions[id];
    }
}

export class NodeDefinition extends SurveyObject {
    survey;
    parent;
    name;
    label;
    multiple;
    
    constructor(id, survey, parent) {
        super(id);
        this.survey = survey;
        this.parent = parent;
    }
}

export class EntityDefinition extends NodeDefinition {
    children = [];
    
    constructor(id, survey, parent) {
        super(id, survey, parent);
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        
        this.children = [];
        for (var i = 0; i < jsonObj.children.length; i++) {
            let nodeJsonObj = jsonObj.children[i];
            let nodeDef;
            if (nodeJsonObj.type == 'ENTITY') { 
                nodeDef = new EntityDefinition(nodeJsonObj.id, this.survey, this);
            } else {
                switch (nodeJsonObj.attributeType) {
                case 'CODE':
                    nodeDef = new CodeAttributeDefinition(nodeJsonObj.id, this.survey, this);
                    break;
                case 'NUMERIC':
                    nodeDef = new NumericAttributeDefinition(nodeJsonObj.id, this.survey, this);
                    break;
                default:
                    nodeDef = new AttributeDefinition(nodeJsonObj.id, this.survey, this);
                }
            }
            nodeDef.fillFromJSON(nodeJsonObj);
            this.children.push(nodeDef);
        }
    }
    
    traverse(visitor) {
        let stack = [];
        stack.push(this);
        while (stack.length > 0) {
            let nodeDef = stack.pop();
            visitor(nodeDef);
            if (nodeDef instanceof EntityDefinition) {
                let children = nodeDef.children;
                for(var i = 0; i < nodeDef.children.length; i++) {
                    let child = nodeDef.children[i];
                    stack.push(child);
                }
            }
        }
    }
    
    get keyAttributeDefinitions() {
        let result = [];
        let queue = [];
        
        for (var i = 0; i < this.children.length; i++) {
            queue.push(this.children[i]);
        }
        while (queue.length > 0) {
            let item = queue.shift();
            if (item instanceof AttributeDefinition) {
                let attrDef = item;
                if (attrDef.key) {
                    result.push(item);
                }
            }
            if (item instanceof EntityDefinition && ! item.multiple) {
                for (var i = 0; i < item.children.length; i++) {
                    queue.push(item.children[i]);
                }
            }
        }
        return result;
    }
}

export class AttributeDefinition extends NodeDefinition {
    key;
    attributeType;
    
    constructor(id, survey, parent) {
        super(id, survey, parent);
    }
}

export class CodeAttributeDefinition extends AttributeDefinition {
    
    codeListId;
    parentCodeAttributeDefinitionId;
    
    constructor(id, survey, parent) {
        super(id, survey, parent);
    }
}

export class NumericAttributeDefinition extends AttributeDefinition {
    
    numericType;
    
    constructor(id, survey, parent) {
        super(id, survey, parent);
    }
}