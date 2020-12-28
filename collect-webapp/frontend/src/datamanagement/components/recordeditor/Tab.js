import React from 'react'

import FormItems from './FormItems'
import TabSetContent from './TabSetContent'

const Tab = (props) => {
  const { tabDef, parentEntity } = props

  if (!parentEntity) {
    return <div>Loading...</div>
  }

  return (
    <>
      <FormItems parentItemDef={tabDef} parentEntity={parentEntity} />
      <TabSetContent tabSetDef={tabDef} parentEntity={parentEntity} />
    </>
  )
}

export default Tab
