import { UIModelObjectDefinition } from './UIModelObjectDefinition';
import { FieldDefinition } from './FieldDefinition';

export class CodeFieldDefinition extends FieldDefinition {
    
    layout;
    itemsOrientation;
    showCode;
    
    constructor(id, parent) {
        super(id, parent);
    }
}