import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';
import Dropzone from 'react-dropzone';

import ServiceFactory from 'services/ServiceFactory'
import JobMonitorModal from 'components/JobMonitorModal'

class DataImportPage extends Component {

    constructor(props) {
        super(props)

        this.state = {
            importFormat: null,
            jobStatusModalOpen: false,
            dataImportSummaryJobId: null,
            dataImportJobId: null,
            fileToBeImportedPreview: null,
            fileToBeImported: null
        }

        this.handleImportButtonClick = this.handleImportButtonClick.bind(this)
        this.onFileDrop = this.onFileDrop.bind(this)
    }

    handleImportButtonClick() {
        switch (this.state.importFormat) {
            case 'CSV':
                break
            case 'BACKUP':
                ServiceFactory.recordService.generateBackupDataImportSummary(this.props.survey, 
                    this.props.survey.schema.firstRootEntityDefinition.name).then(job => {
                        this.setState({jobStatusModalOpen: true, dataImportSummaryJobId: job.id})
                    })
                break
        }
    }

    onFileDrop(files) {
        switch (this.state.importFormat) {
            case 'CSV':
                break
            case 'BACKUP':
                const file = files[0]
                this.setState({fileToBeImported: file, fileToBeImportedPreview: file.name})
                break
        }
    }

    render() {
        let parametersForm = null
        switch(this.state.importFormat) {
            case 'CSV':
                break
            case 'BACKUP':
                parametersForm = 
                    <FormGroup>
                        <FormGroup row>
                            <Label for="file">File:</Label>
                            <Col sm={10}>
                                <Dropzone onDrop={(files) => this.onFileDrop(files)}>
                                    <div>Drop the file here, or click to select a file to upload.</div>
                                </Dropzone>
                            </Col>
                        </FormGroup>
                    </FormGroup>
                break
        }

        return 
        <Form>
            <FormGroup tag="fieldset">
                <legend>Import from</legend>
                <FormGroup check>
                    <Label check>
                        <Input type="radio" value="BACKUP" name="importFormat"
                            checked={this.state.importFormat === 'BACKUP'}
                            onChange={(event) => this.setState({ ...this.state, importFormat: event.target.value })} />{' '}
                        Backup (.collect-data)
                    </Label>
                </FormGroup>
                <FormGroup check>
                    <Label check>
                        <Input type="radio" value="CSV" name="importFormat"
                            checked={this.state.importFormat === 'CSV'}
                            onChange={(event) => this.setState({ ...this.state, importFormat: event.target.value })} />{' '}
                        CSV
                    </Label>
                </FormGroup>                
            </FormGroup>
            {parametersForm}
            <Row>
                <Col sm={{offset: 1, size: 4}} colSpan={4}>
                    <Button onClick={this.handleImportButtonClick} className="btn btn-success">Import</Button>
                </Col>
            </Row>
            <JobMonitorModal
                open={this.state.jobStatusModalOpen}
                title="Importing data"
                jobId={this.state.dataImportSummaryJobId}
                okButtonLabel={'Done'}
            />
        </Form>
    }
}

const mapStateToProps = state => {
    const { survey } = state.preferredSurvey

    return { survey: survey }
}

export default connect(mapStateToProps)(DataImportPage);