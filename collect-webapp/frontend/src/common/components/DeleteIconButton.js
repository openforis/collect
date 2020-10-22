import React from 'react'
import { Button } from 'reactstrap'

import Dialogs from 'common/components/Dialogs'
import L from 'utils/Labels'

const DeleteIconButton = (props) => {
  const { onClick: onClickParam, confirmTitle, confirmMessage } = props

  const onClick = () => {
    const confirmTitleFinal = confirmTitle || L.l('global.deleteConfirmTitle')
    const confirmMessageFinal = confirmMessage || L.l('global.deleteConfirmMessage')

    Dialogs.confirm(confirmTitleFinal, confirmMessageFinal, onClickParam, null, {
      confirmButtonLabel: L.l('global.delete'),
    })
  }

  return (
    <Button onClick={onClick} color="danger" className="btn-delete">
      <span className="fa fa-trash" />
    </Button>
  )
}

export default DeleteIconButton
