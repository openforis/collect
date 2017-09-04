import React, { Component } from 'react';

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
		return (window.document.body.scrollHeight - 180) + 'px'
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