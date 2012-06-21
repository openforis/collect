package org.openforis.collect.persistence.xml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.User;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.util.CollectionUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class DataHandler extends DefaultHandler {
	
	private static final String ATTRIBUTE_VERSION = "version";
	private static final String ATTRIBUTE_MODIFIED_BY = "modified_by";
	private static final String ATTRIBUTE_CREATED_BY = "created_by";
	private static final String ATTRIBUTE_DATE_MODIFIED = "modified";
	private static final String ATTRIBUTE_DATE_CREATED = "created";
	private static final String ATTRIBUTE_STATE = "state";
	private static final String ATTRIBUTE_SYMBOL = "symbol";
	private static final String ATTRIBUTE_REMARKS = "remarks";
	private static final String ATTRIBUTE_DEFINITION_ID = "defn";
	
	private CollectRecord record;
	protected Node<?> node;
	protected String field;
	private boolean failed;
	private List<String> failures;
	private List<String> warnings;
	private StringBuilder content;
	protected Attributes attributes;
	private CollectSurvey survey;
	private int ignoreLevels;
	
	private Map<String, User> users;
	
	public DataHandler(CollectSurvey survey, Map<String, User> users) {
		this.survey = survey;
		this.users = users;
	}

	@Override
	public void startDocument() throws SAXException {
		this.record = null;
		this.node = null;
		this.failed = false;
		this.field = null;
		this.failures = new ArrayList<String>();
		this.warnings = new ArrayList<String>();
		this.attributes = null;
		this.ignoreLevels = 0;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		try {
			if ( failed ) {
				return; 
			} else if ( ignoreLevels > 0 ) {
				pushIgnore();
				return;
			} else if ( node == null ) {
				// if root element, read audit data, version, and 
				startRecord(localName, attributes);
			} else {
				this.content = new StringBuilder();
				this.attributes = attributes;
				if ( node instanceof Entity ) {
					startChildNode(localName, attributes);
				} else if ( node instanceof Attribute ) {
					startAttributeField(localName, attributes);
				}
			}
		} catch ( NullPointerException e ) {
			throw e;
		} catch ( RuntimeException e ) {
			if ( node == null ) {
				fail(e+" at root");
			} else { 
				fail(e+" at "+getPath());
			}
		}
	}

	protected String getPath() {
		if ( node == null ) {
			return "root element";
		} else if ( field == null ){
			return node.getPath();
		} else {
			return node.getPath() + "/" + field;			
		}
	}

	public void startRecord(String localName, Attributes attributes) {
		Schema schema = survey.getSchema();
		EntityDefinition defn = schema.getRootEntityDefinition(localName);
		if ( defn == null ) {
			fail("Unknown root entity: "+localName);
		} else {
			String version = attributes.getValue(ATTRIBUTE_VERSION);
			if ( StringUtils.isBlank(version) ) {
				fail("Missing version number");
			} else {
				record = new CollectRecord(survey, version);
				
				String stateAttr = attributes.getValue(ATTRIBUTE_STATE);
				State state = State.fromCode(stateAttr);
				record.setState(state);
				
				Date created = parseDateTime(attributes.getValue(ATTRIBUTE_DATE_CREATED));
				Date modified = parseDateTime(attributes.getValue(ATTRIBUTE_DATE_MODIFIED));
				record.setCreationDate(created);
				record.setModifiedDate(modified);
				if (users != null) {
					String createdByUserName = attributes.getValue(ATTRIBUTE_CREATED_BY);
					User createdBy = users.get(createdByUserName);
					record.setCreatedBy(createdBy);
					String modifiedByUserName = attributes.getValue(ATTRIBUTE_MODIFIED_BY);
					User modifiedBy = users.get(modifiedByUserName);
					record.setModifiedBy(modifiedBy);
				}				
				node = record.createRootEntity(localName);
			}
		}
	}

	public void startChildNode(String localName, Attributes attributes) {
		Entity entity = (Entity) node;
		NodeDefinition childDefn = getNodeDefinition(entity, localName, attributes);
		if ( childDefn == null ) {
			warn("Undefined node '"+localName+"' in "+getPath());
			pushIgnore();
		} else {
			Node<?> newNode = childDefn.createNode();
			entity.add(newNode);
			Integer stateValue = getNodeState();
			if ( stateValue != null ) {
				entity.setChildState(localName, stateValue);
			}
			this.node = newNode;
		}
	}

	private NodeDefinition getNodeDefinition(Entity entity, String localName, Attributes attributes) {
		EntityDefinition entityDefn = entity.getDefinition();
		Schema schema = entityDefn.getSchema();
		NodeDefinition defn = null;
		String childDefnIdStr = attributes.getValue(ATTRIBUTE_DEFINITION_ID);
		if ( StringUtils.isNotBlank(childDefnIdStr) ) {
			int childDefnId = Integer.parseInt(childDefnIdStr);
			defn = schema.getById(childDefnId);
		} else {
			//compatibility with previous version of data marshaller
			defn = entityDefn.getChildDefinition(localName);
		}
		return defn;
	}
	
	protected void startAttributeField(String localName, Attributes attributes) {
		this.field = localName;
	}

	protected void pushIgnore() {
		ignoreLevels++;
	}

	protected void warn(String msg) {
		warnings.add(msg);
	}

	protected void fail(String msg) {
		failures.add(msg);
		failed = true;
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ( failed ) {
			return;
		} else if ( ignoreLevels > 0 ) {
			popIgnore();
			return;
		} else if ( node == null ) {
			fail("Reached root node before end of document");
		} else {
			try {
				if ( node instanceof Attribute ) {
					endAttributeElement();
				} else {
					endEntityElement();
				}
				this.content = null;
				if ( node == null ) {
					endRecordElement();
				}
			} catch (NullPointerException e) {
				throw e;
			} catch (RuntimeException e) {
				fail(e+" at "+getPath());
			}
		}
	}

	protected void popIgnore() {
		ignoreLevels--;
	}

	protected void setNode(Node<?> node) {
		this.node = node;
	}
	
	protected void endRecordElement() {
		this.record.updateRootEntityKeyValues();
		this.record.updateEntityCounts();
	}
	
	protected void endEntityElement() {
		Node<?> parent = node.getParent();
		removeIfEmpty(node);
		this.node = parent;
	}

	@SuppressWarnings({ "rawtypes" })
	protected void endAttributeElement() {
		Attribute attr = (Attribute) node;
		try {
			if (field != null) {
				Field<?> fld = attr.getField(field);
				if ( fld != null ) {
					setField(fld);
				} else {
					warn("Can't parse field '"+field+"' for "+node.getPath()+" with type "+attr.getClass().getSimpleName());
					this.node = node.getParent();
				}
			}
		} catch (NumberFormatException e) {
			warn(e+" at "+getPath());
		}
		Node<?> parent = node.getParent();
		removeIfEmpty(node);
		if ( field == null ) {
			this.node = parent;
		} else {
			this.field = null;
		}
	}

	protected void removeIfEmpty(Node<?> node) {
		if ( node != null && !node.hasData() ) {
			//if node is empty, remove it
			node.getParent().remove(node.getName(), node.getIndex());
		}
	}
	
	protected String getXmlValue() {
		return content == null ? null : content.toString().trim();
	}
	
	protected void setXmlValue(String content) {
		this.content = new StringBuilder(content);
	}
	
	protected Node<?> getNode() {
		return node;
	}
	
	protected void setField(Field<?> fld) {
		String value = getXmlValue();
		fld.setValueFromString(value);
		String remarks = attributes.getValue(ATTRIBUTE_REMARKS);
		fld.setRemarks(remarks);
		String s = attributes.getValue(ATTRIBUTE_SYMBOL);
		if ( StringUtils.isNotBlank(s) ) {
			char c = s.charAt(0);
			FieldSymbol fs = FieldSymbol.valueOf(c);
			if ( fs != null ) {
				fld.setSymbol(fs.getCode());
			}
		}
		Integer stateValue = getNodeState();
		if ( stateValue != null && stateValue > 0 ) {
			fld.getState().set(stateValue);
		}
	}

	private Integer getNodeState() {
		String state = attributes.getValue(ATTRIBUTE_STATE);
		int stateInt = 0;
		if ( state != null) {
			stateInt = Integer.parseInt(state);
			return stateInt;
		} else {
			return null;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if ( content != null && node instanceof Attribute ) {
			content.append(ch, start, length);
		}
	}
	
	protected Date parseDateTime(String dateTime) {
		Date result = null;
		if(StringUtils.isNotBlank(dateTime)) {
			try {
				//try to parse datetime in xml format 
				Calendar cal = DatatypeConverter.parseDateTime(dateTime);
				result = cal.getTime();
			} catch (Exception e) {
				//try to parse datetime in another format 
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
				try {
					result = sdf.parse(dateTime);
				} catch (ParseException e1) {
					throw new IllegalArgumentException("Invalid format expected for datetime: " + dateTime);
				}
			}
		}
		return result;
	}

	
	public List<String> getFailures() {
		return CollectionUtil.unmodifiableList(failures);
	}

	public List<String> getWarnings() {
		return CollectionUtil.unmodifiableList(warnings);
	}

	public CollectRecord getRecord() {
		return record;
	}

	public Attributes getAttributes() {
		return attributes;
	}
	
}
