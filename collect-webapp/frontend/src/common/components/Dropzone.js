import './dropzone.css'

import React from 'react'
import { useDropzone } from 'react-dropzone'

import L from 'utils/Labels'

const Dropzone = props => {

    const { handleFileDrop, acceptedFileTypes, acceptedFileTypesDescription, fileToBeImportedPreview, width = '100%', height = '200px' } = props

    const handleFilesDrop = acceptedFiles => {
        //only single file upload supported
        if (acceptedFiles !== null && acceptedFiles.length === 1) {
            const file = acceptedFiles[0]
            handleFileDrop(file)
        }
    }

    const {
        getRootProps,
        getInputProps,
        isDragActive,
    } = useDropzone({
        accept: acceptedFileTypes,
        onDrop: handleFilesDrop
    })

    const className = `dropzone${isDragActive ? ' active' : ''}`

    return <div {...getRootProps({className, style: { width, height } })}>
        <input {...getInputProps()} />
        {fileToBeImportedPreview ?
            <p className="fileDroppedMessage">
                <span className="checked large" />{fileToBeImportedPreview}
            </p>
            : <div className="fileSelectContainer">
                <p>{L.l('forms.dropzone.selectFile')}</p>
                <br />
                <br />
                <p>{L.l('forms.dropzone.supportedFileTypes', [acceptedFileTypesDescription])}</p>
            </div>
        }
    </div>
}

export default Dropzone