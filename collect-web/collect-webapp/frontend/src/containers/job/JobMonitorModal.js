import React, { Component } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter, Button, Progress } from 'reactstrap';
import ServiceFactory from 'services/ServiceFactory'

export default class JobMonitorModal extends Component {

    constructor(props) {
        super(props)

        this.handleJobReceived = this.handleJobReceived.bind(this)
        this.handleTimeout = this.handleTimeout.bind(this)
        
        this.state = {
            loading: true,
            open: props.open,
            job: null
        }

        this.timer = setInterval(this.handleTimeout, 2000);
    }

    handleTimeout() {
        this.loadJob()
    }

    loadJob() {
        const jobId = this.props.jobId
        ServiceFactory.jobService.fetchById(jobId).then(this.handleJobReceived)
    }

    handleJobReceived(job) {
        this.setState({
            loading: false,
            job: job 
        })
        if (job.ended) {
            clearInterval(this.timer)
        }
        if (job.status == 'COMPLETED') {
            if (this.props.handleJobCompleted) {
                this.props.handleJobCompleted(job)
            }
        }
    }

    render() {
        return (
            <Modal isOpen={this.state.open} >
                <ModalHeader  >Job status</ModalHeader>
                <ModalBody>
                    {this.state.loading ? 'Loading...'
                        : this.state.job.running ? 
                            <Progress value={this.state.job.progressPercent} />
                            : this.state.job.status 
                    }
                </ModalBody>
                <ModalFooter>
                    {this.state.loading || ! this.state.job.ended ? '' : <Button color="primary">Ok</Button>} {' '}
                    <Button color="secondary">Cancel</Button>
                </ModalFooter>
            </Modal>
        )
    }

}