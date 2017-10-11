import React, { Component } from 'react'
import PropTypes from 'prop-types'
import CheckboxTree from 'react-checkbox-tree';
import 'react-checkbox-tree/lib/react-checkbox-tree.css';
import { EntityDefinition } from 'model/Survey'

export default class SchemaTreeView extends Component {

    constructor(props) {
        super(props)

        const treeData = this.buildTreeData(props.survey, props.includeAttributes, props.allowSingleEntitiesSelection)
        const allNodeIds = this.determineAllNodeIds(treeData)

        this.state = {
            treeData: treeData,
            checkedNodeIds: this.props.selectAll ? allNodeIds: [],
            expandedNodeIds: props.survey ? allNodeIds : []
        }

        this.handleCheck = this.handleCheck.bind(this)
    }

    componentWillReceiveProps(nextProps) {
        const mustUpdateState = this.props.survey.id != nextProps.survey.id 
                || this.props.selectAll != nextProps.selectAll
        
        if (mustUpdateState) {
            const survey = nextProps.survey;
            const treeData = this.buildTreeData(survey, nextProps.includeAttributes, nextProps.allowSingleEntitiesSelection)
            const allNodeIds = this.determineAllNodeIds(treeData)
            this.setState({
                treeData: treeData,
                checkedNodeIds: nextProps.selectAll ? allNodeIds: [],
                expandedNodeIds: survey ? allNodeIds : []
            })
        }
    }

    determineAllNodeIds(treeData) {
        const extractValues = function(items) {
            let result = []
            items.forEach(e => {
                result.push(e.value)
                let childrenValues = extractValues(e.children)
                childrenValues.forEach(v => result.push(v))
            });
            return result;
        }
        let result = extractValues(treeData)
        return result
    }

    buildTreeData(survey, includeAttributes, allowSingleEntitiesSelection) {
        if (survey) {
            const rootEntityDefinition = survey.schema.firstRootEntityDefinition
    
            const schemaTree = new SchemaTree()
            schemaTree.build(rootEntityDefinition, includeAttributes, allowSingleEntitiesSelection)
    
            return [schemaTree.root]
        } else {
            return []
        }
    }

    handleCheck(checkedNodeIds) {
        //keep only one selected item
        const oldCheckedNodeIds = this.state.checkedNodeIds
        const newCheckedNodeIds = checkedNodeIds.filter(id => oldCheckedNodeIds.indexOf(id) < 0)
        
        const survey = this.props.survey
        let selectedNodeDefinitions = []
        newCheckedNodeIds.forEach(id => {
            let def = survey.schema.getDefinitionById(parseInt(id))
            selectedNodeDefinitions.push(def)
        })
        this.props.handleNodeSelect({selectedNodeDefinitions: selectedNodeDefinitions})

        this.setState({ checkedNodeIds: newCheckedNodeIds })
    }
   
    render() {
        return (
            <CheckboxTree
                nodes={this.state.treeData}
                checked={this.state.checkedNodeIds}
                expanded={this.state.expandedNodeIds}
                onCheck={this.handleCheck}
                onExpand={expandedNodeIds => this.setState({ expandedNodeIds })}
                noCascade
                disabled={this.props.selectAll}
            />
        );
    }

}

SchemaTreeView.propTypes = {
    survey: PropTypes.object.isRequired,
    handleNodeSelect: PropTypes.func.isRequired,
    selectAll: PropTypes.bool
}

class SchemaTree {
    root

    build(rootEntityDefinition, includeAttributes, allowSingleEntitiesSelection) {
        this.root = new EntityNode()
        this.root.fillFromNodeDefinition(rootEntityDefinition, includeAttributes, allowSingleEntitiesSelection)
    }

}

class SchemaNode {
    value
    label
    multiple
    disabled

    fillFromNodeDefinition(nodeDef, includeAttributes, allowSingleEntitiesSelection) {
        this.value = nodeDef.id.toString()
        this.multiple = nodeDef.multiple
        this.disabled = !allowSingleEntitiesSelection && nodeDef instanceof EntityDefinition && !nodeDef.multiple
        this.label = (nodeDef.label ? nodeDef.label : '') + ' [' + nodeDef.name + ']'
    }
}

class EntityNode extends SchemaNode {
    children

    fillFromNodeDefinition(nodeDef, includeAttributes, allowSingleEntitiesSelection) {
        super.fillFromNodeDefinition(nodeDef, includeAttributes, allowSingleEntitiesSelection)

        this.children = nodeDef.children.filter(c => includeAttributes || c.type == 'ENTITY').map(c => {
            let childNode
            if (c.type == 'ENTITY') {
                childNode = new EntityNode()
            } else {
                childNode = new AttributeNode()
            }
            childNode.fillFromNodeDefinition(c, includeAttributes, allowSingleEntitiesSelection)
            return childNode
        })
    }
}

class AttributeNode extends SchemaNode {

}