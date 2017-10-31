import React, { Component } from 'react';
import { connect } from 'react-redux'
import { Form, FormGroup, Label, Col, Dropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap';

import SurveySelect from '../SurveySelect/';
import ServiceFactory from 'services/ServiceFactory'
import Constants from 'utils/Constants'

class Header extends Component {

  constructor(props) {
    super(props);

    this.toggle = this.toggle.bind(this);
    this.handleLogoutClick = this.handleLogoutClick.bind(this)

    this.state = {
      dropdownOpen: false
    };
  }

  toggle() {
    this.setState({
      dropdownOpen: !this.state.dropdownOpen
    });
  }

  sidebarToggle(e) {
    e.preventDefault();
    document.body.classList.toggle('sidebar-hidden');
  }

  sidebarMinimize(e) {
    e.preventDefault();
    document.body.classList.toggle('sidebar-minimized');
  }

  mobileSidebarToggle(e) {
    e.preventDefault();
    document.body.classList.toggle('sidebar-mobile-show');
  }

  asideToggle(e) {
    e.preventDefault();
    document.body.classList.toggle('aside-menu-hidden');
  }

  handleLogoutClick() {
    ServiceFactory.sessionService.invalidate().then(r => window.location.assign(Constants.BASE_URL))
  }

  render() {
    const loggedUser = this.props.loggedUser
    if (loggedUser == null) {
      return <div>Loading...</div>
    }
    return (
      <header className="app-header navbar">
        <button className="navbar-toggler mobile-sidebar-toggler d-lg-none" type="button" onClick={this.mobileSidebarToggle}>&#9776;</button>
        <a className="navbar-brand d-md-down-none" href="#"></a>
        <ul className="nav navbar-nav">
          <li className="nav-item d-md-down-none">
            <button className="nav-link navbar-toggler sidebar-toggler" type="button" onClick={this.sidebarToggle}>&#9776;</button>
          </li>
          <li className="nav-item">
            <Form>
              <FormGroup row>
                <Label sm={4} className="d-md-down-none">Preferred survey: </Label>
                <Col sm={8}>
                  <SurveySelect />
                </Col>
              </FormGroup>
            </Form>
          </li>
        </ul>
        
        <ul className="nav navbar-nav ml-auto">
	        <li className="nav-item">
            <Dropdown isOpen={this.state.dropdownOpen} toggle={this.toggle}>
              <DropdownToggle caret>{loggedUser.username}</DropdownToggle>
              <DropdownMenu className="dropdown-menu-right">
                <DropdownItem header className="text-center"><strong>Account</strong></DropdownItem>
                <DropdownItem><i className="fa fa-user"></i> Change Password</DropdownItem>
                <DropdownItem onClick={this.handleLogoutClick}><i className="fa fa-lock"></i> Logout</DropdownItem>
              </DropdownMenu>
            </Dropdown>
          </li>
          {/*
          <li className="nav-item d-md-down-none">
            <button className="nav-link navbar-toggler aside-menu-toggler" type="button" onClick={this.asideToggle}>&#9776;</button>
          </li>
          */}
        </ul>
      </header>
    )
  }
}
const mapStateToProps = state => {
  const {
      loggedUser
  } = state.session

  return {
      loggedUser
  }
}
export default connect(mapStateToProps)(Header);
