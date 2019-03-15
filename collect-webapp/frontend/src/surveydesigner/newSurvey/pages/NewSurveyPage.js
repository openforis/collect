import React, { Component } from 'react'
import { connect } from 'react-redux'
import Grid from '@material-ui/core/Grid'

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
            <Grid container
                justify="center">
                <Grid item>
                    <NewSurveyParametersForm style={{ width: "500px" }} />
                </Grid>
            </Grid>
        )
    }

}

const mapStateToProps = state => {
    const { newSurveyCreated, newSurveySummary } = state.surveyDesigner.newSurvey
    return {
        newSurveyCreated, newSurveySummary
    }
}

export default connect(mapStateToProps)(NewSurveyPage)