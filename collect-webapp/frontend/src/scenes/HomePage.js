import React, { Component } from 'react'
import { connect } from 'react-redux'
import L from 'utils/Labels'

class HomePage extends Component {
  render() {
    const survey = this.props.survey
    const message = survey ? null : L.l('home-page.survey-not-selected-message')
    return (
      <div className="bg-light mb-4 py-3 py-sm-5 home-page jumbotron">
        <h1 className="display-3">{L.l('home-page.welcome-message')}</h1>
        <p className="lead">{L.l('home-page.introduction')}</p>
        <hr className="my-2" />
        {message && (
          <div className="lead">
            <ul>
              <li>{message}</li>
            </ul>
          </div>
        )}
      </div>
    )
  }
}

const mapStateToProps = (state) => {
  return {
    survey: state.activeSurvey ? state.activeSurvey.survey : null,
  }
}

export default connect(mapStateToProps)(HomePage)
