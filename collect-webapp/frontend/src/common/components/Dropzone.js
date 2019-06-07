import './dropzone.css'

import React from 'react';
import ReactDropzone from 'react-dropzone';

import L from 'utils/Labels';

export default class Dropzone extends React.Component {

    constructor(props) {
        super(props)

        this.handleFilesDrop = this.handleFilesDrop.bind(this)
    }

    handleFilesDrop(acceptedFiles) {
        //only single file upload supported
        if (acceptedFiles !== null && acceptedFiles.length === 1) {
            const file = acceptedFiles[0]
            this.props.handleFileDrop(file)
        }
    }

    render() {
        const { acceptedFileTypes, acceptedFileTypesDescription, fileToBeImportedPreview, width = '100%', height = '200px' } = this.props

        return <ReactDropzone
            accept={acceptedFileTypes}
            onDrop={this.handleFilesDrop}
            style={{width, height}}>
            {({ getRootProps, getInputProps }) => (
                <div {...getRootProps({className: 'dropzone'})} style={{width, height}}>
                    <input {...getInputProps()} />
                    {fileToBeImportedPreview ?
                        <p className="fileDroppedMessage">
                            <span className="checked large" />{fileToBeImportedPreview}
                        </p>
                        : <div className="fileSelectContainer">
                            <p>{L.l('forms.dropzone.selectFile')}</p>
                            <br/>
                            <br/>
                            <p>{L.l('forms.dropzone.supportedFileTypes', [acceptedFileTypesDescription])}</p>
                        </div>
                    }
                </div>
            )}
        </ReactDropzone>
    }
}