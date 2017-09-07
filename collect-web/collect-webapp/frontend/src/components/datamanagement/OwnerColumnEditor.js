import React, { Component } from 'react';
import { Button, Input } from 'reactstrap';

export default class OwnerColumnEditor extends Component {
    constructor(props) {
        super(props);
        
        this.state = { owner: props.row.owner };

        this.handleInputChange = this.handleInputChange.bind(this)
    }

    focus() {
        //this.refs.inputRef.focus();
    }

    handleInputChange(event) {
        const ownerId = event.target.value
        const newOwner = ownerId > 0 ? this.props.users.find(u => u.id == ownerId) : null
        this.props.onUpdate({ owner: newOwner });
    }

    render() {
        const owner = this.state.owner
        const emptyOption = <option key="-1" value="-1">---Unassigned---</option>
        const userOptions = [emptyOption].concat(this.props.users.map(u => <option key={u.id} value={u.id}>{u.username}</option>))
        return (
            <span>
                <Input ref="inputRef" type="select" value={owner ? owner.id : '-1'} onChange={this.handleInputChange}>
                    {userOptions}
                </Input>
            </span>
        )
    }
}