import ServiceFactory from 'services/ServiceFactory'
import { EntityCreationCompletedEvent, EntityDeletedEvent, NodeCountUpdatedEvent } from 'model/event/RecordEvent'
import AbstractFormComponent from './AbstractFormComponent'

export default class EntityCollectionComponent extends AbstractFormComponent {
  commandService = ServiceFactory.commandService

  constructor() {
    super()

    this.state = {
      entities: [],
      addingEntity: false,
      maxCount: null,
    }

    this.determineEntities = this.determineEntities.bind(this)
    this.onStateUpdate = this.onStateUpdate.bind(this)
    this.onNewButtonClick = this.onNewButtonClick.bind(this)
  }

  componentDidMount() {
    super.componentDidMount()
    this.updateState()
  }

  updateState() {
    const { addingEntity } = this.state
    this.setState(this.determineNewState(), () => this.onStateUpdate(addingEntity))
  }

  determineNewState() {
    const { itemDef, parentEntity } = this.props
    const { entityDefinitionId } = itemDef
    const maxCount = parentEntity.childrenMaxCountByDefinitionId[entityDefinitionId]
    const entities = this.determineEntities()

    return { addingEntity: false, entities, maxCount }
  }

  determineEntities() {
    const { itemDef, parentEntity } = this.props
    return parentEntity ? parentEntity.getChildrenByDefinitionId(itemDef.entityDefinitionId) : []
  }

  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { parentEntity, itemDef } = this.props
    if (
      (event instanceof EntityCreationCompletedEvent ||
        event instanceof EntityDeletedEvent ||
        event instanceof NodeCountUpdatedEvent) &&
      event.isRelativeToNodes({ parentEntity, nodeDefId: itemDef.entityDefinitionId })
    ) {
      this.updateState()
    }
  }

  onStateUpdate(entityAdded = false) {}

  onNewButtonClick() {
    const { itemDef, parentEntity } = this.props
    const { record } = parentEntity

    this.setState({ addingEntity: true })
    this.commandService.addEntity({
      record,
      parentEntity,
      entityDef: itemDef.entityDefinition,
    })
  }
}
