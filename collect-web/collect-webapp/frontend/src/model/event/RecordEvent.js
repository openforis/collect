import { Event } from './Event';
import Serializable from '../Serializable';

export class RecordEvent extends Event {
	
	eventType;
	surveyName;
	recordId;
	recordStep;
	definitionId;
	ancestorIds;
	nodeId;
	timestamp;
	userName;
	
	constructor(jsonObj) {
        super();
        if (jsonObj) {
            this.fillFromJSON(jsonObj)
        }
    }
    
    /**
     * Returns the first not NaN id in ancestorIds
     * (NaN ids are for entity containers)
     */
    get parentEntityId() {
        let result = this.ancestorIds.find(id => ! isNaN(id) )
        return result ? parseInt(result) : NaN
    }
}

export class AttributeEvent extends RecordEvent {
    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class AttributeUpdatedEvent extends AttributeEvent {
    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class BooleanAttributeUpdatedEvent extends AttributeUpdatedEvent {
    value;

    constructor(jsonObj) {
        super(jsonObj);
    }
}
    

export class CodeAttributeUpdatedEvent extends AttributeUpdatedEvent {
    code;
    qualifier;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class CoordinateAttributeUpdatedEvent extends AttributeUpdatedEvent {
    x;
    y;
    srsId;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class DateAttributeUpdatedEvent extends AttributeUpdatedEvent {
    date;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class DoubleAttributeUpdatedEvent extends AttributeUpdatedEvent {
    value;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class DoubleRangeAttributeUpdatedEvent extends AttributeUpdatedEvent {
    from;
    to;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class IntegerAttributeUpdatedEvent extends AttributeUpdatedEvent {
    value;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class IntegerRangeAttributeUpdatedEvent extends AttributeUpdatedEvent {
    from;
    to;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class TaxonAttributeUpdatedEvent extends AttributeUpdatedEvent {
    code;
    scientificName;
    vernacularName;
    languageCode;
    languageVariety;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class TextAttributeUpdatedEvent extends AttributeUpdatedEvent {
    text;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class TimeAttributeUpdatedEvent extends AttributeUpdatedEvent {
    time;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class RelevanceChangedEvent extends RecordEvent {
    childDefId;
    relevant;

    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class EntityCreatedEvent extends RecordEvent {
    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class EntityCollectionCreatedEvent extends RecordEvent {
    constructor(jsonObj) {
        super(jsonObj);
    }
}

export class RecordEventWrapper extends Serializable {
    
    event;
    eventType;
    
    constructor(jsonObj) {
        super()
        if (jsonObj) {
            this.fillFromJSON(jsonObj)
        }
    }
   
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj);
        this.event = this._parseEvent(jsonObj)
    }

    _parseEvent(jsonObj) {
        switch(jsonObj.eventType) {
        case "EntityCreatedEvent":
            return new EntityCreatedEvent(jsonObj.event);
        case "EntityCollectionCreatedEvent":
            return new EntityCreatedEvent(jsonObj.event);
        case "RelevanceChangedEvent":
            return new RelevanceChangedEvent(jsonObj.event);
        case "BooleanAttributeUpdatedEvent":
            return new BooleanAttributeUpdatedEvent(jsonObj.event);
        case "CodeAttributeUpdatedEvent":
            return new CodeAttributeUpdatedEvent(jsonObj.event);
        case "CoordinateAttributeUpdatedEvent":
            return new CoordinateAttributeUpdatedEvent(jsonObj.event);
        case "DateAttributeUpdatedEvent":
            return new DateAttributeUpdatedEvent(jsonObj.event);
        case "DoubleAttributeUpdatedEvent":
            return new DoubleAttributeUpdatedEvent(jsonObj.event);
        case "DoubleRangeAttributeUpdatedEvent":
            return new DoubleRangeAttributeUpdatedEvent(jsonObj.event);
        case "IntegerAttributeUpdatedEvent":
            return new IntegerAttributeUpdatedEvent(jsonObj.event);
        case "IntegerRangeAttributeUpdatedEvent":
            return new IntegerRangeAttributeUpdatedEvent(jsonObj.event);
        case "TaxonAttributeUpdatedEvent":
            return new TaxonAttributeUpdatedEvent(jsonObj.event);
        case "TextAttributeUpdatedEvent":
            return new TextAttributeUpdatedEvent(jsonObj.event);
        case "TimeAttributeUpdatedEvent":
            return new TimeAttributeUpdatedEvent(jsonObj.event);
        default: 
            console.log("Unsupported event type: " + jsonObj.eventType);
            return null; //TODO throw error?
        }
    }
}