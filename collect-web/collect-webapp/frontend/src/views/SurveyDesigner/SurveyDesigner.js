import React, { Component } from 'react';
import Constants from '../../utils/Constants';

class SurveyDesigner extends Component {
  render() {
	  return (
	    <div>
	      <iframe src={Constants.SERVICES_URL + 'designer.htm'} width="100%" height="500px" />
	    </div>
	  );
  }
}

export default SurveyDesigner;