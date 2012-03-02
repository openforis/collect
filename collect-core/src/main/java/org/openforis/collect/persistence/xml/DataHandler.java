package org.openforis.collect.persistence.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumericRangeAttribute;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.util.CollectionUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataHandler extends DefaultHandler {
	private CollectRecord record;
	protected Node<?> node;
	private String field;
	private boolean failed;
	private List<String> messages;
	private StringBuilder content;
	private Attributes attributes;
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
		this.messages = new ArrayList<String>();
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
				this.record = new CollectRecord(survey, version);
				this.node = record.createRootEntity(localName);
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

	protected void pushIgnore() {
		ignoreLevels++;
	}

	protected void warn(String msg) {
		messages.add(msg);
	}

	protected void fail(String msg) {
		messages.add(msg);
		failed = true;
	}
	
	protected void startAttributeField(String localName, Attributes attributes) {
		this.field = localName;
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

	protected void endEntityElement() {
		this.node = node.getParent();
	}

	protected void setNode(Node<?> node) {
		this.node = node;
	}
	
	@SuppressWarnings("rawtypes")
	protected void endAttributeElement() {
		Attribute attr = (Attribute) node;
		try {
			if ( attr instanceof CoordinateAttribute ) {
				setCoordinateField((CoordinateAttribute) attr);
			} else if ( attr instanceof NumericRangeAttribute ) {
				setNumericRangeField((NumericRangeAttribute<?,?>) attr);
			} else if ( attr instanceof TaxonAttribute ) {
				setTaxonField((TaxonAttribute) attr); 
			} else if ( field == null ) {
				setSingleElementValue(attr);
			} else {
				warn("Can't parse field '"+field+"' for "+node.getPath()+" with type "+attr.getClass().getSimpleName());
				this.node = node.getParent();
			}
		} catch (NumberFormatException e) {
			warn(e+" at "+getPath());
		}
		setRemarks(attr);
		if ( field == null ) {
			this.node = node.getParent();
		} else {
			this.field = null;
		}
	}
		

	protected void setRemarks(Attribute<?,?> attr) {
		String remarks = attributes.getValue("remarks");
		if ( remarks != null ) {
			attr.getField(0).setRemarks(remarks);
		}
	}

	protected String getXmlValue() {
		return content == null ? null : content.toString().trim();
	}
	
	protected void setXmlValue(String content) {
		this.content = new StringBuilder(content);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void setSingleElementValue(Attribute attr) {
		AttributeDefinition defn = (AttributeDefinition) attr.getDefinition();
		String xmlValue = getXmlValue();
		Object val = defn.createValue(xmlValue);
		attr.setValue(val);
		if ( attr instanceof CodeAttribute ) {
			String qualifier = attributes.getValue("qualifier");
			attr.getField(1).setValue(qualifier);
		}
	}

	protected void setTaxonField(TaxonAttribute attr) {
		if ( "id".equals(field) ) {
			setField(attr.getField(0));
		} else if ( "scientific_name".equals(field) ) {
			setField(attr.getField(1));
		} else if ( "vernacular_name".equals(field) ) {
			setField(attr.getField(2));
		} else if ( "vernacular_lang".equals(field) ) {
			// TODO Map to language variety and code instead
			setField(attr.getField(3));
		}
	}

	protected void setNumericRangeField(NumericRangeAttribute<?,?> attr) {
		if ( "from".equals(field) ) {
			setField(attr.getField(0));
		} else if ( "to".equals(field) ) {
			setField(attr.getField(1));
		}
	}

	protected void setCoordinateField(CoordinateAttribute attr) {
		if ( "x".equals(field) ) {
			setField(attr.getField(0));						
		} else if ( "y".equals(field) ) {
			setField(attr.getField(1));
		} else if ( "srs".equals(field) ) {
			setField(attr.getField(2));
		}
	}

	protected Node<?> getNode() {
		return node;
	}
	
	protected void setField(Field<?> fld) {
		String xmlValue = getXmlValue();
		fld.setValueFromString(xmlValue);
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if ( content != null && node instanceof Attribute ) {
			content.append(ch, start, length);
		}
	}
	
	public List<String> getMessages() {
		return CollectionUtil.unmodifiableList(messages);
	}
	
	public CollectRecord getRecord() {
		return record;
	}
}
