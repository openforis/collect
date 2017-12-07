import React, { Component } from 'react';
import ReactDropzone from 'react-dropzone';
import L from 'utils/Labels';

export default class Dropzone extends Component {

    render() {
        const { accteptedFileTypes, acceptedFileTypesDescription, handleFilesDrop, fileToBeImportedPreview, width='100%', height='200px'} = this.props

        return <ReactDropzone accept={accteptedFileTypes} onDrop={handleFilesDrop} style={{
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