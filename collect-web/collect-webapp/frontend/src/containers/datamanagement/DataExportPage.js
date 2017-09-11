import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Container, Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux'

import ServiceFactory from 'services/ServiceFactory'
import JobMonitorModal from 'containers/job/JobMonitorModal'

class DataExportPage extends Component {

    constructor(props) {
        super(props)

        this.state = {
            exportFormat: null,
            jobStatusModalOpen: false,
            csvDataExportJobId: null
        }

        this.handleExportButtonClick = this.handleExportButtonClick.bind(this)
        this.handleCsvDataExportModalOkButtonClick = this.handleCsvDataExportModalOkButtonClick.bind(this)
    }

    handleExportButtonClick() {
        switch (this.state.exportFormat) {
            case 'CSV':
                const survey = this.props.survey
                const surveyId = survey.id
                const rootEntityId = survey.schema.defaultRootEntity.id
                const entityId = null
                const step = 'ENTRY'
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
        this.setState({jobStatusModalOpen: false})
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
                <Row>
                    <Col sm={4} colSpan={4}>
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