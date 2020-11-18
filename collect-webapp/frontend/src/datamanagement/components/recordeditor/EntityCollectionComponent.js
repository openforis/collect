import ServiceFactory from 'services/ServiceFactory'
import { EntityCreationCompletedEvent, EntityDeletedEvent } from 'model/event/RecordEvent'
import AbstractFormComponent from './AbstractFormComponent'

export default class EntityCollectionComponent extends AbstractFormComponent {
  commandService = ServiceFactory.commandService

  constructor() {
    super()

    this.state = {
      entities: [],
      addingEntity: false,
    }

    this.determineEntities = this.determineEntities.bind(this)
    this.onEntitiesUpdated = this.onEntitiesUpdated.bind(this)
    this.handleNewButtonClick = this.handleNewButtonClick.bind(this)
  }

  componentDidMount() {
    super.componentDidMount()
    this.setState({ entities: this.determineEntities() })
  }

  determineEntities() {
    const { itemDef, parentEntity } = this.props
    return parentEntity ? parentEntity.getChildrenByDefinitionId(itemDef.entityDefinitionId) : []
  }

  onRecordEvent(event) {
    super.onRecordEvent(event)

    const { parentEntity, itemDef } = this.props
    if (
      (event instanceof EntityCreationCompletedEvent || event instanceof EntityDeletedEvent) &&
      event.isRelativeToNodes({ parentEntity, nodeDefId: itemDef.entityDefinitionId })
    ) {
      this.setState({ addingEntity: false, entities: this.determineEntities() }, () => this.onEntitiesUpdated())
    }
  }

  onEntitiesUpdated() {}

  handleNewButtonClick() {
    const { itemDef, parentEntity } = this.props
    const { record } = parentEntity

    this.setState({ addingEntity: true })
    this.commandService.addEntity(record, parentEntity, itemDef.entityDefinition)
  }
}
