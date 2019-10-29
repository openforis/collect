import React from 'react'
import { compose } from "redux"
import { connect } from 'react-redux'
import { withFormik } from 'formik'
import { Button, Form, Row, Col, Input } from 'reactstrap'

import User from '../../../model/User'

import Forms, { TextFormItem, SelectFormItem, asyncValidate } from 'common/components/Forms'
import { createNewSurvey } from 'surveydesigner/newSurvey/actions'
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'
import Strings from 'utils/Strings'

const templateTypes = ['BLANK', 'BIOPHYSICAL', 'COLLECT_EARTH', 'COLLECT_EARTH_IPCC']

const NewSurveyParametersForm = props => {

    const {
        userGroups,
        handleSubmit,
        isSubmitting,
    } = props

    const templateTypeOptions = templateTypes.map(type =>
        <option key={type} value={type}>{L.l('survey.templateType.' + type)}</option>)

    const userGroupOptions = [<option key="-1" value="">{L.l('forms.selectOne')}</option>].concat(
        userGroups.map(g => <option key={g.id} value={g.id}>{g.label}</option>))

    const langCodes = L.keys('languages')
    const langItems = langCodes.map(code => ({ code, label: L.l('languages.' + code) }))
    const languageOptions = langItems.sort((a, b) => Strings.compare(a.label, b.label)).map(item =>
        <option key={item.code} value={item.code}>{item.label + ' (' + item.code + ')'}</option>)

    const fieldProps = { labelColSpan: 4, fieldColSpan: 8 }
    return (
        <Form onSubmit={handleSubmit}>
            <TextFormItem
                name="name"
                label={L.l('survey.name')}
                normalize={Forms.normalizeInternalName}
                {...fieldProps}
                {...props}
            />
            <SelectFormItem
                name="templateType"
                label={L.l('survey.templateType')}
                options={templateTypeOptions}
                {...fieldProps}
                {...props}
            />
            <SelectFormItem
                name="defaultLanguageCode"
                label={L.l('survey.defaultLanguage')}
                options={languageOptions}
                {...fieldProps}
                {...props}
            />
            <SelectFormItem
                name="userGroupId"
                label={L.l('survey.userGroup')}
                options={userGroupOptions}
                {...fieldProps}
                {...props}

            />
            <Row>
                <Col sm={{ size: 1, offset: 5 }}>
                    <Button color="primary" type="submit" disabled={isSubmitting}>{L.l('general.new')}</Button>
                </Col>
            </Row>
        </Form>
    )
}

const mapStateToProps = state => {
    const { items: userGroups } = state.userGroups

    return {
        userGroups,
    }
}

const mapPropsToValues = props => {
    const defaultPublicGroup = props.userGroups.find(ug => ug.name === User.DEFAULT_PUBLIC_GROUP_NAME)
    return {
        name: '',
        templateType: 'BLANK',
        defaultLanguageCode: 'en',
        userGroupId: defaultPublicGroup ? defaultPublicGroup.id : ''
    }
}

export default compose(
    connect(mapStateToProps, { createNewSurvey }),
    withFormik({
        mapPropsToValues,
        validate: asyncValidate(ServiceFactory.surveyService.validateSurveyCreation.bind(ServiceFactory.surveyService)),
        handleSubmit: (values, { props }) => props.createNewSurvey(values)
    })
)(NewSurveyParametersForm)