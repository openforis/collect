import { UIModelObjectDefinition } from './UIModelObjectDefinition';
import { AttributeDefinition } from '../Survey';

export class FieldDefinition extends UIModelObjectDefinition {
    
    attributeType;
    attributeDefinitionId;
    label;
    column;
    columnSpan;
    row;
    
    constructor(id, parent) {
        super(id, parent);
        
    }
    
    get attributeDefinition() {
        let survey = this.parent.uiConfiguration.survey;
        return survey.schema.getDefinitionById(this.attributeDefinitionId);
    }
 
}    