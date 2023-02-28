import React from 'react'
import { Button } from 'reactstrap'
import { MenuItem, Select } from '@mui/material'

import { AttributeValueUpdatedEvent, EntityCreationCompletedEvent, EntityDeletedEvent } from 'model/event/RecordEvent'
import DeleteIconButton from 'common/components/DeleteIconButton'
import L from 'utils/Labels'
import Strings from 'utils/Strings'

import FormItems from './FormItems'
import EntityCollectionComponent from './EntityCollectionComponent'
import TabSetContent from './TabSetContent'
import NodeDefLabel from './NodeDefLabel'
import AbstractFormComponent from './AbstractFormComponent'

class MultipleFieldsetHeader extends AbstractFormComponent {
  constructor() {
    super()
    this.state = {
      ...this.state,
      entitiesSummary: [],
      hasEmptyEntity: false,
    }
  }

  componentDidUpdate(prevProps) {
    if (prevProps.entities !== this.props.entities || prevProps.maxCount !== this.props.maxCount) {
      this.updateState()
    }
  }

  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { parentEntity, entities, itemDef } = this.props

    if (
      (event instanceof AttributeValueUpdatedEvent &&
        (entities.some((entity) => event.isRelativeToEntityKeyAttributes({ entity })) ||
          event.isRelativeToDescendantsOf({ parentEntity, entityDefinition: itemDef.entityDefinition }))) ||
      ((event instanceof EntityDeletedEvent || event instanceof EntityCreationCompletedEvent) &&
        event.isRelativeToNodes({ parentEntity, nodeDefId: itemDef.entityDefinitionId }))
    ) {
      this.updateState()
    }
  }

  determineNewState() {
    const newState = super.determineNewState()
    const { entities } = this.props
    const entitiesSummary = this.extractEntitiesSummary()
    const hasEmptyEntity = entities.some((entity) => entity.isEmpty())
    return { ...newState, entitiesSummary, hasEmptyEntity }
  }

  extractEntitiesSummary() {
    const { entities } = this.props
    return entities.map((entity) => entity.summaryLabel)
  }

  render() {
    const {
      parentEntity,
      itemDef,
      maxCount,
      onDeleteButtonClick,
      onNewButtonClick,
      onSelectedEntityChange,
      selectedEntityIndex,
    } = this.props
    const { record } = parentEntity
    const { entitiesSummary, hasEmptyEntity } = this.state

    const { readOnly } = record
    const { entityDefinition } = itemDef

    const maxCountReached = maxCount >= 0 && entitiesSummary.length >= maxCount
    const newButtonDisabled = maxCountReached || hasEmptyEntity

    return (
      <div className="multiple-fieldset-header">
        <NodeDefLabel nodeDefinition={entityDefinition} limitWidth={false} />:
        {entitiesSummary.length > 0 && (
          <EntitySelect
            selectedEntityIndex={selectedEntityIndex}
            entitiesSummary={entitiesSummary}
            onChange={(event) => onSelectedEntityChange(Number(event.target.value))}
          />
        )}
        {!readOnly && (
          <Button
            color="success"
            onClick={onNewButtonClick}
            disabled={newButtonDisabled}
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
        {!readOnly && selectedEntityIndex >= 0 && <DeleteIconButton onClick={onDeleteButtonClick} />}
      </div>
    )
  }
}

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
      selectedEntityIndex: -1,
    }

    this.onDeleteButtonClick = this.onDeleteButtonClick.bind(this)
    this.onSelectedEntityChange = this.onSelectedEntityChange.bind(this)
  }

  getSelectedEntity() {
    const { entities, selectedEntityIndex } = this.state
    return selectedEntityIndex >= 0 && selectedEntityIndex < entities.length ? entities[selectedEntityIndex] : null
  }

  determineNewState() {
    const newState = super.determineNewState()
    return { ...newState, selectedEntityIndex: -1 }
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
    const { entities, maxCount, selectedEntityIndex } = this.state

    if (!parentEntity) {
      return <div>Loading...</div>
    }

    const selectedEntity = this.getSelectedEntity()

    return (
      <div className="multiple-fieldset-wrapper">
        <MultipleFieldsetHeader
          parentEntity={parentEntity}
          itemDef={itemDef}
          entities={entities}
          maxCount={maxCount}
          onDeleteButtonClick={this.onDeleteButtonClick}
          onNewButtonClick={this.onNewButtonClick}
          onSelectedEntityChange={this.onSelectedEntityChange}
          selectedEntityIndex={selectedEntityIndex}
        />
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
