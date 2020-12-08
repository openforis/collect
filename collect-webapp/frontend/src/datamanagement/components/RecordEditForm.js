import React from 'react'

import MaxAvailableSpaceContainer from 'common/components/MaxAvailableSpaceContainer'
import TabSet from './recordeditor/TabSet'
import RecordEditActionBar from './recordeditor/RecordEditActionBar'

const RecordEditForm = (props) => {
  const { record } = props
  if (!record) {
    return <div>Loading...</div>
  }

  const { survey, rootEntity } = record
  const { uiConfiguration } = survey
  const tabSetDefinition = uiConfiguration.getTabSetByRootEntityDefinitionId(rootEntity.definition.id)

  return (
    <>
      <MaxAvailableSpaceContainer className="record-edit-form">
        <RecordEditActionBar record={record} />
        <TabSet tabSetDef={tabSetDefinition} record={record} />
      </MaxAvailableSpaceContainer>
    </>
  )
}

export default RecordEditForm
