import React, { Component } from 'react';
import { Formik } from 'formik';
import { Alert, Button, Form, Row, Col, Input } from 'reactstrap';
import { connect } from 'react-redux';

import User from '../../../model/User'

import Forms, { TextFormItem, SelectFormItem } from 'common/components/Forms'
import { createNewSurvey } from 'surveydesigner/newSurvey/actions'
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'
import Strings from 'utils/Strings'

const templateTypes = ['BLANK', 'BIOPHYSICAL', 'COLLECT_EARTH', 'COLLECT_EARTH_IPCC']

class NewSurveyParametersForm extends Component {

    render() {
        const { userGroups, initialValues, createNewSurvey } = this.props

        const templateTypeOptions = templateTypes.map(type => <option key={type} value={type}>{L.l('survey.templateType.' + type)}</option>)
        const userGroupOptions = [<option key="-1" value="">{L.l('forms.selectOne')}</option>].concat(userGroups.map(g => <option key={g.id} value={g.id}>{g.label}</option>))
        const langCodes = L.keys('languages')
        const langItems = langCodes.map(code => ({ code, label: L.l('languages.' + code) }))
        const languageOptions = langItems.sort((a, b) => Strings.compare(a.label, b.label)).map(item =>
            <option key={item.code} value={item.code}>{item.label + ' (' + item.code + ')'}</option>)

        const fieldProps = { labelColSpan: 4, fieldColSpan: 8 }
        return (
            <Formik
                initialValues={initialValues}
                validate={Forms.asyncValidate(ServiceFactory.surveyService.validateSurveyCreation.bind(ServiceFactory.surveyService))}
                onSubmit={values => createNewSurvey(values)}>
                {formProps => {
                    const {
                        handleSubmit,
                        isSubmitting,
                    } = formProps

                    return (
                        <Form onSubmit={handleSubmit}>
                            <TextFormItem
                                name="name"
                                label={L.l('survey.name')}
                                normalize={Forms.normalizeInternalName}
                                {...fieldProps}
                                {...formProps}
                            />
                            <SelectFormItem
                                name="templateType"
                                label={L.l('survey.templateType')}
                                options={templateTypeOptions}
                                {...fieldProps}
                                {...formProps}
                            />
                            <SelectFormItem
                                name="defaultLanguageCode"
                                label={L.l('survey.defaultLanguage')}
                                options={languageOptions}
                                {...fieldProps}
                                {...formProps}
                            />
                            <SelectFormItem
                                name="userGroupId"
                                label={L.l('survey.userGroup')}
                                options={userGroupOptions}
                                {...fieldProps}
                                {...formProps}

                            />
                            <Row>
                                <Col sm={{ size: 1, offset: 5 }}>
                                    <Button color="primary" type="submit" disabled={isSubmitting}>{L.l('general.new')}</Button>
                                </Col>
                            </Row>
                        </Form>
                    )
                }}
            </Formik>
        )
    }
}

const mapStateToProps = state => {
    const { items: userGroups } = state.userGroups
    const defaultPublicGroup = userGroups.find(ug => ug.name === User.DEFAULT_PUBLIC_GROUP_NAME)
    return {
        userGroups,
        initialValues: {
            name: '',
            templateType: 'BLANK',
            defaultLanguageCode: 'en',
            userGroupId: defaultPublicGroup ? defaultPublicGroup.id : ''
        }
    }
}

export default connect(mapStateToProps, { createNewSurvey })(NewSurveyParametersForm)