import { ColumnDefinition } from 'model/ui/TableDefinition'

import * as FieldsSizes from '../fields/FieldsSizes'

export const calculateWidth = (headingColumn) => FieldsSizes.getWidth({ fieldDef: headingColumn, inTable: true })

const determineColumnInfo = ({ headingColumn, entities, col }) => {
  const { attributeDefinition, col: colOriginal, colSpan } = headingColumn
  const { alwaysRelevant, hideWhenNotRelevant, id: attributeDefinitionId, calculated, hidden } = attributeDefinition

  const relevant =
    alwaysRelevant ||
    entities.some((entity) => entity.hasSomeDescendantRelevant({ nodeDefinition: attributeDefinition }))

  const isNotEmpty = () =>
    entities.length > 0 &&
    entities.some((entity) => entity.hasSomeDescendantNotEmpty({ nodeDefinition: attributeDefinition }))

  const visible = !(calculated && hidden) && (!hideWhenNotRelevant || relevant || isNotEmpty())

  return {
    colOriginal,
    col: visible ? col : -1,
    colSpan,
    attributeDefinitionId,
    relevant,
    visible,
  }
}

export const determineColumnInfoByAttributeDefinitionId = ({ entities, itemDef }) => {
  const { headingColumns } = itemDef

  let currentCol = 1
  return headingColumns.reduce((infoByDefId, headingColumn) => {
    const { attributeDefinition } = headingColumn
    const info = determineColumnInfo({ headingColumn, entities, col: currentCol })
    infoByDefId[attributeDefinition.id] = info
    if (info.visible) {
      currentCol = info.col + headingColumn.colSpan
    }
    return infoByDefId
  }, [])
}

export const determineColumnsVisible = ({ itemDef, columnInfoByDefId }) => {
  const { headingColumns } = itemDef

  return headingColumns.reduce((columnsVisible, headingColumn) => {
    const { attributeDefinition } = headingColumn

    if (columnInfoByDefId[attributeDefinition.id].visible) {
      columnsVisible.push(headingColumn)
    }
    return columnsVisible
  }, [])
}

export const calculateColumnPosition = (columnsInfo) => (headingColumn) => {
  const originalColComparator = (infoA, infoB) => infoA.colOriginal - infoB.colOriginal
  const columnsInfoArr = Object.values(columnsInfo).sort(originalColComparator)

  const firstVisibleColumn =
    headingColumn instanceof ColumnDefinition
      ? headingColumn
      : // if headingColumn is a group, find the first visible descendant (attribute) column
        headingColumn.descendantColumns
          .map((descendantColumn) => columnsInfo[descendantColumn.attributeDefinitionId])
          .sort(originalColComparator)
          .find((descendantColumnInfo) => descendantColumnInfo.visible)

  let col = 1
  columnsInfoArr.some((info) => {
    if (info.attributeDefinitionId === firstVisibleColumn.attributeDefinitionId) {
      return true
    }
    if (info.visible) {
      col += info.colSpan
    }
    return false
  })
  return col
}
