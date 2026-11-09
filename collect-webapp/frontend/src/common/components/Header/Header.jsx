import React from 'react'
import { useSelector } from 'react-redux'

import Breadcrumb from 'common/components/Breadcrumb'
import SurveySelect from 'common/components/SurveySelect'
import Routes from 'Routes'

import User from 'model/User'
import { useEffect } from 'react'
import { useLocation } from 'react-router-dom'

const Header = () => {
  const { loggedUser } = useSelector((state) => state.session)
  const location = useLocation()

  useEffect(() => {
    // show in full screen mode if user has entry_limited role
    if (loggedUser.role === User.ROLE.ENTRY_LIMITED) {
      document.body.classList.add('sidebar-hidden')
    }
  }, [])

  const sidebarToggle = (e) => {
    e.preventDefault()
    document.body.classList.toggle('sidebar-hidden')
  }

  const mobileSidebarToggle = (e) => {
    e.preventDefault()
    document.body.classList.toggle('sidebar-mobile-show')
  }

  // const asideToggle = (e) => {
  //   e.preventDefault()
  //   document.body.classList.toggle('aside-menu-hidden')
  // }

  const surveySelectRequired = Routes.isSurveySelectRequiredForPath(location.pathname)

  return (
    <header className="app-header navbar">
      <button className="navbar-toggler mobile-sidebar-toggler d-lg-none" type="button" onClick={mobileSidebarToggle}>
        &#9776;
      </button>
      <a className="navbar-brand d-md-down-none" href="#"></a>
      <ul className="nav navbar-nav d-md-down-none">
        <li className="nav-item">
          <button className="nav-link navbar-toggler sidebar-toggler" type="button" onClick={sidebarToggle}>
            &#9776;
          </button>
        </li>
      </ul>
      <div className="breadcrumb-wrapper">
        <Breadcrumb />
        {surveySelectRequired && <SurveySelect />}
      </div>
      {/*
        <ul className="nav navbar-nav ml-auto">
          <li className="nav-item d-md-down-none">
            <button className="nav-link navbar-toggler aside-menu-toggler" type="button" onClick={asideToggle}>&#9776;</button>
          </li>
        </ul>
          */}
    </header>
  )
}

export default Header
