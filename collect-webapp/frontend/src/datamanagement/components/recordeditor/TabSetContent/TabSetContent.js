import React, { useCallback, useEffect, useRef, useState } from 'react'
import { TabContent, TabPane, Nav, NavItem, NavLink } from 'reactstrap'
import classnames from 'classnames'

import { MultipleFieldsetDefinition } from 'model/ui/MultipleFieldsetDefinition'

import Tab from '../Tab'
import useTabSetContent from './useTabSetContent'

const TabSetContent = (props) => {
  const { parentEntity } = props

  const { activeTab, setActiveTab, tabs, wrapperRef } = useTabSetContent(props)

  if (tabs.length === 0) return null

  return (
    <div className="tabset-wrapper" ref={wrapperRef}>
      <Nav tabs>
        {tabs.map((tabDef) => (
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
        {tabs.map((tabDef) => {
          const onlyOneMultipleFieldset =
            tabDef.items.length === 1 && tabDef.items[0] instanceof MultipleFieldsetDefinition
          return (
            <TabPane
              key={tabDef.id}
              tabId={tabDef.id}
              className={classnames({ 'only-one-multiple-fieldset': onlyOneMultipleFieldset })}
            >
              <Tab tabDef={tabDef} parentEntity={parentEntity} />
            </TabPane>
          )
        })}
      </TabContent>
    </div>
  )
}

export default TabSetContent
