import React from 'react'
import { Button } from 'reactstrap'
import { MenuItem, Select } from '@material-ui/core'

import DeleteIconButton from 'common/components/DeleteIconButton'
import L from 'utils/Labels'

import FormItems from './FormItems'
import EntityCollectionComponent from './EntityCollectionComponent'
import TabSetContent from './TabSetContent'
import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'
import NodeDefLabel from './NodeDefLabel'

const EntitySelect = (props) => {
  const { entitiesSummary, selectedEntityIndex, onChange } = props

  return (
    <Select
      value={selectedEntityIndex}
      variant="outlined"
      onChange={onChange}
      style={{ display: 'inline-block', width: '200px' }}
    >
      {[
        <MenuItem key="-1" value="-1">
          <em>{L.l('common.selectOne')}</em>
        </MenuItem>,
        ...entitiesSummary.map((summary, index) => (
          <MenuItem key={`entity_${index}`} value={index}>
            {summary}
          </MenuItem>
        )),
      ]}
    </Select>
  )
}

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

    this.onSelectedEntityChange(entities.length - 1)
  }

  onSelectedEntityChange(selectedEntityIndex) {
    // unmount and remount components
    this.setState({ selectedEntityIndex: -1 }, () => this.setState({ selectedEntityIndex }))
  }

  handleDeleteButtonClick() {
    const selectedEntity = this.getSelectedEntity()
    this.commandService.deleteEntity({ entity: selectedEntity })
  }

  render() {
    const { parentEntity, itemDef } = this.props
    const { entitiesSummary, selectedEntityIndex } = this.state
    const { entityDefinition } = itemDef

    if (!parentEntity) {
      return <div>Loading...</div>
    }

    const selectedEntity = this.getSelectedEntity()

    return (
      <>
        <div className="multiple-fieldset-header">
          <NodeDefLabel nodeDefinition={entityDefinition} />:
          {entitiesSummary.length > 0 && (
            <EntitySelect
              selectedEntityIndex={selectedEntityIndex}
              entitiesSummary={entitiesSummary}
              onChange={(event) => this.onSelectedEntityChange(Number(event.target.value))}
            />
          )}
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
