import React, { Component } from 'react';
import Button from 'material-ui/Button';
import Dialog, {
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from 'material-ui/Dialog';

export default class ConfirmDialog extends Component {

    render() {
        const { open, title, message, okOk, onCancel } = this.props
        
        return (
            <Dialog open={open} 
                    ignoreBackdropClick
                    ignoreEscapeKeyUp>
                <DialogTitle>{title}</DialogTitle>
                <DialogContent style={{width: '400px'}}>
                    <DialogContentText>{message}</DialogContentText>
                </DialogContent>
                <DialogActions>
                    <Button raised color="primary" onClick={okOk}>Confirm</Button>
                    {' '}
                    <Button onClick={onCancel}>Cancel</Button>
                    {' '}
                </DialogActions>
            </Dialog>
        )
    }
}
    