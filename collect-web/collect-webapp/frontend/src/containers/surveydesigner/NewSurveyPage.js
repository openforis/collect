import React, { Component } from 'react';
import { Button, Col, Container, Row } from 'reactstrap';
import { connect } from 'react-redux';

import NewSurveyParametersForm from './NewSurveyParametersForm';
import * as SurveyActions from 'actions/surveys'
import L from 'utils/Labels';
import RouterUtils from 'utils/RouterUtils';

class NewSurveyPage extends Component {
    
    constructor(props) {
        super(props)
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.newSurveyCreated) {
            const newSurveySummary = nextProps.newSurveySummary
            this.props.dispatch(SurveyActions.resetNewSurveyForm())
            RouterUtils.navigateToSurveyEditPage(this.props.history, newSurveySummary.id)
        }
    }

    render() {
        return (
            <Container fluid>
                <Row>
                    <Col sm={{ size: 6, offset: 3 }}>
                        <NewSurveyParametersForm  style={{width: "500px"}} />
                    </Col>
                </Row>
            </Container>
        )
    }

}

const mapStateToProps = state => {
    const { newSurveyCreated, newSurveySummary} = state.surveys
    return {
        newSurveyCreated, newSurveySummary
    }
}

export default connect(mapStateToProps)(NewSurveyPage)