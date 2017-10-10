import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { Modal, ModalHeader, ModalBody, ModalFooter, Button, Progress } from 'reactstrap';
import ServiceFactory from 'services/ServiceFactory'

export default class JobMonitorModal extends Component {

    constructor(props) {
        super(props)

        this.handleJobReceived = this.handleJobReceived.bind(this)
        this.handleTimeout = this.handleTimeout.bind(this)
        this.handleOkButtonClick = this.handleOkButtonClick.bind(this)
        this.handleCancelButtonClick = this.handleCancelButtonClick.bind(this)

        this.state = {
            loading: true,
            open: props.open,
            job: null,
            jobId: props.jobId
        }

        if (props.open && props.jobId) {
           this.startTimer()
        }
    }

    static propTypes = {
        jobId: PropTypes.string,
        title: PropTypes.string.isRequired,
        okButtonLabel: PropTypes.string,
		handleJobCompleted: PropTypes.func,
        handleOkButtonClick: PropTypes.func,
        handleCancelButtonClick: PropTypes.func
	}

    componentWillReceiveProps(nextProps) {
        if (nextProps.jobId && nextProps.open && nextProps.jobId != this.props.jobId) {
            this.startTimer()
            this.setState({
                jobId: nextProps.jobId, 
                job: null, 
                open: true,
                loading: true
            })
        } else if (! nextProps.open) {
            this.setState({
                open: false
            })
        }
    }

    startTimer() {
        this.timer = setInterval(this.handleTimeout, 2000);
    }

    handleTimeout() {
        this.loadJob()
    }

    loadJob() {
        const jobId = this.state.jobId
        ServiceFactory.jobService.fetch(jobId).then(this.handleJobReceived)
    }

    handleJobReceived(job) {
        this.setState({
            loading: false,
            job: job 
        })
        if (job.ended) {
            clearInterval(this.timer)
        }
        switch (job.status) {
            case 'COMPLETED':
                if (this.props.handleJobCompleted) {
                    this.props.handleJobCompleted(job)
                }
                break
            case 'ABORTED':
                this.setState({
                    open: false
                })
                break
        }
    }

    handleOkButtonClick() {
        if (this.props.handleOkButtonClick) {
            this.props.handleOkButtonClick()
        }
    }

    handleCancelButtonClick() {
        const jobId = this.props.jobId
        
        ServiceFactory.jobService.cancel(jobId).then(() => {
            if (this.props.handleCancelButtonClick) {
                this.props.handleCancelButtonClick()
            } else {
                this.setState({
                    open: false
                })
            }
        })
    }

    render() {
        const okButtonLabel = this.props.okButtonLabel ? this.props.okButtonLabel : 'Ok'
        return (
            <Modal isOpen={this.state.open} backdrop="static">
                <ModalHeader>{this.props.title}</ModalHeader>
                <ModalBody>
                    {this.state.loading ? 'Loading...'
                        : this.state.job.running ? 
                            <Progress value={this.state.job.progressPercent} />
                            : this.state.job.status 
                    }
                </ModalBody>
                <ModalFooter>
                    {this.state.loading || ! this.state.job.ended ? '' : <Button color="primary"
                        onClick={this.handleOkButtonClick}>{okButtonLabel}</Button>} {' '}
                    <Button color="secondary" onClick={this.handleCancelButtonClick}>Cancel</Button>
                </ModalFooter>
            </Modal>
        )
    }

}