import React, { Component } from 'react';
import { Input } from 'reactstrap';

export default class UserGroupColumnEditor extends Component {
    constructor(props) {
        super(props)
        
        this.state = { userGroupId: props.row.userGroupId }

        this.handleInputChange = this.handleInputChange.bind(this)
    }

    focus() {
        //this.refs.inputRef.focus()
    }

    handleInputChange(event) {
        const userGroupId = parseInt(event.target.value, 10)
        this.props.onUpdate({userGroupId: userGroupId})
    }

    render() {
        const options = this.props.userGroups.map(u => <option key={u.id} value={u.id}>{u.systemDefined ? '---' + u.label + '---': u.label}</option>)
        return (
            <span>
                <Input ref="inputRef" type="select" value={this.state.userGroupId} onChange={this.handleInputChange}>
                    {options}
                </Input>
            </span>
        )
    }
}