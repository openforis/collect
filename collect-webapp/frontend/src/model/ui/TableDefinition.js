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

    this.headingRows = this._extractHeadingRowsFromJson(jsonObj.headingRows)
    this.headingColumns = this._extractHeadingColumnsFromJson(jsonObj.headingColumns)
  }

  _extractHeadingRowsFromJson(jsonArr) {
    const rows = []
    for (let i = 0; i < jsonArr.length; i++) {
      const jsonRow = jsonArr[i]
      const row = []
      for (let j = 0; j < jsonRow.length; j++) {
        const jsonCol = jsonRow[j]
        const col = new ColumnDefinition(jsonCol.id, this)
        col.fillFromJSON(jsonCol)
        row.push(col)
      }
      rows.push(row)
    }
    return rows
  }

  _extractHeadingColumnsFromJson(jsonArr) {
    const columns = []
    for (let i = 0; i < jsonArr.length; i++) {
      const jsonCol = jsonArr[i]
      const col = new ColumnDefinition(jsonCol.id, this)
      col.fillFromJSON(jsonCol)
      columns.push(col)
    }
    return columns
  }

  get entityDefinition() {
    return this.survey.schema.getDefinitionById(this.entityDefinitionId)
  }
}
