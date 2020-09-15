import React from 'react'
import TabSet from './recordeditor/TabSet'

export default (props) => {
  const { record } = props
  if (!record) {
    return <div>Loading...</div>
  }
  const { survey } = record
  const { uiConfiguration } = survey
  const tabSetDefinition = uiConfiguration.getTabSetByRootEntityDefinitionId(record.rootEntity.definition.id)

  return <TabSet tabSetDef={tabSetDefinition} record={record} />
}
