import React, { Component } from 'react';
import { connect } from 'react-redux'
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import { Button, ButtonGroup, ButtonToolbar, Container, ButtonDropdown, 
	DropdownToggle, DropdownMenu, DropdownItem, Row, Col } from 'reactstrap';

import * as Formatters from 'components/datatable/formatters'

class SurveysListPage extends Component {

    constructor(props) {
        super(props)

        this.state = {

        }
    }

    render() {
        if (this.props.surveySummaries == null) {
            return <div>Loading...</div>
        }
        return (
            <Container fluid>
                <BootstrapTable
                    data={this.props.surveySummaries}
                    striped hover condensed
                    height="100%"
                    selectRow={{
                        mode: 'checkbox', clickToSelect: true, hideSelectionColumn: true, bgColor: 'lightBlue',
                        selected: this.props.selectedItemIds
                    }}
                >
                    <TableHeaderColumn key="id" dataField="id" isKey hidden dataAlign="center">Id</TableHeaderColumn>
                    <TableHeaderColumn key="name" dataField="name" filter={{type: 'TextFilter'}} dataSort>Name</TableHeaderColumn>
                    <TableHeaderColumn key="projectName" dataField="projectName" filter={{type: 'TextFilter'}} dataSort>Project</TableHeaderColumn>
                    <TableHeaderColumn key="modifiedDate" dataField="modifiedDate" dataFormat={Formatters.dateTimeFormatter}
				        dataAlign="center" width="110" dataSort>Last Modified</TableHeaderColumn>
                </BootstrapTable>
            </Container>
        )
    }

}


const mapStateToProps = state => {
	return {
		survey: state.preferredSurvey ? state.preferredSurvey.survey : null,
		users: state.users ? state.users.users : null,
		userGroups: state.userGroups ? state.userGroups.userGroups : null,
		loggedUser: state.session ? state.session.loggedUser : null,
		surveySummaries: state.surveySummaries ? state.surveySummaries.items : null
	}
}

export default connect(mapStateToProps)(SurveysListPage)
    