import React, { Component } from 'react'
import PropTypes from 'prop-types'
import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'
import LinearProgress from '@material-ui/core/LinearProgress'
import L from 'utils/Labels'

import Objects from 'utils/Objects'

export default class JobMonitorDialog extends Component {

    static propTypes = {
        jobId: PropTypes.string,
        title: PropTypes.string.isRequired,
        okButtonLabel: PropTypes.string,
        handleOkButtonClick: PropTypes.func,
        handleCancelButtonClick: PropTypes.func,
        handleCloseButtonClick: PropTypes.func
	}

    render() {
        const { open, title, job, cancellingJob, handleCancelButtonClick, handleCloseButtonClick, handleOkButtonClick } = this.props
        const loading = Objects.isNullOrUndefined(job)
        const okButtonLabel = this.props.okButtonLabel ? this.props.okButtonLabel : L.l('general.ok')

        return (
            <Dialog open={open} 
                    disableBackdropClick
                    disableEscapeKeyDown>
                <DialogTitle>{title}</DialogTitle>
                <DialogContent style={{width: '400px'}}>
                    {loading ? <LinearProgress />
                        : job.running ? <LinearProgress variant="determinate" value={job.progressPercent} />
                            : job.status
                    }
                    {! loading && job.running && job.remainingMinutes &&
                        <div className="progress-bar-remaining-time">
                            <label>{L.l('job.remainingMinutes')}:&nbsp;</label>
                            <label>{job.remainingMinutes > 1 ? job.remainingMinutes : L.l('job.lessThanOneMinuteRemaining')}</label>
                        </div>
                    }
                    {! loading && job.failed &&
                        <DialogContentText>{job.errorMessage}</DialogContentText>
                    }
                </DialogContent>
                <DialogActions>
                    {job && job.running &&
                        <Button disabled={cancellingJob} onClick={handleCancelButtonClick}>{L.l('general.cancel')}</Button>}
                    {' '}
                    {job && job.completed && 
                        <Button variant="raised" color="primary" onClick={handleOkButtonClick}>{okButtonLabel}</Button>}
                    {' '}
                    {job && job.failed &&
                        <Button onClick={handleCloseButtonClick}>{L.l('general.close')}</Button>}
                </DialogActions>
            </Dialog>
        )
    }

}