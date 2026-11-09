import './dropzone.css'

import React from 'react'
import PropTypes from 'prop-types'
import { useDropzone } from 'react-dropzone'
import classNames from 'classnames'

import L from 'utils/Labels'

const Size = {
  SMALL: 'small',
  MEDIUM: 'medium',
  LARGE: 'large',
}

const Dropzone = (props) => {
  const {
    className: classNameProps = null,
    handleFileDrop,
    acceptedFileTypes,
    acceptedFileTypesDescription,
    selectedFilePreview = null,
    size = Size.LARGE,
    width = '100%',
    height = '200px',
  } = props

  const handleFilesDrop = (acceptedFiles) => {
    //only single file upload supported
    if (acceptedFiles !== null && acceptedFiles.length === 1) {
      const file = acceptedFiles[0]
      // react-dropzone's accept prop rejects extensions with dashes (e.g. .collect-backup): check extensions here
      const fileName = file.name.toLowerCase()
      if (acceptedFileTypes.split(',').some((ext) => fileName.endsWith(ext.trim().toLowerCase()))) {
        handleFileDrop(file)
      }
    }
  }

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop: handleFilesDrop,
  })

  const className = classNames('dropzone', classNameProps, { active: isDragActive })

  return (
    <div {...getRootProps({ className, style: { width, height } })}>
      <input {...getInputProps({ accept: acceptedFileTypes })} />
      {selectedFilePreview ? (
        <p className="fileDroppedMessage">
          {size === Size.LARGE && <span className="checked large" />}
          {selectedFilePreview}
        </p>
      ) : (
        <div className="fileSelectContainer">
          {size === Size.SMALL ? (
            <span>{L.l('forms.dropzone.selectFileCompact')}</span>
          ) : (
            <p>{L.l('forms.dropzone.selectFile')}</p>
          )}
          {size === Size.LARGE && (
            <>
              <br />
              <br />
            </>
          )}
          {size !== Size.SMALL && <p>{L.l('forms.dropzone.supportedFileTypes', [acceptedFileTypesDescription])}</p>}
        </div>
      )}
    </div>
  )
}

Dropzone.propTypes = {
  className: PropTypes.string,
  handleFileDrop: PropTypes.func.isRequired,
  acceptedFileTypes: PropTypes.string.isRequired,
  acceptedFileTypesDescription: PropTypes.string.isRequired,
  selectedFilePreview: PropTypes.string,
  size: PropTypes.oneOf([Size.SMALL, Size.MEDIUM, Size.LARGE]),
  width: PropTypes.string,
  height: PropTypes.string,
}

export default Dropzone
