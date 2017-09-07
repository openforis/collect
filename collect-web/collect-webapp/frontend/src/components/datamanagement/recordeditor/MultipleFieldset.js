import React, { Component, PropTypes } from 'react'
import classnames from 'classnames';
import { Alert, Button, ButtonGroup, ButtonToolbar, Container, Row, Col,
    Form, FormGroup, Label, Input, FormText, FormFeedback,
    TabContent, TabPane, Nav, NavItem, NavLink, Card, CardTitle, CardText } from 'reactstrap';
import Tab from './Tab'
import FormItems from './FormItems'
import ServiceFactory from '../../../services/ServiceFactory'
import EventQueue from '../../../model/event/EventQueue'
import { EntityCreatedEvent } from '../../../model/event/RecordEvent'

export default class MultipleFieldset extends Component {

    commandService = ServiceFactory.commandService

    constructor(props) {
        super(props)

        let tabs = props.fieldsetDef.tabs
        let firstTabId = tabs.length > 0 ? tabs[0].id : null
        
        this.state = {
            entities: [],
            selelectedEntityId: -1,
            activeTab: firstTabId
        }

        this.handleRecordEventReceived = this.handleRecordEventReceived.bind(this)
        this.determineEntities = this.determineEntities.bind(this)
        this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
        this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
        this.handleTabClick = this.handleTabClick.bind(this)
    }

    componentDidMount() {
        EventQueue.subscribe('recordEvent', this.handleRecordEventReceived)
        
        this.setState({...this.state, entities: this.determineEntities()})
    }

    componentWillUnmount() {
        EventQueue.unsubscribe('recordEvent', this.handleRecordEventReceived)
    }

    determineEntities() {
        let parentEntity = this.props.parentEntity
        if (parentEntity) {
            return parentEntity.childrenByDefinitionId[this.props.fieldsetDef.entityDefinitionId]
        } else {
            return []
        }
    }

    handleRecordEventReceived(event) {
        let parentEntity = this.props.parentEntity
        if (! parentEntity) {
            return;
        }
        if (event.parentEntityId === parentEntity.id) {
            if (event instanceof EntityCreatedEvent) {
                this.setState({...this.state,
                    entities: this.determineEntities(),
                    selelectedEntityId: parseInt(event.nodeId)
                })
            }
        }
    }

    handleNewButtonClick() {
        let parentEntity = this.props.parentEntity
        let record = parentEntity.record
        let parentEntityId = parentEntity.id
        let entityDef = this.props.fieldsetDef.entityDefinition
        this.commandService.addEntity(record, parentEntityId, entityDef)
    }
    
    handleDeleteButtonClick() {
        let selectedEntity = this.state.entities.find(e => e.id === this.state.selelectedEntityId)
        this.commandService.deleteNode(selectedEntity)
    }

    handleTabClick(tabId) {
        if (this.state.activeTab !== tabId) {
            this.setState({...this.state, 
                activeTab: tabId
            });
        }
    }

    render() {
        if (! this.props.parentEntity) {
            return <div>Loading...</div>
        }
        let entityOptions = [<option key="-1" value="-1">Select entity</option>].concat(
            this.state.entities.map(e => <option key={e.id} value={e.id}>{e.summaryLabel}</option>)
        )

        let fieldsetDef = this.props.fieldsetDef

        let selectedEntity = this.state.entities.find(e => e.id === this.state.selelectedEntityId)
        
        let navItems = [], tabPanes = [];
        
        if (selectedEntity) {
            navItems = fieldsetDef.tabs.map(tabDef => 
                <NavItem key={tabDef.id}>
                    <NavLink className={classnames({ active: this.state.activeTab === tabDef.id })}
                        onClick={() => { this.handleTabClick(tabDef.id); }} 
                    >{tabDef.label}</NavLink>
                </NavItem>
            )
            tabPanes = fieldsetDef.tabs.map(tabDef => 
                <TabPane key={tabDef.id} tabId={tabDef.id}>
                    <Tab tabDef={tabDef} parentEntity={selectedEntity} />
                </TabPane>
            )
        }

        return (
            <div>
                <label>Select a {fieldsetDef.label}:</label>
                <Input type="select" id="entityDropdown" style={{display: 'inline-block', width: '200px'}}
                        onChange={(event) => this.setState({...this.state, selelectedEntityId: parseInt(event.target.value)})}
                        value={this.state.selelectedEntityId}>
                    {entityOptions}
                </Input>
                <Button color="success" onClick={this.handleNewButtonClick}>New</Button>
                {this.state.selelectedEntityId > 0 ? 
                    <Button color="danger" onClick={this.handleDeleteButtonClick}><span className="icon-trash"/></Button>
                : ''}

                {selectedEntity ? 
                    <div>
                        <FormItems itemDefs={fieldsetDef.items} parentEntity={selectedEntity} />
                        <Nav tabs>
                            {navItems}
                        </Nav>
                        <TabContent activeTab={this.state.activeTab}>
                            {tabPanes}
                        </TabContent>
                    </div>
                : '' }
            </div>
        )
    }
}