import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { Modal, ModalHeader, ModalBody, ModalFooter, Button, Progress } from 'reactstrap';

export default class JobMonitorModal extends Component {

    static propTypes = {
        jobId: PropTypes.string,
        title: PropTypes.string.isRequired,
        okButtonLabel: PropTypes.string,
        handleOkButtonClick: PropTypes.func,
        handleCancelButtonClick: PropTypes.func,
        handleCloseButtonClick: PropTypes.func
	}

    render() {
        const loading = this.props.job == null
        const okButtonLabel = this.props.okButtonLabel ? this.props.okButtonLabel : 'Ok'
        return (
            <Modal isOpen={this.props.open} backdrop="static">
                <ModalHeader>{this.props.title}</ModalHeader>
                <ModalBody>
                    {loading ? 'Loading...'
                        : this.props.job.running ? 
                            <Progress value={this.state.job.progressPercent} />
                            : this.props.job.status
                    } 
                    {! loading && this.props.job.status === 'FAILED' &&
                        <p>{this.props.job.errorMessage}</p>
                    }
                </ModalBody>
                <ModalFooter>
                    {this.props.job && this.props.job.running &&
                        <Button color="secondary" onClick={this.props.handleCancelButtonClick}>Cancel</Button>}
                    {' '}
                    {this.props.job && this.props.job.completed && 
                        <Button color="primary" onClick={this.props.handleOkButtonClick}>{okButtonLabel}</Button>}
                    {' '}
                    {this.props.job && this.props.job.ended &&
                        <Button color="secondary" onClick={this.props.handleCloseButtonClick}>Close</Button>}
                </ModalFooter>
            </Modal>
        )
    }

}