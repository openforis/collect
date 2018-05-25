import React, { Component } from 'react'
import { connect } from 'react-redux'
import TreeSelect, { SHOW_PARENT } from 'rc-tree-select'
import 'rc-tree-select/assets/index.css'
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd'
import Button from '@material-ui/core/Button'
import ExpansionPanel from '@material-ui/core/ExpansionPanel'
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary'
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails'
import FormHelperText from '@material-ui/core/FormHelperText'
import FormControl from '@material-ui/core/FormControl'
import InputLabel from '@material-ui/core/InputLabel'
import MenuItem from '@material-ui/core/MenuItem'
import Select from '@material-ui/core/Select'
import Typography from '@material-ui/core/Typography'
import ExpandMoreIcon from '@material-ui/icons/ExpandMore'

import QueryResultTable from './QueryResultTable'
import DataQueryFilterDialog from './DataQueryFilterDialog'
import { EntityDefinition } from 'model/Survey'
import { RDBQuery, QueryComponent, FilterCondition } from 'model/DataQuery'
import Workflow from 'model/Workflow'
import ServiceFactory from 'services/ServiceFactory'
import Dialogs from 'components/Dialogs'
import L from 'utils/Labels'
import Arrays from 'utils/Arrays'
import MaxAvailableSpaceContainer from 'components/MaxAvailableSpaceContainer'

class DataViewPage extends Component {

	static initialState = {
		schemaTreeData: null,
		selectedEntity: null,
		selectedEntityTreeNodes: [],
		step: Workflow.STEPS.entry.code,
		availableAttributes: [],
		selectedColumns: [],
		selectedFilter: [],
		queryResult: null,
		queryResultPage: 1,
		queryResultRecordsPerPage: 10,
		queryResultTotalRecords: 0,
		queryPanelExpanded: true,
		queryFilterDialogOpen: false,
		queryFilterDialogQueryComponent: null
	}

	constructor(props) {
		super()
		this.state = DataViewPage.initialState

		this.onEntityChange = this.onEntityChange.bind(this)
		this.onDragEnd = this.onDragEnd.bind(this)
		this.addAttributeToSelectedColumns = this.addAttributeToSelectedColumns.bind(this)
		this.handleQueryButtonClick = this.handleQueryButtonClick.bind(this)
		this.handleResultTableChange = this.handleResultTableChange.bind(this)
		this.handleQueryFilterDialogClose = this.handleQueryFilterDialogClose.bind(this)
		this.handleQueryFilterDialogOk = this.handleQueryFilterDialogOk.bind(this)
	}

	static getDerivedStateFromProps(nextProps, prevState) {
		const survey = nextProps.survey
		if (survey) {
			const schemaTreeData = DropDownTreeData.fromSurvey(survey)
			const rootEntity = survey.schema.firstRootEntityDefinition
			return Object.assign({}, DataViewPage.initialState, {
				schemaTreeData: schemaTreeData,
				selectedEntity: rootEntity,
				selectedEntityTreeNodes: [schemaTreeData.root],
				availableAttributes: DataViewPage.getAttributes(rootEntity)
			})
		} else {
			return null
		}
	}

	static getAttributes(entity) {
		const result = []
		const stack = []
		stack.push(entity)
		while (stack.length !== 0) {
			const currentEntity = stack.pop()
			currentEntity.children.forEach(n => {
				if (! (n instanceof EntityDefinition)) {
					result.push(n)
				}
			})
			if (currentEntity.parent !== null) {
				stack.push(currentEntity.parent)
			}
		}
		return result
	}

	onEntityChange(entityNode) {
		const { survey } = this.props
		const selectedEntityTreeNodes = []
		let selectedEntity = null
		let availableAttributes = null

		if (entityNode) {
			selectedEntityTreeNodes.push(entityNode)
			selectedEntity = survey.schema.getDefinitionById(entityNode.value)
			availableAttributes = DataViewPage.getAttributes(selectedEntity)
		}

		this.setState(Object.assign({}, DataViewPage.initialState, {
			schemaTreeData: this.state.schemaTreeData,
			selectedEntity: selectedEntity,
			availableAttributes: availableAttributes,
			selectedEntityTreeNodes: selectedEntityTreeNodes
		}))
	}

	addAttributeToSelectedColumns(attributeDef) {
		const newAvailableAttributes = Array.from(this.state.availableAttributes)
		const attrDefIdx = newAvailableAttributes.indexOf(attributeDef)
		const newSelectedColumns = Array.from(this.state.selectedColumns)
		
		newAvailableAttributes.splice(attrDefIdx, 1)
		newSelectedColumns.push(new QueryComponent(attributeDef.id, attributeDef))

		this.setState({
			availableAttributes: newAvailableAttributes,
			selectedColumns: newSelectedColumns,
			queryResult: null,
			queryResultPage: 1,
			queryResultRecordsPerPage: 10,
			queryResultTotalRecords: 0
		})
	}

	onDragEnd(result) {
		// dropped outside the list
		if (!result.destination) {
		  return
		}
		const newAvailableAttributes = Array.from(this.state.availableAttributes)
		const newSelectedColumns = Array.from(this.state.selectedColumns)
		const newSelectedFilter = Array.from(this.state.selectedFilter)

		//dropped inside the same list: reorder
		if (result.source.droppableId === result.destination.droppableId) {
			const reorder = (list, startIndex, endIndex) => {
				const [removed] = list.splice(startIndex, 1)
				list.splice(endIndex, 0, removed)
			}
			const list = result.source.droppableId === 'selectedColumns' ? newSelectedColumns : newSelectedFilter
			reorder(list, result.source.index, result.destination.index)
		} else if (result.source.droppableId === 'selectedAttributes') {
			//drag from Attributes
			const [attributeDef] = newAvailableAttributes.splice(result.source.index, 1)
			//drop
			const newDestList = result.destination.droppableId === 'selectedColumns' ? newSelectedColumns: newSelectedFilter
			newDestList.push(new QueryComponent(attributeDef.id, attributeDef))
		} else {
			//drag from Columns/Filter, drop into Filter/Columns
			const newSourceList =  result.source.droppableId === 'selectedColumns' ? newSelectedColumns : newSelectedFilter
			const newDestList = result.destination.droppableId === 'selectedColumns' ? newSelectedColumns: newSelectedFilter
			//remove from source list
			const [removed] = newSourceList.splice(result.source.index, 1)
			//add to dest list
			newDestList.push(removed)
		}
		this.setState({
			availableAttributes: newAvailableAttributes,
			selectedColumns: newSelectedColumns,
			selectedFilter: newSelectedFilter,
			queryResult: null,
			queryResultPage: 1,
			queryResultRecordsPerPage: 10,
			queryResultTotalRecords: 0
		})
	  }

	handleColumnSelectionItemClose(event, attributeDefinitionId) {
		event.stopPropagation()
		const item = this.state.selectedColumns.find(c => c.id === attributeDefinitionId)
		const newAvailableAttributes = Array.from(this.state.availableAttributes)
		newAvailableAttributes.push(item.attributeDefinition)
		this.setState({
			availableAttributes: newAvailableAttributes,
			selectedColumns: Arrays.removeItem(this.state.selectedColumns, item),
			queryResult: null,
			queryResultPage: 1,
			queryResultRecordsPerPage: 10,
			queryResultTotalRecords: 0
		})
	}

	handleFilterSelectionItemClose(attributeDefinitionId) {
		const item = this.state.selectedFilter.find(c => c.id === attributeDefinitionId)
		const newAvailableAttributes = Array.from(this.state.availableAttributes)
		newAvailableAttributes.push(item.attributeDefinition)
		this.setState({
			availableAttributes: newAvailableAttributes,
			selectedFilter: Arrays.removeItem(this.state.selectedFilter, item)
		})
	}

	handleQueryButtonClick() {
		const { survey } = this.props
		const { selectedEntity, selectedColumns, selectedFilter, queryResultPage, queryResultRecordsPerPage} = this.state

		if (selectedColumns.length === 0) {
			Dialogs.alert(L.l('dataView.query.noColumnsSelectedAlert.title'), L.l('dataView.query.noColumnsSelectedAlert.message'))
		} else {
			const query = new RDBQuery()
			query.surveyName = survey.name
			query.recordStep = 'ENTRY'
			query.contextEntityDefinitionId = selectedEntity.id
			query.columns = selectedColumns.map(c => {return {
				attributeDefinitionId: c.attributeDefinition.id,
				filterCondition: c.filterCondition ? c.filterCondition : null
			}})
			query.filter = selectedFilter.map(f => {return {
				attributeDefinitionId: f.attributeDefinition.id,
				filterCondition: f.filterCondition ? f.filterCondition : null
			}})
			query.page = queryResultPage
			query.recordsPerPage = queryResultRecordsPerPage
			query.sortBy = []
			ServiceFactory.queryService.getQueryResult(survey.id, query).then(result => this.setState({
				queryResult: result
			}))
		}		
	}

	handleResultTableChange = (type, { page, sizePerPage }) => {
		this.setState({
			queryResultPage: page,
			queryResultRecordsPerPage: sizePerPage
		}, () => this.handleQueryButtonClick())
	  }
	

	openFilterDialog(queryComponent) {
		switch (queryComponent.attributeDefinition.attributeType) {
			case 'BOOLEAN':
            case 'CODE':
            case 'TAXON':
            case 'DATE':
            case 'NUMBER':
            case 'TIME':
			case 'TEXT':
				this.setState({
					queryFilterDialogOpen: true,
					queryFilterDialogQueryComponent: queryComponent
				})
				break
			default:
				Dialogs.alert('dataView.filterNotSupported')
		}
	}

	handleQueryFilterDialogClose() {
		this.setState({
			queryFilterDialogOpen: false
		})
	}

	handleQueryFilterDialogOk(filterCondition) {
		const newQueryFilterDialogQueryComponent = Object.assign(this.state.queryFilterDialogQueryComponent, 
			{filterCondition: filterCondition})
		this.setState({
			queryFilterDialogOpen: false,
			queryFilterDialogQueryComponent: newQueryFilterDialogQueryComponent
		})
	}

	render() {
		const { survey } = this.props
		const { schemaTreeData, selectedEntityTreeNodes, step, availableAttributes, selectedColumns, selectedFilter, selectedEntity, queryResult,
			queryResultPage, queryResultRecordsPerPage, queryPanelExpanded, queryFilterDialogOpen, queryFilterDialogQueryComponent } = this.state
		const queryResultTotalRecords = queryResult ? queryResult.totalRecords: 0
		
		if (!survey || !schemaTreeData) {
			return <div>{L.l('survey.selectPublishedSurveyFirst')}</div>
		}

		const onEntitySelectChange = (values) => {
			const currentSelectedEntities = selectedEntityTreeNodes
			const newValues = values.filter(v => !Arrays.contains(selectedEntityTreeNodes, v, 'value'))
			let selectedEntity = null
			const newSelectedTreeEntityNode = newValues.length > 0 ? newValues[0] : null
			this.onEntityChange(newSelectedTreeEntityNode)
		}
		
		return (
			<MaxAvailableSpaceContainer className="query-builder">
				<ExpansionPanel expanded={queryPanelExpanded} onChange={(e, expanded) => this.setState({queryPanelExpanded: expanded})}>
					<ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
						<Typography>{L.l('dataView.query')}</Typography>
					</ExpansionPanelSummary>
					<ExpansionPanelDetails>
						<div className="container">
							<div className="row">
								<div className="col-md-4">
									{L.l('dataView.selectEntity')}: 
									<TreeSelect
										style={{ width: 300 }}
										dropdownStyle={{ width: 400, height: 300, overflow: 'auto' }}
										dropdownPopupAlign={{ overflow: { adjustY: 0, adjustX: 0 }, offset: [0, 2] }}
										placeholder={<i>{L.l('dataView.selectEntity')}</i>}
										searchPlaceholder={L.l('dataView.searchEntity')}
										treeLine 
										treeDefaultExpandAll
										treeCheckStrictly
										treeCheckable
										value={selectedEntityTreeNodes}
										treeData={[schemaTreeData.root]}
										treeNodeFilterProp="title"
										onChange={onEntitySelectChange}
										/>
								</div>
							</div>
							{selectedEntity &&
								<div className="row">
									<DragDropContext onDragEnd={this.onDragEnd}>
										<div className="col-md-4">
											{L.l('dataView.availableAttributes')}: 
											<Droppable droppableId="selectedAttributes" isDropDisabled>
											{(provided, snapshot) => (
												<div className="attributes"
													ref={provided.innerRef}>
												{availableAttributes.map((a, index) => (
													<Draggable key={'attribute_' + a.id} 
														draggableId={'attribute_' + a.id} index={index}>
													{(provided, snapshot) => (
														<div
															ref={provided.innerRef}
															{...provided.draggableProps}
															{...provided.dragHandleProps}
															className={'item' + (snapshot.isDragging ? ' dragging': '')} 
															onClick={this.addAttributeToSelectedColumns.bind(this, a)}
															>
				                                            <span className={'icon node-type ' + a.attributeType.toLowerCase()} />
															{a.label}
														</div>
													)}
													</Draggable>
												))}
													{provided.placeholder}
												</div>
											)}
											</Droppable>
										</div>
										<div className="col-md-8">
											<div className="row">
												{L.l('dataView.columns')}: 
												<Droppable droppableId="selectedColumns" direction="horizontal">
												{(provided, snapshot) => (
													<div
														className={'columns' + (snapshot.isDraggingOver ? ' dragging': '')}
														ref={provided.innerRef}
														{...provided.droppableProps}
													>
													{selectedColumns.map((item, index) => (
														<Draggable key={'columns_' + item.attributeDefinition.id} 
															draggableId={'columns_' + item.attributeDefinition.id} index={index}>
														{(provided, snapshot) => (
															<div
																ref={provided.innerRef}
																{...provided.draggableProps}
																{...provided.dragHandleProps}
																className={'closeable item' + (snapshot.isDragging ? ' dragging': '')}
																onClick={this.openFilterDialog.bind(this, item)}
																>
																<span>
																	{item.attributeDefinition.label}
																</span>
																<span><i class={'icon filter fas fa-search ' + (item.filterCondition ? 'filtered' : 'not-filtered')}></i></span>
																<a className="close-btn" onClick={e => this.handleColumnSelectionItemClose(e, item.attributeDefinition.id)}></a>
															</div>
														)}
														</Draggable>
													))}
														{provided.placeholder}
													</div>
												)}
												</Droppable>
											</div>
											<div className="row">
												{L.l('dataView.filter')}: 
												<Droppable droppableId="selectedFilter" direction="horizontal">
												{(provided, snapshot) => (
													<div
														className={'filter' + (snapshot.isDraggingOver ? ' dragging': '')}
														ref={provided.innerRef}
														{...provided.droppableProps}
													>
													{selectedFilter.map((item, index) => (
														<Draggable key={'filter_' + item.attributeDefinition.id} 
															draggableId={'filter_' + item.attributeDefinition.id} index={index}>
														{(provided, snapshot) => (
															<div
																ref={provided.innerRef}
																{...provided.draggableProps}
																{...provided.dragHandleProps}
																className={'closeable item' + (snapshot.isDragging ? ' dragging': '')}
																onClick={this.openFilterDialog.bind(this, item)}
																>
																{item.attributeDefinition.label}
																<span><i class={'icon filter fas fa-search ' + (item.filterCondition ? 'filtered' : 'not-filtered')}></i></span>
																<a className="close-btn" onClick={this.handleFilterSelectionItemClose.bind(this, item.attributeDefinition.id)}></a>
															</div>
														)}
														</Draggable>
													))}
													{provided.placeholder}
													</div>
												)}
												</Droppable>
											</div>
											<div className="row">
												<FormControl>
													<InputLabel htmlFor="step">{L.l('dataView.step')}</InputLabel>
													<Select
														value={step}
														onChange={e => this.setState({step: e.target.value})}
														inputProps={{
															name: 'step',
															id: 'step',
														}}
														style={{width: '200px'}}>
														{Object.keys(Workflow.STEPS).map(s => <MenuItem key={s} value={Workflow.STEPS[s].code}>{Workflow.STEPS[s].label}</MenuItem>)}
													</Select>
												</FormControl>
											</div>
										</div>
									</DragDropContext>
								</div>
							}
						</div>
					</ExpansionPanelDetails>
				</ExpansionPanel>
				{selectedEntity &&
					<div className="row">
						<div className="col" style={{textAlign: "center"}}>
							<Button variant="raised" color="primary" onClick={this.handleQueryButtonClick}>
								{L.l('dataView.query.run')}
							</Button>
						</div>
					</div>
				}
				{queryResult && 
					<div className="row">
						<div className="col">
							<QueryResultTable 
								maxHeight={200}
								columns={selectedColumns}
								queryResult={queryResult}
								handleTableChange={this.handleResultTableChange}
								page={queryResultPage}
								sizePerPage={queryResultRecordsPerPage}
								totalSize={queryResultTotalRecords}
							/>
						</div>
					</div>
				}
				<DataQueryFilterDialog open={queryFilterDialogOpen} 
					queryComponent={queryFilterDialogQueryComponent}
					onClose={this.handleQueryFilterDialogClose}
					onOk={this.handleQueryFilterDialogOk} />
			</MaxAvailableSpaceContainer>
		)
    }
}

class DropDownTreeData {
	root

	static fromSurvey(survey) {
		const data = new DropDownTreeData()
		data.root = DropDownTreeNode.fromNodeDefinition(survey.schema.firstRootEntityDefinition, true)
		return data
	}
}

class DropDownTreeNode {
	label
	children = []

	static fromNodeDefinition(nodeDef, onlyEntities) {
		const node = new DropDownTreeNode()
		node.key = nodeDef.id
		node.id = nodeDef.id
		node.value = "" + nodeDef.id
		node.title = nodeDef.label
		if (nodeDef instanceof EntityDefinition) {
			node.children = nodeDef.children
				.filter(n => onlyEntities ? n instanceof EntityDefinition : true)
				.map(childDef => DropDownTreeNode.fromNodeDefinition(childDef, onlyEntities))
			node.disabled = ! nodeDef.multiple
		}
		return node
	}
}

const mapStateToProps = state => {
	return {
		survey: state.preferredSurvey ? state.preferredSurvey.survey : null,
		loggedUser: state.session ? state.session.loggedUser : null,
		userGroups: state.userGroups ? state.userGroups.items : null
	}
}

class CloseableBox extends Component {
	render() {
		return <div class="closeable" {...this.props}>
				<a class="close-btn" onClick={this.props.onClose}></a>
				{this.props.children}
			</div>
	}
}

export default connect(mapStateToProps)(DataViewPage)