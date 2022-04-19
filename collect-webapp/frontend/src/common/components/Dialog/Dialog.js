import React from 'react'
import MuiDialog from '@mui/material/Dialog'

export const Dialog = (props) => {
  const {
    children,
    disableBackdropClick,
    disableEscapeKeyDown,
    onClose: onCloseProp,
    onBackdropClick,
    onEscapeKeyDown,
    ...otherProps
  } = props

  const onClose = (event, reason) => {
    switch (reason) {
      case 'backdropClick':
        if (disableBackdropClick) return false
        onBackdropClick?.()
        break
      case 'escapeKeyDown':
        if (disableEscapeKeyDown) return false
        onEscapeKeyDown?.()
        break
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
