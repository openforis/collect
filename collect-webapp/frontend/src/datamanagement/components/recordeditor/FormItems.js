import React, { Component } from 'react'
import { Container } from 'reactstrap'

import FormItem from './FormItem'

export default class FormItems extends Component {
  constructor(props) {
    super(props)
  }

  render() {
    const { itemDefs, parentEntity } = this.props

    return (
      <Container className="formItems">
        {itemDefs.map((itemDef) => (
          <FormItem key={itemDef.id} parentEntity={parentEntity} itemDef={itemDef} />
        ))}
      </Container>
    )
  }
}
