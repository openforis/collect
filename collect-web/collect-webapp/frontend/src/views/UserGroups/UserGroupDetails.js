import React, { Component } from 'react';
import PropTypes from 'prop-types'
import ItemDetails from '../../components/MasterDetail/ItemDetails'
import { Alert, Button, ButtonGroup, ButtonToolbar, Container, Row, Col,
    Form, FormGroup, Label, Input, FormText, FormFeedback } from 'reactstrap';

import UserGroupService from '../../services/UserGroupService';

export default class UserGroupDetails extends ItemDetails {

    userGroupService = new UserGroupService();

    updateStateFromProps(props) {
        this.state = {
            newItem: ! props.userGroup.id,
            id: props.userGroup.id,
            name: props.userGroup.name,
            label: props.userGroup.label,
            description: props.userGroup.description,
            visibilityCode: props.userGroup.visibilityCode,
            enabled: props.userGroup.enabled,
            errorFeedback: [],
            alertMessageOpen: false
        }
    }

    extractFormObject() {
        return {
            id: this.state.id,
            name: this.state.name,
            label: this.state.label,
            description: this.state.description,
            visibilityCode: this.state.visibilityCode,
            enabled: this.state.enabled
        }
    }

    handleSaveBtnClick() {
        let formObject = this.extractFormObject();
        this.userGroupService.save(formObject).then(this.updateStateFromResponse);
    }
    
    render() {
		return (
            <div>
                <Alert color={this.state.alertMessageColor} isOpen={this.state.alertMessageOpen}>
                    {this.state.alertMessageText}
                </Alert>
                <Form>
                    <FormGroup row color={this.getFieldState('name')}>
                        <Label for="name" sm={2}>Name</Label>
                        <Col sm={10}>
                            <Input type="text" name="name" id="name" 
                                value={this.state.name}
                                state={this.getFieldState('name')}
                                onChange={(event) => this.setState({...this.state, name: event.target.value})} />
                            {this.state.errorFeedback['name'] &&
                                <FormFeedback>{this.state.errorFeedback['name']}</FormFeedback>
                            }
                        </Col>
                    </FormGroup>
                    <FormGroup row color={this.getFieldState('label')}>
                        <Label for="label" sm={2}>Label</Label>
                        <Col sm={10}>
                            <Input type="text" name="label" id="label" 
                                value={this.state.label}
                                state={this.getFieldState('label')}
                                onChange={(event) => this.setState({...this.state, label: event.target.value})} />
                            {this.state.errorFeedback['label'] &&
                                <FormFeedback>{this.state.errorFeedback['label']}</FormFeedback>
                            }
                        </Col>
                    </FormGroup>
                    <FormGroup row color={this.getFieldState('description')}>
                        <Label for="description" sm={2}>Description</Label>
                        <Col sm={10}>
                            <Input type="text" name="description" id="description" 
                                value={this.state.description}
                                state={this.getFieldState('description')}
                                onChange={(event) => this.setState({...this.state, description: event.target.value})} />
                            {this.state.errorFeedback['description'] &&
                                <FormFeedback>{this.state.errorFeedback['description']}</FormFeedback>
                            }
                        </Col>
                    </FormGroup>
                    <FormGroup row color={this.getFieldState('visibilityCode')}>
                        <Col sm={2}>
                            <Label for="visibilityCode">Visibility</Label>
                        </Col>
                        <Col sm={2}>
                            <Input type="radio" value="P" name="visibilityCode" id="visibilityCodePublic"
                                checked={this.state.visibilityCode === 'P'} 
                                state={this.getFieldState('visibilityCode')}
                                onChange={(event) => this.setState({...this.state, visibilityCode: event.target.value})} />
                            <Label for="visibilityCodePublic">Public</Label>
                        </Col>
                        <Col sm={8}>
                            <Input type="radio" value="N" name="visibilityCode" id="visibilityCodePrivate"
                                checked={this.state.visibilityCode === 'N'} 
                                state={this.getFieldState('visibilityCode')}
                                onChange={(event) => this.setState({...this.state, visibilityCode: event.target.value})} />
                            <Label for="visibilityCodePrivate">Private</Label>
                            {this.state.errorFeedback['visibilityCode'] &&
                                <FormFeedback>{this.state.errorFeedback['visibilityCode']}</FormFeedback>
                            }
                        </Col>
                    </FormGroup>
                    <FormGroup row color={this.getFieldState('enabled')}>
                        <Label for="enabled" sm={2}>Enabled</Label>
                        <Col sm={10}>
                            <Input type="checkbox" name="enabled" id="enabled"
                                checked={this.state.enabled} 
                                state={this.getFieldState('enabled')}
                                onChange={(event) => this.setState({...this.state, enabled: event.target.checked})} />
                            {this.state.errorFeedback['enabled'] &&
                                <FormFeedback>{this.state.errorFeedback['enabled']}</FormFeedback>
                            }
                        </Col>
                    </FormGroup>
                    <FormGroup check row>
                        <Col sm={{ size: 10, offset: 2 }}>
                            <Button color="primary" onClick={this.handleSaveBtnClick}>Save</Button>
                            <Button color="danger" onClick={this.handleDeleteBtnClick}>Delete</Button>
                        </Col>
                    </FormGroup>
                </Form>
            </div>
		);
    }
}