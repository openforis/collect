import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useDispatch, useSelector } from 'react-redux'
import { UncontrolledDropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap'
import classnames from 'classnames'

import * as SessionActions from 'actions/session'
import * as SidebarActions from 'actions/sidebar'
import VersionInfo from 'common/components/VersionInfo'
import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'

const Link = ({ to, iconClassName, label }) => (
  <NavLink to={to} className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}>
    <i className={`fa ${iconClassName}`} />
    {label}
  </NavLink>
)

const Sidebar = () => {
  const { loggedUser } = useSelector((state) => state.session)
  const { openNavItems } = useSelector((state) => state.sidebar)
  const { survey } = useSelector((state) => state.activeSurvey)
  const roleInSurveyGroup = survey?.roleInSurveyGroup

  const isFetchingLoggedUser = loggedUser === null

  const dispatch = useDispatch()
  const navigate = useNavigate()

  const isNavItemOpen = (id) => Boolean(openNavItems[id])

  const handleChangePasswordClick = () => RouterUtils.navigateToPasswordChangePage(navigate)

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
          {loggedUser.canAccessDashboard && (
            <li className="nav-item">
              <Link to="/dashboard" iconClassName="fa-tachometer-alt" label="Dashboard" />
            </li>
          )}
          <li className="nav-item">
            <Link to="/datamanagement" iconClassName="fa-database" label="Data Management" />
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
                  <Link to="/surveydesigner" iconClassName="fa-list" label="List of surveys" />
                </li>
                <li className="nav-item">
                  <Link to="/surveydesigner/new" iconClassName="fa-file" label="New survey" />
                </li>
                <li className="nav-item">
                  <Link to="/surveydesigner/surveyimport" iconClassName="fa-upload" label="Import survey" />
                </li>
              </ul>
            </li>
          )}
          {loggedUser.canAccessDataCleansing({ roleInSurveyGroup }) && (
            <li className="nav-item">
              <Link to="/datacleansing" iconClassName="fa-gem" label="Data Cleansing" />
            </li>
          )}
          {loggedUser.canAccessMap && (
            <li className="nav-item">
              <Link to="/map" iconClassName="fa-map" label="Map" />
            </li>
          )}
          {survey && loggedUser.canAccessSaiku({ roleInSurveyGroup }) && (
            <li className="nav-item">
              <Link to="/saiku" iconClassName="fa-chart-bar" label="Saiku" />
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
                  <Link to="/backup" iconClassName="fa-save" label={L.l('backup')} />
                </li>
                <li className="nav-item">
                  <Link to="/restore" iconClassName="fa-upload" label={L.l('restore')} />
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
                  <Link to="/users" iconClassName="fa-user" label="Users" />
                </li>
                <li className="nav-item">
                  <Link to="/usergroups" iconClassName="fa-users" label="Groups" />
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
            <a href="https://www.openforis.org" target="_blank" rel="noopener noreferrer">
              Open Foris
            </a>
            &copy; 2010-2024
          </span>
        </div>
      </nav>
    </div>
  )
}

export default Sidebar
