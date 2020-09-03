import React, { Component } from 'react'
import { TabContent, TabPane, Nav, NavItem, NavLink, Card, Button, CardTitle, CardText, Row, Col } from 'reactstrap'
import classnames from 'classnames'
import FormItems from './FormItems'

export default class Tab extends Component {
  constructor(props) {
    super(props)

    let tabs = props.tabDef.tabs
    let firstTabId = tabs.length > 0 ? tabs[0].id : null
    this.state = {
      activeTab: firstTabId,
    }

    this.toggle = this.toggle.bind(this)
  }

  toggle(tabId) {
    if (this.state.activeTab !== tabId) {
      this.setState({ ...this.state, activeTab: tabId })
    }
  }

  render() {
    const { rootTabDef, parentEntity } = this.props
    const { activeTab } = this.state

    if (!parentEntity) {
      return <div>Loading...</div>
    }

    let navItems = rootTabDef.tabs.map((tabDef) => (
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
    let tabPanes = rootTabDef.tabs.map((tabDef) => (
      <TabPane key={tabDef.id} tabId={tabDef.id}>
        <Tab tabDef={tabDef} parentEntity={parentEntity} />
      </TabPane>
    ))
    return (
      <div>
        <FormItems itemDefs={rootTabDef.items} parentEntity={parentEntity} />
        <Nav tabs>{navItems}</Nav>
        <TabContent activeTab={activeTab}>{tabPanes}</TabContent>
      </div>
    )
  }
}
