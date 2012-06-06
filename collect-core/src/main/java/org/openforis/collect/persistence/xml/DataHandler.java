package org.openforis.collect.persistence.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;
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
	
	public DataHandler(CollectSurvey survey) {
		this.survey = survey;
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
			String version = attributes.getValue("version");
			if ( StringUtils.isBlank(version) ) {
				fail("Missing version number");
			} else {
				record = new CollectRecord(survey, version);
				node = record.createRootEntity(localName);
			}
		}
	}

	public void startChildNode(String localName, Attributes attributes) {
		Entity entity = (Entity) node;
		EntityDefinition defn = entity.getDefinition();
		NodeDefinition childDefn = defn.getChildDefinition(localName);
		if ( childDefn == null ) {
			warn("Undefined node '"+localName+"' in "+getPath());
			pushIgnore();
		} else {
			Node<?> newNode = childDefn.createNode();
			entity.add(newNode);
			this.node = newNode;
		}
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
		if ( "individually_owned".equals(localName)) {
//			System.out.println(localName);
		}
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
		this.node = node.getParent();
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
		if ( field == null ) {
			this.node = node.getParent();
		} else {
			this.field = null;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setSingleElementValue(Attribute attr) {
		AttributeDefinition defn = (AttributeDefinition) attr.getDefinition();
		String xmlValue = getXmlValue();
		Value val = defn.createValue(xmlValue);
		attr.setValue(val);
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
		String remarks = attributes.getValue("remarks");
		fld.setRemarks(remarks);
		String s = attributes.getValue("symbol");
		if ( StringUtils.isNotBlank(s) ) {
			char c = s.charAt(0);
			FieldSymbol fs = FieldSymbol.valueOf(c);
			if ( fs != null ) {
				fld.setSymbol(fs.getCode());
			}
		}
		String state = attributes.getValue("state");
		int stateInt = 0;
		if ( state != null) {
			stateInt = Integer.parseInt(state);
			if ( stateInt > 0 ) {
				fld.getState().set(stateInt);
			}
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if ( content != null && node instanceof Attribute ) {
			content.append(ch, start, length);
		}
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
