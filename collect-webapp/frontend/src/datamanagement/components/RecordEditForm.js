import React from 'react'

import MaxAvailableSpaceContainer from 'common/components/MaxAvailableSpaceContainer'
import TabSet from './recordeditor/TabSet'
import RecordEditActionBar from './recordeditor/RecordEditActionBar'

const RecordEditForm = (props) => {
  const { record, inPopUp } = props
  if (!record) {
    return <div>Loading...</div>
  }

  const { survey, rootEntity } = record
  const { uiConfiguration } = survey
  const tabSetDefinition = uiConfiguration.getTabSetByRootEntityDefinitionId(rootEntity.definition.id)

  return (
    <>
      <MaxAvailableSpaceContainer className="record-edit-form">
        <RecordEditActionBar record={record} inPopUp={inPopUp} />
        <TabSet tabSetDef={tabSetDefinition} record={record} />
      </MaxAvailableSpaceContainer>
    </>
  )
}

RecordEditForm.defaultProps = {
  inPopUp: false,
}

export default RecordEditForm
