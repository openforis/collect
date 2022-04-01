import React from 'react'
import MuiDialog from '@material-ui/core/Dialog'

export const Dialog = (props) => {
  const { children, disableBackdropClick, disableEscapeKeyDown, onClose: onCloseProp, ...otherProps } = props

  const onClose = (event, reason) => {
    if ((disableBackdropClick && reason === 'backdropClick') || (disableEscapeKeyDown && reason === 'escapeKeyDown')) {
      return false
    }

    if (onCloseProp) {
      onCloseProp(event, reason)
    }
  }

  return (
    <MuiDialog onClose={onClose} {...otherProps}>
      {children}
    </MuiDialog>
  )
}
