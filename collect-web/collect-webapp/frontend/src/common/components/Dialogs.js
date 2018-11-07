import React, { Component } from 'react';
import { render, unmountComponentAtNode, findDOMNode } from 'react-dom';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'
import DialogActions from '@material-ui/core/DialogActions'
import L from 'utils/Labels';

const CONFIRM_TARGET_DIV_ID = 'ofc-confirm-dialog'
const ALERT_TARGET_DIV_ID = 'ofc-alert-dialog'

class ConfirmDialog extends Component {

    cancelButton = null
    
    constructor(props) {
        super(props)

        this.handleEntering = this.handleEntering.bind(this)
        this.handleConfirm = this.handleConfirm.bind(this)
        this.handleCancel = this.handleCancel.bind(this)
        this.close = this.close.bind(this)
    }

    handleEntering() {
        findDOMNode(this.cancelButton).focus()
    }

    handleConfirm() {
        if (this.props.onConfirm) {
            this.props.onConfirm()
        }
        this.close()
    }

    handleCancel() {
        if (this.props.onCancel) {
            this.props.onCancel()
        }
        this.close()
    }

    close() {
        Dialogs._removeTargetDiv(CONFIRM_TARGET_DIV_ID)
    }

    render() {
        const { title, message, configuration } = this.props
        const { confirmButtonLabel, cancelButtonLabel } = configuration
        
        return (
            <Dialog open={true}
                    onEntering={this.handleEntering}
                    onBackdropClick={this.handleCancel}
                    onEscapeKeyDown={this.handleCancel}>
                <DialogTitle>{title}</DialogTitle>
                <DialogContent style={{width: '400px'}}>
                    <DialogContentText>{message}</DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button variant="raised" color="primary" onClick={this.handleConfirm}>{confirmButtonLabel}</Button>
                    {' '}
                    <Button onClick={this.handleCancel} ref={n => this.cancelButton = n}>{cancelButtonLabel}</Button>
                </DialogActions>
            </Dialog>
        )
    }

}

class AlertDialog extends Component {

    okButton = null

    constructor(props) {
        super(props)

        this.handleEntering = this.handleEntering.bind(this)
        this.handleOk = this.handleOk.bind(this)
        this.handleCancel = this.handleCancel.bind(this)
        this.close = this.close.bind(this)
    }

    handleEntering() {
        findDOMNode(this.okButton).focus()
    }

    handleOk() {
        if (this.props.onOk) {
            this.props.onOk()
        }
        this.close()
    }

    handleCancel() {
        if (this.props.onCancel) {
            this.props.onCancel()
        }
        this.close()
    }

    close() {
        Dialogs._removeTargetDiv(ALERT_TARGET_DIV_ID)
    }

    render() {
        const { title, message } = this.props
        const okButtonLabel = L.l('global.ok')
        return (
            <Dialog open={true}
                    onEntering={this.handleEntering}
                    onBackdropClick={this.handleCancel}
                    onEscapeKeyDown={this.handleCancel}>
                <DialogTitle>{title}</DialogTitle>
                <DialogContent style={{width: '400px'}}>
                    <DialogContentText>{message}</DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button variant="raised" color="primary" ref={n => this.okButton = n} onClick={this.handleOk}>{okButtonLabel}</Button>
                </DialogActions>
            </Dialog>
        )
    }

}

class ConfirmDialogConfiguration {

    confirmButtonLabel = L.l('global.confirm')
    cancelButtonLabel = L.l('global.cancel')
}



export default class Dialogs {

    static confirm(title, message, onConfirm, onCancel, configuration) {
        const targetDiv = Dialogs._createTargetDiv(CONFIRM_TARGET_DIV_ID)
        render(<ConfirmDialog title={title} message={message} onConfirm={onConfirm} onCancel={onCancel} 
            configuration={Object.assign({}, new ConfirmDialogConfiguration(), configuration)} />, targetDiv)
    }

    static alert(title, message, onOk= null) {
        const targetDiv = Dialogs._createTargetDiv(ALERT_TARGET_DIV_ID)
        render(<AlertDialog title={title} message={message} onOk={onOk} />, targetDiv)
    }

    static _createTargetDiv(targetDivId) {
        const targetDiv = document.createElement('div')
        targetDiv.id = targetDivId
        document.body.appendChild(targetDiv)
        return targetDiv
    }

    static _removeTargetDiv(targetDivId) {
        const target = document.getElementById(targetDivId)
        unmountComponentAtNode(target)
        target.parentNode.removeChild(target)
    }
}


