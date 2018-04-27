import React, { Component } from 'react'
import { Form, FormGroup, Label, Input, FormText } from 'reactstrap'
import Button from 'material-ui/Button'
import AddIcon from '@material-ui/icons/Add'
import List, { ListItem, ListItemSecondaryAction, ListItemText } from 'material-ui/List'
import Checkbox from 'material-ui/Checkbox'
import Dialog, { DialogTitle } from 'material-ui/Dialog';

export default class DataQueryFilterDialog extends Component {

    constructor(props) {
        super(props)

        this.state = {
            attributeDefinition: null,
            availableMembers: [], 
            selectedAvailableMembers: [],
            usedMembers: [],
            selectedUsedMembers: []
        }

        this.handleToggleAvailableMember = this.handleToggleAvailableMember.bind(this)
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        const availableMembers = []
        return {
            attributeDefinition: nextProps.attributeDefinition,
            availableMembers: availableMembers,
            selectedAvailableMembers: [],
            usedMembers: [],
            selectedUsedMembers: []
        }
    }

    handleToggleAvailableMember(value) {
        const { selectedAvailableMembers } = this.state;
        const currentIndex = selectedAvailableMembers.indexOf(value);
        const newSelected = [...selectedAvailableMembers];

        if (currentIndex === -1) {
            newSelected.push(value);
        } else {
            newSelected.splice(currentIndex, 1);
        }
        this.setState({
            selectedAvailableMembers: newSelected,
        });
    }

    render() {
        const { onClose, ...other } = this.props
        const { availableMembers, usedMembers, selectedAvailableMembers, selectedUsedMembers } = this.state

        return (
            <Dialog {...other} onClose={onClose}>
                <DialogTitle id="simple-dialog-title">Filter</DialogTitle>
                <div>
                    <div className="col-md-5">
                        <label>Available Members</label>
                        <List>
                            {availableMembers.map(v => (
                                <ListItem
                                    key={v}
                                    role={undefined}
                                    dense
                                    button
                                    onClick={this.handleToggleAvailableMember(v)}>
                                    <Checkbox
                                        checked={selectedAvailableMembers.indexOf(v) !== -1}
                                        tabIndex={-1}
                                        disableRipple />
                                    <ListItemText primary={`Line item ${v + 1}`} />
                                </ListItem>
                            ))}
                        </List>
                    </div>
                    <div className="col-md-2">
                        <Button variant="fab" mini color="secondary" aria-label="add">
                            <AddIcon />
                        </Button>
                    </div>
                    <div className="col-md-5">
                        <label>Used Members</label>
                        <List>
                            {usedMembers.map(v => (
                                <ListItem
                                    key={v}
                                    role={undefined}
                                    dense
                                    button
                                    onClick={this.handleToggleAvailableMember(v)}>
                                    <Checkbox
                                        checked={selectedUsedMembers.indexOf(v) !== -1}
                                        tabIndex={-1}
                                        disableRipple />
                                    <ListItemText primary={`Line item ${v + 1}`} />
                                </ListItem>
                            ))}
                        </List>
                    </div>
                </div>
            </Dialog>
        )
    }

}
