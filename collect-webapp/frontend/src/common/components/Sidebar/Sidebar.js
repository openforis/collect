import React from 'react'
import { NavLink, useHistory } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { UncontrolledDropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap'
import classnames from 'classnames'

import * as SessionActions from 'actions/session'
import * as SidebarActions from 'actions/sidebar'
import VersionInfo from 'common/components/VersionInfo'
import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'

const Sidebar = () => {
  const { loggedUser } = useSelector((state) => state.session)
  const { openNavItems } = useSelector((state) => state.sidebar)
  const isFetchingLoggedUser = loggedUser === null

  const dispatch = useDispatch()
  const history = useHistory()

  const isNavItemOpen = (id) => Boolean(openNavItems[id])

  const handleChangePasswordClick = () => RouterUtils.navigateToPasswordChangePage(history)

  const handleLogoutClick = () => dispatch(SessionActions.logout())

  const handleNavDropdownClick = (e) => {
    e.preventDefault()
    const elementLiId = e.target.parentElement.id
    dispatch(SidebarActions.toggleSidebarDropdownItem(elementLiId))
  }

  if (isFetchingLoggedUser) {
    return <div>Loading...</div>
  }

  return (
    <div className="sidebar">
      <nav className="sidebar-nav">
        <ul className="nav">
          <li className="nav-item">
            <NavLink to={'/dashboard'} className="nav-link" activeClassName="active">
              <i className="fa fa-tachometer-alt"></i>Dashboard
            </NavLink>
          </li>
          <li className="nav-item">
            <NavLink to={'/datamanagement'} className="nav-link" activeClassName="active">
              <i className="fa fa-database"></i>Data Management
            </NavLink>
          </li>
          {loggedUser.canAccessSurveyDesigner && (
            <li
              id="sidebar-nav-item-survey-designer"
              className={`nav-item nav-dropdown ${classnames({
                open: isNavItemOpen('sidebar-nav-item-survey-designer'),
              })}`}
            >
              <a className="nav-link nav-dropdown-toggle" href="#" onClick={handleNavDropdownClick}>
                <i className="fa fa-flask" aria-hidden="true"></i>Survey Designer
              </a>
              <ul className="nav-dropdown-items">
                <li className="nav-item">
                  <NavLink to={'/surveydesigner'} exact className="nav-link" activeClassName="active">
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
            <li
              id="sidebar-nav-item-backup-restore"
              className={`nav-item nav-dropdown ${classnames({
                open: isNavItemOpen('sidebar-nav-item-backup-restore'),
              })}`}
            >
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
            <li
              id="sidebar-nav-item-security"
              className={`nav-item nav-dropdown ${classnames({
                open: isNavItemOpen('sidebar-nav-item-security'),
              })}`}
            >
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

export default Sidebar
