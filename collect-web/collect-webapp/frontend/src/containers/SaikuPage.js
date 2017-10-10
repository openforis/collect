import React, { Component } from 'react';
import { connect } from 'react-redux';

class SaikuPage extends Component {
}

const mapStateToProps = state => {
	return {
		survey: state.preferredSurvey ? state.preferredSurvey.survey : null
	}
}

export default connect(mapStateToProps)(SaikuPage)