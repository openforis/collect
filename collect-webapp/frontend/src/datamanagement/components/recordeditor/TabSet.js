import React, { Component } from 'react'
import { TabContent, TabPane, Nav, NavItem, NavLink } from 'reactstrap'
import classnames from 'classnames'

import Tab from './Tab'

export default class TabSet extends Component {
  constructor(props) {
    super()
    const { tabSetDef } = props
    const { tabs } = tabSetDef
    const firstTabId = tabs.length > 0 ? tabs[0].id : null
    this.toggle = this.toggle.bind(this)
    this.state = {
      activeTab: firstTabId,
    }
  }

  toggle(tabId) {
    if (this.state.activeTab !== tabId) {
      this.setState({ activeTab: tabId })
    }
  }

  render() {
    const { tabSetDef, record } = this.props
    const { activeTab } = this.state

    const navItems = tabSetDef.tabs.map((tabDef) => (
      <NavItem key={tabDef.id}>
        <NavLink
          className={classnames({ active: activeTab === tabDef.id })}
          onClick={() => {
            this.toggle(tabDef.id)
          }}
        >
          {tabDef.label}
        </NavLink>
      </NavItem>
    ))
    const tabPanes = tabSetDef.tabs.map((tabDef) => (
      <TabPane key={tabDef.id} tabId={tabDef.id}>
        <Tab tabDef={tabDef} parentEntity={record.rootEntity} />
      </TabPane>
    ))
    return (
      <div>
        <Nav tabs>{navItems}</Nav>
        <TabContent activeTab={activeTab}>{tabPanes}</TabContent>
      </div>
    )
  }
}
