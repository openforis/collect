import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';

import ServiceFactory from 'services/ServiceFactory';
import SchemaTreeView from './SchemaTreeView';
import Workflow from 'model/Workflow';
import * as Actions from 'actions';
import Objects from 'utils/Objects'

const csvExportAdditionalOptions = [
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

class CsvDataExportPage extends Component {
    
        constructor(props) {
            super(props)
    
            this.state = {
                exportFormat: null,
                stepGreaterOrEqual: 'ENTRY',
                exportMode: 'ALL_ENTITIES',
                selectedEntityDefinition: null,
                entityId: null,
                exportOnlyOwnedRecords: false,
                includeRecordFiles: true,
                headingSource: 'ATTRIBUTE_NAME',
                csvExportAdditionalOptionsOpen: false
            }
    
            this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
            this.handleCsvDataExportModalOkButtonClick = this.handleCsvDataExportModalOkButtonClick.bind(this)
            this.handleEntitySelect = this.handleEntitySelect.bind(this)
        }
    
        handleExportButtonClick() {
            if (! this.validateForm()) {
                return
            }
            const survey = this.props.survey
            const surveyId = survey.id
            
            const parameters = {
                surveyId: survey.id,
                rootEntityId: survey.schema.firstRootEntityDefinition.id,
                stepGreaterOrEqual: this.state.step,
                entityId: this.state.selectedEntityDefinition ? this.state.selectedEntityDefinition.id: null,
                exportOnlyOwnedRecords: this.state.exportOnlyOwnedRecords,
                headingSource: this.state.headingSource,
                alwaysGenerateZipFile: true
            }
            csvExportAdditionalOptions.forEach(o => {
                const val = this.state[o.name]
                parameters[o.name] = Objects.isNullOrUndefined(val) ? null : val
            })
            ServiceFactory.recordService.startCSVDataExport(surveyId, parameters).then(job => {
                this.props.dispatch(Actions.startJobMonitor({
                    jobId: job.id, 
                    title: 'Exporting data',
                    okButtonLabel: 'Download CSV file',                        
                    handleOkButtonClick: this.handleCsvDataExportModalOkButtonClick
                }))
            })
        }

        validateForm() {
            if (this.state.exportMode === 'SELECTED_ENTITY' && ! this.state.selectedEntityDefinition) {
                alert('Please select an entity to export')
                return false
            }
            return true
        }
    
        handleCsvDataExportModalOkButtonClick(job) {
            if (job.completed) {
                const survey = this.props.survey
                const surveyId = survey.id
                ServiceFactory.recordService.downloadCSVDataExportResult(surveyId)
            }
            this.props.dispatch(Actions.closeJobMonitor())
        }
        
        handleEntitySelect(event) {
            this.setState({...this.state, selectedEntityDefinition: event.selectedNodeDefinitions.length == 1 ? event.selectedNodeDefinitions[0]: null})
        }
    
        render() {
            if (!this.props.survey) {
                return <div>Select survey first</div>
            }
            const additionalOptionsFormGroups = csvExportAdditionalOptions.map(o => {
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
    
            const steps = Workflow.STEPS
            const stepsOptions = Object.keys(steps).map(s => <option key={s} value={steps[s].code}>{steps[s].label}</option>)
            
            return (
                <Form>
                    <FormGroup tag="fieldset">
                        <legend>Parameters</legend>
                        <Form>
                            <FormGroup row>
                                <Label for="stepSelect" sm={1}>Step:</Label>
                                <Col sm={10}>
                                    <Input type="select" name="step" id="stepSelect" style={{ maxWidth: '100px' }} 
                                        onChange={e => this.setState({step: e.target.value})}>{stepsOptions}</Input>
                                </Col>
                            </FormGroup>
                            <FormGroup row>
                                <Label for="exportMode" sm={1}>Export mode:</Label>
                                <Col sm={10}>
                                    <FormGroup check>
                                        <Label check>
                                            <Input type="radio" value="ALL_ENTITIES" name="exportMode"
                                                checked={this.state.exportMode === 'ALL_ENTITIES'} 
                                                onChange={(event) => this.setState({...this.state, exportMode: event.target.value})} />
                                            All entities
                                        </Label>
                                        <span style={{display: 'inline-block', width: '40px'}}></span>
                                        <Label check>
                                            <Input type="radio" value="SELECTED_ENTITY" name="exportMode"
                                                checked={this.state.exportMode === 'SELECTED_ENTITY'} 
                                                onChange={(event) => this.setState({...this.state, exportMode: event.target.value})} />
                                            Only selected entities
                                        </Label>
                                    </FormGroup>
                                </Col>
                            </FormGroup>
                            {this.state.exportMode === 'SELECTED_ENTITY' &&
                                <FormGroup row>
                                    <Label sm={1}>Select entities to export:</Label>
                                    <Col sm={{size: 10 }}>
                                        <SchemaTreeView survey={this.props.survey}
                                            handleNodeSelect={this.handleEntitySelect} />
                                    </Col>
                                </FormGroup>
                            }
                            <FormGroup row>
                                <div>
                                    <Button onClick={e => this.setState({csvExportAdditionalOptionsOpen: ! this.state.csvExportAdditionalOptionsOpen})}>Additional Options</Button>
                                    <Collapse isOpen={this.state.csvExportAdditionalOptionsOpen}>
                                        <Card>
                                            <CardBlock>
                                                <FormGroup row>
                                                    <Col sm={{size: 12}}>
                                                        <Label check>
                                                            <Input type="checkbox" onChange={event => this.setState({exportOnlyOwnedRecords: event.target.checked})} 
                                                                checked={this.state.exportOnlyOwnedRecords} />{' '}
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
                        </Form>
                    </FormGroup>
                    <Row>
                        <Col sm={{ size: 'auto', offset: 5 }}>
                            <Button onClick={this.handleExportButtonClick} className="btn btn-success">Export</Button>
                        </Col>
                    </Row>
                </Form>
            )
        }
    }
    
    
    
    const mapStateToProps = state => {
        const { survey } = state.preferredSurvey
    
        return { survey: survey }
    }
    
    export default connect(mapStateToProps)(CsvDataExportPage);