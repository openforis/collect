import React, { Component } from 'react';
import RecordDataTable from './RecordDataTable';
//import RecordEditor from './RecordEditor';

class DataManagement extends Component {
  render() {
	  return (
	    <div>
	      <div className="row">
	        <div className="col-lg-12">
	          <RecordDataTable />
	        </div>
	      </div>
	    </div>
	  );
  }
}

export default DataManagement;