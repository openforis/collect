import React from 'react'
import { Button, Input } from 'reactstrap'

import FormItems from './FormItems'
import EntityCollectionComponent from './EntityCollectionComponent'
import TabSetContent from './TabSetContent'

export default class MultipleFieldset extends EntityCollectionComponent {
  constructor() {
    super()
    this.state = {
      ...this.state,
      selectedEntityIndex: -1,
    }

    this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
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

  onSelectedEntityChange(event) {
    const selectedEntityIndex = Number(event.target.value)
    // unmount and remount components
    this.setState({ selectedEntityIndex: -1 }, () => this.setState({ selectedEntityIndex }))
  }

  handleDeleteButtonClick() {
    const selectedEntity = this.getSelectedEntity()
    this.commandService.deleteEntity(selectedEntity)
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
      <>
        <label>Select a {itemDef.label}:</label>
        <Input
          type="select"
          id="entityDropdown"
          style={{ display: 'inline-block', width: '200px' }}
          onChange={(event) => this.onSelectedEntityChange(event)}
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
          <>
            <FormItems itemDefs={itemDef.items} parentEntity={selectedEntity} />
            <TabSetContent tabSetDef={itemDef} parentEntity={selectedEntity} />
          </>
        )}
      </>
    )
  }
}
