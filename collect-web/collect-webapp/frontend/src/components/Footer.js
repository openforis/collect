import React, { Component } from 'react';

class Footer extends Component {
  render() {
    return (
      <footer className="app-footer">
        <a href="http://www.openforis.org/tools/collect">Open Foris Collect</a> &copy; 2017.
        <span className="float-right">Powered by <a href="http://www.openforis.org">Open Foris</a></span>
      </footer>
    )
  }
}

export default Footer;