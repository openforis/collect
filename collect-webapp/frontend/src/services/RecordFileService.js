import AbstractService from './AbstractService'

export default class RecordFileService extends AbstractService {
  downloadRecordFile({ fileAttribute }) {
    const { survey, record, path } = fileAttribute
    const recordId = record.id || 0
    const queryData = this._toQueryData({ nodePath: path, r: new Date().getTime() })
    this.downloadFile(`${this.BASE_URL}survey/${survey.id}/data/records/${recordId}/${record.step}/file?${queryData}`)
  }

  getRecordFileThumbnailUrl({ node }) {
    const { survey, record, path } = node
    const recordId = record.id || 0 // use 0 in survey preview (record in memory)
    const queryData = this._toQueryData({ nodePath: path, r: new Date().getTime() })
    return `${this.BASE_URL}survey/${survey.id}/data/records/${recordId}/${record.step}/file-thumbnail?${queryData}`
  }
}
