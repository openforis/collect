import React from 'react'
import { Button } from 'reactstrap'

const DeleteIconButton = (props) => (
  <Button onClick={props.onClick} color="danger" className="btn-delete">
    <span className="fa fa-trash" />
  </Button>
)

export default DeleteIconButton
