import React, { Component, PropTypes } from 'react'
import { TabContent, TabPane, Nav, NavItem, NavLink, Card, Button, CardTitle, CardText, Row, Col } from 'reactstrap';
import classnames from 'classnames';
import Tab from './Tab'

export default class TabSet extends Component {

    constructor(props) {
        super(props)

        let tabs = props.tabSetDef.tabs
        let firstTabId = tabs.length > 0 ? tabs[0].id : null
        this.toggle = this.toggle.bind(this);
        this.state = {
          activeTab: firstTabId
        };
    }

    toggle(tabId) {
        if (this.state.activeTab !== tabId) {
          this.setState({...this.state, 
            activeTab: tabId
          });
        }
    }

    render() {
        let tabSetDef = this.props.tabSetDef
        let navItems = tabSetDef.tabs.map(tabDef => 
            <NavItem>
                <NavLink className={classnames({ active: this.state.activeTab === tabDef.id })}
                    onClick={() => { this.toggle(tabDef.id); }} 
                >{tabDef.label}</NavLink>
            </NavItem>
        )
        let tabPanes = tabSetDef.tabs.map(tabDef => 
            <TabPane key={tabDef.id} tabId={tabDef.id}>
                <Tab tabDef={tabDef} parentEntity={this.props.record.rootEntity} />
            </TabPane>
        )
        return (
            <div>
                <Nav tabs>
                    {navItems}
                </Nav>
                <TabContent activeTab={this.state.activeTab}>
                    {tabPanes}
                </TabContent>
            </div>
        )
    }

}
    