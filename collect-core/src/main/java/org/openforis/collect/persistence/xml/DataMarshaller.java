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
import org.openforis.collect.model.User;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.FieldDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.State;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * @author S. Ricci
 */
public class DataMarshaller {
	
	private static final String INDENT_FEATURE = "http://xmlpull.org/v1/doc/features.html#indent-output";

	private static final String RECORD_VERSION_ATTRIBUTE = "version";
	private static final String RECORD_STEP_ATTRIBUTE = "step";
	private static final String RECORD_CREATED_BY_ATTRIBUTE = "created_by";
	private static final String RECORD_CREATED_ATTRIBUTE = "created";
	private static final String RECORD_MODIFIED_BY_ATTRIBUTE = "modified_by";
	private static final String RECORD_MODIFIED_ATTRIBUTE = "modified";
	private static final String STATE_ATTRIBUTE = "state";
	private static final String SYMBOL_ATTRIBUTE = "symbol";
	private static final String REMARKS_ATTRIBUTE = "remarks";
	
	public void write(CollectRecord record, Writer out) throws IOException, XmlPullParserException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		XmlSerializer serializer = factory.newSerializer();
		serializer.setFeature(INDENT_FEATURE, true);
		serializer.setOutput(out);
		//do not use UTF-8 encoding: unicode text will be escaped by the serializer instead
		//serializer.startDocument("UTF-8", true);
		serializer.startDocument(null, true); 
		
		Entity rootEntity = record.getRootEntity();
		String rootEntityName = rootEntity.getName();
        serializer.startTag(null, rootEntityName);
        
        ModelVersion version = record.getVersion();
        if ( version != null ) {
        	serializer.attribute(null, RECORD_VERSION_ATTRIBUTE, version.getName());
        }
        serializer.attribute(null, RECORD_STEP_ATTRIBUTE, Integer.toString(record.getStep().getStepNumber()));
        if ( record.getState() != null ) {
            serializer.attribute(null, STATE_ATTRIBUTE, record.getState().getCode());
        }
        User createdBy = record.getCreatedBy();
		if ( createdBy != null ) {
        	serializer.attribute(null, RECORD_CREATED_BY_ATTRIBUTE, createdBy.getName());
        }
        User modifiedBy = record.getModifiedBy();
		if ( modifiedBy != null ) {
        	serializer.attribute(null, RECORD_MODIFIED_BY_ATTRIBUTE, modifiedBy.getName());
        }
        addDateAttribute(serializer, RECORD_CREATED_ATTRIBUTE, record.getCreationDate());
        addDateAttribute(serializer, RECORD_MODIFIED_ATTRIBUTE, record.getModifiedDate());
        
        writeChildren(serializer, rootEntity);
		serializer.endTag(null, rootEntityName);
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
		
		writeState(serializer, attr);
		
		int cnt = attr.getFieldCount();
		for (int i = 0; i < cnt; i++) {
			writeField(serializer, attr, i);
		}
		serializer.endTag(null, name);
	}

	private void write(XmlSerializer serializer, Entity entity) throws IOException {
		String name = entity.getName();
		
		serializer.startTag(null, name);
		
		writeState(serializer, entity);
		
		writeChildren(serializer, entity);
		
		serializer.endTag(null, name);
	}

	private void writeChildren(XmlSerializer serializer, Entity rootEntity) throws IOException {
		List<Node<?>> children = rootEntity.getChildren();
		for (Node<?> node : children) {
			write(serializer, node);
		}
		writeEmptyNodes(serializer, rootEntity);
	}

	private void writeState(XmlSerializer serializer, Node<?> node) throws IOException {
		Entity parent = node.getParent();
		if ( parent != null ) {
			State s = parent.getChildState(node.getName());
			int state = s.intValue();
			if (state > 0) {
				serializer.attribute(null, STATE_ATTRIBUTE, Integer.toString(state));
			}
		}
	}

	/**
	 * Writes empty nodes child of an entity if there is a node state specified.
	 * 
	 * @param serializer
	 * @param entity
	 * @throws IOException
	 */
	private void writeEmptyNodes(XmlSerializer serializer, Entity entity) throws IOException {
		EntityDefinition defn = entity.getDefinition();
		List<NodeDefinition> childDefns = defn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefns) {
			String childName = childDefn.getName();
			if (entity.getCount(childName) == 0 ) {
				State childState = entity.getChildState(childName);
				int childStateInt = childState.intValue();
				if (childStateInt > 0) {
					serializer.startTag(null, childName);
					serializer.attribute(null, STATE_ATTRIBUTE, Integer.toString(childStateInt));
					serializer.endTag(null, childName);
				}
			}
		}
	}

	private void writeField(XmlSerializer serializer, Attribute<?, ?> attr, int fieldIdx) throws IOException {
		AttributeDefinition definition = attr.getDefinition();
		List<FieldDefinition<?>> fldDefns = definition.getFieldDefinitions();
		Field<?> fld = attr.getField(fieldIdx);
		if ( fld.hasData() ) {
			FieldDefinition<?> fldDefn = fldDefns.get(fieldIdx);
			String name = fldDefn.getName();
			serializer.startTag(null, name);
			Object val = fld.getValue();
			if ( StringUtils.isNotBlank(fld.getRemarks()) ) {
				serializer.attribute(null, REMARKS_ATTRIBUTE, fld.getRemarks());
			}
			if ( fld.getSymbol() != null ) {
				serializer.attribute(null, SYMBOL_ATTRIBUTE, fld.getSymbol().toString());
			}
			State state = fld.getState();
			int stateInt = state.intValue();
			if ( stateInt > 0 ) {
				serializer.attribute(null, STATE_ATTRIBUTE, Integer.toString(stateInt));
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
