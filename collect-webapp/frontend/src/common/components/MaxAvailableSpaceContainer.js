import React, { Component } from 'react'

const HEADER_HEIGHT = 55
const PADDING = 10

class MaxAvailableSpaceContainer extends Component {
  constructor() {
    super()

    this.state = {
      maxAvailableHeight: this.calculateMaxAvailableHeight(),
    }

    this.handleWindowResize = this.handleWindowResize.bind(this)
  }

  componentDidMount() {
    this.handleWindowResize()
    window.addEventListener('resize', this.handleWindowResize)
  }

  componentWillUnmount() {
    window.removeEventListener('resize', this.handleWindowResize)
  }

  handleWindowResize() {
    this.setState({ ...this.state, maxAvailableHeight: this.calculateMaxAvailableHeight() })
  }

  calculateMaxAvailableHeight() {
    return window.document.body.clientHeight - (HEADER_HEIGHT + PADDING) + 'px'
  }

  render() {
    const { className } = this.props
    return (
      <div
        className={`max-available-space ${className ? className : ''}`}
        style={{ height: this.state.maxAvailableHeight }}
      >
        {this.props.children}
      </div>
    )
  }
}

export default MaxAvailableSpaceContainer
