import React, { Component } from 'react';
import Constants from '../../utils/Constants';

class DataCleansing extends Component {
  render() {
	  return (
	    <div>
	      <iframe src={Constants.SERVICES_URL + 'datacleansing/main.html'} width="100%" height="500px" />
	    </div>
	  );
  }
}

export default DataCleansing;