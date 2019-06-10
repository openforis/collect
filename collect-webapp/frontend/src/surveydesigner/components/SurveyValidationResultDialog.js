import React, { Component } from 'react'
import { connect } from 'react-redux'

import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogTitle from '@material-ui/core/DialogTitle'

import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table'

import { hideSurveyValidation, publishSurvey } from '../../actions/surveys'

import L from 'utils/Labels'

const messageFormatter = (cell, row) => L.l(row.messageKey, row.messageArgs)

class SurveyValidationResultDialog extends Component {

    constructor(props) {
        super(props)

        this.handleConfirm = this.handleConfirm.bind(this)
        this.handleClose = this.handleClose.bind(this)
    }

    handleConfirm() {
        const { validationSurvey, publishSurvey } = this.props
        publishSurvey(validationSurvey, true)
    }

    handleClose() {
        this.props.hideSurveyValidation()
    }

    render() {
        const { validationResult, validationSurvey } = this.props
        const hasErrors = validationResult.errors.length > 0
        return (
            <Dialog open={true}
                maxWidth="md"
                onClose={this.handleClose}>
                <DialogTitle>{L.l(`survey.publish.confirmDialog.${hasErrors ? 'errorsFoundTitle' : 'publishWithWarningsTitle'}`,
                    validationSurvey.name)}</DialogTitle>
                <DialogContent>
                    <BootstrapTable height={400}
                        data={validationResult.results}>
                        <TableHeaderColumn key="flag" dataField="flag"
                            width="100" dataSort>{L.l('validation.severity')}</TableHeaderColumn>
                        <TableHeaderColumn key="path" isKey dataField="path"
                            width="300" dataSort>{L.l('validation.path')}</TableHeaderColumn>
                        <TableHeaderColumn key="message" dataField="message"
                            width="500" dataSort dataFormat={messageFormatter}>{L.l('validation.message')}</TableHeaderColumn>
                    </BootstrapTable>
                </DialogContent>
                <DialogActions>
                    {!hasErrors &&
                        <Button variant="contained" color="secondary"
                            onClick={this.handleConfirm}>{L.l('survey.publish')}</Button>}
                    {' '}
                    <Button onClick={this.handleClose}>{L.l('general.close')}</Button>
                </DialogActions>
            </Dialog>
        )
    }
}

const mapStateToProps = state => {
    const surveysListState = state.surveyDesigner.surveysList

    const { validationResult, validationSurvey } = surveysListState

    return {
        validationResult,
        validationSurvey
    }
}

export default connect(mapStateToProps, { hideSurveyValidation, publishSurvey })(SurveyValidationResultDialog)

