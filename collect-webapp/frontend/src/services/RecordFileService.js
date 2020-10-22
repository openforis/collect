import AbstractService from './AbstractService'

export default class RecordFileService extends AbstractService {
  downloadRecordFile({ fileAttribute }) {
    const { record } = fileAttribute
    const { survey } = record
    const recordId = record.id || 0
    this.downloadFile(
      `${this.BASE_URL}survey/${survey.id}/data/records/${recordId}/${record.step}/node/${fileAttribute.id}/file`
    )
  }
}
