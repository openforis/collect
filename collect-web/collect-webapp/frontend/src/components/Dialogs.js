import React, { Component } from 'react';
import { render, unmountComponentAtNode, findDOMNode } from 'react-dom';
import Button from 'material-ui/Button';
import Dialog, {
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from 'material-ui/Dialog';

const TARGET_DIV_ID = 'ofc-confirm-alert'

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
        const target = document.getElementById(TARGET_DIV_ID);
        unmountComponentAtNode(target);
        target.parentNode.removeChild(target);
    }

    render() {
        const { title, message, configuration } = this.props
        const { confirmButtonLabel, cancelButtonLabel } = configuration

        return (
            <Dialog open={true}
                    ignoreBackdropClick
                    onEntering={this.handleEntering}
                    onEscapeKeyUp={this.handleCancel}>
                <DialogTitle>{title}</DialogTitle>
                <DialogContent style={{width: '400px'}}>
                    <DialogContentText>{message}</DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button raised color="primary" onClick={this.handleConfirm}>{confirmButtonLabel}</Button>
                    {' '}
                    <Button onClick={this.handleCancel} ref={n => this.cancelButton = n}>{cancelButtonLabel}</Button>
                    {' '}
                </DialogActions>
            </Dialog>
        )
    }

}

class ConfirmDialogConfiguration {

    confirmButtonLabel = 'Confirm'
    cancelButtonLabel = 'Cancel'
}

export default class Dialogs {

    static confirm(title, message, onConfirm, onCancel, configuration) {
        const targetDiv = document.createElement('div');
        targetDiv.id = TARGET_DIV_ID;
        document.body.appendChild(targetDiv);
        render(<ConfirmDialog title={title} message={message} onConfirm={onConfirm} onCancel={onCancel} 
            configuration={Object.assign({}, new ConfirmDialogConfiguration(), configuration)} />, targetDiv)
    }

}


