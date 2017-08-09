import React, { Component, PropTypes } from 'react'
import Axios from 'axios'

class RecordEditor extends Component {
  constructor(props) {
    super(props);
  }

  static propTypes = {
      record: PropTypes.object
  }
  
  componentDidMount() {
  }
  
  componentWillReceiveProps(nextProps, nextState) {
  }

  render() {
	  var record = this.props.record;
	  var survey = record.survey;
	  
	  return (
		<div>Record {record.id}</div>
	  );
  }
}

export default RecordEditor
