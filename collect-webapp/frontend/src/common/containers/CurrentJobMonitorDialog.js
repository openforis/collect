import React, { Component } from 'react';
import { connect } from 'react-redux';

import JobMonitorDialog from 'common/components/JobMonitorDialog'
import * as JobActions from 'actions/job'

class CurrentJobMonitorDialog extends Component {

    timer = null

    constructor(props) {
        super(props)

        this.handleTimeout = this.handleTimeout.bind(this)
        this.handleCancelButtonClick = this.handleCancelButtonClick.bind(this)
        this.handleOkButtonClick = this.handleOkButtonClick.bind(this)
        this.handleCloseButtonClick = this.handleCloseButtonClick.bind(this)

        if (props.open && props.jobMonitorConfiguration.jobId) {
            this.startTimer()
         } 
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.open && nextProps.jobMonitorConfiguration.jobId && 
                (this.props.jobMonitorConfiguration === null || 
                    nextProps.jobMonitorConfiguration.jobId !== this.props.jobMonitorConfiguration.jobId)) {
            this.startTimer()
        } else if (nextProps.job && nextProps.job.ended) {
            this.stopTimer()
        }
    }

    startTimer() {
        this.timer = setInterval(this.handleTimeout, 2000);
    }

    stopTimer() {
        clearInterval(this.timer)
    }

    handleTimeout() {
        this.loadJob()
    }

    handleOkButtonClick() {
        if (this.props.jobMonitorConfiguration.handleOkButtonClick) {
            this.props.jobMonitorConfiguration.handleOkButtonClick(this.props.job)
        }
        this.dispatchCloseJobMonitorAction()
    }
    
    handleCancelButtonClick() {
        if (this.props.jobMonitorConfiguration.handleCancelButtonClick) {
            this.props.jobMonitorConfiguration.handleCancelButtonClick(this.props.job)
        } else {
            this.props.dispatch(JobActions.cancelJob(this.props.jobMonitorConfiguration.jobId))
        }
    }

    handleCloseButtonClick() {
        this.dispatchCloseJobMonitorAction()
    }

    dispatchCloseJobMonitorAction() {
        this.props.dispatch(JobActions.closeJobMonitor())
    }

    loadJob() {
        if (this.props.jobMonitorConfiguration) {
            const jobId = this.props.jobMonitorConfiguration.jobId
            this.props.dispatch(JobActions.fetchJob(jobId))
        } else {
            this.stopTimer()
        }
    }

    render() {
        const { open, jobMonitorConfiguration, job, cancellingJob } = this.props
        
        if (!open){
            return <div></div>
        }
        return (
            <JobMonitorDialog
                open={open}
                title={jobMonitorConfiguration.title}
                jobId={jobMonitorConfiguration.jobId}
                job={job}
                cancellingJob={cancellingJob}
                okButtonLabel={jobMonitorConfiguration.okButtonLabel}
                handleJobCompleted={jobMonitorConfiguration.handleJobCompleted}
                handleOkButtonClick={this.handleOkButtonClick}
                handleCancelButtonClick={this.handleCancelButtonClick}
                handleCloseButtonClick={this.handleCloseButtonClick}
            />
        )
    }
}

const mapStateToProps = state => {
    const {
        open,
        job,
        jobMonitorConfiguration,
        cancellingJob
    } = state.currentJob || {
        open: false,
        jobMonitorConfiguration: null,
        job: null,
        cancellingJob: false
    }
    return {
        open,
        jobMonitorConfiguration,
        job,
        cancellingJob
    }
}

export default connect(mapStateToProps)(CurrentJobMonitorDialog)