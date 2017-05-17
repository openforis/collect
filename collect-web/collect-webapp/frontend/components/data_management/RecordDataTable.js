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
      recordsPerPage: 25,
    };
    this.fetchData = this.fetchData.bind(this);
    this.handlePageChange = this.handlePageChange.bind(this);
    this.handleSizePerPageChange = this.handleSizePerPageChange.bind(this);
  }

  static propTypes = {
      survey: PropTypes.object
  }
  
  componentDidMount() {
	  this.fetchData(1);
  }
  
  componentWillReceiveProps(nextProps, nextState) {
	  this.fetchData(1, this.state.recordsPerPage, nextProps.survey.id);
  }

  handlePageChange(page, recordsPerPage) {
    this.fetchData(page, recordsPerPage);
  }

  handleSizePerPageChange(recordsPerPage) {
    // When changing the size per page always navigating to the first page
    this.fetchData(1, recordsPerPage);
  }

  fetchData(page = this.state.page, recordsPerPage = this.state.recordsPerPage, 
		  surveyId = this.props.survey == null ? null: this.props.survey.id) {
	  if (surveyId == null)
		  return;
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

  render() {
	  const noSurveySelected = this.props.survey == null;
	  if (noSurveySelected) {
		  return <div>Please select a survey first</div>
	  } else {
		  var survey = this.props.survey;
		  
		  const options = {
			  onPageChange: this.handlePageChange,
			  onSizePerPageList: this.handleSizePerPageChange,
			  page: this.state.page,
			  sizePerPage: this.state.recordsPerPage,
		  };
		  
		  function dateFormatter(cell, row) {
			  return Moment(new Date(cell)).format('DD/MM/YYYY');
		  }
		  function rootEntityKeyFormatter(cell, row) {
			  var keyIdx = this.name.substring(3);
			  return row.rootEntityKeys[keyIdx];
		  }
		  var columns = [];
		  columns.push(<TableHeaderColumn key="id" dataField="id" isKey hidden dataAlign="center">Id</TableHeaderColumn>);
		  
		  var keyAttributes = this.findKeyAttributes(survey);
		  for (var i=0; i < keyAttributes.length; i++) {
	    		var keyAttr = keyAttributes[i];
	    		columns.push(<TableHeaderColumn key={'key' + i} dataField={'key' + i} dataFormat={rootEntityKeyFormatter}>{keyAttr.label}</TableHeaderColumn>);
		  }
		  columns.push(<TableHeaderColumn key="creationDate" dataField="creationDate" dataFormat={dateFormatter} 
		    dataAlign="center" width="150">Created</TableHeaderColumn>);
		  columns.push(<TableHeaderColumn key="modifiedDate" dataField="modifiedDate" dataFormat={dateFormatter} 
		    dataAlign="center" width="150">Modified</TableHeaderColumn>);
		  
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
				  selectRow={ {mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue'} }
				  >{columns}
			  </BootstrapTable>
		  );
		  
	  }
  }
  
  findKeyAttributes(survey) {
	  var keyAttributes = [];
	  var queue = [];
	  var rootEntity = survey.schema.rootEntities[0];
	  for (var i=0; i < rootEntity.children.length; i++) {
		  queue.push(rootEntity.children[i]);
	  }
	  while (queue.length > 0) {
		  var node = queue.shift();
		  switch(node.type) {
		  case 'ENTITY':
			  if (! node.multiple) {
				  for (var i=0; i < node.children.length; i++) {
					  var child = node.children[i];
					  queue.push(child);
				  }
			  }
			  break;
		  case 'ATTRIBUTE':
			  if (node.key) {
				  keyAttributes.push(node);
			  }
			  break;
		  }
	  }
	  return keyAttributes;
  }
  
  visitNodes(survey, visitFunction) {
	  var stack = [];
	  stack.push(survey.schema.rootEntities[0]);
	  while (stack.length > 0) {
		  var node = stack.pop();
		  visitFunction(node);
		  switch(node.type) {
		  case 'ENTITY':
			  for (var i=0; i < node.children.length; i++) {
			    var child = node.children[i];
			    stack.push(child);
			  }
			  break;
		  }
	  }
  }
}

const mapStateToProps = state => {
  const { preferredSurvey } = state
  
  const {
	    isFetching,
	    lastUpdated,
	    survey
	  } = preferredSurvey || {
	    isFetching: true,
	    survey: null
	  }
  return {
	  isFetching,
	  lastUpdated,
	  survey
  }
}
export default connect(mapStateToProps)(RecordDataTable)
