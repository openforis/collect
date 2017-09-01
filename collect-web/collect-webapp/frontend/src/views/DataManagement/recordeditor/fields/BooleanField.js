import React, { Component, PropTypes } from 'react'
import classnames from 'classnames';
import { Label, Input } from 'reactstrap';

export default class BooleanField extends Component {

    constructor(props) {
        super(props)

        this.state = {
            selected: false
        }

        this.handleInputChange = this.handleInputChange.bind(this)
    }

    handleInputChange(event) {
        this.setState({...this.state, selected: event.target.checked})
    }

    render() {
        return (
            <Input id="checkbox" type="checkbox" onChange={this.handleInputChange} />
        )
    }
}