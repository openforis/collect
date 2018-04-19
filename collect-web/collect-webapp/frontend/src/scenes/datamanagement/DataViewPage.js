import React, { Component } from 'react'
import { connect } from 'react-redux'
import TreeSelect, { SHOW_PARENT } from 'rc-tree-select'
import 'rc-tree-select/assets/index.css'
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';
import Chip from 'material-ui/Chip'

import { EntityDefinition } from 'model/Survey'
import L from 'utils/Labels'
import Arrays from 'utils/Arrays'

class DataViewPage extends Component {

	constructor(props) {
		super()
		this.state = {
			treeData: null,
			selectedEntity: null,
			selectedEntityTreeNodes: [],
			columnsSelection: [],
			filterSelection: []
		}

		this.onEntityChange = this.onEntityChange.bind(this)
		this.onDragEnd = this.onDragEnd.bind(this)
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
			columnsSelection: [],
			filterSelection: []
		})
	}

	onDragEnd(result) {
		// dropped outside the list
		if (!result.destination) {
		  return
		}
		const attribute = DataViewPage.getAttributes(this.state.selectedEntity)[result.source.index]
		switch(result.destination.droppableId) {
			case 'selectedColumns':
				const newColumnsSelection = Arrays.addItem(this.state.columnsSelection, 
					{id: attribute.id, attribute: attribute}, true, 'id')
				this.setState({
					columnsSelection: newColumnsSelection
				})
				break
			case 'selectedFilter':
				const newFilterSelection = Arrays.addItem(this.state.filterSelection, 
					{id: attribute.id, attribute: attribute}, true, 'id')
				this.setState({
					filterSelection: newFilterSelection
				})
				break
		}
		
	  }

	render() {
		const { survey } = this.props
		const { treeData, selectedEntityTreeNodes, columnsSelection, filterSelection, selectedEntity } = this.state
		
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
			padding: grid * 2,
			margin: `0 0 ${grid}px 0`,
			background: isDragging ? "lightgreen" : "white",
			border: 'solid 1px black',
			borderTopLeftRadius: 15,
			borderTopRightRadius: 15,
			borderBottomLeftRadius: 15,
			borderBottomRightRadius: 15,
			width: '200px',
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
									{columnsSelection.map((item, index) => (
										<Draggable key={'columns_' + item.attribute.id} 
											draggableId={'columns_' + item.attribute.id} index={index}>
										{(provided, snapshot) => (
											<div
												ref={provided.innerRef}
												{...provided.draggableProps}
												{...provided.dragHandleProps}
												style={getColumnItemStyle(
													snapshot.isDragging,
													provided.draggableProps.style
												)}
											>
												{item.attribute.label}
												<span className="close-icon">
													<i className="far fa-times-circle"/>
												</span>
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
									{filterSelection.map((item, index) => (
										<Draggable key={'filter_' + item.attribute.id} 
											draggableId={'filter_' + item.attribute.id} index={index}>
										{(provided, snapshot) => (
											<div
												ref={provided.innerRef}
												{...provided.draggableProps}
												{...provided.dragHandleProps}
												style={getFilterItemStyle(
													snapshot.isDragging,
													provided.draggableProps.style
												)}
											>
												{item.attribute.label}
												<span className="close-icon">
													<i className="far fa-times-circle"/>
												</span>
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
	columns = []
	filter = []
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

export default connect(mapStateToProps)(DataViewPage)