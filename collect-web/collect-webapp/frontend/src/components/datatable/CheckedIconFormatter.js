import React, { Component } from 'react';

export default class CheckedIconFormatter extends Component {
    render() {
      return (
          <span className={this.props.checked ? 'checked': ''}></span>
      );
    }
  }