import React, { Component, PropTypes } from 'react'
import classnames from 'classnames'
import { Button, Input, TabContent, TabPane, Nav, NavItem, NavLink } from 'reactstrap'
import Tab from './Tab'
import FormItems from './FormItems'
import ServiceFactory from '../../../services/ServiceFactory'
import EventQueue from '../../../model/event/EventQueue'
import { EntityCreatedEvent } from '../../../model/event/RecordEvent'

export default class MultipleFieldset extends Component {
  commandService = ServiceFactory.commandService

  constructor(props) {
    super(props)
    const { fieldsetDef } = props

    const { tabs } = fieldsetDef
    const firstTabId = tabs.length > 0 ? tabs[0].id : null

    this.state = {
      entities: [],
      selectedEntityIndex: -1,
      activeTab: firstTabId,
    }

    this.handleRecordEventReceived = this.handleRecordEventReceived.bind(this)
    this.determineEntities = this.determineEntities.bind(this)
    this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
    this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
    this.handleTabClick = this.handleTabClick.bind(this)
  }

  componentDidMount() {
    EventQueue.subscribe('recordEvent', this.handleRecordEventReceived)

    this.setState({ entities: this.determineEntities() })
  }

  componentWillUnmount() {
    EventQueue.unsubscribe('recordEvent', this.handleRecordEventReceived)
  }

  determineEntities() {
    const { fieldsetDef, parentEntity } = this.props
    if (parentEntity) {
      return parentEntity.getChildrenByDefinitionId(fieldsetDef.entityDefinitionId)
    } else {
      return []
    }
  }

  getSelectedEntity() {
    const { entities, selectedEntityIndex } = this.state
    return selectedEntityIndex >= 0 && selectedEntityIndex < entities.length ? entities[selectedEntityIndex] : null
  }

  handleRecordEventReceived(event) {
    const { parentEntity, fieldsetDef } = this.props
    if (!parentEntity) {
      return
    }
    const { record } = parentEntity
    if (
      event instanceof EntityCreatedEvent &&
      event.recordId === record.id &&
      event.recordStep === record.step &&
      event.parentEntityPath === parentEntity.path &&
      Number(event.definitionId) === fieldsetDef.entityDefinition.id
    ) {
      const entities = this.determineEntities()
      this.setState({ entities, selectedEntityIndex: entities.length - 1 })
    }
  }

  handleNewButtonClick() {
    const { fieldsetDef, parentEntity } = this.props
    const { record } = parentEntity
    const entityDef = fieldsetDef.entityDefinition

    this.commandService.addEntity(record, parentEntity, entityDef)
  }

  handleDeleteButtonClick() {
    const selectedEntity = this.getSelectedEntity()
    this.commandService.deleteNode(selectedEntity)
  }

  handleTabClick(tabId) {
    if (this.state.activeTab !== tabId) {
      this.setState({ activeTab: tabId })
    }
  }

  render() {
    const { parentEntity, fieldsetDef } = this.props
    const { entities, selectedEntityIndex } = this.state

    if (!parentEntity) {
      return <div>Loading...</div>
    }
    const entityOptions = [
      <option key="-1" value="-1">
        Select entity
      </option>,
    ].concat(
      entities.map((entity, index) => (
        <option key={`entity_${index}`} value={index}>
          {entity.summaryLabel}
        </option>
      ))
    )

    const selectedEntity = this.getSelectedEntity()

    let navItems = [],
      tabPanes = []

    if (selectedEntity) {
      navItems = fieldsetDef.tabs.map((tabDef) => (
        <NavItem key={tabDef.id}>
          <NavLink
            className={classnames({ active: this.state.activeTab === tabDef.id })}
            onClick={() => {
              this.handleTabClick(tabDef.id)
            }}
          >
            {tabDef.label}
          </NavLink>
        </NavItem>
      ))
      tabPanes = fieldsetDef.tabs.map((tabDef) => (
        <TabPane key={tabDef.id} tabId={tabDef.id}>
          <Tab tabDef={tabDef} parentEntity={selectedEntity} />
        </TabPane>
      ))
    }

    return (
      <div>
        <label>Select a {fieldsetDef.label}:</label>
        <Input
          type="select"
          id="entityDropdown"
          style={{ display: 'inline-block', width: '200px' }}
          onChange={(event) => this.setState({ selectedEntityIndex: Number(event.target.value) })}
          value={selectedEntityIndex}
        >
          {entityOptions}
        </Input>
        <Button color="success" onClick={this.handleNewButtonClick}>
          New
        </Button>
        {selectedEntityIndex > 0 ? (
          <Button color="danger" onClick={this.handleDeleteButtonClick}>
            <span className="fa fa-trash" />
          </Button>
        ) : (
          ''
        )}

        {selectedEntity ? (
          <div>
            <FormItems itemDefs={fieldsetDef.items} parentEntity={selectedEntity} />
            <Nav tabs>{navItems}</Nav>
            <TabContent activeTab={this.state.activeTab}>{tabPanes}</TabContent>
          </div>
        ) : (
          ''
        )}
      </div>
    )
  }
}
