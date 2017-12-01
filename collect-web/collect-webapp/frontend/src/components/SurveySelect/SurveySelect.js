import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { UncontrolledDropdown, DropdownToggle, DropdownMenu, DropdownItem, FormControl } from 'reactstrap';

import { selectPreferredSurvey } from '../../actions'
import Arrays from 'utils/Arrays'

class SurveySelect extends Component {
    constructor( props ) {
        super( props );
		this.handleSurveySelect = this.handleSurveySelect.bind( this );
		
		this.state = {
			dropdownOpen: false,
			selectedSurveyName: '---Select a survey---'
		}
    }
    static propTypes = {
        summaries: PropTypes.array.isRequired,
        selectedSurvey: PropTypes.object,
		isFetchingSummaries: PropTypes.bool.isRequired,
		isFetchingPreferredSurvey: PropTypes.bool.isRequired,
        lastUpdated: PropTypes.number,
        dispatch: PropTypes.func.isRequired
    }
    componentDidMount() {
        //const { dispatch } = this.props
        //dispatch(fetchSurveySummaries())
	}
	
    handleSurveySelect(surveyId) {
    	const { summaries } = this.props
    	var survey = summaries.find(summary => summary.id === surveyId);
    	if (survey) {
			this.setState({selectedSurveyName: survey.name})
    		this.props.dispatch(selectPreferredSurvey(survey));
    	}
	}
	
    render() {
		const { selectedSurvey, summaries } = this.props
		const publishedSurveys = summaries.filter(s => s.published && !s.temporary)
    	const isEmpty = publishedSurveys.length === 0
		Arrays.sort(publishedSurveys, 'name')
    	
    	const dropdownItems = publishedSurveys.map(s => 
			<DropdownItem key={s.id} className="survey-item" onClick={this.handleSurveySelect.bind(this, s.id)}>
				<div className="survey-name">{s.name}</div><div className="survey-project-name">{s.projectName}</div>
			</DropdownItem>)
		
    	return (<div>
			{
			isEmpty ? <span>No published surveys found</span>
            : <div style={{ width: '300px' }}>
				<UncontrolledDropdown>
					<DropdownToggle className="survey-dropdown-toggle" caret>{this.state.selectedSurveyName}</DropdownToggle>
					<DropdownMenu>
						{dropdownItems}
					</DropdownMenu>
				</UncontrolledDropdown>
              </div>
			}
			</div>
		)
    }
}

const mapStateToProps = state => {
  const { preferredSurvey, surveySummaries } = state
  const {
	  	isFetching: isFetchingPreferredSurvey,
	  	lastUpdated: lastPreferredSurveyUpdate,
	    survey: selectedSurvey
	  } = preferredSurvey || {
		  isFetching: true,
		  survey: null
	  }
  const {
	  	isFetching: isFetchingSummaries,
	    lastUpdated: lastSummariesUpdate,
	    items: summaries
	  } = surveySummaries || {
		  isFetching: true,
		  items: []
	  }
  
  return {
    isFetchingSummaries,
    summaries,
    lastSummariesUpdate,
    isFetchingPreferredSurvey,
    selectedSurvey,
    lastPreferredSurveyUpdate
  }
}
export default connect(mapStateToProps)(SurveySelect)