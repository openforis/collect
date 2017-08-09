import { connect } from 'react-redux'
import RecordDataTable from './RecordDataTable'

const mapStateToProps = state => {
  return {
    survey: state.preferredSurvey ? state.preferredSurvey.survey : null
  }
}

const SelectedSurveyRecordDataTable = connect(
  mapStateToProps,
//  mapDispatchToProps
)(RecordDataTable)

export default SelectedSurveyRecordDataTable