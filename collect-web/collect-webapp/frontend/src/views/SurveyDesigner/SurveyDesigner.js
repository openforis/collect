import React, { Component } from 'react';

class SurveyDesigner extends Component {
  render() {
	  return (
	    <div>
	      <iframe src="http://localhost:8480/collect/designer.htm" width="100%" height="500px" />
	    </div>
	  );
  }
}

export default SurveyDesigner;