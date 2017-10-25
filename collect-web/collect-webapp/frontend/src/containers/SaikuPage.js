import React, { Component } from 'react';
import { Button, ButtonGroup, ButtonToolbar, Card, CardBlock, Collapse, Container, 
    Form, FormGroup, Label, Input, Row, Col } from 'reactstrap';
import { connect } from 'react-redux';

import * as Actions from 'actions';
import ServiceFactory from 'services/ServiceFactory'
import MaxAvailableSpaceContainer from 'components/MaxAvailableSpaceContainer';

class SaikuPage extends Component {

	constructor(props) {
		super(props)

		this.handleSurveyChange = this.handleSurveyChange.bind(this)
		this.handleGenerateSaikuDbClick = this.handleGenerateSaikuDbClick.bind(this)
		this.handleSaikuDbGenerationJobCompleted = this.handleSaikuDbGenerationJobCompleted.bind(this)

		this.state = {
			selectedLanguage: null,
			reportingRepositoryInfo: null,
			showSaiku: false
		}
	}

	componentDidMount() {
		const survey = this.props.survey
		if (survey) {
			this.handleSurveyChange(survey)
		}
	}

	componentWillReceiveProps(nextProps) {
		const survey = nextProps.survey
		if (survey) {
			this.handleSurveyChange(survey)
		}
	}

	handleSurveyChange(survey) {
		this.setState({
			selectedLanguage: survey.defaultLanguage
		})
		ServiceFactory.saikuService.fetchReportingRepositoryInfo(survey).then(response => 
			this.setState({reportingRepositoryInfo: response.object}))
	}

	handleGenerateSaikuDbClick() {
		const survey = this.props.survey
		const language = this.state.selectedLanguage
		ServiceFactory.saikuService.startReportingRepositoryGeneration(survey, language).then(job => {
            this.props.dispatch(Actions.startJobMonitor({
                jobId: job.id, 
				title: 'Generating Saiku Database',
				handleJobCompleted: this.handleSaikuDbGenerationJobCompleted
            }))
        })
	}

	handleSaikuDbGenerationJobCompleted() {
		setTimeout(() => this.props.dispatch(Actions.closeJobMonitor()))
		this.setState({showSaiku: true})
	}

	render() {
		const survey = this.props.survey
		if (! survey) {
			return <div>Select a survey first</div>
		}
		const applicationInfo = this.props.applicationInfo
		const reportingRepositoryInfo = this.state.reportingRepositoryInfo
		const dbGenerationNeeded = reportingRepositoryInfo ? reportingRepositoryInfo.updatedRecordsSinceLastUpdate > 0: false
		return (
			this.state.showSaiku ? 
				<MaxAvailableSpaceContainer>
					<iframe src={applicationInfo.saikuUrl} 
								title="Open Foris Collect - Saiku"
								width="100%" height="100%" />
				</MaxAvailableSpaceContainer>
			:
			<Form inline>
				<FormGroup tag="fieldset">
					<legend>Prepare Saiku database</legend>
					<FormGroup row>
						<Label for="languageSelect">Language</Label>
						<Input type="select" name="select" id="languageSelect" value={this.state.selectedLanguage}
							onChange={e => this.setState({selectedLanguage: e.target.value})}>
							{survey.languages.map(lang => <option key={lang} value={lang}>{lang}</option>)}
						</Input>
					</FormGroup>
					{reportingRepositoryInfo && (
						<FormGroup row>
							<FormGroup row>
								<Label sm={8}>Updated records since last Saiku DB generation:</Label>
								<Col sm={2}>
									<div>{reportingRepositoryInfo.updatedRecordsSinceLastUpdate}</div>
								</Col>
							</FormGroup>
							<FormGroup row>
								<Label sm={8}>Last Saiku DB generation:</Label>
								<Col sm={2}>
									<div>{reportingRepositoryInfo.lastUpdate}</div>
								</Col>
							</FormGroup>
						</FormGroup>
					)}
				</FormGroup>
				<Row>
					<Col sm={{ size: 'auto', offset: 5 }}>
						<Button onClick={this.handleGenerateSaikuDbClick} className="btn btn-success">Generate Saiku DB</Button>
					</Col>
				</Row>
			</Form>
		)
	}
}

const mapStateToProps = state => {
	return {
		applicationInfo: state.applicationInfo ? state.applicationInfo.info : null,
		survey: state.preferredSurvey ? state.preferredSurvey.survey : null
	}
}

export default connect(mapStateToProps)(SaikuPage)