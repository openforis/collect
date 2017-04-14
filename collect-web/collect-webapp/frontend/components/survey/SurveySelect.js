import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import { fetchSurveySummaries, selectPreferredSurvey, invalidateSurvey } from '../../actions'

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
    	//var survey = this.state.surveySummaries[event.target.value];
    	this.props.dispatch(selectPreferredSurvey(event.target.value));
        //this.setState( { value: event.target.value } );
    }
    handleSubmit(event) {
        alert( 'The selected survey is: ' + this.state.value );
        event.preventDefault();
    }
    render() {
    	const { preferredSurvey, summaries, isFetching, lastUpdated } = this.props
    	const isEmpty = summaries.length === 0
    	
    	var options = [];
    	for (var i=0; i < summaries.length; i++) {
    		var summary = summaries[i];
    		options.push(<option key={i} value={summary.id}>{summary.name}</option>);
    	}
    	return (<div>
			{
			isEmpty ? (isFetching ? <span>Loading...</span> : <span>No published surveys found</span>)
            : <div style={{ opacity: isFetching ? 0.5 : 1 }}>
	            <select value={preferredSurvey == null ? '': preferredSurvey.id} 
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