import React, { Component } from 'react';
import { Button, Modal, ModalHeader, ModalBody, ModalFooter } from 'reactstrap';

class ConfirmModal extends Component {
    
    constructor(props) {
        super(props)

        this.state = {
            open: false,
            okButtonLabel: 'Confirm',
            cancelButtonLabel: 'Cancel'
        }

        this.handleOk = this.handleOk.bind(this)
        this.handleCancel = this.handleCancel.bind(this)
    }

    open(title, message, handleOk, handleCancel, config) {
        this.setState({
            open: true,
            title: title,
            message: message,
            handleOk: handleOk,
            handleCancel: handleCancel
        })
    }

    handleOk() {
        if (this.state.handleOk) {
            this.state.handleOk()
        }
        this.setState({
            open: false
        })
    }

    handleCancel() {
        if (this.state.handleCancel) {
            this.state.handleCancel()
        }
        this.setState({
            open: false
        })
    }

    render() {
        return (
            <Modal isOpen={this.state.open} backdrop="static">
                <ModalHeader>{this.state.title}</ModalHeader>
                <ModalBody>{this.state.message}</ModalBody>
                <ModalFooter>
                    <Button color="primary" onClick={this.handleOk}>{this.state.okButtonLabel}</Button>{' '}
                    <Button color="secondary" onClick={this.handleCancel}>{this.state.cancelButtonLabel}</Button>
                </ModalFooter>
            </Modal>
        )
    }

}

export default class Modals {

    static confirmModal = new ConfirmModal()
    
    static confirm(title, message, handleOk, handleCancel) {
        Modals.confirmModal.open(title, message, handleOk, handleCancel)
    }

}


