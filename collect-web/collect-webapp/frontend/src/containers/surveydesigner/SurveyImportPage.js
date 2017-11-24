import React, { Component } from 'react';
import { Button, Col, Container, Row } from 'reactstrap';
import L from 'utils/Labels'

import SurveyImportForm from './SurveyImportForm';

export default class SurveyImportPage extends Component {
    constructor(props) {
        super(props)
    }

    render() {
        return (
            <Container fluid>
                <Row>
                    <Col sm={{ size: 6, offset: 3 }}>
                        <SurveyImportForm style={{width: "500px"}} />
                    </Col>
                </Row>
            </Container>
        )
    }

}