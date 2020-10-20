import './FileField.css'

import React from 'react'
import { Button, Progress } from 'reactstrap'

import L from 'utils/Labels'
import ServiceFactory from 'services/ServiceFactory'

import AbstractField from './AbstractField'
import Dropzone from 'common/components/Dropzone'
import { FileAttributeDefinition } from '../../../../model/Survey'

const EXTENSIONS_BY_FILE_TYPE = {
  [FileAttributeDefinition.FileTypes.AUDIO]: '.mp3,.wav,.3gp',
  [FileAttributeDefinition.FileTypes.IMAGE]: '.jpg,.jpeg,.png,.btm',
  [FileAttributeDefinition.FileTypes.VIDEO]: '.avi,.mkv',
  [FileAttributeDefinition.FileTypes.DOCUMENT]: '.doc,.docx,.xls,.xlsx,.pdf,.odt,.xml',
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
  }

  onFileSelect(file) {
    this.setState({ uploading: true, selectedFileName: file.name })
    const attr = this.getAttribute()
    ServiceFactory.commandService.updateAttributeFile(attr, file).then(() => {
      this.setState({ uploading: false })
    })
  }

  onDownloadClick() {}

  render() {
    const { fieldDef } = this.props
    const { dirty, value: valueState = {}, selectedFileName, uploading } = this.state
    const { value = { fileName: null, fileSize: null } } = valueState || {}
    const { fileName, fileSize } = value
    const { attributeDefinition: attrDef } = fieldDef
    const { fileType } = attrDef

    const extensions = EXTENSIONS_BY_FILE_TYPE[fileType]
    return (
      <div>
        {fileName && (
          <Button type="button" onClick={this.onDownloadClick}>
            Download
          </Button>
        )}
        {!uploading && !fileName && (
          <Dropzone
            className="file-field-dropzone"
            compact
            acceptedFileTypes={extensions}
            acceptedFileTypesDescription={L.l('dataManagement.dataEntry.fileField.acceptedFileDescription', [
              fileType,
              extensions,
            ])}
            handleFileDrop={(file) => this.onFileSelect(file)}
            height="60px"
            selectedFilePreview={selectedFileName}
          />
        )}
        {uploading && <Progress animated value={100} />}
      </div>
    )
  }
}
