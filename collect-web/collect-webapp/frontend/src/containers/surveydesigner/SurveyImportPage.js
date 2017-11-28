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
                <Row>
                    <Col md={{ size: 6, offset: 3 }}>
                        <SurveyImportForm style={{width: "500px"}} />
                    </Col>
                </Row>
            </Container>
        )
    }
}

const mapStateToProps = state => {
    const { surveyFileImported } = state.surveys
    return {
        surveyFileImported
    }
}

export default connect(mapStateToProps)(SurveyImportPage)