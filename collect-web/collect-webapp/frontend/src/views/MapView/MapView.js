import React, { Component } from 'react';
import Constants from '../../utils/Constants';
import MaxAvailableSpaceContainer from '../../containers/MaxAvailableSpaceContainer';

class MapView extends Component {

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

export default MapView;