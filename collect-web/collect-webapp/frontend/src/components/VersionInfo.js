import React, { Component } from 'react';
import { UncontrolledTooltip } from 'reactstrap';
import { connect } from 'react-redux'

import Constants from 'utils/Constants'

class VersionInfo extends Component {

  render() {
    const applicationInfo = this.props.applicationInfo
    
    if (! applicationInfo) {
      return <div>Loading...</div>
    }

    const versionVerifiedIcon =
      applicationInfo.latestReleaseVersionVerified ?
        applicationInfo.latestReleaseVersion ?
          <span className="fa fa-check success" id="latest-version-running-icon" />
          : <span className="fa fa-exclamation-triangle warning" id="old-version-running-icon" />
        : <span className="fa fa-exclamation-circle error" id="cannot-verify-latest-version-icon" />

    const versionTooltip = 
        applicationInfo.latestReleaseVersionVerified ? 
          applicationInfo.latestReleaseVersion ? 
            <UncontrolledTooltip placement="right" className="success" target="latest-version-running-icon">
              Latest version running
            </UncontrolledTooltip>
          : <UncontrolledTooltip placement="right" className="warning" target="old-version-running-icon">
            Old version running: please update it running Update Open Foris Collect
          </UncontrolledTooltip>
        : <UncontrolledTooltip placement="right" className="error" target="cannot-verify-latest-version-icon">
          Cannot verify if you are running the latest version of Collect
        </UncontrolledTooltip>
    
    return (
      <div>
        <span>Version: {Constants.APP_VERSION} {versionVerifiedIcon}</span>
        {versionTooltip}
      </div>
    )
  }
}

function mapStateToProps(state) {
  return {
    applicationInfo: state.applicationInfo ? state.applicationInfo.info : null
  }
}
export default connect(mapStateToProps)(VersionInfo);