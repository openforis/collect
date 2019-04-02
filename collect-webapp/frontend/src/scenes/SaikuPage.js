import React, { Component } from 'react';
import { Button, Form, FormGroup, Label, Input, Col, Container } from 'reactstrap';
import { connect } from 'react-redux';

import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import Grid from '@material-ui/core/Grid';

import * as JobActions from 'actions/job';
import ServiceFactory from 'services/ServiceFactory'
import Dates from 'utils/Dates'
import MaxAvailableSpaceContainer from 'common/components/MaxAvailableSpaceContainer';

class SaikuPage extends Component {

	constructor(props) {
		super(props)

		this.handleSurveyChange = this.handleSurveyChange.bind(this)
		this.handleGenerateSaikuDbClick = this.handleGenerateSaikuDbClick.bind(this)
		this.handleSaikuDbGenerationJobCompleted = this.handleSaikuDbGenerationJobCompleted.bind(this)
		this.handleStartSaikuClick = this.handleStartSaikuClick.bind(this)

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
			this.setState({ reportingRepositoryInfo: response.object }))
	}

	handleGenerateSaikuDbClick() {
		const survey = this.props.survey
		const language = this.state.selectedLanguage
		ServiceFactory.saikuService.startReportingRepositoryGeneration(survey, language).then(job => {
			this.props.dispatch(JobActions.startJobMonitor({
				jobId: job.id,
				title: 'Generating Saiku Database',
				handleJobCompleted: this.handleSaikuDbGenerationJobCompleted
			}))
		})
	}

	handleSaikuDbGenerationJobCompleted() {
		setTimeout(() => this.props.dispatch(JobActions.closeJobMonitor()))
		this.setState({ showSaiku: true })
	}

	handleStartSaikuClick() {
		this.setState({ showSaiku: true })
	}

	render() {
		const survey = this.props.survey
		if (!survey) {
			return <div>Select a survey first</div>
		}
		const applicationInfo = this.props.applicationInfo
		const reportingRepositoryInfo = this.state.reportingRepositoryInfo
		const dbGenerationNeeded = reportingRepositoryInfo ? reportingRepositoryInfo.updatedRecordsSinceLastUpdate > 0 : false
		return (
			this.state.showSaiku ?
				<MaxAvailableSpaceContainer>
					<iframe src={applicationInfo.saikuUrl}
						title="Open Foris Collect - Saiku"
						width="100%" height="100%" />
				</MaxAvailableSpaceContainer>
				: <Grid container
						direction="column"
						alignItems="center"
						spacing={40}>
					<Grid item>
						<ExpansionPanel defaultExpanded
							style={{ width: '550px' }}>
							<ExpansionPanelSummary expandIcon={<ExpandMoreIcon />}>Prepare Saiku database</ExpansionPanelSummary>
							<ExpansionPanelDetails>
								<Container>
									<Form sm={4}>
										<FormGroup row inline>
											<Label sm={2} for="languageSelect">Language</Label>
											<Col sm={4}>
												<Input type="select" name="select" id="languageSelect" className="form-control" value={this.state.selectedLanguage}
													onChange={e => this.setState({ selectedLanguage: e.target.value })}>
													{survey.languages.map(lang => <option key={lang} value={lang}>{lang}</option>)}
												</Input>
											</Col>
										</FormGroup>
										{reportingRepositoryInfo && (
											<Form>
												<FormGroup row inline>
													<Label sm={8}>Last Saiku DB generation:</Label>
													<Col sm={4} className="form-control">
														{Dates.formatDatetime(reportingRepositoryInfo.lastUpdate)}
													</Col>
												</FormGroup>
												<FormGroup row inline>
													<Label sm={8}>Updated records since last generation:</Label>
													<Col sm={4} className="form-control">
														{reportingRepositoryInfo.updatedRecordsSinceLastUpdate}
													</Col>
												</FormGroup>
											</Form>
										)}
										<FormGroup row>
											{dbGenerationNeeded && <div>Saiku DB Generation needed</div>}
										</FormGroup>
										<FormGroup row>
											<Col sm={{ size: 2, offset: 4 }}>
												<Button onClick={this.handleGenerateSaikuDbClick} className="btn btn-success">Generate Saiku DB</Button>
											</Col>
										</FormGroup>
									</Form>
								</Container>
							</ExpansionPanelDetails>
						</ExpansionPanel>
					</Grid>
					<Grid item>
						<Button onClick={this.handleStartSaikuClick} className="btn btn-info">Start Saiku</Button>
					</Grid>
				</Grid>
		)
	}
}

const mapStateToProps = state => {
	return {
		applicationInfo: state.applicationInfo ? state.applicationInfo.info : null,
		survey: state.activeSurvey ? state.activeSurvey.survey : null
	}
}

export default connect(mapStateToProps)(SaikuPage)