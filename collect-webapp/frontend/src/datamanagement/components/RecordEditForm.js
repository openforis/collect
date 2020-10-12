import React from 'react'
import TabSet from './recordeditor/TabSet'

import MaxAvailableSpaceContainer from 'common/components/MaxAvailableSpaceContainer'

export default (props) => {
  const { record } = props
  if (!record) {
    return <div>Loading...</div>
  }
  const { survey, rootEntity } = record
  const { uiConfiguration } = survey
  const tabSetDefinition = uiConfiguration.getTabSetByRootEntityDefinitionId(rootEntity.definition.id)

  return (
    <MaxAvailableSpaceContainer>
      <TabSet tabSetDef={tabSetDefinition} record={record} />
    </MaxAvailableSpaceContainer>
  )
}
