import React, {Component} from 'react';
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import Axios from 'axios'

class RecordDataTable extends Component {
  constructor(props) {
    super(props);

    this.state = {
      items: [],
      totalSize: 0,
      page: 1,
      sizePerPage: 20,
    };
    this.fetchData = this.fetchData.bind(this);
    this.handlePageChange = this.handlePageChange.bind(this);
    this.handleSizePerPageChange = this.handleSizePerPageChange.bind(this);
  }

  componentDidMount() {
    this.fetchData();
  }

  fetchData(page = this.state.page, sizePerPage = this.state.sizePerPage) {
	  var surveyId = 74;
	  Axios.get('http://localhost:8380/collect/survey/' + surveyId + '/data/records/summary.json', {
	    	params: {
	    		maxNumberOfRows: sizePerPage,
	    		offset: (page - 1) * sizePerPage,
	    		//sorting: state.sorting,
	    		//filtering: state.filtering
	    	}
	  }).then((res) => {
        this.setState({items: res.data.records, totalSize: res.data.count, page, sizePerPage});
      });
  }

  handlePageChange(page, sizePerPage) {
    this.fetchData(page, sizePerPage);
  }

  handleSizePerPageChange(sizePerPage) {
    // When changing the size per page always navigating to the first page
    this.fetchData(1, sizePerPage);
  }

  render() {
    const options = {
      onPageChange: this.handlePageChange,
      onSizePerPageList: this.handleSizePerPageChange,
      page: this.state.page,
      sizePerPage: this.state.sizePerPage,
    };

    return (
       <BootstrapTable
        data={this.state.items}
        options={options}
        fetchInfo={{dataTotalSize: this.state.totalSize}}
        remote
        pagination
        striped
        hover
        condensed
      >
        <TableHeaderColumn dataField="id" isKey dataAlign="center">Id</TableHeaderColumn>
      </BootstrapTable>
    );
  }
}
export default RecordDataTable;