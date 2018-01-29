import React, { Component } from 'react';
import { Button, Col, Container, Row } from 'reactstrap';
import { connect } from 'react-redux';
import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils'

import SurveyImportForm from './SurveyImportForm';

class SurveyImportPage extends Component {
    constructor(props) {
        super(props)
    }

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