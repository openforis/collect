import React, { Component } from 'react';
import Constants from 'utils/Constants';
import MaxAvailableSpaceContainer from 'components/MaxAvailableSpaceContainer';

class SurveyDesignerPage extends Component {
  render() {
	  return (
	    <MaxAvailableSpaceContainer>
	      <iframe src={Constants.BASE_URL + 'designer.htm'} 
						title="Open Foris Collect - Survey Designer"
						width="100%" height="100%" />
	    </MaxAvailableSpaceContainer>
	  );
  }
}

export default SurveyDesignerPage;