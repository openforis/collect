import React, { Component } from 'react';
import { connect } from 'react-redux';

import JobMonitorModal from 'components/JobMonitorModal'
import * as Actions from 'actions'

class CurrentJobMonitorModal extends Component {

    timer = null

    constructor(props) {
        super(props)

        this.handleTimeout = this.handleTimeout.bind(this)
        this.handleCancelButtonClick = this.handleCancelButtonClick.bind(this)
        this.handleOkButtonClick = this.handleOkButtonClick.bind(this)
        
        if (props.open && props.jobId) {
            this.startTimer()
         } 
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.jobId && nextProps.open && nextProps.jobId != this.props.jobId) {
            this.startTimer()
        } else if (nextProps.job) {
            const job = nextProps.job
            if (job.ended) {
                clearInterval(this.timer)
            }
        }
    }

    startTimer() {
        this.timer = setInterval(this.handleTimeout, 2000);
    }

    handleTimeout() {
        this.loadJob()
    }

    handleOkButtonClick() {
        if (this.props.handleOkButtonClick) {
            this.props.handleOkButtonClick()
        }
    }
    
    handleCancelButtonClick() {
        if (this.props.handleCancelButtonClick) {
            this.props.handleCancelButtonClick()
        } else {
            this.props.dispatch(Actions.cancelJob(this.props.jobId))
        }
    }

    loadJob() {
        const jobId = this.props.jobId
        this.props.dispatch(Actions.fetchJob(jobId))
    }

    render() {
        return (
        <JobMonitorModal
            open={this.props.open}
            title={this.props.title}
            jobId={this.props.jobId}
            job={this.props.job}
            okButtonLabel={this.props.okButtonLabel ? this.props.okButtonLabel: 'Ok'}
            handleOkButtonClick={this.handleOkButtonClick}
            handleCancelButtonClick={this.handleCancelButtonClick}
            handleJobCompleted={this.props.handleJobCompleted}
        />
        )
    }
}

const mapStateToProps = state => {
    const {
        open,
        jobId,
        job,
        title,
        okButtonLabel,
        handleOkButtonClick,
        handleCancelButtonClick,
        handleJobCompleted
    } = state.currentJob || {
        open: false,
        jobId: null,
        job: null
    }
    return {
        open,
        jobId,
        job,
        title,
        okButtonLabel,
        handleOkButtonClick,
        handleCancelButtonClick,
        handleJobCompleted
    }
}

export default connect(mapStateToProps)(CurrentJobMonitorModal)