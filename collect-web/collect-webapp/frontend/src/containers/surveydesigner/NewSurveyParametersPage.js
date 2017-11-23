import React, { Component } from 'react';
import { Button, Col, Container, Row } from 'reactstrap';
import L from 'utils/Labels'

import NewSurveyParametersForm from './NewSurveyParametersForm';

export default class NewSurveyParametersPage extends Component {
    constructor(props) {
        super(props)
    }

    render() {
        const submitting = this.props.submitting
        return (
            <Container fluid>
                <Row>
                    <Col sm={{ size: 6, offset: 3 }}>
                        <NewSurveyParametersForm  style={{width: "500px"}} />
                    </Col>
                </Row>
            </Container>
        )
    }

}