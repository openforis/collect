import React from 'react'
import { Input } from 'reactstrap'

import AbstractField from './AbstractField'

export default class TextField extends AbstractField {
  constructor(props) {
    super(props)

    this.onChange = this.onChange.bind(this)

    this.state = {
      value: { value: '' },
      dirty: false,
    }
  }

  extractValueFromProps() {
    const attr = this.getSingleAttribute()
    return attr.value
  }

  onChange(event) {
    const { value } = event.target
    this.setState({ value: { value }, dirty: true })
    this.sendAttributeUpdateCommand()
  }

  render() {
    const { value: valueState } = this.state
    const { value } = valueState

    return <Input value={value} onChange={this.onChange} />
  }
}
