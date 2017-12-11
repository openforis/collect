import React, { Component } from 'react';
import PropTypes from 'prop-types'
import Button from 'material-ui/Button';
import Dialog, {
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from 'material-ui/Dialog';
import { LinearProgress } from 'material-ui/Progress';

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
        const { open, title, job, handleCancelButtonClick, handleCloseButtonClick, handleOkButtonClick } = this.props
        const loading = Objects.isNullOrUndefined(job)
        const okButtonLabel = this.props.okButtonLabel ? this.props.okButtonLabel : 'Ok'
        return (
            <Dialog open={open} 
                    ignoreBackdropClick
                    ignoreEscapeKeyUp>
                <DialogTitle>{title}</DialogTitle>
                <DialogContent style={{width: '400px'}}>
                    {loading ? 
                        <LinearProgress />
                        : job.running ? 
                            <LinearProgress mode="determinate" value={job.progressPercent} />
                            : job.status
                    } 
                    {! loading && job.failed &&
                        <DialogContentText>>{job.errorMessage}</DialogContentText>
                    }
                </DialogContent>
                <DialogActions>
                    {job && job.running &&
                        <Button onClick={handleCancelButtonClick}>Cancel</Button>}
                    {' '}
                    {job && job.completed && 
                        <Button raised color="primary" onClick={handleOkButtonClick}>{okButtonLabel}</Button>}
                    {' '}
                    {job && job.failed &&
                        <Button onClick={handleCloseButtonClick}>Close</Button>}
                </DialogActions>
            </Dialog>
        )
    }

}