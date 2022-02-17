import React, { Component } from 'react'

import Constants from 'Constants'
import MaxAvailableSpaceContainer from 'common/components/MaxAvailableSpaceContainer'
import { withLocation } from 'common/hooks'

class SurveEditPage extends Component {
  render() {
    const path = this.props.location.pathname
    const surveyId = path.substring(path.lastIndexOf('/') + 1)
    return (
      <MaxAvailableSpaceContainer>
        <iframe
          src={Constants.BASE_URL + 'zk/surveydesigner/survey_edit.zul?id=' + surveyId}
          title="Open Foris Collect - Survey Designer"
          width="100%"
          height="100%"
        />
      </MaxAvailableSpaceContainer>
    )
  }
}

export default withLocation(SurveEditPage)
