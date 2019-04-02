import AbstractService from './AbstractService';
import { RecordEventWrapper } from '../model/event/RecordEvent'
import EventQueue from '../model/event/EventQueue'

export default class CommandService extends AbstractService {

    constructor() {
        super()
        this._handleEventResponse = this._handleEventResponse.bind(this)
    }

    addAttribute(record, parentEntityId, attrDef) {
        let username = "admin";

        let command = {
            username: username,
            surveyId: record.survey.id,
            recordId: record.id,
            parentEntityId: parentEntityId,
            nodeDefId: attrDef.id
        };

        return this.postJson('command/record/attribute', command) 
            .then(this._handleEventResponse)
    }

    updateAttribute(attribute, attributeType, valueByField) {
        let username = "admin";

        let command = {
            username: username,
            surveyId: attribute.record.survey.id,
            recordId: attribute.record.id,
            parentEntityId: attribute.parent.id,
            nodeDefId: attribute.definition.id,
            nodeId: attribute.id,
            attributeType: attributeType,
            valueByField: valueByField
        }
        return this.patchJson('command/record/attribute', command) 
            .then(this._handleEventResponse)
    }

    addEntity(record, parentEntityId, entityDef) {
        let username = "admin";

        let command = {
            username: username,
            surveyId: record.survey.id,
            recordId: record.id,
            parentEntityId: parentEntityId,
            nodeDefId: entityDef.id
        };

        return this.postJson('command/record/entity', command) 
            .then(this._handleEventResponse)
        }


    _handleEventResponse(res) {
        res.forEach(eventJsonObj => {
            let eventWrapper = new RecordEventWrapper(eventJsonObj);
            EventQueue.publish('recordEvent', eventWrapper.event);
        });
    }
}
