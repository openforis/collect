import React, { Component } from 'react';
import { NavLink } from 'react-router-dom'
import { connect } from 'react-redux'
import { UncontrolledDropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap';

import * as SessionActions from 'actions/session'
import VersionInfo from 'components/VersionInfo'
import L from 'utils/Labels';
import RouterUtils from 'utils/RouterUtils'

class Sidebar extends Component {

  constructor(props) {
    super(props)
    this.handleNavDropdownClick = this.handleNavDropdownClick.bind(this)
    this.handleLogoutClick = this.handleLogoutClick.bind(this)
    this.handleChangePasswordClick = this.handleChangePasswordClick.bind(this)    
  }

  handleNavDropdownClick(e) {
    e.preventDefault();
    e.target.parentElement.classList.toggle('open');
  }

  activeRoute(routeName) {
    return this.props.location.pathname.indexOf(routeName) > -1 ? 'nav-item nav-dropdown open' : 'nav-item nav-dropdown';
  }

  handleChangePasswordClick() {
    RouterUtils.navigateToPasswordChangePage(this.props.history)
  }

  handleLogoutClick() {
    this.props.dispatch(SessionActions.logout())
  }
      // secondLevelActive(routeName) {
  //   return this.props.location.pathname.indexOf(routeName) > -1 ? "nav nav-second-level collapse in" : "nav nav-second-level collapse";
  // }

  render() {
    if (this.props.isFetchingLoggedUser) {
      return <div>Loading...</div>
    }
    const loggedUser = this.props.loggedUser
     
    return (
      <div className="sidebar">
        <nav className="sidebar-nav">
          <ul className="nav">
            {
            <li className="nav-item">
              <NavLink to={'/dashboard'} className="nav-link" activeClassName="active"><i className="fa fa-tachometer"></i>Dashboard</NavLink>
            </li>
            }
            <li className="nav-item">
              <NavLink to={'/datamanagement'} className="nav-link" activeClassName="active"><i className="fa fa-database"></i>Data Management</NavLink>
            </li>
            {loggedUser.canAccessSurveyDesigner &&
              <li className="nav-item nav-dropdown">
                <a className="nav-link nav-dropdown-toggle" href="#" onClick={this.handleNavDropdownClick}><i className="fa fa-flask" aria-hidden="true"></i>Survey Designer</a>
                <ul className="nav-dropdown-items">
                  <li className="nav-item">
                    <NavLink to={'/surveydesigner'} className="nav-link" activeClassName="active"><i className="fa fa-list" aria-hidden="true"></i> List of surveys</NavLink>
                  </li>
                  <li className="nav-item">
                    <NavLink to={'/surveydesigner/newsurvey'} className="nav-link" activeClassName="active"><i className="fa fa-file" aria-hidden="true"></i> New survey</NavLink>
                  </li>
                  <li className="nav-item">
                    <NavLink to={'/surveydesigner/surveyimport'} className="nav-link" activeClassName="active"><i className="fa fa-upload" aria-hidden="true"></i> Import survey</NavLink>
                  </li>
                </ul>
              </li>
            }
            {loggedUser.canAccessDataCleansing &&
              <li className="nav-item">
                <NavLink to={'/datacleansing'} className="nav-link" activeClassName="active"><i className="fa fa-diamond"></i>Data Cleansing</NavLink>
              </li>
            }
            <li className="nav-item">
              <NavLink to={'/map'} className="nav-link" activeClassName="active"><i className="fa fa-map-o"></i>Map</NavLink>
	          </li>
            {loggedUser.canAccessSaiku &&
              <li className="nav-item">
                <NavLink to={'/saiku'} className="nav-link" activeClassName="active"><i className="fa fa-bar-chart"></i>Saiku</NavLink>
              </li>
            }
            {loggedUser.canAccessBackupRestore ?
              <li className="nav-item nav-dropdown">
                <a className="nav-link nav-dropdown-toggle" href="#" onClick={this.handleNavDropdownClick}><i className="fa fa-floppy-o"></i>{L.l('backupRestore')}</a>
                <ul className="nav-dropdown-items">
                  <li className="nav-item">
                    <NavLink to={'/backup'} className="nav-link" activeClassName="active"><i className="fa fa-floppy-o"></i> {L.l('backup')}</NavLink>
                  </li>
                  <li className="nav-item">
                    <NavLink to={'/restore'} className="nav-link" activeClassName="active"><i className="fa fa-upload"></i> {L.l('restore')}</NavLink>
                  </li>
                </ul>
              </li>
            : ''}
            <li className="divider"></li>
            {loggedUser.canAccessUsersManagement ?
              <li className="nav-item nav-dropdown">
                <a className="nav-link nav-dropdown-toggle" href="#" onClick={this.handleNavDropdownClick}><i className="fa fa-lock"></i>Security</a>
                <ul className="nav-dropdown-items">
                  <li className="nav-item">
                    <NavLink to={'/users'} className="nav-link" activeClassName="active"><i className="fa fa-user"></i> Users</NavLink>
                  </li>
                  <li className="nav-item">
                    <NavLink to={'/usergroups'} className="nav-link" activeClassName="active"><i className="fa fa-users"></i> Groups</NavLink>
                  </li>
                </ul>
              </li>
            : ''}
          </ul>
          <div style={{position: 'fixed', bottom: '50px'}}>
            <UncontrolledDropdown>
              <DropdownToggle tag="span" caret>
                <i className="fa fa-user"></i>{'  '}{loggedUser.username}
              </DropdownToggle>
              <DropdownMenu className="dropdown-menu-right">
                <DropdownItem header className="text-center"><strong>{L.l('general.account')}</strong></DropdownItem>
                <DropdownItem onClick={this.handleChangePasswordClick}><i className="fa fa-user"></i> {L.l('account.change-password')}</DropdownItem>
                <DropdownItem onClick={this.handleLogoutClick}><i className="fa fa-lock"></i> {L.l('account.logout')}</DropdownItem>
              </DropdownMenu>
            </UncontrolledDropdown>
          </div>
          <div style={{position: 'fixed', bottom: '0px'}}>
            <VersionInfo />
            <span><a href="http://www.openforis.org" target="_blank" rel="noopener noreferrer">Open Foris</a> &copy; 2017</span>
          </div>
        </nav>
      </div>
    )
  }
}

function mapStateToProps(state) {
  const {
    loggedUser
  } = state.session || {
    isFetchingLoggedUser: true
  }
  return {
    loggedUser: loggedUser,
    isFetchingLoggedUser: loggedUser === null
  }
}
export default connect(mapStateToProps)(Sidebar);