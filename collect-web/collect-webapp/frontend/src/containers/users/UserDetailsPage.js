import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { Alert, Button, Col, Form, FormGroup, Label, Input, FormFeedback } from 'reactstrap';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'

import { SimpleFormItem } from 'components/Forms'
import * as UsersActions from 'actions/users'
import ServiceFactory from 'services/ServiceFactory';
import AbstractItemDetailsPage from 'components/AbstractItemDetailsPage';
import User from 'model/User'
import L from 'utils/Labels'

class UserDetailsPage extends AbstractItemDetailsPage {
   
	static propTypes = {
		user: PropTypes.object.isRequired,
    }
    
    getInitialState() {
        let s = super.getInitialState()
        s = {...s, 
            newItem: true,
            id: null,
            username: '',
            rawPassword: '',
            retypedPassword: '',
            role: 'ENTRY',
            enabled: false,
        }
        return s
    }

    updateStateFromProps(props) {
        this.setState({
            newItem: ! props.user.id,
            id: props.user.id,
            username: props.user.username,
            rawPassword: '',
            retypedPassword: '',
            role: props.user.role,
            enabled: props.user.enabled,
            errorFeedback: [],
            alertMessageOpen: false
        })
    }

    extractFormObject() {
        return {
            id: this.state.id,
            username: this.state.username,
            rawPassword: this.state.rawPassword,
            retypedPassword: this.state.retypedPassword,
            enabled: this.state.enabled,
            role: this.state.role
        }
    }

    handleSaveBtnClick() {
        let formObject = this.extractFormObject();
        ServiceFactory.userService.save(formObject).then(this.updateStateFromResponse);
    }

    updateStateFromResponse(res) {
        super.updateStateFromResponse(res)
        if (res.statusOk) {
            this.setState({
                newItem: false,
                id: res.form.id
            })
            this.props.dispatch(UsersActions.receiveUser(res.form));
        }
    }

    render() {
        const EMPTY_OPTION = <option key="-1" value="">--- Select one ---</option>
        
        return (
            <div>
                <Alert color={this.state.alertMessageColor} isOpen={this.state.alertMessageOpen}>
                    {this.state.alertMessageText}
                </Alert>
                <Form>
                    <SimpleFormItem fieldId='username' 
                            fieldState={this.getFieldState('username')}
                            errorFeedback={this.state.errorFeedback['username']}
                            label={'user.username'}>
                        <Input type="text" name="username" id="username" 
                                value={this.state.username} 
                                readOnly={! this.state.newItem}
                                state={this.getFieldState('username')}
                                onChange={(event) => this.setState({...this.state, username: event.target.value})} />
                    </SimpleFormItem>
                    <SimpleFormItem fieldId='enabled' 
                            fieldState={this.getFieldState('enabled')}
                            errorFeedback={this.state.errorFeedback['enabled']}
                            label={'user.enabled'}>
                        <FormGroup check>
                            <Label check>
                                <Input type="checkbox" name="enabled" id="enabled"
                                    checked={this.state.enabled} 
                                    state={this.getFieldState('enabled')}
                                    onChange={(event) => this.setState({...this.state, enabled: event.target.checked})} />
                            </Label>
                        </FormGroup>
                    </SimpleFormItem>
                    <SimpleFormItem fieldId='roleSelect' 
                            fieldState={this.getFieldState('role')}
                            errorFeedback={this.state.errorFeedback['role']}
                            label={'user.role'}>
                        <Input type="select" name="role" id="roleSelect" 
                            onChange={(event) => this.setState({...this.state, role: event.target.value})}
                            state={this.getFieldState('role')}
                            value={this.state.role}>
                            {User.ROLES.map(role => <option key={role} value={role}>{role}</option>)}
                        </Input>
                    </SimpleFormItem>
                    <SimpleFormItem fieldId='rawPassword' 
                            fieldState={this.getFieldState('rawPassword')}
                            errorFeedback={this.state.errorFeedback['rawPassword']}
                            label={'user.rawPassword'}>
                        <Input type="password" name="rawPassword" id="rawPassword" 
                            value={this.state.rawPassword}
                            state={this.getFieldState('rawPassword')}
                            onChange={(event) => this.setState({...this.state, rawPassword: event.target.value})} />
                    </SimpleFormItem>
                    <SimpleFormItem fieldId='retypedPassword' 
                            fieldState={this.getFieldState('retypedPassword')}
                            errorFeedback={this.state.errorFeedback['retypedPassword']}
                            label={'user.retypedPassword'}>
                        <Input type="password" name="retypedPassword" id="retypedPassword"
                            value={this.state.retypedPassword}
                            state={this.getFieldState('retypedPassword')}
                            onChange={(event) => this.setState({...this.state, retypedPassword: event.target.value})} />
                    </SimpleFormItem>
                    <FormGroup check row>
                        <Col sm={{ size: 10, offset: 2 }}>
                            <Button color="primary" onClick={this.handleSaveBtnClick}>Save</Button>
                        </Col>
                    </FormGroup>
                </Form>
            </div>
		);
    }
}

function mapStateToProps(state) {
    return {}
}

export default connect(mapStateToProps)(UserDetailsPage);