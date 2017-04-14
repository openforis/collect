import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import Axios from 'axios'
import Moment from 'moment';

class RecordDataTable extends Component {
  constructor(props) {
    super(props);
    
    this.state = {
      records: [],
      totalSize: 0,
      page: 1,
      recordsPerPage: 20,
    };
    this.fetchData = this.fetchData.bind(this);
    this.handlePageChange = this.handlePageChange.bind(this);
    this.handleSizePerPageChange = this.handleSizePerPageChange.bind(this);
  }

  static propTypes = {
      preferredSurvey: PropTypes.object
  }
  
  componentDidMount() {
	  fetchData(1, this.state.recordsPerPage);
  }

  fetchData(page = this.state.page, recordsPerPage = this.state.recordsPerPage) {
	  console.log("loading records, state=" + this.props)
	  var surveyId = this.props.survey;
	  Axios.get('http://localhost:8380/collect/survey/' + surveyId + '/data/records/summary.json', {
	    	params: {
	    		maxNumberOfRows: recordsPerPage,
	    		offset: (page - 1) * recordsPerPage,
	    		//sorting: state.sorting,
	    		//filtering: state.filtering
	    	}
	  }).then((res) => {
        this.setState({records: res.data.records, totalSize: res.data.count, page, recordsPerPage});
      });
  }

  handlePageChange(page, recordsPerPage) {
    this.fetchData(page, recordsPerPage);
  }

  handleSizePerPageChange(recordsPerPage) {
    // When changing the size per page always navigating to the first page
    this.fetchData(1, recordsPerPage);
  }

  render() {
    const options = {
      onPageChange: this.handlePageChange,
      onSizePerPageList: this.handleSizePerPageChange,
      page: this.state.page,
      sizePerPage: this.state.sizePerPage,
    };
    
    function dateFormatter(cell, row) {
    	return Moment(new Date(cell)).format('DD/MM/YYYY');
    }
    
    return (
       <BootstrapTable
        data={this.state.records}
        options={options}
        fetchInfo={{dataTotalSize: this.state.totalSize}}
        remote
        pagination
        striped
        hover
        condensed
      >
        <TableHeaderColumn dataField="id" isKey dataAlign="center">Id</TableHeaderColumn>
        <TableHeaderColumn dataField="creationDate" dataFormat={dateFormatter} dataAlign="center">Created</TableHeaderColumn>
        <TableHeaderColumn dataField="modifiedDate" dataFormat={dateFormatter} dataAlign="center">Modified</TableHeaderColumn>
      </BootstrapTable>
    );
  }
}

const mapStateToProps = state => {
  const { preferredSurvey } = state
  return {
	survey: preferredSurvey
  }
}
export default connect(mapStateToProps)(RecordDataTable)
