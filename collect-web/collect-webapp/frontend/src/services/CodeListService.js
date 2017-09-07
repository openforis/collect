import AbstractService from './AbstractService';
import { RecordEventWrapper } from '../model/event/RecordEvent'
import EventQueue from '../model/event/EventQueue'

export default class CodeListService extends AbstractService {

    findAvailableItems(parentEntity, codeAttrDef) {
        let record = parentEntity.record
        let survey = record.survey

        return this.get('survey/'+ survey.id + '/codelist/' + codeAttrDef.codeListId, {
            recordId: record.id,
            parentEntityId: parentEntity.id,
            codeAttrDefId: codeAttrDef.id
        })
    }
}
