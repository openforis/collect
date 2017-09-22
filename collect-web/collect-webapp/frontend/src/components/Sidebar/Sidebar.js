import React, { Component } from 'react';
import { NavLink } from 'react-router-dom'
import { connect } from 'react-redux'

class Sidebar extends Component {

  handleClick(e) {
    e.preventDefault();
    e.target.parentElement.classList.toggle('open');
  }

  activeRoute(routeName) {
    return this.props.location.pathname.indexOf(routeName) > -1 ? 'nav-item nav-dropdown open' : 'nav-item nav-dropdown';
  }

  // secondLevelActive(routeName) {
  //   return this.props.location.pathname.indexOf(routeName) > -1 ? "nav nav-second-level collapse in" : "nav nav-second-level collapse";
  // }

  render() {
    if (this.props.isFetchingLoggedUser) {
      return <div>Loading...</div>
    }
    const loggedUser = this.props.loggedUser
    const preferredSurvey = this.props.preferredSurvey

    return (
      <div className="sidebar">
        <nav className="sidebar-nav">
          <ul className="nav">
            {
            <li className="nav-item">
              <NavLink to={'/dashboard'} className="nav-link" activeClassName="active"><i className="icon-speedometer"></i>Dashboard</NavLink>
            </li>
            }
            {preferredSurvey ? 
              <li className="nav-item">
                <NavLink to={'/datamanagement'} className="nav-link" activeClassName="active"><i className="icon-layers"></i>Data Management</NavLink>
              </li>
            : ''}
            {loggedUser.canAccessSurveyDesigner ?
              <li className="nav-item">
                <NavLink to={'/surveydesigner'} className="nav-link" activeClassName="active"><i className="icon-layers"></i>Survey Designer</NavLink>
              </li>
              : ''}
            {loggedUser.canAccessDataCleansing ?
              <li className="nav-item">
                <NavLink to={'/datacleansing'} className="nav-link" activeClassName="active"><i className="icon-diamond"></i>Data Cleansing</NavLink>
              </li>
            : ''}
            <li className="nav-item">
              <NavLink to={'/map'} className="nav-link" activeClassName="active"><i className="icon-map"></i>Map</NavLink>
	          </li>
            <li className="divider"></li>
            {loggedUser.canAccessUsersManagement ?
              <li className="nav-item nav-dropdown">
                <a className="nav-link nav-dropdown-toggle" href="#" onClick={this.handleClick.bind(this)}><i className="icon-people"></i>Users Management</a>
                <ul className="nav-dropdown-items">
                  <li className="nav-item">
                    <NavLink to={'/users'} className="nav-link" activeClassName="active"><i className="icon-user"></i> Users</NavLink>
                  </li>
                  <li className="nav-item">
                    <NavLink to={'/usergroups'} className="nav-link" activeClassName="active"><i className="icon-people"></i> Groups</NavLink>
                  </li>
                </ul>
              </li>
            : ''}
          </ul>
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
  const preferredSurvey = state.preferredSurvey ? state.preferredSurvey.survey : null

  return {
    loggedUser: loggedUser,
    isFetchingLoggedUser: loggedUser === null,
    preferredSurvey: preferredSurvey
  }
}
export default connect(mapStateToProps)(Sidebar);
