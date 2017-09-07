import React, { Component } from 'react';
import { connect } from 'react-redux'
import { Jumbotron, Button } from 'reactstrap';

class DataManagementPage extends Component {
    render() {
        const survey = this.props.survey
        const message = survey ? ' ' : 'Please select a survey or create a new one from the Survey Designer'
        return (
            <Jumbotron>
                <h1 className="display-3">Welcome to Open Foris Collect!</h1>
                <p className="lead">Open Foris Collect is the main entry point for data collected in field-based inventories. It provides a fast, easy, flexible way to set up a survey with a user-friendly interface. Collect handles multiple data types and complex validation rules, all in a multilanguage environment.</p>
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

    