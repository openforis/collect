import React, { Component } from 'react';
import { Container } from 'reactstrap';
import { connect } from 'react-redux';
import RouterUtils from 'utils/RouterUtils'

import SurveyImportForm from './SurveyImportForm';

class SurveyImportPage extends Component {

    componentWillReceiveProps(nextProps) {
        if (nextProps.surveyFileImported) {
            RouterUtils.navigateToSurveyEditPage(this.props.history, nextProps.importedSurveyId)
        }
    }

    render() {
        return (
            <Container fluid>
                <SurveyImportForm />
            </Container>
        )
    }
}

const mapStateToProps = state => {
    const { surveyFileImported, importedSurveyId } = state.surveys
    return {
        surveyFileImported, importedSurveyId
    }
}

export default connect(mapStateToProps)(SurveyImportPage)