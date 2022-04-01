import React, { Component } from 'react'
import { render, unmountComponentAtNode, findDOMNode } from 'react-dom'

import Button from '@material-ui/core/Button'
import DialogContent from '@material-ui/core/DialogContent'
import DialogContentText from '@material-ui/core/DialogContentText'
import DialogTitle from '@material-ui/core/DialogTitle'
import DialogActions from '@material-ui/core/DialogActions'
import LinearProgress from '@material-ui/core/LinearProgress'

import L from 'utils/Labels'
import { Dialog } from 'common/components'

const CONFIRM_TARGET_DIV_ID = 'ofc-confirm-dialog'
const ALERT_TARGET_DIV_ID = 'ofc-alert-dialog'
const LOADING_TARGET_DIV_ID = 'ofc-loading-dialog'

class BaseDialog extends Component {
  okButton = null
  cancelButton = null

  constructor(props) {
    super(props)

    const { targetDivId } = props

    this.targetDivId = targetDivId

    this.handleEntered = this.handleEntered.bind(this)
    this.handleOk = this.handleOk.bind(this)
    this.handleCancel = this.handleCancel.bind(this)
    this.close = this.close.bind(this)
  }

  handleEntered() {
    const focusBtn = this.okButton ? this.okButton : this.cancelButton
    if (focusBtn) findDOMNode(focusBtn).focus()
  }

  handleOk() {
    const { onOk } = this.props

    if (onOk) onOk()

    this.close()
  }

  handleCancel() {
    const { onCancel } = this.props

    if (onCancel) onCancel()

    this.close()
  }

  close() {
    Dialogs._removeTargetDiv(this.targetDivId)
  }
}

class ConfirmDialog extends BaseDialog {
  render() {
    const { title, message, configuration } = this.props

    const { confirmButtonLabel, cancelButtonLabel } = configuration

    return (
      <Dialog
        open
        TransitionProps={{
          onEntered: () => {
            this.handleEntered()
          },
        }}
        onBackdropClick={this.handleCancel}
        onEscapeKeyDown={this.handleCancel}
      >
        <DialogTitle>{title}</DialogTitle>
        <DialogContent style={{ width: '400px' }}>
          <DialogContentText>{message}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button variant="contained" color="primary" onClick={this.handleOk}>
            {confirmButtonLabel}
          </Button>{' '}
          <Button onClick={this.handleCancel} ref={(n) => (this.cancelButton = n)}>
            {cancelButtonLabel}
          </Button>
        </DialogActions>
      </Dialog>
    )
  }
}

class AlertDialog extends BaseDialog {
  render() {
    const { title, message } = this.props

    return (
      <Dialog
        open
        TransitionProps={{
          onEntered: () => {
            this.handleEntered()
          },
        }}
        onBackdropClick={this.handleCancel}
        onEscapeKeyDown={this.handleCancel}
      >
        <DialogTitle>{title}</DialogTitle>
        <DialogContent style={{ width: '400px' }}>
          <DialogContentText>{message}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button variant="contained" color="primary" ref={(n) => (this.okButton = n)} onClick={this.handleOk}>
            {L.l('global.ok')}
          </Button>
        </DialogActions>
      </Dialog>
    )
  }
}

class LoadingDialog extends BaseDialog {
  render() {
    const { title, allowCancel } = this.props
    return (
      <Dialog
        open
        TransitionProps={{
          onEntered: () => {
            this.handleEntered()
          },
        }}
      >
        <DialogTitle>{title}</DialogTitle>
        <DialogContent style={{ width: '400px' }}>
          <LinearProgress />
        </DialogContent>
        {allowCancel && (
          <DialogActions>
            <Button
              variant="contained"
              color="primary"
              ref={(n) => (this.cancelButton = n)}
              onClick={this.handleCancel}
            >
              {L.l('global.cancel')}
            </Button>
          </DialogActions>
        )}
      </Dialog>
    )
  }
}

class ConfirmDialogConfiguration {
  confirmButtonLabel = L.l('global.confirm')
  cancelButtonLabel = L.l('global.cancel')
}

export default class Dialogs {
  static confirm(title, message, onOk, onCancel, configuration) {
    render(
      <ConfirmDialog
        targetDivId={CONFIRM_TARGET_DIV_ID}
        title={title}
        message={message}
        onOk={onOk}
        onCancel={onCancel}
        configuration={Object.assign({}, new ConfirmDialogConfiguration(), configuration)}
      />,
      Dialogs._createTargetDiv(CONFIRM_TARGET_DIV_ID)
    )
  }

  static alert(title, message, onOk = null) {
    render(
      <AlertDialog targetDivId={ALERT_TARGET_DIV_ID} title={title} message={message} onOk={onOk} />,
      Dialogs._createTargetDiv(ALERT_TARGET_DIV_ID)
    )
  }

  static showLoadingDialog(allowCancel = false, onCancel = null, title = L.l('global.loading')) {
    return render(
      <LoadingDialog targetDivId={LOADING_TARGET_DIV_ID} title={title} allowCancel={allowCancel} onCancel={onCancel} />,
      Dialogs._createTargetDiv(LOADING_TARGET_DIV_ID)
    )
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
