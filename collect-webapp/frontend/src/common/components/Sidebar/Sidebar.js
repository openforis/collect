import React from 'react'
import { NavLink, useHistory } from 'react-router-dom'
import { connect } from 'react-redux'
import { UncontrolledDropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap'

import * as SessionActions from 'actions/session'
import VersionInfo from 'common/components/VersionInfo'
import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'

const handleNavDropdownClick = (e) => {
  e.preventDefault()
  e.target.parentElement.classList.toggle('open')
}

const Sidebar = (props) => {
  const { dispatch, isFetchingLoggedUser, loggedUser } = props

  if (isFetchingLoggedUser) {
    return <div>Loading...</div>
  }

  const history = useHistory()

  const handleChangePasswordClick = () => RouterUtils.navigateToPasswordChangePage(history)

  const handleLogoutClick = () => dispatch(SessionActions.logout())

  return (
    <div className="sidebar">
      <nav className="sidebar-nav">
        <ul className="nav">
          {
            <li className="nav-item">
              <NavLink to={'/dashboard'} className="nav-link" activeClassName="active">
                <i className="fa fa-tachometer-alt"></i>Dashboard
              </NavLink>
            </li>
          }
          <li className="nav-item">
            <NavLink to={'/datamanagement'} className="nav-link" activeClassName="active">
              <i className="fa fa-database"></i>Data Management
            </NavLink>
          </li>
          {loggedUser.canAccessSurveyDesigner && (
            <li className="nav-item nav-dropdown">
              <a className="nav-link nav-dropdown-toggle" href="#" onClick={handleNavDropdownClick}>
                <i className="fa fa-flask" aria-hidden="true"></i>Survey Designer
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/surveydesigner'} className="nav-link" activeClassName="active">
                    <i className="fa fa-list" aria-hidden="true"></i> List of surveys
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={'/surveydesigner/new'} className="nav-link" activeClassName="active">
                    <i className="fa fa-file" aria-hidden="true"></i> New survey
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={'/surveydesigner/surveyimport'} className="nav-link" activeClassName="active">
                    <i className="fa fa-upload" aria-hidden="true"></i> Import survey
                  </NavLink>
                </li>
              </ul>
            </li>
          )}
          {loggedUser.canAccessDataCleansing && (
            <li className="nav-item">
              <NavLink to={'/datacleansing'} className="nav-link" activeClassName="active">
                <i className="fa fa-gem"></i>Data Cleansing
              </NavLink>
            </li>
          )}
          <li className="nav-item">
            <NavLink to={'/map'} className="nav-link" activeClassName="active">
              <i className="fa fa-map"></i>Map
            </NavLink>
          </li>
          {loggedUser.canAccessSaiku && (
            <li className="nav-item">
              <NavLink to={'/saiku'} className="nav-link" activeClassName="active">
                <i className="fa fa-chart-bar"></i>Saiku
              </NavLink>
            </li>
          )}
          {loggedUser.canAccessBackupRestore ? (
            <li className="nav-item nav-dropdown">
              <a className="nav-link nav-dropdown-toggle" href="#" onClick={handleNavDropdownClick}>
                <i className="fa fa-save"></i>
                {L.l('backupRestore')}
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/backup'} className="nav-link" activeClassName="active">
                    <i className="fa fa-save"></i> {L.l('backup')}
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={'/restore'} className="nav-link" activeClassName="active">
                    <i className="fa fa-upload"></i> {L.l('restore')}
                  </NavLink>
                </li>
              </ul>
            </li>
          ) : (
            ''
          )}
          <li className="divider"></li>
          {loggedUser.canAccessUsersManagement ? (
            <li className="nav-item nav-dropdown">
              <a className="nav-link nav-dropdown-toggle" href="#" onClick={handleNavDropdownClick}>
                <i className="fa fa-lock"></i>Security
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/users'} className="nav-link" activeClassName="active">
                    <i className="fa fa-user"></i> Users
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to={'/usergroups'} className="nav-link" activeClassName="active">
                    <i className="fa fa-users"></i> Groups
                  </NavLink>
                </li>
              </ul>
            </li>
          ) : (
            ''
          )}
        </ul>
        <div style={{ position: 'fixed', bottom: '50px' }}>
          <UncontrolledDropdown>
            <DropdownToggle tag="span" caret>
              <i className="fa fa-user"></i>
              {'  '}
              {loggedUser.username}
            </DropdownToggle>
            <DropdownMenu className="dropdown-menu-right">
              <DropdownItem header className="text-center">
                <strong>{L.l('general.account')}</strong>
              </DropdownItem>
              <DropdownItem onClick={handleChangePasswordClick}>
                <i className="fa fa-user"></i> {L.l('account.change-password')}
              </DropdownItem>
              <DropdownItem onClick={handleLogoutClick}>
                <i className="fa fa-lock"></i> {L.l('account.logout')}
              </DropdownItem>
            </DropdownMenu>
          </UncontrolledDropdown>
        </div>
        <div style={{ position: 'fixed', bottom: '0px' }}>
          <VersionInfo />
          <span>
            <a href="http://www.openforis.org" target="_blank" rel="noopener noreferrer">
              Open Foris
            </a>{' '}
            &copy; 2020
          </span>
        </div>
      </nav>
    </div>
  )
}

const mapStateToProps = (state) => {
  const { loggedUser } = state.session || {
    isFetchingLoggedUser: true,
  }
  return {
    loggedUser: loggedUser,
    isFetchingLoggedUser: loggedUser === null,
  }
}

export default connect(mapStateToProps)(Sidebar)
