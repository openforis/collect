import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { Alert, Button, ButtonGroup, ButtonToolbar, Container, Row, Col,
    Form, FormGroup, Label, Input, FormText, FormFeedback } from 'reactstrap';

import UserService from 'services/UserService';
import AbstractItemDetailsPage from 'components/AbstractItemDetailsPage';

export default class UserDetailsPage extends AbstractItemDetailsPage {
   
    userService = new UserService();
    
	static propTypes = {
		user: PropTypes.object.isRequired,
    }
    
    updateStateFromProps(props) {
        this.state = {
            newItem: ! props.user.id,
            id: props.user.id,
            username: props.user.username,
            rawPassword: '',
            retypedPassword: '',
            enabled: props.user.enabled,
            errorFeedback: [],
            alertMessageOpen: false
        }
    }

    extractFormObject() {
        return {
            id: this.state.id,
            username: this.state.username,
            rawPassword: this.state.rawPassword,
            retypedPassword: this.state.retypedPassword,
            enabled: this.state.enabled
        }
    }

    handleSaveBtnClick() {
        let formObject = this.extractFormObject();
        this.userService.save(formObject).then(this.updateStateFromResponse);
    }

    handleDeleteBtnClick() {

    }

    render() {
		return (
            <div>
                <Alert color={this.state.alertMessageColor} isOpen={this.state.alertMessageOpen}>
                    {this.state.alertMessageText}
                </Alert>
                <Form>
                    <FormGroup row color={this.getFieldState('username')}>
                        <Label for="username" sm={2}>Username</Label>
                        <Col sm={10}>
                            <Input type="text" name="username" id="username" 
                                value={this.state.username} 
                                readOnly={! this.state.newItem}
                                state={this.getFieldState('username')}
                                onChange={(event) => this.setState({...this.state, username: event.target.value})} />
                            {this.state.errorFeedback['username'] &&
                                <FormFeedback>{this.state.errorFeedback['username']}</FormFeedback>
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
                    <FormGroup row color={this.getFieldState('rawPassword')}>
                        <Label for="rawPassword" sm={2}>Password</Label>
                        <Col sm={10}>
                            <Input type="password" name="rawPassword" id="rawPassword" 
                                value={this.state.rawPassword}
                                state={this.getFieldState('rawPassword')}
                                onChange={(event) => this.setState({...this.state, rawPassword: event.target.value})} />
                            {this.state.errorFeedback['rawPassword'] &&
                                <FormFeedback>{this.state.errorFeedback['rawPassword']}</FormFeedback>
                            }
                        </Col>
                    </FormGroup>
                    <FormGroup row color={this.getFieldState('retypedPassword')}>
                        <Label for="retypedPassword" sm={2}>Retype Password</Label>
                        <Col sm={10}>
                            <Input type="password" name="retypedPassword" id="retypedPassword"
                                value={this.state.retypedPassword}
                                state={this.getFieldState('retypedPassword')}
                                onChange={(event) => this.setState({...this.state, retypedPassword: event.target.value})} />
                            {this.state.errorFeedback['retypedPassword'] &&
                                <FormFeedback>{this.state.errorFeedback['retypedPassword']}</FormFeedback>
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
