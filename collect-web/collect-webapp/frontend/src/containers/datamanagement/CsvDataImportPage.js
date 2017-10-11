import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';
import Dropzone from 'react-dropzone';

import ServiceFactory from 'services/ServiceFactory';
import SchemaTreeView from './SchemaTreeView';
import Workflow from 'model/Workflow';
import Arrays from 'utils/Arrays'
import * as Actions from 'actions';

class CsvDataImportPage extends Component {

    
    constructor(props) {
        super(props)

        this.handleStepCheck = this.handleStepCheck.bind(this)
        this.handleEntitySelect = this.handleEntitySelect.bind(this)
        this.handleImportButtonClick = this.handleImportButtonClick.bind(this)
        this.handleFileDrop = this.handleFileDrop.bind(this)
        this.handleDataImportComplete = this.handleDataImportComplete.bind(this)
        this.handleJobModalOkButtonClick = this.handleJobModalOkButtonClick.bind(this)

        this.state = {
            importType: 'update',
            selectedSteps: Object.keys(Workflow.STEPS).map(s => Workflow.STEPS[s].code),
            validateRecords: true,
            deleteEntitiesBeforeImport: false,
            newRecordVersionName: null,
            fileSelected: false,
            fileToBeImportedPreview: null,
            fileToBeImported: null
        }
    }

    handleEntitySelect(event) {
        this.setState({...this.state, selectedEntityDefinition: event.selectedNodeDefinitions.length == 1 ? event.selectedNodeDefinitions[0]: null})
    }

    handleStepCheck(event) {
        const newSelectedSteps = Arrays.addOrRemoveItem(this.state.selectedSteps, event.target.value, ! event.target.checked)
        this.setState({selectedSteps: newSelectedSteps})
    }

    handleImportButtonClick() {
        const entityDef = this.state.selectedEntityDefinition
        if (this.state.importType == 'update' && entityDef == null) {
            return
        }
        const entityDefId = entityDef == null ? null : entityDef.id
        const survey = this.props.survey
        
        ServiceFactory.recordService.startCsvDataImport(survey, 
            survey.schema.firstRootEntityDefinition.name,
            this.state.fileToBeImported,
            this.state.importType,
            this.state.selectedSteps,
            entityDefId,
            this.state.validateRecords,
            this.state.deleteEntitiesBeforeImport,
            this.state.newRecordVersionName
        ).then(job => {
            this.props.dispatch(Actions.startJobMonitor(job.id, 'Importing data', 'Ok', this.handleJobModalOkButtonClick, null, this.handleDataImportComplete))
        })
    }

    handleJobModalOkButtonClick() {
        this.props.dispatch(Actions.closeJobMonitor())
    }

    handleDataImportComplete(job) {
        if (! job.completed) {
            this.loadErrorsPage()
        }
    }

    handleFileDrop(files) {
        const file = files[0]
        this.setState({fileSelected: true, fileToBeImported: file, fileToBeImportedPreview: file.name})
    }

    render() {
        const survey = this.props.survey
        if (survey == null) {
            return <div>Select a survey first</div>
        }

        const IMPORT_TYPES = {
            update: {
                label: 'Update existing records'
            },
            newRecords: {
                label: 'Insert new records'
            }, 
            multipleFiles: {
                label: 'Import multiple CSV/Excel files in a single ZIP file'
            }
        }
        const importTypeOptions = Object.keys(IMPORT_TYPES).map(type =>
            <option key={type} value={type}>{IMPORT_TYPES[type].label}</option> 
        )
        
        const steps = Workflow.STEPS 
        const stepsChecks = Object.keys(steps).map(s => {
            const checked = this.state.selectedSteps.indexOf(steps[s].code) >= 0
            return (
                <Label check key={s}>
                    <Input type="checkbox" value={steps[s].code} checked={checked}
                        onChange={this.handleStepCheck} /> {steps[s].label}
                </Label>
            )
        })
        const entitySelectionEnabled = this.state.importType == 'update'

        return (
            <Form>
                <FormGroup tag="fieldset">
                    <legend>Parameters</legend>
                    <FormGroup row>
                        <Label for="stepSelect">Import type:</Label>
                        <Col sm={10}>
                            <Input type="select" name="step" id="stepSelect"
                                value={this.state.importType} 
                                onChange={e => this.setState({importType: e.target.value})}>{importTypeOptions}</Input>
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label>Apply to step:</Label>
                        <Col sm={10}>
                            {stepsChecks}
                        </Col>
                    </FormGroup>
                    <FormGroup row check>
                        <Label check>
                            <Input type="checkbox" checked={this.state.validateRecords}
                                onChange={e => this.setState({validateRecords: e.target.checked})} /> Validate records after import
                        </Label>
                    </FormGroup>
                    <FormGroup row check>
                        <Label check>
                            <Input type="checkbox" checked={this.state.deleteEntitiesBeforeImport}
                                onChange={e => this.setState({deleteEntitiesBeforeImport: e.target.checked})} /> Delete entities before import
                        </Label>
                    </FormGroup>
                </FormGroup>
                {entitySelectionEnabled ?
                    <FormGroup row>
                        <Label>Entity:</Label>
                        <Col sm={{size: 10 }}>
                            <SchemaTreeView survey={this.props.survey}
                                handleNodeSelect={this.handleEntitySelect} />
                        </Col>
                    </FormGroup>
                    : ''
                }
                <FormGroup row>
                    <Label for="file">File:</Label>
                    <Col sm={10}>
                        <Dropzone accept=".csv,.xls,xlsx,.zip" onDrop={(files) => this.handleFileDrop(files)} style={{
                            width: '100%', height: '200px', 
                            borderWidth: '2px', borderColor: 'rgb(102, 102, 102)', 
                            borderStyle: 'dashed', borderRadius: '5px'
                        }}>
                        {this.state.fileToBeImportedPreview ?
                            <p style={{fontSize: '2em', textAlign: 'center'}}><span className="checked large" />{this.state.fileToBeImportedPreview}</p>
                            : <p>Click to select a CSV (.csv), MS Excel (.xls, .xlsx), or ZIP (.zip) file or drop it here.</p>
                        }
                        </Dropzone>
                    </Col>
                </FormGroup>
                <FormGroup row>
                    <Col sm={{size: 2, offset: 5}}>
                        <Button color="primary" onClick={this.handleImportButtonClick}>Import</Button>
                    </Col>
                </FormGroup>
            </Form>
        )
    }
}

const mapStateToProps = state => {
    const { survey } = state.preferredSurvey

    return { survey: survey }
}

export default connect(mapStateToProps)(CsvDataImportPage);