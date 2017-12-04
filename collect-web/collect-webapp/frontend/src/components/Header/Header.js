import React, { Component } from 'react';
import { connect } from 'react-redux'
import { withRouter } from 'react-router-dom'

import Breadcrumb from 'components/Breadcrumb'
import SurveySelect from 'components/SurveySelect'
import Routes from 'Routes'
import ServiceFactory from 'services/ServiceFactory'
import RouterUtils from 'utils/RouterUtils'

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
    ServiceFactory.sessionService.invalidate().then(r => RouterUtils.navigateToLoginPage(true))
  }

  render() {
    const { pathname } = this.props.location
    const surveySelectRequired = Routes.isSurveySelectRequiredForPath(pathname);

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
        </ul>
        <div className="breadcrumb-wrapper">
          <Breadcrumb />
          {surveySelectRequired && <SurveySelect />}
        </div>
          {/*
        <ul className="nav navbar-nav ml-auto">
          <li className="nav-item d-md-down-none">
            <button className="nav-link navbar-toggler aside-menu-toggler" type="button" onClick={this.asideToggle}>&#9776;</button>
          </li>
        </ul>
          */}
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
export default withRouter(connect(mapStateToProps)(Header));
