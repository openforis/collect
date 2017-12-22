import React, { Component } from 'react';
import Constants from 'Constants';
import MaxAvailableSpaceContainer from 'components/MaxAvailableSpaceContainer';

class MapPage extends Component {

  render() {
	  return (
	    <MaxAvailableSpaceContainer>
	      <iframe src={Constants.BASE_URL + 'datamanager/map.html'} 
						title="Open Foris Collect - Map Visualizer"
						width="100%" height="100%" />
	    </MaxAvailableSpaceContainer>
	  );
  }
}

export default MapPage;