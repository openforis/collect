import React, { Component } from 'react'
import Button from 'material-ui/Button'
import Input, { InputLabel } from 'material-ui/Input';
import { FormControl, FormHelperText } from 'material-ui/Form';
import List, { ListItem, ListItemSecondaryAction, ListItemText } from 'material-ui/List'
import Checkbox from 'material-ui/Checkbox'
import Dialog, {
    DialogActions,
    DialogContent,
    DialogContentText,
    DialogTitle,
  } from 'material-ui/Dialog'
import LeftArrowIcon from '@material-ui/icons/ChevronLeft'
import RightArrowIcon from '@material-ui/icons/ChevronRight'
import FastForwardIcon from '@material-ui/icons/FastForward'
import FastRewindIcon from '@material-ui/icons/FastRewind'
import L from 'utils/Labels'    

export default class DataQueryFilterDialog extends Component {

    constructor(props) {
        super(props)

        this.state = {
            attributeDefinition: null,
            filterType: null,
            availableMembers: [], 
            selectedAvailableMembers: [],
            usedMembers: [],
            selectedUsedMembers: []
        }

        this.handleToggleAvailableMember = this.handleToggleAvailableMember.bind(this)
        this.extractQueryCondition = this.extractQueryCondition.bind(this)
        this.handleOk = this.handleOk.bind(this)
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        const attrDef = nextProps.attributeDefinition
        if (attrDef) {
            let filterType
            const availableMembers = []
            switch(attrDef.attributeType) {
                case 'BOOLEAN':
                    filterType = 'IN'
                    availableMembers.push('true')
                    availableMembers.push('false')
                    break
                case 'CODE':
                    filterType = 'IN'
                    availableMembers.push('val1')
                    availableMembers.push('val2')
                    break
                case 'TAXON':
                    filterType = 'IN'
                    break
                case 'DATE':
                case 'NUMBER':
                case 'TIME':
                    filterType = 'BETWEEN'
                    break
                case 'TEXT':
                    filterType = 'CONTAINS'
                    break
    
            }
            return {
                attributeDefinition: nextProps.attributeDefinition,
                filterType: filterType,
                availableMembers: availableMembers,
                selectedAvailableMembers: [],
                usedMembers: [],
                selectedUsedMembers: []
            }
        } else {
            return null
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

    handleOk() {
        const queryCondition = this.extractQueryCondition()
        this.props.onOk(queryCondition)
    }

    extractQueryCondition() {
        const condition = {
            type: this.state.filterType
        }
        switch(this.state.filterType) {
            case 'IN':
                condition.values = this.state.selectedUsedMembers
                break
            case 'BETWEEN':
                condition.minValue = this.state.minValue
                condition.maxValue = this.state.maxValue
                break
            case 'CONTAINS':
                condition.value = this.state.value
        }
    }

    render() {
        const { onClose, ...other } = this.props
        const { filterType, availableMembers, usedMembers, selectedAvailableMembers, selectedUsedMembers } = this.state

        return (
            <Dialog {...other} onClose={onClose}>
                <DialogTitle id="simple-dialog-title">Filter</DialogTitle>
                {filterType === 'IN' && 
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
                                <RightArrowIcon />
                            </Button>
                            <Button variant="fab" mini color="secondary" aria-label="add">
                                <FastForwardIcon />
                            </Button>
                            <Button variant="fab" mini color="secondary" aria-label="add">
                                <LeftArrowIcon />
                            </Button>
                            <Button variant="fab" mini color="secondary" aria-label="add">
                                <FastRewindIcon />
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
                }
                {filterType === 'BETWEEN' && 
                    <div>
                        <div className="col-md-6">
                            <FormControl>
                                <InputLabel htmlFor="min-value">Min</InputLabel>
                                <Input id="min-value" value={this.state.minValue} onChange={(e) => this.setState({minValue: e.target.value})} />
                            </FormControl>
                        </div>
                        <div className="col-md-6">
                            <FormControl>
                                <InputLabel htmlFor="max-value">Max</InputLabel>
                                <Input id="max-value" value={this.state.maxValue} onChange={(e) => this.setState({maxValue: e.target.value})} />
                            </FormControl>
                        </div>
                    </div>
                }
                <DialogActions>
                    <Button onClick={this.props.onClose}>
                        {L.l('general.cancel')}
                    </Button>
                    <Button onClick={this.handleOk} color="primary" variant="raised">
                        {L.l('general.ok')}
                    </Button>
                </DialogActions>
            </Dialog>
        )
    }

}
