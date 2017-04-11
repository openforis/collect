import React from 'react';

class SurveySelect extends React.Component {
    constructor( props ) {
        super( props );
        this.handleChange = this.handleChange.bind( this );
        this.handleSubmit = this.handleSubmit.bind( this );
        this.state = {
        	value: '',
    		options: []
        }
    }
    componentDidMount() {
        $.ajax({
            url: 'http://localhost:8380/collect/surveys/summary.json',
            success: this.propsLoadSuccessHandler
        })
    }
    propsLoadSuccessHandler(data) {
        for (var i = 0; i < data.length; i++) {
            var option = data[i];
            this.state.options.push(
                <option key={i} value={option.id}>{option.name}</option>
            );
        }
        this.forceUpdate();
    }
    handleChange(event) {
        this.setState( { value: event.target.value } );
    }
    handleSubmit(event) {
        alert( 'The selected survey is: ' + this.state.value );
        event.preventDefault();
    }
    render() {
        return <select value={this.state.value} 
        	onChange={this.handleChange}>{this.state.options}</select>
    }
}
export default SurveySelect;