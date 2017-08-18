import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { Button, ButtonGroup, ButtonToolbar, Container, Row, Col,
	Form, FormGroup, Label, Input, FormText } from 'reactstrap';

class UserDetails extends Component {
   
    state = {
        newUser: true,
        id: null,
        username: '',
        password: '',
        retypedPassword: '',
        enabled: true
    };
    
	constructor( props ) {
        super( props );

        this.state = {
            newUser: props.user.id != null,
            id: props.user.id,
            username: props.user.username,
            enabled: props.user.enabled
        }
        
        this.handleSaveBtnClick = this.handleSaveBtnClick.bind(this);
        this.handleDeleteBtnClick = this.handleDeleteBtnClick.bind(this);
    }
    
	static propTypes = {
		user: PropTypes.object.isRequired,
	}

    handleSaveBtnClick() {
        let formObject = this.state;
        //this.props.dispatch(updateUser(formObject))
    }

    handleDeleteBtnClick() {

    }

    handleSubmit(event) {
        console.log(event.target)
    }

    render() {
		return (
			<Form onSubmit={(values) => {
                console.log('Success!', values)
              }}>
                <FormGroup row>
                    <Label for="username" sm={2}>Username</Label>
                    <Col sm={10}>
                        <Input type="text" name="username" id="username" 
                            value={this.props.user.username} 
                            readOnly={! this.state.newUser}
                            onChange={(event) => this.setState({...this.state, username: event.target.value})} />
                    </Col>
                </FormGroup>
                <FormGroup row>
                    <Label for="password" sm={2}>Password</Label>
                    <Col sm={10}>
                        <Input type="password" name="password" id="password" 
                            onChange={(event) => this.setState({...this.state, password: event.target.value})} />
                    </Col>
                </FormGroup>
                <FormGroup row>
                    <Label for="retypedPassword" sm={2}>Retype Password</Label>
                    <Col sm={10}>
                        <Input type="password" name="retypedPassword" id="retypedPassword"
                            onChange={(event) => this.setState({...this.state, retypedPassword: event.target.value})} />
                    </Col>
                </FormGroup>
                <FormGroup check row>
                    <Col sm={{ size: 10, offset: 2 }}>
                        <Button type="submit" color="primary" onClick={this.handleSaveBtnClick}>Save</Button>
                        <Button color="danger" onClick={this.handleDeleteBtnClick}>Delete</Button>
                    </Col>
                </FormGroup>
            </Form>
		);
  }
}

export default UserDetails;