import React from 'react';
import Axios from 'axios'

class SurveySelect extends React.Component {
    constructor( props ) {
        super( props );
        this.handleChange = this.handleChange.bind( this );
        this.handleSubmit = this.handleSubmit.bind( this );
        this.state = {
        	value: '',
        	surveySummaries: []
        }
    }
    componentDidMount() {
    	Axios.get('http://localhost:8380/collect/survey/summaries.json')
        	.then((res) => {
        		var summaries = res.data;
        		this.state.surveySummaries = summaries;
                this.forceUpdate();
        	});
    }
    handleChange(event) {
        this.setState( { value: event.target.value } );
    }
    handleSubmit(event) {
        alert( 'The selected survey is: ' + this.state.value );
        event.preventDefault();
    }
    render() {
    	var options = [];
    	for (var i=0; i < this.state.surveySummaries.length; i++) {
    		var summary = this.state.surveySummaries[i];
    		options.push(<option key={i} value={summary.id}>{summary.name}</option>);
    	}
    	return <select value={this.state.value} 
        	onChange={this.handleChange}>{options}</select>
    }
}
export default SurveySelect;