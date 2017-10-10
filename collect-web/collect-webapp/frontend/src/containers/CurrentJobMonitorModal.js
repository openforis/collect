import React, { Component } from 'react';
import { connect } from 'react-redux';

import JobMonitorModal from 'components/JobMonitorModal'

class CurrentJobMonitorModal extends Component {

    constructor(props) {
        super(props)
    }

    render() {
        return (
        <JobMonitorModal
            open={this.props.open}
            title={this.props.title}
            jobId={this.props.jobId}
            okButtonLabel={this.props.okButtonLabel ? this.props.okButtonLabel: 'Ok'}
            handleOkButtonClick={this.props.handleOkButtonClick}
            handleCancelButtonClick={this.props.handleCancelButtonClick}
            handleJobCompleted={this.props.handleJobCompleted}
        />
        )
    }

}

const mapStateToProps = state => {
    const {
        open,
        jobId,
        title,
        okButtonLabel,
        handleOkButtonClick,
        handleCancelButtonClick,
        handleJobCompleted
    } = state.currentJob || {
        open: false,
        jobId: null
    }
    return {
        open,
        jobId,
        title,
        okButtonLabel,
        handleOkButtonClick,
        handleCancelButtonClick,
        handleJobCompleted
    }
}

export default connect(mapStateToProps)(CurrentJobMonitorModal)