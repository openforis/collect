import './SurveyValidationResultDialog.scss'

import React, { Component } from 'react'
import { connect } from 'react-redux'

import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'

import { hideSurveyValidation, publishSurvey } from '../../actions/surveys'

import L from 'utils/Labels'
import { DataGrid } from 'common/components'

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
