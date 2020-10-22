import './dropzone.css'

import React from 'react'
import { useDropzone } from 'react-dropzone'
import classNames from 'classnames'

import L from 'utils/Labels'

const Dropzone = (props) => {
  const {
    className: classNameProps,
    compact,
    handleFileDrop,
    acceptedFileTypes,
    acceptedFileTypesDescription,
    selectedFilePreview,
    width = '100%',
    height = '200px',
  } = props

  const handleFilesDrop = (acceptedFiles) => {
    //only single file upload supported
    if (acceptedFiles !== null && acceptedFiles.length === 1) {
      const file = acceptedFiles[0]
      handleFileDrop(file)
    }
  }

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    accept: acceptedFileTypes,
    onDrop: handleFilesDrop,
  })

  const className = classNames('dropzone', classNameProps, { active: isDragActive })

  return (
    <div {...getRootProps({ className, style: { width, height } })}>
      <input {...getInputProps()} />
      {selectedFilePreview ? (
        <p className="fileDroppedMessage">
          {!compact && <span className="checked large" />}
          {selectedFilePreview}
        </p>
      ) : (
        <div className="fileSelectContainer">
          <p>{L.l('forms.dropzone.selectFile')}</p>
          {!compact && (
            <>
              <br />
              <br />
            </>
          )}
          <p>{L.l('forms.dropzone.supportedFileTypes', [acceptedFileTypesDescription])}</p>
        </div>
      )}
    </div>
  )
}

Dropzone.defaultProps = {
  className: null,
  compact: false,
}

export default Dropzone
