import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';

import ServiceFactory from 'services/ServiceFactory'
import JobMonitorModal from 'containers/job/JobMonitorModal'
import SchemaTreeView from './SchemaTreeView'


const additionalOptions = [
    {
        name: 'includeKMLColumnForCoordinates',
        label: 'Include KML column for coordinate attributes (for Fusion Tables)*'
    },
    {
        name: 'includeAllAncestorAttributes',
        label: 'Include all ancestor attributes*'
    },
    {
        name: 'includeCompositeAttributeMergedColumn',
        label: 'Include composite attributes merged column (e.g. date, time, coordinate)*'
    },
    {
        name: 'codeAttributeExpanded',
        label: 'Expand code attributes (add boolean columns for each code value)*'
    },
    {
        name: 'includeCodeItemLabelColumn',
        label: 'Include code item label column*'
    }
]

class DataExportPage extends Component {

    

    constructor(props) {
        super(props)

        this.state = {
            exportFormat: null,
            jobStatusModalOpen: false,
            csvDataExportJobId: null,
            allEntitiesSelected: false,
            stepGreaterOrEqual: 'ENTRY',
            selectedEntityDefinition: null,
            entityId: null,
            exportOnlyOwnedRecords: false,
            headingSource: 'ATTRIBUTE_NAME',
            additionalOptionsOpen: false
        }

        this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
        this.handleCsvDataExportModalOkButtonClick = this.handleCsvDataExportModalOkButtonClick.bind(this)
        this.handleEntityCheck = this.handleEntityCheck.bind(this)
    }

    handleExportButtonClick() {
        switch (this.state.exportFormat) {
            case 'CSV':
                const survey = this.props.survey
                const surveyId = survey.id
                const parameters = {
                    surveyId: survey.id,
                    rootEntityId: survey.schema.defaultRootEntity.id,
                    stepGreaterOrEqual: this.state.step,
                    entityId: this.state.selectedEntityDefinition ? this.state.selectedEntityDefinition.id: null,
                    exportOnlyOwnedRecords: this.state.exportOnlyOwnedRecords,
                    headingSource: this.state.headingSource,
                    alwaysGenerateZipFile: true
                }
                additionalOptions.forEach(o => {
                    parameters[o.name] = this.state[o.name]
                })
                ServiceFactory.recordService.startCSVDataExport(surveyId, parameters).then(job => {
                    this.setState({
                        jobStatusModalOpen: true,
                        csvDataExportJobId: job.id
                    })
                })
                break;
            case 'BACKUP':
                break;
        }
    }

    handleCsvDataExportModalOkButtonClick(job) {
        const survey = this.props.survey
        const surveyId = survey.id
        ServiceFactory.recordService.downloadCSVDataExportResult(surveyId)
        this.setState({ jobStatusModalOpen: false })
    }

    handleEntityCheck(event) {
        this.setState({...this.state, selectedEntityDefinition: event.selectedNodeDefinitions.length == 1 ? event.selectedNodeDefinitions[0]: null})
    }

    render() {
        if (!this.props.survey) {
            return <div>Select survey first</div>
        }
        const additionalOptionsFormGroups = additionalOptions.map(o => {
            return <FormGroup check key={o.name}>
                <Label check>
                    <Input type="checkbox" onChange={event => {
                        const newProp = new Object()
                        newProp[o.name] = event.target.checked
                        this.setState(newProp)
                     }} />{' '}
                    {o.label}
                </Label>
            </FormGroup>
        });

        const steps = ['ENTRY', 'CLEANSING', 'ANALYSIS']
        const stepsOptions = steps.map(s => <option key={s} value={s}>{s}</option>)
        let parametersForm = null;
        switch (this.state.exportFormat) {
            case 'CSV':
                parametersForm =
                    <FormGroup>
                        <FormGroup row>
                            <Label for="stepSelect">Step:</Label>
                            <Col sm={10}>
                                <Input type="select" name="step" id="stepSelect" style={{ maxWidth: '100px' }} 
                                    onChange={e => this.setState({step: e.target.value})}>{stepsOptions}</Input>
                            </Col>
                        </FormGroup>
                        <FormGroup row>
                            <Label>Entity:</Label>
                            <Col sm={{size: 10 }}>
                                <SchemaTreeView survey={this.props.survey} selectAll={this.state.allEntitiesSelected}
                                    handleCheck={this.handleEntityCheck} />
                            </Col>
                        </FormGroup>
                        <FormGroup row>
                            <Col sm={{offset: 1, size: 10 }}>
                                <FormGroup check>
                                    <Label check>
                                        <Input type="checkbox"
                                            onChange={event => this.setState({allEntitiesSelected: event.target.checked})} />{' '}
                                        Export all entities
                                    </Label>
                                </FormGroup>
                            </Col>
                        </FormGroup>
                        <FormGroup row>
                            <div>
                                <Button onClick={e => this.setState({additionalOptionsOpen: ! this.state.additionalOptionsOpen})}>Additional Options</Button>
                                <Collapse isOpen={this.state.additionalOptionsOpen}>
                                    <Card>
                                        <CardBlock>
                                            <FormGroup row>
                                                <Col sm={{size: 12}}>
                                                    <Label check>
                                                        <Input type="checkbox" onChange={event => this.setState({exportOnlyOwnedRecords: event.target.checked})} />{' '}
                                                        Export only owned records
                                                    </Label>
                                                </Col>
                                            </FormGroup>
                                            <FormGroup row>
                                                <Col sm={4}>
                                                    <Label for="headingsSourceSelect">Source for file headings:</Label>
                                                </Col>
                                                <Col sm={8}>
                                                    <Input type="select" name="headingsSource" id="headingsSourceSelect" style={{ maxWidth: '200px' }} 
                                                        onChange={e => this.setState({headingSource: e.target.value})}>
                                                        <option value="ATTRIBUTE_NAME">Attribute name</option>
                                                        <option value="INSTANCE_LABEL">Attribute label</option>
                                                        <option value="REPORTING_LABEL">Reporting label (Saiku)</option>
                                                    </Input>
                                                </Col>
                                            </FormGroup>
                                            {additionalOptionsFormGroups}
                                        </CardBlock>
                                    </Card>
                                </Collapse>
                            </div>
                        </FormGroup>
                    </FormGroup>
                break
            case 'BACKUP':
                parametersForm =
                    <FormGroup tag="fieldset">
                        <legend>Parameters</legend>
                    </FormGroup>
                break
        }
        return (
            <Form>
                <FormGroup tag="fieldset">
                    <legend>Export Format</legend>
                    <FormGroup check>
                        <Label check>
                            <Input type="radio" value="CSV" name="exportFormat" id="exportFormatCSVRadioButton"
                                checked={this.state.exportFormat === 'CSV'}
                                onChange={(event) => this.setState({ ...this.state, exportFormat: event.target.value })} />{' '}
                            CSV
                        </Label>
                    </FormGroup>
                    <FormGroup check>
                        <Label check>
                            <Input type="radio" value="BACKUP" name="exportFormat" id="exportFormatBackupRadioButton"
                                checked={this.state.exportFormat === 'BACKUP'}
                                onChange={(event) => this.setState({ ...this.state, exportFormat: event.target.value })} />{' '}
                            Backup (.collect-data)
                        </Label>
                    </FormGroup>
                </FormGroup>
                {parametersForm}
                <Row>
                    <Col sm={{offset: 1, size: 4}} colSpan={4}>
                        <Button onClick={this.handleExportButtonClick} className="btn btn-success">Export</Button>
                    </Col>
                </Row>
                <JobMonitorModal
                    open={this.state.jobStatusModalOpen}
                    title="Exporting CSV data"
                    jobId={this.state.csvDataExportJobId}
                    okButtonLabel={'Download'}
                    handleOkButtonClick={this.handleCsvDataExportModalOkButtonClick}
                />
            </Form>
        )
    }
}



const mapStateToProps = state => {
    const { survey } = state.preferredSurvey

    return { survey: survey }
}

export default connect(mapStateToProps)(DataExportPage);