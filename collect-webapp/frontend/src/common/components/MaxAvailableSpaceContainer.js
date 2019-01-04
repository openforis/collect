import React, { Component } from 'react';

const HEADER_HEIGHT = 55
const PADDING = 10

class MaxAvailableSpaceContainer extends Component {

	constructor(props) {
		super(props)

		this.state = {
			maxAvailableHeight: this.calculateMaxAvailableHeight()
		}

		this.handleWindowResize = this.handleWindowResize.bind(this)
	}

	componentDidMount() {
		this.handleWindowResize();
		window.addEventListener("resize", this.handleWindowResize);
	}

	componentWillUnmount() {
		window.removeEventListener("resize", this.handleWindowResize);
	}

	handleWindowResize() {
		this.setState({ ...this.state, maxAvailableHeight: this.calculateMaxAvailableHeight() })
	}

	calculateMaxAvailableHeight() {
		return (window.document.body.scrollHeight - (HEADER_HEIGHT + PADDING)) + 'px'
	}

	render() {
		return (
			<div style={{ height: this.state.maxAvailableHeight }}>
				{this.props.children}
			</div>
		);
	}
}

export default MaxAvailableSpaceContainer;