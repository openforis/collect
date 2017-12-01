import React, { Component } from 'react';

const HEADER_HEIGHT = 55
const BREADCRUMB_HEIGHT = 50
const PADDING = 10

class MaxAvailableSpaceContainer extends Component {

	constructor(props) {
		super(props)
		this.state = {
			maxAvailableHeight: this.calculateMaxAvailableHeight()
		}
	}

	componentDidMount() {
		this.setState({...this.state, maxAvailableHeight: this.calculateMaxAvailableHeight()})
	}

	calculateMaxAvailableHeight() {
		return (window.document.body.scrollHeight - (HEADER_HEIGHT + BREADCRUMB_HEIGHT + PADDING)) + 'px'
	}
	
  render() {
	  return (
	    <div style={{height: this.state.maxAvailableHeight}}>
					{this.props.children}
	    </div>
		);
  }
}

export default MaxAvailableSpaceContainer;