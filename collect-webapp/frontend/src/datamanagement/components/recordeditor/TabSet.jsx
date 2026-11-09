import React from 'react'

import TabSetContent from './TabSetContent'

const TabSet = (props) => {
  const { tabSetDef, record } = props

  return <TabSetContent tabSetDef={tabSetDef} parentEntity={record.rootEntity} />
}

export default TabSet
