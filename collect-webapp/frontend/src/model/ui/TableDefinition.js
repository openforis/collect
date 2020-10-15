import { UIModelObjectDefinition } from './UIModelObjectDefinition'

export class TableHeadingComponentDefinition extends UIModelObjectDefinition {}

export class ColumnDefinition extends TableHeadingComponentDefinition {
  attributeDefinitionId

  get attributeDefinition() {
    return this.survey.schema.getDefinitionById(this.attributeDefinitionId)
  }
}

export class ColumnGroupDefinition extends TableHeadingComponentDefinition {
  headingComponents = []
  entityDefinitionId
  label

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)
    this.headingComponents = []
    const jsonArrObj = jsonObj.headingComponents
    for (let i = 0; i < jsonArrObj.length; i++) {
      const itemJsonObj = jsonArrObj[i]
      const item =
        itemJsonObj.type === 'COLUMN_GROUP'
          ? new ColumnGroupDefinition(itemJsonObj.id, this)
          : new ColumnDefinition(itemJsonObj.id, this)
      item.fillFromJSON(itemJsonObj)
      this.headingComponents.push(item)
    }
  }

  get entityDefinition() {
    return this.survey.schema.getDefinitionById(this.entityDefinitionId)
  }

  get descendantColumns() {
    const columns = []
    const stack = []
    stack.push(...this.headingComponents)
    while (stack.length) {
      const headingComponent = stack.pop()
      if (headingComponent instanceof ColumnGroupDefinition) {
        stack.push(...headingComponent.headingComponents)
      } else {
        columns.push(headingComponent)
      }
    }
    return columns
  }
}

export class TableDefinition extends UIModelObjectDefinition {
  headingComponents = []
  headingRows = []
  headingColumns = []
  entityDefinitionId
  totalHeadingColumns
  totalHeadingRows
  row
  column
  columnSpan

  fillFromJSON(jsonObj) {
    super.fillFromJSON(jsonObj)
    this.headingComponents = this._extractHeadingComponentsFromJson(jsonObj.headingComponents)
    this.headingRows = this._extractHeadingRowsFromJson(jsonObj.headingRows)
    this.headingColumns = this._extractHeadingComponentsFromJson(jsonObj.headingColumns)
  }

  _extractHeadingComponentsFromJson(jsonArr) {
    return jsonArr.reduce((components, jsonComponent) => {
      const component =
        jsonComponent.type === 'COLUMN_GROUP'
          ? new ColumnGroupDefinition(jsonComponent.id, this)
          : new ColumnDefinition(jsonComponent.id, this)
      component.fillFromJSON(jsonComponent)
      components.push(component)
      return components
    }, [])
  }

  _extractHeadingRowsFromJson(jsonArr) {
    const rows = []
    for (let i = 0; i < jsonArr.length; i++) {
      const jsonRow = jsonArr[i]
      const row = this._extractHeadingComponentsFromJson(jsonRow)
      rows.push(row)
    }
    return rows
  }

  get entityDefinition() {
    return this.survey.schema.getDefinitionById(this.entityDefinitionId)
  }
}
