import React, { Component } from 'react'
import { connect } from 'react-redux'
import TreeSelect, { SHOW_PARENT } from 'rc-tree-select'
import 'rc-tree-select/assets/index.css'
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd'
import Button from 'material-ui/Button'

import { EntityDefinition } from 'model/Survey'
import ServiceFactory from 'services/ServiceFactory'
import L from 'utils/Labels'
import Arrays from 'utils/Arrays'

class DataViewPage extends Component {

	constructor(props) {
		super()
		this.state = {
			treeData: null,
			selectedEntity: null,
			selectedEntityTreeNodes: [],
			selectedColumns: [],
			selectedFilter: []
		}

		this.onEntityChange = this.onEntityChange.bind(this)
		this.onDragEnd = this.onDragEnd.bind(this)
		this.getListByDroppableId = this.getListByDroppableId.bind(this)
		this.updateSelectionState = this.updateSelectionState.bind(this)
		this.handleQueryButtonClick = this.handleQueryButtonClick.bind(this)
	}

	static getDerivedStateFromProps(nextProps, prevState) {
		const survey = nextProps.survey
		if (survey) {
			const treeData = DropDownTreeData.fromSurvey(survey)
			const rootEntity = survey.schema.firstRootEntityDefinition
			return {
				treeData: treeData,
				selectedEntity: rootEntity,
				selectedEntityTreeNodes: [treeData.root]
			}
		} else {
			return null
		}
	}

	static getAttributes(entity) {
		return entity ? entity.children.filter(n => !(n instanceof EntityDefinition)) : []
	}

	onEntityChange(entityNode) {
		const { survey } = this.props
		const newSelectedEntityTreeNodes = []
		let selectedEntity = null

		if (entityNode) {
			newSelectedEntityTreeNodes.push(entityNode)
			selectedEntity = survey.schema.getDefinitionById(entityNode.value)
		}

		this.setState({
			selectedEntity: selectedEntity,
			selectedEntityTreeNodes: newSelectedEntityTreeNodes,
			selectedColumns: [],
			selectedFilter: []
		})
	}

	getListByDroppableId(droppableId) {
		switch(droppableId) {
			case 'selectedColumns':
				return this.state.selectedColumns
			case 'selectedFilter':
				return this.state.selectedFilter
			default:
				return DataViewPage.getAttributes(this.state.selectedEntity)		
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
		const attributeDefs = DataViewPage.getAttributes(this.state.selectedEntity)
		const attributeDef = attributeDefs[result.source.index]
		
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
			switch(result.destination.droppableId) {
				case 'selectedColumns':
					const newSelectedColumns = Arrays.addItem(this.state.selectedColumns, 
						{id: attributeDef.id, attributeDefinition: attributeDef}, true, 'id')
					this.setState({
						selectedColumns: newSelectedColumns
					})
					break
				case 'selectedFilter':
					const newSelectedFilter = Arrays.addItem(this.state.selectedFilter, 
						{id: attributeDef.id, attributeDefinition: attributeDef}, true, 'id')
					this.setState({
						selectedFilter: newSelectedFilter
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
	  }

	handleColumnSelectionItemClose(attributeDefinitionId) {
		const item = this.state.selectedColumns.find(c => c.id === attributeDefinitionId)
		this.setState({
			selectedColumns: Arrays.removeItem(this.state.selectedColumns, item)
		})
	}

	handleFilterSelectionItemClose(attributeDefinitionId) {
		const item = this.state.selectedFilter.find(c => c.id === attributeDefinitionId)
		this.setState({
			selectedFilter: Arrays.removeItem(this.state.selectedFilter, item)
		})
	}

	handleQueryButtonClick() {
		const { survey } = this.props
		const { selectedEntity, selectedColumns, selectedFilter} = this.state
		const query = new RDBQuery()
		query.surveyName = survey.name
		query.recordStep = 'ENTRY'
		query.contextEntityDefinitionId = selectedEntity.id
		query.columns = selectedColumns.map(c => {return {
			attributeDefinitionId: c.attributeDefinition.id
		}})
		query.filter = selectedFilter
		query.page = 1
		query.recordsPerPage = 20
		query.sortBy = []
		ServiceFactory.queryService.getQueryResult(survey.id, query).then(result => console.log(result))
	}

	render() {
		const { survey } = this.props
		const { treeData, selectedEntityTreeNodes, selectedColumns, selectedFilter, selectedEntity } = this.state
		
		if (!survey || !treeData) {
			return <div>{L.l('survey.selectPublishedSurveyFirst')}</div>
		}

		const selectedEntityAttributes = DataViewPage.getAttributes(selectedEntity)

		const onEntitySelectChange = (values) => {
			const currentSelectedEntities = selectedEntityTreeNodes
			const newValues = values.filter(v => !Arrays.contains(selectedEntityTreeNodes, v, 'value'))
			let selectedEntity = null
			const newSelectedTreeEntityNode = newValues.length > 0 ? newValues[0] : null
			this.onEntityChange(newSelectedTreeEntityNode)
		}

		const grid = 4
		
		const getAttributeItemStyle = (isDragging, draggableStyle) => ({
			userSelect: "none",
			padding: grid * 2,
			margin: `0 0 ${grid}px 0`,
			background: isDragging ? "lightgreen" : "white",
			...draggableStyle
		  });
		
		const getAttributesListStyle = isDraggingOver => ({
			background: isDraggingOver ? "lightblue" : "lightgrey",
			padding: grid,
			width: 250
		  });
		  
		const getColumnsListStyle = isDraggingOver => ({
			background: isDraggingOver ? "lightblue" : "lightgrey",
			padding: grid,
			width: '100%',
			minHeight: 100
		});
		
		const getColumnItemStyle = (isDragging, draggableStyle) => ({
			userSelect: "none",
			margin: `0 0 ${grid}px 0`,
			background: isDragging ? "lightgreen" : "white",
			border: 'solid 1px black',
			borderTopLeftRadius: 15,
			borderTopRightRadius: 15,
			borderBottomLeftRadius: 15,
			borderBottomRightRadius: 15,
			width: '200px',
			float: 'left',
			...draggableStyle
		});
		
		const getFilterListStyle = isDraggingOver => ({
			background: isDraggingOver ? "lightblue" : "lightgrey",
			padding: grid,
			width: '100%',
			minHeight: 100
		});
		
		const getFilterItemStyle = (isDragging, draggableStyle) => ({
			userSelect: "none",
			padding: grid * 2,
			margin: `0 0 ${grid}px 0`,
			background: isDragging ? "lightgreen" : "white",
			width: '200px',
			float: 'left',
			...draggableStyle
		  });
		 

		return (
			<div>
				<div className="row">
					<div className="col-md-4">
						{L.l('dataView.entity')}: 
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
							treeData={[treeData.root]}
							treeNodeFilterProp="title"
							onChange={onEntitySelectChange}
							/>
					</div>
				</div>
				<div className="row">
					<DragDropContext onDragEnd={this.onDragEnd}>
						<div className="col">
							{L.l('dataView.attributes')}: 
							<Droppable droppableId="selectedAttributes" isDropDisabled>
							{(provided, snapshot) => (
								<div
									ref={provided.innerRef}
									style={getAttributesListStyle(snapshot.isDraggingOver)}
								>
								{selectedEntityAttributes.map((a, index) => (
									<Draggable key={'attribute_' + a.id} 
										draggableId={'attribute_' + a.id} index={index}>
									{(provided, snapshot) => (
										<div
											ref={provided.innerRef}
											{...provided.draggableProps}
											{...provided.dragHandleProps}
											style={getAttributeItemStyle(
												snapshot.isDragging,
												provided.draggableProps.style
											)}
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
										ref={provided.innerRef}
										style={getColumnsListStyle(snapshot.isDraggingOver)}
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
												style={getColumnItemStyle(
													snapshot.isDragging,
													provided.draggableProps.style
												)}
												className="closeable">
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
										ref={provided.innerRef}
										style={getFilterListStyle(snapshot.isDraggingOver)}
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
												style={getFilterItemStyle(
													snapshot.isDragging,
													provided.draggableProps.style
												)}
												className="closeable">
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
							<div className="row">
								<Button variant="raised" color="primary" onClick={this.handleQueryButtonClick}>
									Query
      							</Button>
							</div>
						</div>
					</DragDropContext>
				</div>
			</div>
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
		node.value = nodeDef.id
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