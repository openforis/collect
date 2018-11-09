import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { UncontrolledDropdown, DropdownToggle, DropdownMenu, DropdownItem } from 'reactstrap';

import { selectActiveSurvey } from 'actions';
import Arrays from 'utils/Arrays';
import L from 'utils/Labels';

class SurveySelect extends Component {
    constructor( props ) {
      super( props );

			this.state = {
				dropdownOpen: false
			}

			this.handleSurveySelect = this.handleSurveySelect.bind(this)
		}

		static propTypes = {
			summaries: PropTypes.array.isRequired,
			selectedSurvey: PropTypes.object,
			isFetchingSummaries: PropTypes.bool.isRequired,
			isFetchingActiveSurvey: PropTypes.bool.isRequired,
			lastUpdated: PropTypes.number,
			dispatch: PropTypes.func.isRequired
    }
	
    handleSurveySelect(surveyId) {
			const { summaries, selectedSurvey } = this.props
			if (!selectedSurvey || selectedSurvey.id !== surveyId) {
				var survey = summaries.find(summary => summary.id === surveyId);
				if (survey) {
					this.props.dispatch(selectActiveSurvey(survey));
				}
			}
		}

		render() {
			const { selectedSurvey, summaries } = this.props
			const publishedSurveys = summaries.filter(s => s.published && !s.temporary)
			const isEmpty = publishedSurveys.length === 0

			if (isEmpty) {
				return <span className="survey-selector">{L.l('survey.noPublishedSurveysFound')}</span>
			} else {
				//sort surveys by name
				Arrays.sort(publishedSurveys, 'name')
				const selectedSurveyName = selectedSurvey ? selectedSurvey.name : L.l('survey.selectPublishedSurveyDropdownHeading')
				const dropdownItems = publishedSurveys.map(s => 
					<DropdownItem key={s.id} className="survey-item" onClick={this.handleSurveySelect.bind(this, s.id)}>
						<div className="survey-name">{s.name}</div><div className="survey-project-name">{s.projectName}</div>
					</DropdownItem>)
				
				return <UncontrolledDropdown className="survey-selector">
								<DropdownToggle className="survey-dropdown-toggle" caret>{selectedSurveyName}</DropdownToggle>
								<DropdownMenu>
									{dropdownItems}
								</DropdownMenu>
							</UncontrolledDropdown>
				}
		}
}

const mapStateToProps = state => {
	const { activeSurvey } = state
	const { surveysList } = state.surveyDesigner
  const {
	  	isFetching: isFetchingActiveSurvey,
	  	lastUpdated: lastActiveSurveyUpdate,
	    survey: selectedSurvey
	  } = activeSurvey || {
		  isFetching: true,
		  survey: null
	  }
  const {
	  	isFetching: isFetchingSummaries,
	    lastUpdated: lastSummariesUpdate,
	    items: summaries
	  } = surveysList || {
		  isFetching: true,
		  items: []
	  }
  
  return {
    isFetchingSummaries,
    summaries,
    lastSummariesUpdate,
    isFetchingActiveSurvey,
    selectedSurvey,
    lastActiveSurveyUpdate
  }
}
export default connect(mapStateToProps)(SurveySelect)