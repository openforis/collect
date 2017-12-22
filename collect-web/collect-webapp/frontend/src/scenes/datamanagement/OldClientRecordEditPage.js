import React, { Component } from 'react';
import Constants from 'Constants';
import MaxAvailableSpaceContainer from 'components/MaxAvailableSpaceContainer';
import ServiceFactory from 'services/ServiceFactory'

export default class OldClientRecordEditPage extends Component {

    constructor(props) {
        super(props)

        this.state = {
            loading: true
        }
    }

    componentDidMount() {
        let idParam = this.props.match.params.id;
        let recordId = parseInt(idParam);

        ServiceFactory.recordService.fetchSurveyId(recordId).then(res => {
            let surveyId = parseInt(res);
            this.setState({
                loading: false,
                surveyId: surveyId,
                recordId: recordId
            })
        })
    }

    render() {
        if (this.state.loading) {
            return <div>Loading...</div>
        }
        const url = Constants.BASE_URL + 'old_client.htm?edit=true&surveyId=' + this.state.surveyId + '&recordId=' + this.state.recordId
        return (
            <MaxAvailableSpaceContainer>
                <iframe src={url}
                    title="Open Foris Collect - Edit Record"
                    width="100%" height="100%" />
            </MaxAvailableSpaceContainer>
        );
    }
}