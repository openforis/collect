import React, { useState } from 'react'
import { TabContent, TabPane, Nav, NavItem, NavLink } from 'reactstrap'
import classnames from 'classnames'
import Tab from './Tab'
import FormItems from './FormItems'

const Fieldset = (props) => {
  const { fieldsetDef, parentEntity } = props

  const [activeTab, setActiveTab] = useState(null)

  return (
    <fieldset>
      <legend>{fieldsetDef.label}</legend>
      <FormItems itemDefs={fieldsetDef.items} parentEntity={parentEntity} />
      <Nav tabs>
        {fieldsetDef.tabs.map((tabDef) => (
          <NavItem key={tabDef.id}>
            <NavLink
              className={classnames({ active: activeTab === tabDef.id })}
              onClick={() => setActiveTab(tabDef.id)}
            >
              {tabDef.label}
            </NavLink>
          </NavItem>
        ))}
      </Nav>
      <TabContent activeTab={activeTab}>
        {fieldsetDef.tabs.map((tabDef) => (
          <TabPane key={tabDef.id} tabId={tabDef.id}>
            <Tab tabDef={tabDef} parentEntity={parentEntity} />
          </TabPane>
        ))}
      </TabContent>
    </fieldset>
  )
}

export default Fieldset
