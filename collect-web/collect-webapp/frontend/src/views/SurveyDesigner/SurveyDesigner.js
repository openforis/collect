import React, { Component } from 'react';
import Constants from '../../utils/Constants';

class SurveyDesigner extends Component {
  render() {
	  return (
	    <div>
	      <iframe src={Constants.BASE_URL + 'designer.htm'} 
						title="Open Foris Collect - Survey Designer"
						width="100%" height="500px" />
	    </div>
	  );
  }
}

export default SurveyDesigner;