import React, { Component, PropTypes } from 'react'
import { TabContent, TabPane, Nav, NavItem, NavLink, Card, Button, CardTitle, CardText, Row, Col } from 'reactstrap';
import classnames from 'classnames';
import Tab from './Tab'
import FormItems from './FormItems'

export default class Fieldset extends Component {

    render() {
        let fieldsetDef = this.props.fieldsetDef

        let navItems = fieldsetDef.tabs.map(tabDef => 
            <NavItem key={tabDef.id}>
                <NavLink className={classnames({ active: this.state.activeTab === tabDef.id })}
                    onClick={() => { this.toggle(tabDef.id); }} 
                >{tabDef.label}</NavLink>
            </NavItem>
        )
        let tabPanes = fieldsetDef.tabs.map(tabDef => 
            <TabPane key={tabDef.id} tabId={tabDef.id}>
                <Tab tabDef={tabDef} parentEntity={this.props.parentEntity} />
            </TabPane>
        )
        return (
            <fieldset>
                <legend>{fieldsetDef.label}</legend>
                <FormItems itemDefs={fieldsetDef.items} parentEntity={this.props.parentEntity} />
                <Nav tabs>
                    {navItems}
                </Nav>
                <TabContent activeTab={this.state.activeTab}>
                    {tabPanes}
                </TabContent>
            </fieldset>
        )
    }
}