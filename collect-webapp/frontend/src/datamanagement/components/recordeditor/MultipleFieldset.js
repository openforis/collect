import React from 'react'
import { Button } from 'reactstrap'
import { MenuItem, Select } from '@material-ui/core'

import DeleteIconButton from 'common/components/DeleteIconButton'
import L from 'utils/Labels'
import Strings from 'utils/Strings'

import FormItems from './FormItems'
import EntityCollectionComponent from './EntityCollectionComponent'
import TabSetContent from './TabSetContent'
import { AttributeValueUpdatedEvent } from 'model/event/RecordEvent'
import NodeDefLabel from './NodeDefLabel'

const EntitySelect = (props) => {
  const { entitiesSummary, selectedEntityIndex, onChange } = props

  return (
    <Select value={selectedEntityIndex} variant="outlined" onChange={onChange} style={{ width: '300px' }}>
      {[
        <MenuItem key="-1" value="-1">
          <em>{L.l('common.selectOne')}</em>
        </MenuItem>,
        ...entitiesSummary.map((summary, index) => (
          <MenuItem key={`entity_${index}`} value={index}>
            {Strings.isBlank(summary) ? L.l('common.emptyItem') : summary}
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

    this.onDeleteButtonClick = this.onDeleteButtonClick.bind(this)
  }

  getSelectedEntity() {
    const { entities, selectedEntityIndex } = this.state
    return selectedEntityIndex >= 0 && selectedEntityIndex < entities.length ? entities[selectedEntityIndex] : null
  }

  determineNewState() {
    const newState = super.determineNewState()
    const { entities } = newState
    return { ...newState, selectedEntityIndex: -1, entitiesSummary: this.extractEntitiesSummary(entities) }
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

  extractEntitiesSummary(entities) {
    return entities.map((entity) => entity.summaryLabel)
  }

  updateEntitiesSummary() {
    const { entities } = this.state
    this.setState({ entitiesSummary: this.extractEntitiesSummary(entities) })
  }

  onStateUpdate(entityAdded) {
    super.onStateUpdate(entityAdded)

    if (entityAdded) {
      // Select first entity
      const { entities } = this.state
      this.onSelectedEntityChange(entities.length - 1)
    }
  }

  onSelectedEntityChange(selectedEntityIndex) {
    // unmount and remount components
    this.setState({ selectedEntityIndex: -1 }, () => this.setState({ selectedEntityIndex }))
  }

  onDeleteButtonClick() {
    const selectedEntity = this.getSelectedEntity()
    this.commandService.deleteEntity({ entity: selectedEntity })
  }

  render() {
    const { parentEntity, itemDef } = this.props
    const { entitiesSummary, selectedEntityIndex, maxCount } = this.state
    const { record } = parentEntity
    const { readOnly } = record
    const { entityDefinition } = itemDef

    if (!parentEntity) {
      return <div>Loading...</div>
    }

    const selectedEntity = this.getSelectedEntity()

    const maxCountReached = maxCount && entitiesSummary.length >= maxCount

    return (
      <div className="multiple-fieldset-wrapper">
        <div className="multiple-fieldset-header">
          <NodeDefLabel nodeDefinition={entityDefinition} limitWidth={false} />:
          {entitiesSummary.length > 0 && (
            <EntitySelect
              selectedEntityIndex={selectedEntityIndex}
              entitiesSummary={entitiesSummary}
              onChange={(event) => this.onSelectedEntityChange(Number(event.target.value))}
            />
          )}
          {!readOnly && (
            <Button
              color="success"
              onClick={this.onNewButtonClick}
              disabled={maxCountReached}
              title={
                maxCountReached
                  ? L.l('dataManagement.dataEntry.multipleNodesComponent.cannotAddNewNodes.maxCountReached', [
                      maxCount,
                      entityDefinition.labelOrName,
                    ])
                  : ''
              }
            >
              {L.l('common.new')}
            </Button>
          )}
          {!readOnly && selectedEntity && <DeleteIconButton onClick={this.onDeleteButtonClick} />}
        </div>
        {selectedEntity && (
          <>
            <FormItems parentItemDef={itemDef} parentEntity={selectedEntity} />
            <TabSetContent tabSetDef={itemDef} parentEntity={selectedEntity} />
          </>
        )}
      </div>
    )
  }
}
