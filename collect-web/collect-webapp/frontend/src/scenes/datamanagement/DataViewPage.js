import React, { Component } from 'react'
import { connect } from 'react-redux'
import TreeSelect, { SHOW_PARENT } from 'rc-tree-select'
import 'rc-tree-select/assets/index.css'
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd'
import Button from 'material-ui/Button'
import ExpansionPanel, {
    ExpansionPanelSummary,
    ExpansionPanelDetails,
} from 'material-ui/ExpansionPanel'
import Typography from 'material-ui/Typography';
import ExpandMoreIcon from 'material-ui-icons/ExpandMore';

import QueryResultTable from './QueryResultTable'
import { EntityDefinition } from 'model/Survey'
import ServiceFactory from 'services/ServiceFactory'
import Dialogs from 'components/Dialogs'
import L from 'utils/Labels'
import Arrays from 'utils/Arrays'
import MaxAvailableSpaceContainer from '../../components/MaxAvailableSpaceContainer';

class DataViewPage extends Component {

	static initialState = {
		schemaTreeData: null,
		selectedEntity: null,
		selectedEntityTreeNodes: [],
		availableAttributes: [],
		selectedColumns: [],
		selectedFilter: [],
		queryResult: null,
		queryResultPage: 1,
		queryResultRecordsPerPage: 10,
		queryResultTotalRecords: 0,
		queryPanelExpanded: true
	}

	constructor(props) {
		super()
		this.state = DataViewPage.initialState

		this.onEntityChange = this.onEntityChange.bind(this)
		this.onDragEnd = this.onDragEnd.bind(this)
		this.getListByDroppableId = this.getListByDroppableId.bind(this)
		this.updateSelectionState = this.updateSelectionState.bind(this)
		this.handleQueryButtonClick = this.handleQueryButtonClick.bind(this)
		this.handleResultTableChange = this.handleResultTableChange.bind(this)
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

	getListByDroppableId(droppableId) {
		switch(droppableId) {
			case 'selectedColumns':
				return this.state.selectedColumns
			case 'selectedFilter':
				return this.state.selectedFilter
			default:
				return this.state.availableAttributes
		}
	}

	updateSelectionState(droppableId, list) {
		if (droppableId === 'selectedColumns') {
			this.setState({
				selectedColumns: list
			})
		} else {
			this.setState({
				selectedFilter: list
			})
		}
	}

	onDragEnd(result) {
		// dropped outside the list
		if (!result.destination) {
		  return
		}
		const sourceList = this.getListByDroppableId(result.source.droppableId)
		const destList = this.getListByDroppableId(result.destination.droppableId)

		//dropped inside the same list: reorder
		if (result.source.droppableId === result.destination.droppableId) {
			const reorder = (list, startIndex, endIndex) => {
				const result = Array.from(list)
				const [removed] = result.splice(startIndex, 1)
				result.splice(endIndex, 0, removed)
				return result
			}
			const newDestList =  reorder(destList, result.source.index, result.source.index)
			this.updateSelectionState(result.destination.droppableId, newDestList)
		} else if (result.source.droppableId === 'selectedAttributes') {
			const newAvailableAttributes = Array.from(this.state.availableAttributes)
			const [attributeDef] = newAvailableAttributes.splice(result.source.index, 1)
			switch(result.destination.droppableId) {
				case 'selectedColumns':
					const newSelectedColumns = Arrays.addItem(this.state.selectedColumns, 
						{id: attributeDef.id, attributeDefinition: attributeDef}, true, 'id')
					this.setState({
						availableAttributes: newAvailableAttributes,
						selectedColumns: newSelectedColumns
					})
					break
				case 'selectedFilter':
					const newSelectedFilter = Arrays.addItem(this.state.selectedFilter, 
						{id: attributeDef.id, attributeDefinition: attributeDef}, true, 'id')
					this.setState({
						availableAttributes: newAvailableAttributes,
						selectedFilter: newSelectedFilter,

					})
					break
			}
		} else {
			const newSourceList = Array.from(sourceList)
			const [removed] = newSourceList.splice(result.source.index, 1)
			const newDestList = Array.from(destList)
			newDestList.push(removed)
			this.updateSelectionState(result.source.droppableId, newSourceList)
			this.updateSelectionState(result.destination.droppableId, newDestList)
		}
		/*
		this.setState({
			availableAttributes: newAvailableAttributes,
			selectedColumns: newSelectedColumns,
			selectedFilter: newSelectedFilter,
			queryResult: null,
			queryResultPage: 1,
			queryResultRecordsPerPage: 10,
			queryResultTotalRecords: 0,
		})
		*/
	  }

	handleColumnSelectionItemClose(attributeDefinitionId) {
		const item = this.state.selectedColumns.find(c => c.id === attributeDefinitionId)
		const newAvailableAttributes = Array.from(this.state.availableAttributes)
		newAvailableAttributes.push(item.attributeDefinition)
		this.setState({
			availableAttributes: newAvailableAttributes,
			selectedColumns: Arrays.removeItem(this.state.selectedColumns, item),
			queryResult: null,
			queryResultPage: 1,
			queryResultRecordsPerPage: 10,
			queryResultTotalRecords: 0,
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
				attributeDefinitionId: c.attributeDefinition.id
			}})
			query.filter = selectedFilter
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
	

	render() {
		const { survey } = this.props
		const { schemaTreeData, selectedEntityTreeNodes, availableAttributes, selectedColumns, selectedFilter, selectedEntity, queryResult,
			queryResultPage, queryResultRecordsPerPage, queryPanelExpanded } = this.state
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
															>
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
																>
																{item.attributeDefinition.label}
																<a className="close-btn" onClick={this.handleColumnSelectionItemClose.bind(this, item.attributeDefinition.id)}></a>
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
																>
																{item.attributeDefinition.label}
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

class RDBQuery {
	surveyName
	recordStep
	contextEntityDefinitionId
	columns = []
	filter = []
	page = 1
	recordsPerPage = 20
	sortBy = []
}

class RDBQueryColumn {
	attribute
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