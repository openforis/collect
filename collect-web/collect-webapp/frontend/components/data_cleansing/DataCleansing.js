import React, { PropTypes } from 'react';
import { PageHeader } from 'react-bootstrap';

const title = 'Data Cleansing';

function DataCleansing(props, context) {
  context.setTitle(title);
  return (
    <div>
      <div className="row">
        <div className="col-lg-12">
          <PageHeader>Data Cleansing</PageHeader>
        </div>
      </div>
    </div>
  );
}


DataCleansing.contextTypes = { setTitle: PropTypes.func.isRequired };
export default DataCleansing;
