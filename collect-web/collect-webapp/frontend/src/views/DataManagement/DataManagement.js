import React, { Component } from 'react';
import SelectedSurveyRecordDataTable from './SelectedSurveyRecordDataTable';
import RecordEditor from './RecordEditor';

class DataManagement extends Component {
  render() {
	  return (
	    <div>
	      <div className="row">
	        <div className="col-lg-12">
	          <SelectedSurveyRecordDataTable />
	        </div>
	      </div>
	    </div>
	  );
  }
}

export default DataManagement;