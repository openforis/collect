import { Event } from './Event';
import Serializable from '../Serializable';

export class RecordEvent extends Event {
	
	eventType
	surveyName
	recordId
	recordStep
	definitionId
	ancestorIds
	nodeId
	timestamp
	userName
	
	constructor(jsonObj) {
        super()
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
}

export class AttributeUpdatedEvent extends AttributeEvent {
}

export class BooleanAttributeUpdatedEvent extends AttributeUpdatedEvent {
    value
}
    

export class CodeAttributeUpdatedEvent extends AttributeUpdatedEvent {
    code
    qualifier
}

export class CoordinateAttributeUpdatedEvent extends AttributeUpdatedEvent {
    x
    y
    srsId
}

export class DateAttributeUpdatedEvent extends AttributeUpdatedEvent {
    date
}

export class DoubleAttributeUpdatedEvent extends AttributeUpdatedEvent {
    value
}

export class DoubleRangeAttributeUpdatedEvent extends AttributeUpdatedEvent {
    from
    to
}

export class IntegerAttributeUpdatedEvent extends AttributeUpdatedEvent {
    value
}

export class IntegerRangeAttributeUpdatedEvent extends AttributeUpdatedEvent {
    from
    to
}

export class TaxonAttributeUpdatedEvent extends AttributeUpdatedEvent {
    code
    scientificName
    vernacularName
    languageCode
    languageVariety
}

export class TextAttributeUpdatedEvent extends AttributeUpdatedEvent {
    text
}

export class TimeAttributeUpdatedEvent extends AttributeUpdatedEvent {
    time
}

export class RelevanceChangedEvent extends RecordEvent {
    childDefId
    relevant
}

export class EntityCreatedEvent extends RecordEvent {
}

export class EntityCollectionCreatedEvent extends RecordEvent {
}

export class RecordEventWrapper extends Serializable {
    event
    eventType
    
    constructor(jsonObj) {
        super()
        if (jsonObj) {
            this.fillFromJSON(jsonObj)
        }
    }
   
    fillFromJSON(jsonObj) {
        super.fillFromJSON(jsonObj)
        this.event = this._parseEvent(jsonObj)
    }

    _parseEvent(jsonObj) {
        switch(jsonObj.eventType) {
        case "EntityCreatedEvent":
            return new EntityCreatedEvent(jsonObj.event)
        case "EntityCollectionCreatedEvent":
            return new EntityCreatedEvent(jsonObj.event)
        case "RelevanceChangedEvent":
            return new RelevanceChangedEvent(jsonObj.event)
        case "BooleanAttributeUpdatedEvent":
            return new BooleanAttributeUpdatedEvent(jsonObj.event)
        case "CodeAttributeUpdatedEvent":
            return new CodeAttributeUpdatedEvent(jsonObj.event)
        case "CoordinateAttributeUpdatedEvent":
            return new CoordinateAttributeUpdatedEvent(jsonObj.event)
        case "DateAttributeUpdatedEvent":
            return new DateAttributeUpdatedEvent(jsonObj.event)
        case "DoubleAttributeUpdatedEvent":
            return new DoubleAttributeUpdatedEvent(jsonObj.event)
        case "DoubleRangeAttributeUpdatedEvent":
            return new DoubleRangeAttributeUpdatedEvent(jsonObj.event)
        case "IntegerAttributeUpdatedEvent":
            return new IntegerAttributeUpdatedEvent(jsonObj.event)
        case "IntegerRangeAttributeUpdatedEvent":
            return new IntegerRangeAttributeUpdatedEvent(jsonObj.event)
        case "TaxonAttributeUpdatedEvent":
            return new TaxonAttributeUpdatedEvent(jsonObj.event)
        case "TextAttributeUpdatedEvent":
            return new TextAttributeUpdatedEvent(jsonObj.event)
        case "TimeAttributeUpdatedEvent":
            return new TimeAttributeUpdatedEvent(jsonObj.event)
        default: 
            console.log("Unsupported event type: " + jsonObj.eventType)
            return null //TODO throw error?
        }
    }
}