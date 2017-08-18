import React, { Component, PropTypes } from 'react';
import ReactDOM from 'react-dom';
import { Redirect } from 'react-router'
import { connect } from 'react-redux'

import Constants from '../../../utils/Constants';
import { logInUser } from '../../../actions';

class Signin extends Component {
  constructor( props ) {
    super( props );
  }

  static propTypes = {
    dispatch: PropTypes.func.isRequired
  }

  handleSubmit(event) {
    const { dispatch } = this.props

    let credentials = {
      "username": this.refs.usernameTextField.value,
      "password": this.refs.passwordTextField.value
    }
    dispatch(logInUser(credentials))
  }

  render() {
    return (
      <div className="app flex-row align-items-center">
        <div className="container">
          <div className="row justify-content-center">
            <div className="col-md-8">
              <div className="card-group mb-0">
                <div className="card p-4">
                  <div className="card-block">
                    <h1>Login</h1>
                    <p className="text-muted">Sign In to your account</p>
                    <div className="input-group mb-3">
                      <span className="input-group-addon"><i className="icon-user"></i></span>
                      <input ref="usernameTextField" name="username" type="text" className="form-control" placeholder="Username"/>
                    </div>
                    <div className="input-group mb-4">
                      <span className="input-group-addon"><i className="icon-lock"></i></span>
                      <input ref="passwordTextField" name="password" type="password" className="form-control" placeholder="Password"/>
                    </div>
                    <div className="row">
                      <div className="col-6">
                        <button type="submit" className="btn btn-primary px-4" onClick={this.handleSubmit.bind(this)}>Login</button>
                      </div>
                      {/*
                      <div className="col-6 text-right">
                        <button type="button" className="btn btn-link px-0">Forgot password?</button>
                      </div>
                      */}
                    </div>
                  </div>
                </div>
                {/*
                <div className="card card-inverse card-primary py-5 d-md-down-none" style={{ width: 44 + '%' }}>
                  <div className="card-block text-center">
                    <div>
                      <h2>Sign up</h2>
                      <p>Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                      <button type="button" className="btn btn-primary active mt-3">Register Now!</button>
                    </div>
                  </div>
                </div>
                */}
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => {
  return state;
}  

export default connect(mapStateToProps)(Signin)
