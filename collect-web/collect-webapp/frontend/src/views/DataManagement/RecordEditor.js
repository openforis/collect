import React, { Component, PropTypes } from 'react'

import TabSet from './RecordDetails/TabSet'
import { Record } from '../../model/Record'
import RecordService from '../../services/RecordService'
import SurveyService from '../../services/SurveyService'

export default class RecordEditor extends Component {

  recordService = new RecordService()
  surveyService = new SurveyService()

  constructor(props) {
    super(props);

    this.state = {
      record: null
    }
  }

  static propTypes = {
      survey: PropTypes.object
  }
  
  componentDidMount() {
    let idParam = this.props.match.params.id;
    let recordId = parseInt(idParam);

    this.recordService.fetchSurveyId(recordId).then(res => {
      let surveyId = parseInt(res);
      this.surveyService.fetchById(surveyId).then(survey => {
        this.recordService.fetchById(survey, recordId).then(record => {
          this.setState({...this.state, record: record});
        });
      })
    })

  }
  
  componentWillReceiveProps(nextProps, nextState) {
    
  }

  render() {
    let record = this.state.record
    if (! record) {
      return <div>Loading...</div>
    }
    let survey = record.survey
    let uiConf = survey.uiConfiguration
    let tabSetDefinition = uiConf.getTabSetByRootEntityDefinitionId(record.rootEntity.definition.id)

	  return (
		  <TabSet tabSetDef={tabSetDefinition} record={record} />
	  );
  }
}
