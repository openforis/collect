import React from 'react'
import classnames from 'classnames'
import { Button, Input, TabContent, TabPane, Nav, NavItem, NavLink } from 'reactstrap'

import Tab from './Tab'
import FormItems from './FormItems'
import EntityCollectionComponent from './EntityCollectionComponent'

export default class MultipleFieldset extends EntityCollectionComponent {
  constructor(props) {
    super()
    const { itemDef } = props

    const { tabs } = itemDef
    const firstTabId = tabs.length > 0 ? tabs[0].id : null

    this.state = {
      ...this.state,
      selectedEntityIndex: -1,
      activeTab: firstTabId,
    }

    this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
    this.handleTabClick = this.handleTabClick.bind(this)
  }

  getSelectedEntity() {
    const { entities, selectedEntityIndex } = this.state
    return selectedEntityIndex >= 0 && selectedEntityIndex < entities.length ? entities[selectedEntityIndex] : null
  }

  onEntitiesUpdated() {
    super.onEntitiesUpdated()
    const { entities } = this.state
    this.setState({ selectedEntityIndex: entities.length - 1 })
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
    const { parentEntity, itemDef } = this.props
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

    return (
      <div>
        <label>Select a {itemDef.label}:</label>
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
        {selectedEntity && (
          <Button color="danger" onClick={this.handleDeleteButtonClick}>
            <span className="fa fa-trash" />
          </Button>
        )}
        {selectedEntity && (
          <div>
            <FormItems itemDefs={itemDef.items} parentEntity={selectedEntity} />
            {itemDef.tabs.length > 0 && (
              <>
                <Nav tabs>
                  {itemDef.tabs.map((tabDef) => (
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
                  ))}
                </Nav>
                <TabContent activeTab={this.state.activeTab}>
                  {itemDef.tabs.map((tabDef) => (
                    <TabPane key={tabDef.id} tabId={tabDef.id}>
                      <Tab tabDef={tabDef} parentEntity={selectedEntity} />
                    </TabPane>
                  ))}
                </TabContent>{' '}
              </>
            )}
          </div>
        )}
      </div>
    )
  }
}
