import React, { Component } from 'react';
import { Button, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import Typography from '@material-ui/core/Typography';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';

import ServiceFactory from 'services/ServiceFactory';
import SchemaTreeView from './SchemaTreeView';
import Workflow from 'model/Workflow';
import * as JobActions from 'actions/job';
import Objects from 'utils/Objects'
import Arrays from 'utils/Arrays'
import L from 'utils/Labels';

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
                modifiedSince: '',
                modifiedUntil: '',
                exportMode: 'ALL_ENTITIES',
                selectedEntityDefinition: null,
                entityId: null,
                exportOnlyOwnedRecords: false,
                includeRecordFiles: true,
                headingSource: 'ATTRIBUTE_NAME'
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
                stepGreaterOrEqual: this.state.stepGreaterOrEqual,
                modifiedSince: this.state.modifiedSince,
                modifiedUntil: this.state.modifiedUntil,
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
                this.props.dispatch(JobActions.startJobMonitor({
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
            this.props.dispatch(JobActions.closeJobMonitor())
        }
        
        handleEntitySelect(event) {
            const selectedEntityDefinition = Arrays.singleItemOrNull(event.selectedNodeDefinitions)
            this.setState({...this.state, selectedEntityDefinition: selectedEntityDefinition})
        }
    
        render() {
            if (!this.props.survey) {
                return <div>Select survey first</div>
            }
            const additionalOptionsFormGroups = csvExportAdditionalOptions.map(o => {
                return <FormGroup check key={o.name}>
                    <Label check>
                        <Input type="checkbox" onChange={event => {
                            const newProp = {}
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
                <Container>
                    <Form>
                        <FormGroup tag="fieldset">
                            <legend>Parameters</legend>
                            <FormGroup row>
                                <Label md={2} for="stepSelect">Step:</Label>
                                <Col md={10}>
                                    <Input type="select" name="step" id="stepSelect" style={{ maxWidth: '100px' }} 
                                        value={this.state.stepGreaterOrEqual}
                                        onChange={e => this.setState({stepGreaterOrEqual: e.target.value})}>{stepsOptions}</Input>
                                </Col>
                            </FormGroup>
                            <FormGroup row>
                                <Label md={2} for="exportMode">Export mode:</Label>
                                <Col md={10}>
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
                                <ExpansionPanel>
                                    <ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>
                                        <Typography>{L.l('general.additionalOptions')}</Typography>
                                    </ExpansionPanelSummary>
                                    <ExpansionPanelDetails>
                                        <div>
                                            <FormGroup row>
                                                <Label md={3} for="modifiedSince">Modified since:</Label>
                                                <Col md={4}>
                                                    <Input type="date" name="modifiedSince" id="modifiedSince"
                                                        value={this.state.modifiedSince}
                                                        onChange={e => this.setState({modifiedSince: e.target.value})} />
                                                </Col>
                                                <Label md={1} for="modifiedUntil">until:</Label>
                                                <Col md={4}>
                                                    <Input type="date" name="modifiedUntil" id="modifiedUntil"
                                                        value={this.state.modifiedUntil}
                                                        onChange={e => this.setState({modifiedUntil: e.target.value})} />
                                                </Col>
                                            </FormGroup>
                                            <FormGroup check row>
                                                <Label check>
                                                    <Input type="checkbox" onChange={event => this.setState({exportOnlyOwnedRecords: event.target.checked})} 
                                                        checked={this.state.exportOnlyOwnedRecords} />{' '}
                                                    Export only owned records
                                                </Label>
                                            </FormGroup>
                                            <FormGroup row>
                                                <Col md={4}>
                                                    <Label for="headingsSourceSelect">Source for file headings:</Label>
                                                </Col>
                                                <Col md={6}>
                                                    <Input type="select" name="headingsSource" id="headingsSourceSelect" style={{ maxWidth: '200px' }} 
                                                        onChange={e => this.setState({headingSource: e.target.value})}>
                                                        <option value="ATTRIBUTE_NAME">Attribute name</option>
                                                        <option value="INSTANCE_LABEL">Attribute label</option>
                                                        <option value="REPORTING_LABEL">Reporting label (Saiku)</option>
                                                    </Input>
                                                </Col>
                                            </FormGroup>
                                            {additionalOptionsFormGroups}
                                            <FormGroup row>
                                                <Col md={12}>
                                                    <Label>* = {L.l('dataManagement.export.notCompatibleWithCSVDataImportNote')}</Label>
                                                </Col>
                                            </FormGroup>
                                        </div>
                                    </ExpansionPanelDetails>
                                </ExpansionPanel>
                            </FormGroup>
                        </FormGroup>
                        <Row>
                            <Col sm={{ size: 'auto', offset: 5 }}>
                                <Button onClick={this.handleExportButtonClick} className="btn btn-success">Export</Button>
                            </Col>
                        </Row>
                    </Form>
                </Container>
            )
        }
    }
    
    
    
    const mapStateToProps = state => {
        const { survey } = state.preferredSurvey
    
        return { survey: survey }
    }
    
    export default connect(mapStateToProps)(CsvDataExportPage);