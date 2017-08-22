import React, { Component } from 'react';
import Constants from '../../utils/Constants';

class Map extends Component {
  render() {
	  return (
	    <div>
	      <iframe src={Constants.SERVICES_URL + 'datamanager/map.html'} 
						title="Open Foris Collect - Map Visualizer"
						width="100%" height="500px" />
	    </div>
	  );
  }
}

export default Map;