import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { Button, ButtonGroup, ButtonToolbar, Container, Row, Col,
    Form, FormGroup, Label, Input, FormText, FormFeedback } from 'reactstrap';
import UserService from '../../services/UserService';

class UserDetails extends Component {
   
    userService = new UserService();

    state = {
        newUser: true,
        id: null,
        username: '',
        password: '',
        retypedPassword: '',
        enabled: true,
        errorFeedback: []
    };
    
	constructor( props ) {
        super( props );

        this.state = {
            newUser: ! props.user.id,
            id: props.user.id,
            username: props.user.username,
            enabled: props.user.enabled,
            errorFeedback: []
        }
        
        this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this);
        this.handleDeleteBtnClick = this.handleDeleteBtnClick.bind(this);
    }
    
	static propTypes = {
		user: PropTypes.object.isRequired,
	}

    handleSaveBtnClick() {
        let formObject = {
            id: this.state.id,
            username: this.state.username,
            rawPassword: this.state.rawPassword,
            retypedPassword: this.state.retypedPassword,
            enabled: this.state.enabled
        };
        this.userService.update(formObject).then(response => {
            console.log(response)
            let errorFeedback = {};
            if (response.status === 'ERROR') {
                response.errors.map(error => {
                    errorFeedback[error.field] = error.code; //TODO give i18n label
                })
            }
            this.setState({...this.state, errorFeedback: errorFeedback})
        });
    }

    handleDeleteBtnClick() {

    }

    handleSubmit(event) {
    }

    render() {
		return (
			<Form>
                <FormGroup row color={this.state.errorFeedback['username'] ? 'danger' : ''}>
                    <Label for="username" sm={2}>Username</Label>
                    <Col sm={10}>
                        <Input type="text" name="username" id="username" 
                            value={this.state.username} 
                            readOnly={! this.state.newUser}
                            state={this.state.errorFeedback['username'] ? 'danger': ''}
                            onChange={(event) => this.setState({...this.state, username: event.target.value})} />
                        {this.state.errorFeedback['username'] &&
                            <FormFeedback>{this.state.errorFeedback['username']}</FormFeedback>
                        }
                    </Col>
                </FormGroup>
                <FormGroup row color={this.state.errorFeedback['rawPassword'] ? 'danger' : ''}>
                    <Label for="rawPassword" sm={2}>Password</Label>
                    <Col sm={10}>
                        <Input type="password" name="rawPassword" id="rawPassword" 
                            state={this.state.errorFeedback['rawPassword'] ? 'danger': ''}
                            onChange={(event) => this.setState({...this.state, rawPassword: event.target.value})} />
                        {this.state.errorFeedback['rawPassword'] &&
                            <FormFeedback>{this.state.errorFeedback['rawPassword']}</FormFeedback>
                        }
                    </Col>
                </FormGroup>
                <FormGroup row color={this.state.errorFeedback['retypedPassword'] ? 'danger' : ''}>
                    <Label for="retypedPassword" sm={2}>Retype Password</Label>
                    <Col sm={10}>
                        <Input type="password" name="retypedPassword" id="retypedPassword"
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
		);
  }
}

export default UserDetails;