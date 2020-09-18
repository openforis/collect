import AbstractService from './AbstractService'

export default class CodeListService extends AbstractService {
  findAvailableItems(parentEntity, codeAttrDef) {
    let record = parentEntity.record
    let survey = record.survey

    return this.get('survey/' + survey.id + '/codelist/' + codeAttrDef.codeListId, {
      recordId: record.preview ? null : record.id,
      recordStep: record.dataStep,
      parentEntityPath: parentEntity.path,
      codeAttrDefId: codeAttrDef.id,
    })
  }
}
