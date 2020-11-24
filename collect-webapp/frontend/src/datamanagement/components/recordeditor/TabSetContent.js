import React, { useEffect, useRef, useState } from 'react'
import { TabContent, TabPane, Nav, NavItem, NavLink } from 'reactstrap'
import classnames from 'classnames'

import { MultipleFieldsetDefinition } from 'model/ui/MultipleFieldsetDefinition'
import { useWindowResize } from 'common/hooks'

import Tab from './Tab'

const TabSetContent = (props) => {
  const { tabSetDef, parentEntity } = props
  const { tabs } = tabSetDef

  const version = parentEntity.record.version
  const tabsInVersion = tabs.filter((tabDef) => tabDef.isInVersion(version))

  const hasTabs = Boolean(tabsInVersion.length)
  const firstTabDefId = hasTabs ? tabsInVersion[0].id : null

  const [activeTab, setActiveTab] = useState(firstTabDefId)

  const wrapperRef = useRef()

  const adjustSize = () => {
    const wrapper = wrapperRef.current
    if (wrapper) {
      const totalHeight = wrapper.parentElement.clientHeight
      wrapper.style.height = totalHeight + 'px'

      const [navTabEl, tabContentEl] = wrapper.children
      if (tabContentEl) {
        const height = totalHeight - navTabEl.clientHeight
        tabContentEl.style.height = height + 'px'
      }
    }
  }

  useWindowResize(adjustSize)

  useEffect(() => {
    adjustSize()
  }, [wrapperRef])

  if (!hasTabs) return null

  return (
    <div className="tabset-wrapper" ref={wrapperRef}>
      <Nav tabs>
        {tabsInVersion.map((tabDef) => (
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
        {tabsInVersion.map((tabDef) => {
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
