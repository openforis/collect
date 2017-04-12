import React, { PropTypes } from 'react';
import { PageHeader } from 'react-bootstrap';
import RecordDataTable from './RecordDataTable';

const title = 'Data Management';

function DataManagement(props, context) {
  context.setTitle(title);
  return (
    <div>
      <div className="row">
        <div className="col-lg-12">
          <PageHeader>Data Management</PageHeader>
          <RecordDataTable />
        </div>
      </div>
    </div>
  );
}
DataManagement.contextTypes = { setTitle: PropTypes.func.isRequired };
export default DataManagement;