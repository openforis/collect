import React from 'react'
import classnames from 'classnames'

const Preloader = (props) => {
  const { children, loading } = props

  return (
    <div className={classnames('app-loader', { loaded: !loading })}>
      <div className="loader-wrapper">
        <div className="loader"></div>
        <div className="loader-section section-left"></div>
        <div className="loader-section section-right"></div>
      </div>
      {!loading && children}
    </div>
  )
}

export default Preloader
