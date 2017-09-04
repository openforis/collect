import React, { Component } from 'react';

export default class UserRoleDetailsPage extends Component {
    constructor(props) {
        super(props);
        this.updateData = this.updateData.bind(this);
        this.state = { role: props.defaultValue.role };
    }
    focus() {
        this.refs.roleSelect.focus();
    }
    updateData() {
        this.props.onUpdate({ role: this.state.role });
    }
    render() {
        return (
            <span>
                <select
                    ref="roleSelect"
                    value={this.state.role}
                    onKeyDown={this.props.onKeyDown}
                    onChange={(ev) => { this.setState({ role: ev.currentTarget.value }); }} >
                    {this.props.roles.map(role => (<option key={role} value={role}>{role}</option>))}
                </select>
                <button
                    className='btn btn-info btn-xs textarea-save-btn'
                    onClick={this.updateData}>
                    save
            </button>
            </span>
        );
    }
}