import React, { Component } from 'react';
import PropTypes from 'prop-types';

export default class ServerConnectionVerifier extends Component {
    
    timer

    constructor(props) {
        super(props)
    }

    componentDidMount() {
        
    }

    render() {
       return this.props.children
    }
}
