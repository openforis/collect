import './FileField.css'

import React from 'react'
import { Button, Progress } from 'reactstrap'

import L from 'utils/Labels'
import ServiceFactory from 'services/ServiceFactory'

import { FileAttributeDefinition } from 'model/Survey'

import DeleteIconButton from 'common/components/DeleteIconButton'
import Dropzone from 'common/components/Dropzone'
import Image from 'common/components/Image'

import AbstractField from './AbstractField'

const EXTENSIONS_BY_FILE_TYPE = {
  [FileAttributeDefinition.FileTypes.AUDIO]: '.mp3,.wav,.3gp',
  [FileAttributeDefinition.FileTypes.IMAGE]: '.jpg,.jpeg,.png,.btm',
  [FileAttributeDefinition.FileTypes.VIDEO]: '.avi,.mkv',
  [FileAttributeDefinition.FileTypes.DOCUMENT]: '.doc,.docx,.xls,.xlsx,.pdf,.odt,.xml',
}

const FileThumbnail = (props) => {
  const { node, onClick } = props
  if (!node) {
    return null
  }
  const { filename = null } = node.value || {}
  const { definition, record } = node
  const { fileType } = definition
  const { id: recordId = 0 } = record
  const { survey } = record

  const thumbnailUrl =
    fileType === FileAttributeDefinition.FileTypes.IMAGE && filename
      ? `${ServiceFactory.recordFileService.BASE_URL}survey/${survey.id}/data/records/${recordId}/${record.step}/node/${node.id}/thumbnail`
      : null

  return thumbnailUrl ? (
    <Image src={thumbnailUrl} maxWidth={150} maxHeight={150} onClick={onClick} />
  ) : (
    <Button type="button" onClick={onClick}>
      Download
    </Button>
  )
}

export default class FileField extends AbstractField {
  constructor() {
    super()

    this.state = {
      ...this.state,
      uploading: false,
      selectedFileName: null,
    }

    this.onFileSelect = this.onFileSelect.bind(this)
    this.onDownloadClick = this.onDownloadClick.bind(this)
    this.onDeleteClick = this.onDeleteClick.bind(this)
  }

  onFileSelect(file) {
    this.setState({ uploading: true, selectedFileName: file.name })
    const attribute = this.getAttribute()
    ServiceFactory.commandService.updateAttributeFile({ attribute, file }).then(() => {
      this.setState({ uploading: false })
    })
  }

  onDownloadClick() {
    const fileAttribute = this.getAttribute()
    ServiceFactory.recordFileService.downloadRecordFile({ fileAttribute })
  }

  onDeleteClick() {
    const fileAttribute = this.getAttribute()
    ServiceFactory.commandService.deleteAttributeFile({ fileAttribute })
  }

  render() {
    const { fieldDef } = this.props
    const { value: valueState = {}, uploading } = this.state
    const { filename = null } = valueState || {}
    const { attributeDefinition } = fieldDef
    const { fileType } = attributeDefinition

    const node = this.getAttribute()
    const extensions = EXTENSIONS_BY_FILE_TYPE[fileType]

    return (
      <div>
        {filename && (
          <div>
            <FileThumbnail node={node} onClick={this.onDownloadClick} />
            <DeleteIconButton onClick={this.onDeleteClick} />
          </div>
        )}
        {!uploading && !filename && (
          <Dropzone
            className="file-field-dropzone"
            compact
            acceptedFileTypes={extensions}
            acceptedFileTypesDescription={L.l('dataManagement.dataEntry.attribute.file.acceptedFileDescription', [
              fileType,
              extensions,
            ])}
            handleFileDrop={(file) => this.onFileSelect(file)}
            height="60px"
          />
        )}
        {uploading && <Progress animated value={100} />}
      </div>
    )
  }
}
