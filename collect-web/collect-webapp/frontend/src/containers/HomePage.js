import React, { Component } from 'react';
import { connect } from 'react-redux'
import { Jumbotron, Button } from 'reactstrap';
import L from 'utils/Labels'

class DataManagementPage extends Component {
    render() {
        const survey = this.props.survey
        const message = survey ? ' ' : L.l('home-page.survey-not-selected-message')
        return (
            <Jumbotron>
                <h1 className="display-3">{L.l('home-page.welcome-message')}</h1>
                <p className="lead">{L.l('home-page.introduction')}</p>
                <hr className="my-2" />
                <p className="lead">{message}</p>
            </Jumbotron>    
        )
    }

}

const mapStateToProps = state => {
	return {
		survey: state.preferredSurvey ? state.preferredSurvey.survey : null
	}
}

export default connect(mapStateToProps)(DataManagementPage)

    