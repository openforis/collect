import './SurveyValidationResultDialog.scss'

import React, { Component } from 'react'
import { connect } from 'react-redux'

import Button from '@material-ui/core/Button'
import Dialog from '@material-ui/core/Dialog'
import DialogActions from '@material-ui/core/DialogActions'
import DialogContent from '@material-ui/core/DialogContent'
import DialogTitle from '@material-ui/core/DialogTitle'

import { hideSurveyValidation, publishSurvey } from '../../actions/surveys'

import L from 'utils/Labels'
import { DataGrid } from 'common/components/DataGrid'

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
      <Dialog open={true} maxWidth="md" onClose={this.handleClose}>
        <DialogTitle>
          {L.l(
            `survey.publish.confirmDialog.${hasErrors ? 'errorsFoundTitle' : 'publishWithWarningsTitle'}`,
            validationSurvey.name
          )}
        </DialogTitle>
        <DialogContent>
          <DataGrid
            className="survey-validation-result-data-grid"
            checkboxSelection={false}
            columns={[
              { field: 'flag', width: 120, sortable: true, headerName: 'validation.severity' },
              { field: 'path', width: 350, sortable: true, headerName: 'validation.path' },
              {
                field: 'message',
                width: 350,
                sortable: true,
                headerName: 'validation.message',
                valueFormatter: ({ row }) => L.l(row.messageKey, row.messageArgs),
              },
            ]}
            disableSelectionOnClick
            getRowId={(row) => row.path}
            rows={validationResult.results}
          />
        </DialogContent>
        <DialogActions>
          {!hasErrors && (
            <Button variant="contained" color="secondary" onClick={this.handleConfirm}>
              {L.l('survey.publish')}
            </Button>
          )}{' '}
          <Button onClick={this.handleClose}>{L.l('general.close')}</Button>
        </DialogActions>
      </Dialog>
    )
  }
}

const mapStateToProps = (state) => {
  const surveysListState = state.surveyDesigner.surveysList

  const { validationResult, validationSurvey } = surveysListState

  return {
    validationResult,
    validationSurvey,
  }
}

export default connect(mapStateToProps, { hideSurveyValidation, publishSurvey })(SurveyValidationResultDialog)
