import React, { Component } from 'react';
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';
import RecordService from 'services/RecordService'
import * as Formatters from 'components/datatable/formatters'
import OwnerColumnEditor from './OwnerColumnEditor'

class RecordDataTable extends Component {
	recordService = new RecordService();

  constructor(props) {
    super(props);
    
    this.state = {
      records: [],
      totalSize: 0,
      page: 1,
			recordsPerPage: 25
    }
    this.fetchData = this.fetchData.bind(this)
		this.handlePageChange = this.handlePageChange.bind(this)
		this.handleCellEdit = this.handleCellEdit.bind(this)
    this.handleSizePerPageChange = this.handleSizePerPageChange.bind(this)
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
	  this.recordService.fetchRecordSummaries(surveyId, recordsPerPage, page).then((res) => {
			this.setState({records: res.records, totalSize: res.count, page, recordsPerPage});
	  });
  }

	handleCellEdit(row, fieldName, value) {
		if (fieldName == 'owner') {
			const recordId = row.id
			const newOwner = value.owner
			this.recordService.updateOwner(row, newOwner).then(res => {
				const newRecords = this.state.records.map(r => r.id == recordId ? {...r, owner: newOwner} : r)
				this.setState({records: newRecords})
			})
		}
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
			
			const createOwnerEditor = (onUpdate, props) => (<OwnerColumnEditor onUpdate={ onUpdate } {...props}/>);

		  function rootEntityKeyFormatter(cell, row) {
				var keyIdx = this.name.substring(3);
			  return row.rootEntityKeys[keyIdx];
			}
			
			function usernameFormatter(cell, row) {
				return cell ? cell.username : ''
			}

		  var columns = [];
		  columns.push(<TableHeaderColumn key="id" dataField="id" isKey hidden dataAlign="center">Id</TableHeaderColumn>);
		  
		  var keyAttributes = this.findKeyAttributes(survey);
		  for (var i=0; i < keyAttributes.length; i++) {
	    		var keyAttr = keyAttributes[i];
	    		columns.push(<TableHeaderColumn key={'key' + i} dataField={'key' + i} dataFormat={rootEntityKeyFormatter}
						editable={ false }>{keyAttr.label}</TableHeaderColumn>);
			}

			columns.push(
				<TableHeaderColumn key="owner" dataField="owner" dataFormat={usernameFormatter} 
					customEditor={ { getElement: createOwnerEditor, customEditorParameters: { users: this.props.users } } }
					dataAlign="center" width="150">Owner</TableHeaderColumn>,
				<TableHeaderColumn key="entryComplete" dataField="entryComplete" dataFormat={Formatters.checkedIconFormatter}
					dataAlign="center" width="100" editable={ false }>Entered</TableHeaderColumn>,
				<TableHeaderColumn key="cleansingComplete" dataField="cleansingComplete" dataFormat={Formatters.checkedIconFormatter}
					dataAlign="center" width="100" editable={ false }>Cleansed</TableHeaderColumn>,
				<TableHeaderColumn key="creationDate" dataField="creationDate" dataFormat={Formatters.dateFormatter} 
					dataAlign="center" width="150" editable={ false }>Created</TableHeaderColumn>,
				<TableHeaderColumn key="modifiedDate" dataField="modifiedDate" dataFormat={Formatters.dateFormatter} 
		    	dataAlign="center" width="150" editable={ false }>Modified</TableHeaderColumn>
			);
		  
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
				  selectRow={ {mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue', 
						onSelect: this.props.handleRowSelect,
						selected: this.props.selectedItemIds} }
					options={{ onRowDoubleClick: this.props.handleRowDoubleClick, onCellEdit: this.handleCellEdit}}
					cellEdit={ { mode: 'click', blurToSave: true } }
				  >{columns}
			  </BootstrapTable>
		  );
		  
	  }
  }
  
  findKeyAttributes(survey) {
	  let keyAttributes = [];
	  let queue = [];
	  let rootEntity = survey.schema.rootEntities[0];
	  for (let i=0; i < rootEntity.children.length; i++) {
		  queue.push(rootEntity.children[i]);
	  }
	  while (queue.length > 0) {
		let node = queue.shift();
		switch(node.type) {
		case 'ENTITY':
			if (! node.multiple) {
				for (let i=0; i < node.children.length; i++) {
					let child = node.children[i];
					queue.push(child);
				}
			}
			break;
		default:
			  if (node.key) {
				  keyAttributes.push(node);
			  }
		  }
	  }
	  return keyAttributes;
  }
  
  visitNodes(survey, visitFunction) {
	  let stack = [];
	  stack.push(survey.schema.rootEntities[0]);
	  while (stack.length > 0) {
		let node = stack.pop();
		visitFunction(node);
		switch(node.type) {
		case 'ENTITY':
			for (let i=0; i < node.children.length; i++) {
			let child = node.children[i];
			stack.push(child);
			}
			break;
		default:
		}
	  }
  }
}

const mapStateToProps = state => {
	return {
		survey: state.preferredSurvey ? state.preferredSurvey.survey : null,
		users: state.users ? state.users.users: null
	}
}
  
export default connect(mapStateToProps)(RecordDataTable)