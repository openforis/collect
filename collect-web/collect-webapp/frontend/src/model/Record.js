import { Serializable } from './Serializable';
import { Survey, NodeDefinition, EntityDefinition } from './Survey';

export class Record extends Serializable {
    id;
    survey;
    step;
    stepNumber;
    rootEntity;
    rootEntityKeys = [];
    nodeById = [];
    
    constructor(survey, jsonData) {
        super();
        this.survey = survey;
        this.nodeById = [];
        if (jsonData) {
            this.fillFromJSON(jsonData);
        }
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        
        let rootEntityDefId = parseInt(jsonObj.rootEntity.definitionId);
        let rootEntityDef = this.survey.schema.getDefinitionById(rootEntityDefId);
            
        this.rootEntity = new Entity(this, rootEntityDef, null);
        this.rootEntity.fillFromJSON(jsonObj.rootEntity);
    }
    
    getNodeById(nodeId: number): Node {
        return this.nodeById[nodeId];
    }
}

export class Node extends Serializable {
    
    record;
    parent;
    definition;
    id;
    
    constructor(record, definition, parent) {
        super();
        this.record = record;
        this.definition = definition;
        this.parent = parent;
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
    }
}

export class Entity extends Node {
    
    childrenByDefinitionId;
    
    constructor(record, definition, parent) {
        super(record, definition, parent);
    }
    
    get summaryLabel() {
        return "Entity " + this.id;
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        
        let $this = this;
        this.childrenByDefinitionId = [];
        for (var defIdStr in jsonObj.childrenByDefinitionId) {
            let defId = parseInt(defIdStr);
            let def = this.record.survey.schema.getDefinitionById(defId);
            let childrenJsonObj = jsonObj.childrenByDefinitionId[defId];
            let children = [];
            for (var i = 0; i < childrenJsonObj.length; i++) {
                let childJsonObj = childrenJsonObj[i];
                let node;
                if (def instanceof EntityDefinition) {
                    node = new Entity(this.record, def, this);
                } else {
                    node = new Attribute(this.record, def, this);
                }
                node.fillFromJSON(childJsonObj);
                children.push(node);
                $this.record.nodeById[node.id] = node;
            }
            $this.childrenByDefinitionId[defId] = children;
        }
    }
    
    getDescendants(ancestorDefIds) {
        let currentEntity = this;
        let descendants;
        for (var ancestorDefId in ancestorDefIds) {
            descendants = currentEntity.childrenByDefinitionId[ancestorDefId];
        }
        return descendants;
    } 
    
    getSingleChild(defId) {
        let children = this.childrenByDefinitionId[defId];
        return children == null || children.length == 0 ? null : children[0];
    }
    
    addChild(child) {
        let children = this.childrenByDefinitionId[child.definition.id];
        if (children == null) {
            children = [];
            this.childrenByDefinitionId[child.definition.id] = children;
        }
        children.push(child);
    }
}

export class Attribute extends Node {
    
    fields = [];
    
    constructor(record, definition, parent) {
        super(record, definition, parent);
    }
    
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        
        this.fields = [];
        for (var i = 0; i < jsonObj.fields.length; i++) {
            let fieldJsonObj = jsonObj.fields[i];
            let field = new Field();
            field.fillFromJSON(fieldJsonObj);
            this.fields.push(field);
        }
    }
    
    get allFieldsFilled() {
        for (var i = 0; i < this.fields.length; i++) {
            let field = this.fields[i];
            if (field.value == null) {
                return false;
            }
        }
        return true;
    }
    
    setFieldValue(fieldIdx, value) {
        if (this.fields == null) {
            this.fields = [];
        }
        while (this.fields.length <= fieldIdx) {
            this.fields.push(new Field());
        }
        this.fields[fieldIdx].value = value;
    }
}

export class Field extends Serializable {
    
    value;
    remarks;
    
    constructor() {
        super();
    }
    
    get intValue() {
        return this.value == null ? null : parseInt(this.value.toString());
    }
}
