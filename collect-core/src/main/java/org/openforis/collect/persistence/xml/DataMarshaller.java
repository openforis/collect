package org.openforis.collect.persistence.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.xmlpull.v1.XmlSerializer;

/**
 * @author S. Ricci
 */
public class DataMarshaller {

	@Autowired
	private XmlSerializerFactory xmlSerializerFactory;
	
	public void write(CollectRecord record, Writer out) throws IOException {
		xmlSerializerFactory.newSerializer();
		XmlSerializer serializer = new org.openforis.collect.persistence.xml.FastXmlSerializer();
		serializer.setOutput(out);
		serializer.startDocument("UTF-8", true);
        serializer.startTag(null, "record");
        
        serializer.attribute(null, "version", record.getVersion().getName());
        serializer.attribute(null, "step", Integer.toString(record.getStep().getStepNumber()));
        if ( record.getState() != null ) {
            serializer.attribute(null, "state", record.getState().getCode());
        }
        if ( record.getCreatedBy() != null ) {
        	serializer.attribute(null, "createdBy", record.getCreatedBy().getName());
        }
        if ( record.getModifiedBy() != null ) {
        	serializer.attribute(null, "modifiedBy", record.getModifiedBy().getName());
        }
        addDateAttribute(serializer, "created", record.getCreationDate());
        addDateAttribute(serializer, "modified", record.getModifiedDate());
        
        Entity rootEntity = record.getRootEntity();
		write(serializer, rootEntity);
		serializer.endTag(null, "record");
		serializer.endDocument();
		serializer.flush();
	}

	private void write(XmlSerializer serializer, Node<?> node) throws IOException {
		if (node instanceof Entity) {
			write(serializer, (Entity) node);
		} else if (node instanceof Attribute) {
			write(serializer, (Attribute<?,?>) node);
		}
	}

	private void write(XmlSerializer serializer, Attribute<?,?> attr) throws IOException {
		String name = attr.getName();
		serializer.startTag(null, name);
		int cnt = attr.getFieldCount();
		for (int i = 0; i < cnt; i++) {
			writeField(serializer, attr, i);
		}
		serializer.endTag(null, name);
	}

	private void write(XmlSerializer serializer, Entity entity) throws IOException {
		String name = entity.getName();
		serializer.startTag(null, name);
		
		writeChildStates(serializer, entity);
		
		//write children
		List<Node<?>> children = entity.getChildren();
		for (Node<?> node : children) {
			write(serializer, node);
		}
		serializer.endTag(null, name);
	}

	private void writeChildStates(XmlSerializer serializer, Entity entity) throws IOException {
		serializer.startTag(null, "child_states");
		EntityDefinition defn = entity.getDefinition();
		List<NodeDefinition> childDefns = defn.getChildDefinitions();
		for (NodeDefinition nodeDefn : childDefns) {
			String childName = nodeDefn.getName();
			State state = entity.getChildState(childName);
			if ( state != null ) {
				int stateVal = state.intValue();
				if ( stateVal > 0 ) {
					serializer.startTag(null, childName);
					serializer.text(Integer.toString(stateVal));
					serializer.endTag(null, childName);
				}
			}
		}
		serializer.endTag(null, "child_states");
	}

	private void writeField(XmlSerializer serializer, Attribute<?, ?> attr, int fieldIdx) throws IOException {
		AttributeDefinition definition = attr.getDefinition();
		List<FieldDefinition> fldDefns = definition.getFieldsDefinitions();
		Field<?> fld = attr.getField(fieldIdx);
		if ( fld.hasData() ) {
			FieldDefinition fldDefn = fldDefns.get(fieldIdx);
			String name = fldDefn.getName();
			serializer.startTag(null, name);
			Object val = fld.getValue();
			if ( StringUtils.isNotBlank(fld.getRemarks()) ) {
				serializer.attribute(null, "remarks", fld.getRemarks());
			}
			if ( fld.getSymbol() != null ) {
				serializer.attribute(null, "symbol", fld.getSymbol().toString());
			}
			if ( fld.getState() != null ) {
				int state = fld.getState().intValue();
				if ( state > 0 ) {
					serializer.attribute(null, "state", Integer.toString(state));
				}
			}
			if ( val != null ) {
				String valStr = val.toString();
				serializer.text(valStr);
			}
			serializer.endTag(null, name);
		}
	}
	
	private String dateToString(Date date) {
		String result = null;
		if ( date != null ) {
			try {
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime(date);
				XMLGregorianCalendar xmlCal;
				DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
				xmlCal = datatypeFactory.newXMLGregorianCalendar(cal);
				result = xmlCal.toString();
			} catch (DatatypeConfigurationException e) {
				//nothing to do
			}
		}
		return result;
	}
	
	private void addDateAttribute(XmlSerializer serializer, String attributeName, Date date) throws IllegalArgumentException, IllegalStateException, IOException {
		String dateString = dateToString(date);
		if ( dateString != null ) {
			serializer.attribute(null, attributeName, dateString);
		}
	}
	
}
