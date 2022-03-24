import React, { Component } from 'react'
import { Input } from 'reactstrap'

export default class OwnerColumnEditor extends Component {
  constructor(props) {
    super(props)

    const { owner } = props

    this.state = { owner }

    this.handleInputChange = this.handleInputChange.bind(this)
  }

  handleInputChange(event) {
    const ownerId = parseInt(event.target.value, 10)
    const newOwner = ownerId > 0 ? this.props.users.find((u) => u.id === ownerId) : null
    this.props.onUpdate({ owner: newOwner })
  }

  render() {
    const { owner } = this.state
    const emptyOption = (
      <option key="-1" value="-1">
        ---Unassigned---
      </option>
    )
    const userOptions = [emptyOption].concat(
      this.props.users.map((u) => (
        <option key={u.id} value={u.id}>
          {u.username}
        </option>
      ))
    )
    return (
      <span>
        <Input type="select" value={owner ? owner.id : '-1'} onChange={this.handleInputChange}>
          {userOptions}
        </Input>
      </span>
    )
  }
}
