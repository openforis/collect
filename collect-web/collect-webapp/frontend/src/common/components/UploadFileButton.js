import React from 'react';

import Button from '@material-ui/core/Button'

import { LinearProgress } from '@material-ui/core';

import L from 'utils/Labels';

const bytesToMegaBytes = value => Math.floor(value * 100 / 1024 / 1024) / 100

const UploadFileButton = props => {
    const {
        uploading = false,
        disabled = false,
        label = L.l('global.upload'),
        percent,
        totalBytes,
        loadedBytes,
        onClick,
        onCancel
    } = props

    const roundPercent = Math.floor(percent)
    const loadedMB = bytesToMegaBytes(loadedBytes)
    const totalMB = bytesToMegaBytes(totalBytes)
    return (
        uploading ? (
            <div className="uploading-file_wrapper">
                <div className="uploading-file_message">{L.l('global.uploadingFile')}</div>
                <div>{`${loadedMB}/${totalMB} MB (${roundPercent}%)`}</div>
                <LinearProgress variant="determinate"
                                value={roundPercent} />
                <Button onClick={onCancel}>{L.l('general.cancel')}</Button>
            </div>
        )
            : (
                <Button disabled={disabled}
                        onClick={onClick}
                        color="primary"
                        variant="raised">{label}</Button>
            )
    )
}

export default UploadFileButton