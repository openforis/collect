import AbstractService from './AbstractService';

export default class CodeListService extends AbstractService {

    findAllItems(survey, codeListId, level) {
        return this.get('survey/'+ survey.id + '/codelist/' + codeListId, {
            level: level
        })
    }

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
