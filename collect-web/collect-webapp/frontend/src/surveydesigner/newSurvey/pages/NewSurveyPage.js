import React, { Component } from 'react'
import { Col, Container, Row } from 'reactstrap'
import { connect } from 'react-redux'

import NewSurveyParametersForm from '../components/NewSurveyParametersForm'
import * as NewSurveyActions from 'surveydesigner/newSurvey/actions'
import RouterUtils from 'utils/RouterUtils'

class NewSurveyPage extends Component {
    
    componentWillReceiveProps(nextProps) {
        if (nextProps.newSurveyCreated) {
            const newSurveySummary = nextProps.newSurveySummary
            this.props.dispatch(NewSurveyActions.resetNewSurveyForm())
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
    const { newSurveyCreated, newSurveySummary} = state.surveyDesigner.newSurvey
    return {
        newSurveyCreated, newSurveySummary
    }
}

export default connect(mapStateToProps)(NewSurveyPage)