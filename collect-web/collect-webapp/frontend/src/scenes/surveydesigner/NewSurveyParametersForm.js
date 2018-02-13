import React, { Component } from 'react';
import { Field, SubmissionError, reduxForm } from 'redux-form'
import { Alert, Button, ButtonGroup, ButtonToolbar, Container, ButtonDropdown, 
    DropdownToggle, DropdownMenu, DropdownItem, Form, FormFeedback, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';

import Forms from 'components/Forms'
import * as SurveysActions from 'actions/surveys';
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'
import Strings from 'utils/Strings'

const templateTypes = ['BLANK', 'BIOPHYSICAL', 'COLLECT_EARTH', 'COLLECT_EARTH_IPCC']

const asyncValidate = (values /*, dispatch */) => {
    return ServiceFactory.surveyService.validateSurveyCreation(values.name, values.templateType,
        values.defaultLanguageCode, values.userGroupId).then(r => {
            Forms.handleValidationResponse(r)
        })
}

class NewSurveyParametersForm extends Component {

    constructor(props) {
        super(props)

        this.submit = this.submit.bind(this)
    }

    submit(values) {
        return this.props.dispatch(SurveysActions.createNewSurvey(values.name, values.templateType,
            values.defaultLanguageCode, values.userGroupId))
    }
    
    render() {
        const { userGroups, error, handleSubmit, pristine, reset, submitting } = this.props

        const templateTypeOptions = templateTypes.map(type => <option key={type} value={type}>{L.l('survey.templateType.' + type)}</option>)
        const userGroupOptions = [<option key="-1" value="">{L.l('forms.selectOne')}</option>].concat(userGroups.map(g => <option key={g.id} value={g.id}>{g.label}</option>))
        const languageOptions = L.keys('languages')
            .sort((a, b) => Strings.compare(L.l('languages.' + a), L.l('languages.' + b)))
            .map(l => <option key={l} value={l}>{L.l('languages.' + l)}</option>)

        return (
            <Form onSubmit={handleSubmit(this.submit)}>
                <Field
                    name="name"
                    type="text"
                    component={Forms.renderFormItemInputField}
                    label={L.l('survey.name')}
                    normalize={Forms.normalizeInternalName}
                />
                <Field
                    name="templateType"
                    component={Forms.renderFormItemSelect}
                    label={L.l('survey.templateType')}
                    options={templateTypeOptions}
                />
                <Field
                    name="defaultLanguageCode"
                    component={Forms.renderFormItemSelect}
                    label={L.l('survey.defaultLanguage')}
                    options={languageOptions}
                />
                <Field
                    name="userGroupId"
                    component={Forms.renderFormItemSelect}
                    label={L.l('survey.userGroup')}
                    options={userGroupOptions}
                />
                {error && <Alert color="danger">{error}</Alert>}
                <Row>
                    <Col sm={{ size: 1, offset: 5 }}>
                        <Button color="primary" type="submit" disabled={submitting}>{L.l('general.new')}</Button>
                    </Col>
                </Row>
            </Form>
        )
    }
}

const mapStateToProps = state => {
    const {items: userGroups} = state.userGroups
    return {
        userGroups
    }
}

NewSurveyParametersForm = connect(mapStateToProps)(NewSurveyParametersForm)

export default reduxForm({ 
    form: 'newSurveyParametersForm',
    asyncValidate,
    asyncBlurFields: ['name', 'templateType', 'userGroupId', 'defaultLanguageCode'],
    initialValues: {
        name: '',
        templateType: 'BLANK', 
        defaultLanguageCode: 'en',
        userGroupId: ''
    }
})(NewSurveyParametersForm)