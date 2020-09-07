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
        {itemDefs.map((itemDef) => {
          const nodeDefinition = itemDef.attributeDefinition || itemDef.entityDefinition
          const childDefIndex = parentEntity.definition.getChildDefinitionIndexById(nodeDefinition.id)
          const relevant = parentEntity.childrenRelevance[childDefIndex]
          return (
            (relevant || !nodeDefinition.hideWhenNotRelevant) && (
              <FormItem key={itemDef.id} parentEntity={parentEntity} itemDef={itemDef} />
            )
          )
        })}
      </Container>
    )
  }
}
