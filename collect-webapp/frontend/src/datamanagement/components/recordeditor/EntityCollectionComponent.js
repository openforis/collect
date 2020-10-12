import { Component } from 'react'
import ServiceFactory from 'services/ServiceFactory'
import EventQueue from 'model/event/EventQueue'
import { EntityCreatedEvent } from 'model/event/RecordEvent'
import { EntityDeletedEvent, RecordEvent } from '../../../model/event/RecordEvent'

export default class EntityCollectionComponent extends Component {
  commandService = ServiceFactory.commandService

  constructor() {
    super()

    this.state = {
      entities: [],
    }

    this.handleRecordEventReceived = this.handleRecordEventReceived.bind(this)
    this.determineEntities = this.determineEntities.bind(this)
    this.onEntitiesUpdated = this.onEntitiesUpdated.bind(this)
    this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
  }

  componentDidMount() {
    EventQueue.subscribe(RecordEvent.TYPE, this.handleRecordEventReceived)

    this.setState({ entities: this.determineEntities() })
  }

  componentWillUnmount() {
    EventQueue.unsubscribe(RecordEvent.TYPE, this.handleRecordEventReceived)
  }

  determineEntities() {
    const { itemDef, parentEntity } = this.props
    return parentEntity ? parentEntity.getChildrenByDefinitionId(itemDef.entityDefinitionId) : []
  }

  handleRecordEventReceived(event) {
    const { parentEntity, itemDef } = this.props
    if (!parentEntity) {
      return
    }
    if (
      (event instanceof EntityCreatedEvent || event instanceof EntityDeletedEvent) &&
      event.isRelativeToNodes({ parentEntity, nodeDefId: itemDef.entityDefinitionId })
    ) {
      this.setState({ entities: this.determineEntities() }, () => this.onEntitiesUpdated())
    }
  }

  onEntitiesUpdated() {}

  handleNewButtonClick() {
    const { itemDef, parentEntity } = this.props
    const { record } = parentEntity

    this.commandService.addEntity(record, parentEntity, itemDef.entityDefinition)
  }
}
