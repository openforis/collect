import React from 'react'
import PropTypes from 'prop-types'
import { Spinner } from 'reactstrap'

const LoadingSpinnerSmall = (props) => {
  const { alignRight } = props

  const style = alignRight ? { position: 'absolute', top: '10px', right: 0, marginRight: '20px' } : {}

  return <Spinner color="primary" size="sm" style={style} />
}

LoadingSpinnerSmall.propTypes = {
  alignRight: PropTypes.bool,
}

LoadingSpinnerSmall.defaultProps = {
  alignRight: false,
}

export default LoadingSpinnerSmall
