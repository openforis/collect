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
    className: classNameProps,
    handleFileDrop,
    acceptedFileTypes,
    acceptedFileTypesDescription,
    selectedFilePreview,
    size,
    width,
    height,
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

Dropzone.defaultProps = {
  className: null,
  selectedFilePreview: null,
  size: 'large',
  width: '100%',
  height: '200px',
}

export default Dropzone
