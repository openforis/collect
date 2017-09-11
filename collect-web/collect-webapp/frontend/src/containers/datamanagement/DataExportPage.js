import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux'
import Collapsible from 'react-collapsible';

import ServiceFactory from 'services/ServiceFactory'
import JobMonitorModal from 'containers/job/JobMonitorModal'
import SchemaTreeView from './SchemaTreeView'

class DataExportPage extends Component {

    constructor(props) {
        super(props)

        this.state = {
            exportFormat: null,
            jobStatusModalOpen: false,
            csvDataExportJobId: null,
            allEntitiesSelected: false,
            selectedEntityDefinition: null,
            additionalCsvExportOption: null
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
                const rootEntityId = survey.schema.defaultRootEntity.id
                const entityId = this.state.selectedEntityDefinition ? this.state.selectedEntityDefinition.id: null
                const step = this.state.step
                ServiceFactory.recordService.startCSVDataExport(surveyId, rootEntityId, entityId, step).then(job => {
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
        const jobStatusMonitor = this.state.jobStatusModalOpen ? <JobMonitorModal
            title="Exporting CSV data"
            jobId={this.state.csvDataExportJobId} open={true}
            okButtonLabel={'Download'}
            handleOkButtonClick={this.handleCsvDataExportModalOkButtonClick}
        /> : null

        const additionalOptions = [
            {
                name: 'includeKMLColumnForCoordinates',
                label: 'Include KML column for coordinate attributes (for Fusion Tables)*'
            },
            {
                name: 'includeAllAncestorAttributes',
                label: 'Include all ancestor attributes'
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
                label: 'Include code item label column'
            }
        ]

        const additionalOptionsFormGroups = additionalOptions.map(o => {
            const createNewAdditionalCsvExportOptions = function(value) {
                let newAdditionalCsvExportOptions = {...this.state.additionalCsvExportOption}
                newAdditionalCsvExportOptions[o.name] = value
            }
            
            return <FormGroup check>
                <Label check>
                    <Input type="checkbox" onChange={event => this.setState(
                            {additionalCsvExportOptions: createNewAdditionalCsvExportOptions.apply(this, [event.target.checked])}
                        )} />{' '}
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
                            <Collapsible trigger="Additional options">
                                {additionalOptionsFormGroups}
                            </Collapsible>
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
                        <Button onClick={this.handleExportButtonClick}>Export</Button>
                    </Col>
                </Row>
                {jobStatusMonitor}
            </Form>
        )
    }
}



const mapStateToProps = state => {
    const { survey } = state.preferredSurvey

    return { survey: survey }
}

export default connect(mapStateToProps)(DataExportPage);