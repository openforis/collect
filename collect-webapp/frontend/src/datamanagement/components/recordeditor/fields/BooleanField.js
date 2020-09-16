import React, { Component } from 'react'
import { Input } from 'reactstrap'

export default class BooleanField extends Component {
  constructor() {
    super()

    this.state = {
      selected: false,
    }

    this.handleInputChange = this.handleInputChange.bind(this)
  }

  handleInputChange(event) {
    this.setState({ selected: event.target.checked })
  }

  render() {
    return <Input id="checkbox" type="checkbox" onChange={this.handleInputChange} />
  }
}
