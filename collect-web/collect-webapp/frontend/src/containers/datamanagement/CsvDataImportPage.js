import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormFeedback, FormGroup, Label, Input, Row, Col, Modal, ModalHeader, ModalBody, ModalFooter } from 'reactstrap';
import { connect } from 'react-redux';
import Dropzone from 'react-dropzone';
import {BootstrapTable, TableHeaderColumn} from 'react-bootstrap-table';

import ServiceFactory from 'services/ServiceFactory';
import SchemaTreeView from './SchemaTreeView';
import Workflow from 'model/Workflow';
import Arrays from 'utils/Arrays'
import * as JobActions from 'actions/job';
import L from 'utils/Labels';

class CsvDataImportPage extends Component {

    
    constructor(props) {
        super(props)

        this.handleStepCheck = this.handleStepCheck.bind(this)
        this.handleEntitySelect = this.handleEntitySelect.bind(this)
        this.handleImportButtonClick = this.handleImportButtonClick.bind(this)
        this.handleFileDrop = this.handleFileDrop.bind(this)
        this.handleJobModalOkButtonClick = this.handleJobModalOkButtonClick.bind(this)
        this.handleDataImportComplete = this.handleDataImportComplete.bind(this)
        this.handleDataImoprtJobFailed = this.handleDataImoprtJobFailed.bind(this)
        this.handleErrorsModalCloseButtonClick = this.handleErrorsModalCloseButtonClick.bind(this)

        this.state = {
            importType: 'update',
            selectedSteps: Object.keys(Workflow.STEPS).map(s => Workflow.STEPS[s].code),
            validateRecords: true,
            deleteEntitiesBeforeImport: false,
            newRecordVersionName: null,
            fileSelected: false,
            fileToBeImportedPreview: null,
            fileToBeImported: null,
            errorModalOpen: false,
            selectedEntityDefinition: null,
            additionalOptionsOpen: false
        }
    }

    handleEntitySelect(event) {
        this.setState({...this.state, selectedEntityDefinition: event.selectedNodeDefinitions.length === 1 ? event.selectedNodeDefinitions[0]: null})
    }

    handleStepCheck(event) {
        const newSelectedSteps = Arrays.addOrRemoveItem(this.state.selectedSteps, event.target.value, ! event.target.checked)
        this.setState({selectedSteps: newSelectedSteps})
    }

    handleImportButtonClick() {
        if (!this.validateForm()) {
            return
        }
        const survey = this.props.survey
        const entityDef = this.state.importType === 'newRecords' ? 
            survey.schema.firstRootEntityDefinition
            : this.state.selectedEntityDefinition
        const entityDefId = entityDef === null ? null : entityDef.id
        
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
            this.props.dispatch(JobActions.startJobMonitor({
                jobId: job.id, 
                title: 'Importing data',
                okButtonLabel: 'Ok',                        
                handleOkButtonClick: this.handleJobModalOkButtonClick,
                handleJobCompleted: this.handleDataImportComplete,
                handleJobFailed: this.handleDataImoprtJobFailed
            }))
        })
    }

    validateForm() {
        if (! this.state.fileSelected) {
            alert(L.l('dataManagement.csvDataImport.validation.fileNotSelected'))
            return false
        }
        if (this.state.importType === 'update' && this.state.selectedEntityDefinition === null) {
            alert(L.l('dataManagement.csvDataImport.validation.entityNotSelected'))
            return false
        }
        return true
    }

    handleJobModalOkButtonClick() {
    }

    handleDataImportComplete(job) {
    }

    handleDataImoprtJobFailed(job) {
        this.loadErrorsPage(job)
    }

    handleFileDrop(files) {
        const file = files[0]
        this.setState({fileSelected: true, fileToBeImported: file, fileToBeImportedPreview: file.name})
    }

    loadErrorsPage(job) {
        ServiceFactory.recordService.loadCsvDataImportStatus(this.props.survey).then(job => {
            if (job.errors.length > 0) {
                this.setState({errorModalOpen: true, errors: job.errors})
                setTimeout(() => this.props.dispatch(JobActions.closeJobMonitor()))
            }
        })
    }

    handleErrorsModalCloseButtonClick() {
        this.setState({errorModalOpen: false})
    }

    render() {
        const survey = this.props.survey
        if (survey === null) {
            return <div>Select a survey first</div>
        }

        const IMPORT_TYPES = ['update', 'newRecords', 'multipleFiles']
        const importTypeOptions = IMPORT_TYPES.map(type =>
            <option key={type} value={type}>{L.l('dataManagement.csvDataImport.importType.' + type)}</option> 
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
        const entitySelectionEnabled = this.state.importType === 'update'
        const entityNotSelected = entitySelectionEnabled && this.state.selectedEntityDefinition === null
        const acceptedFileTypes = this.state.importType === 'multipleFiles' ? '.zip': '.csv,.xls,xlsx,.zip'
        const acceptedFileTypesDescription = this.state.importType === 'multipleFiles' ? 'ZIP (.zip)': 'CSV (.csv), MS Excel (.xls, .xlsx), or ZIP (.zip)'

        const formatErrorMessage = function(cell, row) {
            return L.l(row.message, row.messageArgs)
        }
    
        return (
            <Form>
                <FormGroup tag="fieldset">
                    <legend>Parameters</legend>
                    <FormGroup row>
                        <Label for="stepSelect">{L.l('dataManagement.csvDataImport.importType.label')}:</Label>
                        <Col sm={10}>
                            <Input type="select" name="step" id="stepSelect"
                                value={this.state.importType} 
                                onChange={e => this.setState({importType: e.target.value})}>{importTypeOptions}</Input>
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <Label>{L.l('dataManagement.csvDataImport.applyToSteps')}:</Label>
                        <Col sm={10}>
                            {stepsChecks}
                        </Col>
                    </FormGroup>
                    {entitySelectionEnabled &&
                        <FormGroup row>
                            <Label className={entityNotSelected ? 'invalid': ''}>{L.l('dataManagement.csvDataImport.entity')}:</Label>
                            <Col sm={{size: 10 }}>
                                <SchemaTreeView survey={this.props.survey}
                                    handleNodeSelect={this.handleEntitySelect} />
                                <FormFeedback style={entityNotSelected ? {display: 'block'}: {}}>{L.l('dataManagement.csvDataImport.validation.entityNotSelected')}</FormFeedback>
                            </Col>
                        </FormGroup>}
                    <FormGroup row>
                        <Label for="file">File:</Label>
                        <Col sm={10}>
                            <Dropzone accept={acceptedFileTypes} onDrop={(files) => this.handleFileDrop(files)} style={{
                                width: '100%', height: '200px', 
                                borderWidth: '2px', borderColor: 'rgb(102, 102, 102)', 
                                borderStyle: 'dashed', borderRadius: '5px'
                            }}>
                            {this.state.fileToBeImportedPreview ?
                                <p style={{fontSize: '2em', textAlign: 'center'}}><span className="checked large" />{this.state.fileToBeImportedPreview}</p>
                                : <p>Click to select a {acceptedFileTypesDescription} file or drop it here.</p>
                            }
                            </Dropzone>
                        </Col>
                    </FormGroup>
                    <FormGroup row>
                        <div>
                            <Button onClick={e => this.setState({additionalOptionsOpen: ! this.state.additionalOptionsOpen})}>{L.l('general.additionalOptions')}</Button>
                            <Collapse isOpen={this.state.additionalOptionsOpen}>
                                <Card>
                                    <CardBlock>
                                        <FormGroup row check>
                                            <Label check>
                                                <Input type="checkbox" checked={this.state.validateRecords}
                                                    onChange={e => this.setState({validateRecords: e.target.checked})} /> {L.l('dataManagement.csvDataImport.validateRecords')}
                                            </Label>
                                        </FormGroup>
                                        <FormGroup row check>
                                            <Label check>
                                                <Input type="checkbox" checked={this.state.deleteEntitiesBeforeImport}
                                                    onChange={e => this.setState({deleteEntitiesBeforeImport: e.target.checked})} /> {L.l('dataManagement.csvDataImport.deleteEntities')}
                                            </Label>
                                        </FormGroup>
                                    </CardBlock>
                                </Card>
                            </Collapse>
                        </div>
                    </FormGroup>
                </FormGroup>
                <FormGroup row>
                    <Col sm={{size: 2, offset: 5}}>
                        <Button color="primary" onClick={this.handleImportButtonClick}>Import</Button>
                    </Col>
                </FormGroup>

                <Modal isOpen={this.state.errorModalOpen} style={{maxWidth: '1000px'}}>
                    <ModalHeader toggle={() => this.setState({errorModalOpen: ! this.state.errorModalOpen})}>Errors in uploaded file</ModalHeader>
                    <ModalBody>
                        <BootstrapTable data={this.state.errors} striped hover condensed exportCSV csvFileName={'ofc_csv_data_import_errors.csv'}>
							<TableHeaderColumn dataField="id" isKey hidden>Id</TableHeaderColumn>
							<TableHeaderColumn dataField="fileName" width="200">{L.l('dataManagement.csvDataImport.filename')}</TableHeaderColumn>
							<TableHeaderColumn dataField="row" width="50">{L.l('dataManagement.csvDataImport.row')}</TableHeaderColumn>
							<TableHeaderColumn dataField="columns" width="150">{L.l('dataManagement.csvDataImport.columns')}</TableHeaderColumn>
                            <TableHeaderColumn dataField="errorType" width="140">{L.l('dataManagement.csvDataImport.error-type')}</TableHeaderColumn>
                            <TableHeaderColumn dataField="message" width="400" dataFormat={formatErrorMessage}
                                csvFormat={formatErrorMessage}>{L.l('dataManagement.csvDataImport.error-message')}</TableHeaderColumn>
						</BootstrapTable>
                    </ModalBody>
                    <ModalFooter>
                        <Button color="primary" onClick={this.handleErrorsModalCloseButtonClick}>Close</Button>
                    </ModalFooter>
                </Modal>
            </Form>
        )
    }
}

const mapStateToProps = state => {
    const { survey } = state.preferredSurvey

    return { survey: survey }
}

export default connect(mapStateToProps)(CsvDataImportPage);