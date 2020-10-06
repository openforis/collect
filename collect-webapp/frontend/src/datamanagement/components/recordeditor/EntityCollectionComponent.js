import { Component } from 'react'
import ServiceFactory from 'services/ServiceFactory'
import EventQueue from 'model/event/EventQueue'
import { EntityCreatedEvent } from 'model/event/RecordEvent'
import { EntityDeletedEvent } from '../../../model/event/RecordEvent'

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
    EventQueue.subscribe('recordEvent', this.handleRecordEventReceived)

    this.setState({ entities: this.determineEntities() })
  }

  componentWillUnmount() {
    EventQueue.unsubscribe('recordEvent', this.handleRecordEventReceived)
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
    const { record } = parentEntity

    if (
      (event instanceof EntityCreatedEvent || event instanceof EntityDeletedEvent) &&
      event.recordId === record.id &&
      event.recordStep === record.step &&
      event.parentEntityPath === parentEntity.path &&
      Number(event.definitionId) === itemDef.entityDefinition.id
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
