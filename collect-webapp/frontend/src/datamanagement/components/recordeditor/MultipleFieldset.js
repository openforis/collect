import React from 'react'
import { Button, Input } from 'reactstrap'

import DeleteIconButton from 'common/components/DeleteIconButton'
import L from 'utils/Labels'

import FormItems from './FormItems'
import EntityCollectionComponent from './EntityCollectionComponent'
import TabSetContent from './TabSetContent'
import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'

export default class MultipleFieldset extends EntityCollectionComponent {
  constructor() {
    super()
    this.state = {
      ...this.state,
      entitiesSummary: [],
      selectedEntityIndex: -1,
    }

    this.handleDeleteButtonClick = this.handleDeleteButtonClick.bind(this)
  }

  componentDidMount() {
    super.componentDidMount()

    this.updateEntitiesSummary()
  }

  getSelectedEntity() {
    const { entities, selectedEntityIndex } = this.state
    return selectedEntityIndex >= 0 && selectedEntityIndex < entities.length ? entities[selectedEntityIndex] : null
  }

  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { entities } = this.state

    if (
      event instanceof AttributeValueUpdatedEvent &&
      entities.some((entity) => event.isRelativeToEntityKeyAttributes({ entity }))
    ) {
      this.updateEntitiesSummary()
    }
  }

  updateEntitiesSummary() {
    const { entities } = this.state
    this.setState({ entitiesSummary: entities.map((entity) => entity.summaryLabel) })
  }

  onEntitiesUpdated() {
    super.onEntitiesUpdated()

    const { entities } = this.state

    this.updateEntitiesSummary()
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
    const { entitiesSummary, selectedEntityIndex } = this.state
    const { entityDefinition } = itemDef

    if (!parentEntity) {
      return <div>Loading...</div>
    }
    const entityOptions = [
      <option key="-1" value="-1">
        {L.l('common.selectOne')}
      </option>,
    ].concat(
      entitiesSummary.map((summary, index) => (
        <option key={`entity_${index}`} value={index}>
          {summary}
        </option>
      ))
    )

    const selectedEntity = this.getSelectedEntity()

    return (
      <>
        <div className="multiple-fieldset-header">
          <label>{entityDefinition.labelOrName}:</label>
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
            {L.l('common.new')}
          </Button>
          {selectedEntity && <DeleteIconButton onClick={this.handleDeleteButtonClick} />}
        </div>
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
