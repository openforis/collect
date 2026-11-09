import './FileField.css'

import React from 'react'
import { Button, Progress } from 'reactstrap'

import L from 'utils/Labels'
import ServiceFactory from 'services/ServiceFactory'

import { FileAttributeDefinition } from 'model/Survey'

import DeleteIconButton from 'common/components/DeleteIconButton'
import Dropzone from 'common/components/Dropzone'
import Image from 'common/components/Image'
import * as FieldsSizes from './FieldsSizes'

import AbstractField from './AbstractField'

const EXTENSIONS_BY_FILE_TYPE = {
  [FileAttributeDefinition.FileTypes.AUDIO]: '.3gp,.mp3,.wav',
  [FileAttributeDefinition.FileTypes.IMAGE]: '.jpg,.jpeg,.png,.bmp',
  [FileAttributeDefinition.FileTypes.VIDEO]: '.avi,.mov,.mkv',
  [FileAttributeDefinition.FileTypes.DOCUMENT]: '.doc,.docx,.odt,.pdf,.xml,.xls,.xlsx,.zip',
}

const FileThumbnail = (props) => {
  const { inTable, node, onClick } = props
  if (!node) {
    return null
  }
  const { filename = null } = node.value || {}
  const { definition } = node
  const { fileType } = definition

  const thumbnailUrl =
    fileType === FileAttributeDefinition.FileTypes.IMAGE && filename
      ? ServiceFactory.recordFileService.getRecordFileThumbnailUrl({ node })
      : null

  const thumbnailSize = inTable ? 30 : 150

  return thumbnailUrl ? (
    <Image src={thumbnailUrl} maxWidth={thumbnailSize} maxHeight={thumbnailSize} onClick={onClick} />
  ) : (
    <Button type="button" onClick={onClick}>
      {L.l('common.download')}
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
    const { fieldDef, inTable, parentEntity } = this.props
    const { value: valueState = {}, uploading } = this.state
    const { record } = parentEntity
    const { filename = null } = valueState || {}
    const { attributeDefinition } = fieldDef
    const { fileType } = attributeDefinition
    const readOnly = record.readOnly

    const node = this.getAttribute()
    const extensions = EXTENSIONS_BY_FILE_TYPE[fileType]

    return (
      <div style={{ width: FieldsSizes.getWidth({ inTable, fieldDef }), textAlign: inTable ? 'center' : 'inherit' }}>
        {filename && (
          <>
            <FileThumbnail inTable={inTable} node={node} onClick={this.onDownloadClick} />
            {!readOnly && (
              <DeleteIconButton
                title={L.l('dataManagement.dataEntry.attribute.file.deleteFile')}
                onClick={this.onDeleteClick}
              />
            )}
          </>
        )}
        {!readOnly && !uploading && !filename && (
          <Dropzone
            className="file-field-dropzone"
            acceptedFileTypes={extensions}
            acceptedFileTypesDescription={L.l('dataManagement.dataEntry.attribute.file.acceptedFileDescription', [
              fileType,
              extensions,
            ])}
            handleFileDrop={(file) => this.onFileSelect(file)}
            height={inTable ? '30px' : 'auto'}
            size={inTable ? 'small' : 'medium'}
          />
        )}
        {uploading && <Progress animated value={100} />}
      </div>
    )
  }
}
