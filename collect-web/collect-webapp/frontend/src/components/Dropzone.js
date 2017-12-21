import React, { Component } from 'react';
import ReactDropzone from 'react-dropzone';
import L from 'utils/Labels';
import Arrays from 'utils/Arrays';

export default class Dropzone extends Component {

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
        const { acceptedFileTypes, acceptedFileTypesDescription, handleFilesDrop, fileToBeImportedPreview, width='100%', height='200px'} = this.props

        return <ReactDropzone accept={acceptedFileTypes} onDrop={this.handleFilesDrop} style={{
                width: width, height: height, 
                borderWidth: '2px', borderColor: 'rgb(102, 102, 102)', 
                borderStyle: 'dashed', borderRadius: '5px'
            }}>
            {fileToBeImportedPreview ?
                <p style={{fontSize: '2em', textAlign: 'center'}}>
                    <span className="checked large" />{fileToBeImportedPreview}
                </p>
                : <p>{L.l('forms.fileDropMessage', [acceptedFileTypesDescription])}</p>
            }
        </ReactDropzone>
    }
}