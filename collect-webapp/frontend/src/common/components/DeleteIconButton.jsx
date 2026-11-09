import React from 'react'
import { Button } from 'reactstrap'

import Dialogs from 'common/components/Dialogs'
import L from 'utils/Labels'

const DeleteIconButton = (props) => {
  const { onClick: onClickParam, confirmTitle, confirmMessage, title } = props

  const onClick = () => {
    const confirmTitleFinal = confirmTitle || L.l('common.delete.confirm.title')
    const confirmMessageFinal = confirmMessage || L.l('common.delete.confirm.message')

    Dialogs.confirm(confirmTitleFinal, confirmMessageFinal, onClickParam, null, {
      confirmButtonLabel: L.l('common.delete.label'),
    })
  }

  return (
    <Button onClick={onClick} color="danger" className="btn-delete" title={title}>
      <span className="fa fa-trash" />
    </Button>
  )
}

export default DeleteIconButton
