import React, { Component } from 'react';
import {
    Alert, Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container,
    Form, FormGroup, Label, Input, Row, Col
} from 'reactstrap';
import { connect } from 'react-redux';

import * as JobActions from 'actions/job';
import Forms, { SimpleFormItem } from 'components/Forms';
import Workflow from 'model/Workflow';
import SurveyService from 'services/SurveyService';
import ServiceFactory from 'services/ServiceFactory';
import Objects from 'utils/Objects'
import L from 'utils/Labels'
import RouterUtils from 'utils/RouterUtils';

class SurveyClonePage extends Component {

    constructor(props) {
        super(props)

        
        this.state = {
            originalSurveySummary: null,
            originalSurveyType: null,
            newSurveyName: null,
            validating: false,
            validationErrors: null
        }

        this.handleSubmit = this.handleSubmit.bind(this)
        this.validateFormAsync = this.validateFormAsync.bind(this)
        this.handleNameChange = this.handleNameChange.bind(this)
    }

    componentWillReceiveProps(nextProps) {
        const { surveySummaries } = nextProps
        if (surveySummaries && !this.state.originalSurveySummary) {
            const path = this.props.location.pathname
            const surveyName = path.substring(path.lastIndexOf('/') + 1)
            const surveySummary = surveySummaries.find(s => s.name === surveyName)
            this.setState({
                originalSurveySummary: surveySummary,
                originalSurveyType: surveySummary.temporary ? 'TEMPORARY': 'PUBLISHED',
                newSurveyName: surveySummary.name + '_copy'
            })
        }
    }

    handleSubmit() {
        const { originalSurveySummary, originalSurveyType, newSurveyName } = this.state
        ServiceFactory.surveyService.startClone(originalSurveySummary.name, originalSurveyType, newSurveyName).then(job => {
            this.props.dispatch(JobActions.startJobMonitor({
                jobId: job.id,
                title: L.l('survey.clone.cloningSurvey'),
                handleOkButtonClick: this.handleCloneModalOkButtonClick
            }))
        })
    }

    validateFormAsync() {
        this.setState({validating: true})
        const originalSurveyName = this.state.originalSurveySummary.name
        const originalSurveyType = this.state.originalSurveyType
        const newSurveyName = this.state.newSurveyName

        ServiceFactory.surveyService.validateClone(originalSurveyName, originalSurveyType, newSurveyName).then(r => {
            const validationErrors = r.status === 'OK' ? null : r.objects.errors
            this.setState({
                validating: false, 
                validationErrors: validationErrors
            })
        })
    }

    handleNameChange(e) {
        this.setState({newSurveyName: Forms.normalizeInternalName(e.target.value)})
    }

    handleCloneModalOkButtonClick(job) {
        if (job.completed) {
            ServiceFactory.surveyService.getClonedSurveyId().then(surveyId => {
                RouterUtils.navigateToSurveyEditPage(this.props.history, surveyId)
            })
        }
        this.props.dispatch(JobActions.closeJobMonitor())
    }

    render() {
        const { error, originalSurveySummary, originalSurveyType, newSurveyName, validating, validationErrors } = this.state
        if (! originalSurveySummary) {
            return <div>Loading...</div>
        }
        const surveyTypes = ['TEMPORARY', 'PUBLISHED']
        const surveyTypeRadioBoxes = surveyTypes.map(type =>
            <Label key={type} check>
                <Input type="radio" value={type} name="originalSurveyType"
                    checked={originalSurveyType === type}
                    onChange={(e) => this.setState({originalSurveyType: e.target.value })}
                    disabled={type === 'PUBLISHED' && !originalSurveySummary.published || type === 'TEMPORARY' && !originalSurveySummary.temporary}
                    />
                {L.l('survey.surveyType.' + type.toLowerCase())}
            </Label>
        )
       
        return (
            <Container>
                <FormGroup tag="fieldset">
                    <legend>{L.l('survey.clone.title')}</legend>
                    <Form onSubmit={this.handleSubmit}>
                        <SimpleFormItem
                            fieldId="originalSurveyName"
                            label={L.l('survey.clone.originalSurvey.name')}
                            labelColSpan={3}
                            fieldColSpan={9}
                            name="originalSurveyName"
                            type="text">
                            <Input type="text" readOnly value={originalSurveySummary.name} />
                        </SimpleFormItem>
                        <FormGroup row>
                            <Label sm={3}>{L.l('survey.clone.originalSurvey.type')}:</Label>
                            <Col sm={9}>
                                <FormGroup check>
                                    {surveyTypeRadioBoxes}
                                </FormGroup>
                            </Col>
                        </FormGroup>
                        <SimpleFormItem
                            fieldId="newSurveyName"
                            label={L.l('survey.clone.newSurveyName')}
                            labelColSpan={3}
                            fieldColSpan={9}
                            name="newSurveyName"
                            validationErrors={validationErrors}
                            type="text">
                            <Input type="text" 
                                value={newSurveyName} 
                                onChange={this.handleNameChange} 
                                onBlur={this.validateFormAsync} 
                                valid={Forms.getValidState('newSurveyName', validationErrors)} />
                        </SimpleFormItem>
                        {error && <Alert color="danger">{error}</Alert>}
                        <Row>
                            <Col xs={{offset: 5}}>
                                <Button color="primary" type="submit" disabled={validationErrors || validating}>{L.l('survey.clone')}</Button>
                            </Col>
                        </Row>
                    </Form>
                </FormGroup>
            </Container>
        )
    }
}

const mapStateToProps = state => {
	return {
		loggedUser: state.session ? state.session.loggedUser : null,
		surveySummaries: state.surveySummaries ? state.surveySummaries.items : null
	}
}

export default connect(mapStateToProps)(SurveyClonePage)