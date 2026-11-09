import React from 'react'
import classNames from 'classnames'

import { CoordinateAttributeDefinition, NumericAttributeDefinition, TaxonAttributeDefinition } from 'model/Survey'
import * as FieldsSizes from '../fields/FieldsSizes'
import L from 'utils/Labels'

const getHeadingLabel = ({ headingComponent }) => {
  const { nodeDefinition } = headingComponent
  const { labelOrName } = nodeDefinition

  if (nodeDefinition instanceof NumericAttributeDefinition) {
    const { precisions } = nodeDefinition
    if (precisions && precisions.length === 1) {
      const precision = precisions[0]
      const unit = nodeDefinition.survey.units.find((unit) => unit.id === precision.unitId)
      const suffix = ` (${unit.abbreviation})`
      if (!labelOrName.endsWith(suffix)) {
        return labelOrName + suffix
      }
    }
  }
  return labelOrName
}

const isCompositeAttribute = (attrDef) =>
  attrDef instanceof CoordinateAttributeDefinition || attrDef instanceof TaxonAttributeDefinition

const HeadingRow = ({
  columnCalculator,
  columnInfoByDefId,
  headingRow,
  firstRow,
  totalHeadingRows,
  totalHeadingColumns,
  includeRowNumberColumn,
  includeDeleteColumn,
}) => [
  ...(firstRow && includeRowNumberColumn
    ? [
        <div
          key="heading-cell-row-number"
          className="grid-cell"
          style={{
            gridRowStart: 1,
            gridRowEnd: totalHeadingRows + 1,
            gridColumnStart: 1,
            gridColumnEnd: 2,
          }}
        >
          {L.l('dataManagement.dataEntry.multipleNodesComponent.rowNumberHeading')}
        </div>,
      ]
    : []),
  ...headingRow.map((headingComponent) => {
    const { colSpan, row, rowSpan } = headingComponent
    const { nodeDefinition } = headingComponent
    const { id: nodeDefId } = nodeDefinition
    const col = columnCalculator(headingComponent)

    const columnInfo = columnInfoByDefId[nodeDefId]
    const { relevant } = columnInfo ? columnInfo : { relevant: true }

    return (
      <div
        key={`heading-cell-${row}-${nodeDefId}`}
        className={classNames('grid-cell', { 'not-relevant': !relevant })}
        style={{
          gridRowStart: row,
          gridRowEnd: row + rowSpan,
          gridColumnStart: col + (includeRowNumberColumn ? 1 : 0),
          gridColumnEnd: col + colSpan + (includeRowNumberColumn ? 1 : 0),
        }}
      >
        <div style={{ width: '100%' }}>
          <div style={{ textAlign: 'center' }}>{getHeadingLabel({ headingComponent })}</div>
          {isCompositeAttribute(nodeDefinition) && (
            <div style={{ display: 'flex' }}>
              {nodeDefinition.availableFieldNames.map((fieldName) => (
                <div
                  key={fieldName}
                  style={{ width: FieldsSizes.getFieldWidthPx({ fieldDef: headingComponent, fieldName }) }}
                >
                  {nodeDefinition.getFieldLabel(fieldName) ||
                    L.l(
                      `dataManagement.dataEntry.attribute.${nodeDefinition.attributeType.toLocaleLowerCase()}.${fieldName.toLocaleLowerCase()}`
                    )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    )
  }),
  ...(firstRow && includeDeleteColumn
    ? [
        <div
          key="heading-cell-delete"
          className="grid-cell"
          style={{
            gridRowStart: 1,
            gridRowEnd: totalHeadingRows + 1,
            gridColumnStart: totalHeadingColumns + 1 + (includeRowNumberColumn ? 1 : 0),
            gridColumnEnd: totalHeadingColumns + 1 + (includeRowNumberColumn ? 1 : 0),
          }}
        />,
      ]
    : []),
]

export default HeadingRow
