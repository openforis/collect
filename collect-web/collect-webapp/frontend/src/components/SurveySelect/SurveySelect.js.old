import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import { fetchSurveySummaries, fetchFullPreferredSurvey, invalidateSurvey } from '../../actions'

class SurveySelect extends Component {
    constructor( props ) {
        super( props );
        this.handleChange = this.handleChange.bind( this );
        this.handleSubmit = this.handleSubmit.bind( this );
    }
    static propTypes = {
        summaries: PropTypes.array.isRequired,
        isFetching: PropTypes.bool.isRequired,
        lastUpdated: PropTypes.number,
        dispatch: PropTypes.func.isRequired
    }
    componentDidMount() {
        const { dispatch } = this.props
        dispatch(fetchSurveySummaries())
    }
    handleChange(event) {
    	const { preferredSurvey, summaries, isFetching, lastUpdated } = this.props
    	//var survey = this.state.surveySummaries[event.target.value];
        //this.setState( { value: event.target.value } );
    	var survey = getItem(summaries, 'id', event.target.value);
    	if (survey) {
    		this.props.dispatch(fetchFullPreferredSurvey(survey.id));
    	}
    	function getItem(arr, prop, value) {
    	    for(var i = 0; i < arr.length; i++) {
    	    	var item = arr[i];
    	        if(item[prop] == value) {
    	            return item;
    	        }
    	    }
    	    return null;
    	}
    }
    handleSubmit(event) {
        event.preventDefault();
    }

    render() {
    	const { preferredSurvey, summaries, isFetching, lastUpdated } = this.props
    	const isEmpty = summaries.length === 0
    	
    	var options = [];
    	options.push(<option key='-1' value='-1'>---Select---</option>);
    	for (var i=0; i < summaries.length; i++) {
    		var summary = summaries[i];
    		options.push(<option key={i} value={summary.id}>{summary.name}</option>);
    	}
    	return (<div>
			{
			isEmpty ? (isFetching ? <span>Loading...</span> : <span>No published surveys found</span>)
            : <div style={{ opacity: isFetching ? 0.5 : 1 }}>
	            <select value={preferredSurvey == null ? '': preferredSurvey.survey.id} 
	        		onChange={this.handleChange}>{options}</select>
              </div>
			}
			</div>
		)
    }
}

const mapStateToProps = state => {
  const { preferredSurvey, surveySummaries } = state
  const {
	    isFetching,
	    lastUpdated,
	    items: summaries
	  } = surveySummaries || {
	    isFetching: true,
	    items: []
	  }
  
  return {
	preferredSurvey,
    summaries,
    isFetching,
    lastUpdated
  }
}
export default connect(mapStateToProps)(SurveySelect)