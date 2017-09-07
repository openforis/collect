import React, { Component } from 'react'
import PropTypes from 'prop-types'

import TabSet from 'components/datamanagement/recordeditor/TabSet'
import { Record } from 'model/Record'
import EventQueue from 'model/event/EventQueue'
import { AttributeUpdatedEvent, EntityCreatedEvent, CodeAttributeUpdatedEvent, 
  CoordinateAttributeUpdatedEvent, DateAttributeUpdatedEvent, TextAttributeUpdatedEvent }  from 'model/event/RecordEvent'
import { Attribute, Entity } from 'model/Record'
import ServiceFactory from 'services/ServiceFactory'

export default class RecordEditPage extends Component {

  recordUpdater = null;

  constructor(props) {
    super(props);

    this.state = {
      record: null
    }
  }

  static propTypes = {
      survey: PropTypes.object
  }
  
  componentDidMount() {
    let idParam = this.props.match.params.id;
    let recordId = parseInt(idParam);

    this.recordService.fetchSurveyId(recordId).then(res => {
      let surveyId = parseInt(res);
      ServiceFactory.surveyService.fetchById(surveyId).then(survey => {
        ServiceFactory.recordService.fetchById(survey, recordId).then(record => {
          this.recordUpdater = new RecordUpdater(record)
          this.setState({...this.state, record: record});
        });
      })
    })
  }

  componentWillUnmount() {
    if (this.recordUpdater) {
      this.recordUpdater.unmount();
    }
  }
  
  render() {
    let record = this.state.record
    if (! record) {
      return <div>Loading...</div>
    }
    let survey = record.survey
    let uiConf = survey.uiConfiguration
    let tabSetDefinition = uiConf.getTabSetByRootEntityDefinitionId(record.rootEntity.definition.id)

	  return (
		  <TabSet tabSetDef={tabSetDefinition} record={record} />
	  );
  }
}

class RecordUpdater {

  record = null

  constructor(record) {
    this.record = record;

    this.handleRecordEventReceived = this.handleRecordEventReceived.bind(this)
    
    EventQueue.subscribe('recordEvent', this.handleRecordEventReceived)
  }

  handleRecordEventReceived(event) {
    let record = this.record
    if (record && record.id == event.recordId && record.step == event.recordStep) {
      let survey = record.survey;
      let parentEntityId = event.parentEntityId;
      let parentEntity = record.getNodeById(parentEntityId);
      let definition = survey.schema.getDefinitionById(parseInt(event.definitionId));
      let nodeId = parseInt(event.nodeId);
      
      if (event instanceof EntityCreatedEvent) {
        let newEntity = new Entity(record, definition, parentEntity);
        newEntity.id = nodeId;
        parentEntity.addChild(newEntity);
      } else if (event instanceof AttributeUpdatedEvent) {
        let attr = record.getNodeById(nodeId);
        if (attr == null) {
            attr = new Attribute(record, definition, parentEntity);
            attr.id = nodeId;
            parentEntity.addChild(attr);
        } else {
        }
        this._setValueInAttribute(attr, event);
      }
    }
  }

  _setValueInAttribute(attr, event) {
    if (event instanceof CodeAttributeUpdatedEvent) {
        attr.setFieldValue(0, event.code);
        attr.setFieldValue(1, event.qualifier);
    } else if (event instanceof CoordinateAttributeUpdatedEvent) {
        attr.setFieldValue(0, event.x);
        attr.setFieldValue(1, event.y);
        attr.setFieldValue(2, event.srsId);
    } else if (event instanceof DateAttributeUpdatedEvent) {
        let date: Date = event.date;
        attr.setFieldValue(0, date.getFullYear());
        attr.setFieldValue(1, date.getMonth());
        attr.setFieldValue(2, date.getDay());
    } else if (event instanceof TextAttributeUpdatedEvent) {
        attr.setFieldValue(0, event.text);
    }
  }

  unmount() {
    EventQueue.unsubscribe('recordEvent', this.handleRecordEventReceived)
  }
}
