import React from 'react';
import PropTypes from 'prop-types'
import { Alert, Button, Col, Form, FormGroup, Label, Input, FormFeedback } from 'reactstrap';
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
    
import * as Actions from 'actions';
import ServiceFactory from 'services/ServiceFactory';
import AbstractItemDetailsPage from 'components/AbstractItemDetailsPage';

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
            this.props.actions.receiveUser(res.form);
        }
    }

    render() {
        const roles = ['VIEW', 'ENTRY_LIMITED', 'ENTRY', 'CLEANSING', 'ANALYSIS', 'ADMIN']
        const EMPTY_OPTION = <option key="-1" value="">--- Select one ---</option>
        
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
                            <FormGroup check>
                                <Label check>
                                    <Input type="checkbox" name="enabled" id="enabled"
                                        checked={this.state.enabled} 
                                        state={this.getFieldState('enabled')}
                                        onChange={(event) => this.setState({...this.state, enabled: event.target.checked})} />
                                </Label>
                            </FormGroup>
                            {this.state.errorFeedback['enabled'] && <FormFeedback>{this.state.errorFeedback['enabled']}</FormFeedback>}
                        </Col>
                    </FormGroup>
                    <FormGroup row color={this.getFieldState('role')}>
                        <Label for="role" sm={2}>Role</Label>
                        <Col sm={10}>
                            <Input type="select" name="role" id="roleSelect" 
                                onChange={(event) => this.setState({...this.state, role: event.target.value})}
                                value={this.state.role}>
                                {[EMPTY_OPTION].concat(roles.map(role => <option key={role} value={role}>{role}</option>))}
                            </Input>
                            {this.state.errorFeedback['role'] &&
                                <FormFeedback>{this.state.errorFeedback['role']}</FormFeedback>
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

function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(Actions, dispatch)
    };
}

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(UserDetailsPage);