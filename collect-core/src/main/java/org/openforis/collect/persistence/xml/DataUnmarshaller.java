package org.openforis.collect.persistence.xml;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.parsers.SAXParser;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecordContext;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NumericRangeAttribute;
import org.openforis.idm.model.TaxonAttribute;
import org.openforis.idm.util.CollectionUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author G. Miceli
 *
 */
public class DataUnmarshaller {

	private CollectSurvey survey;
	
	private CollectRecordContext recordContext;
	
	private final Log log = LogFactory.getLog(getClass());

	public DataUnmarshaller(CollectSurvey survey, CollectRecordContext recordContext) {
		super();
		this.survey = survey;
		this.recordContext = recordContext;
	}

	private CollectRecord parse(InputSource source) throws DataUnmarshallerException {
		SAXParser p = new SAXParser();
		CollectXmlDataHandler handler = new CollectXmlDataHandler();
		p.setContentHandler(handler);
		try {
			p.parse(source);
			List<String> messages = handler.getMessages();
			if ( messages.isEmpty() ) {
				return handler.getRecord();
			} else {
				throw new DataUnmarshallerException(messages);
			}
		} catch (SAXException e) {
			throw new DataUnmarshallerException(e);
		} catch (IOException e) {
			throw new DataUnmarshallerException(e);
		}
	}
	
	public CollectRecord parse(String filename) throws DataUnmarshallerException {
		FileReader reader = null;
		try {
			reader = new FileReader(filename);
			CollectRecord record = parse(reader); 
			reader.close();
			return record;
		} catch (IOException e) {
			throw new DataUnmarshallerException(e);
		} finally {
			if ( reader != null ) {
				try {
					reader.close();
				} catch (IOException e) {
					log.warn("Failed to close Reader: "+e.getMessage());
				}
			}
		}
	}
	
	public CollectRecord parse(Reader reader)  throws IOException, DataUnmarshallerException {
		InputSource is = new InputSource(reader);
		return parse(is);
	}

	class CollectXmlDataHandler extends DefaultHandler {
		private CollectRecord record;
		private Node<?> node;
		private String field;
		private boolean failed;
		private List<String> messages;
		private StringBuilder content;
		private Attributes attributes;
	
		@Override
		public void startDocument() throws SAXException {
			this.record = null;
			this.node = null;
			this.failed = false;
			this.field = null;
			this.messages = new ArrayList<String>();
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			try {
				if ( failed ) {
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
				messages.add(e.getMessage());
				failed = true;
			}
		}

		public void startRecord(String localName, Attributes attributes) {
			Schema schema = survey.getSchema();
			EntityDefinition defn = schema.getRootEntityDefinition(localName);
			if ( defn == null ) {
				throw new RuntimeException("Unknown root entity: "+localName);
			} else {
				String version = attributes.getValue("version");
				if ( StringUtils.isBlank(version) ) {
					throw new RuntimeException("Missing version number");
				} else {
					this.record = new CollectRecord(recordContext, survey, version);
					this.node = record.createRootEntity(localName);
				}
			}
		}

		public void startChildNode(String localName, Attributes attributes) {
			Entity entity = (Entity) node;
			EntityDefinition defn = entity.getDefinition();
			NodeDefinition childDefn = defn.getChildDefinition(localName);
			if ( childDefn == null ) {
				messages.add("Unknown element '"+localName+"' in "+node.getPath());
			} else {
				Node<?> newNode = childDefn.createNode();
				entity.add(newNode);
				this.node = newNode;
			}
		}

		private void startAttributeField(String localName, Attributes attributes) {
			this.field = localName;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ( failed ) {
				return;
			} else if ( node == null ) {
				throw new RuntimeException("Reached root node before end of document");
			} else {
				if ( node instanceof Attribute ) {
					endAttributeElement();
				} else {
					endEntityElement();
				}
				this.content = null;
			}
		}

		private void endEntityElement() {
			this.node = node.getParent();
		}

		@SuppressWarnings("rawtypes")
		private void endAttributeElement() {
			Attribute attr = (Attribute) node;
			if ( attr instanceof CoordinateAttribute ) {
				setValues((CoordinateAttribute) attr);
			} else if ( attr instanceof NumericRangeAttribute ) {
				setValues((NumericRangeAttribute<?,?>) attr);
			} else if ( attr instanceof TaxonAttribute ) {
				setValues((TaxonAttribute) attr); 
			} else if ( field == null ) {
				setValue(attr);
			} else {
				throw new UnsupportedOperationException("Can't parse field '"+field+"' for attribute "+node.getClass());
			}
			setRemarks(attr);
			if ( field == null ) {
				this.node = node.getParent();
			} else {
				this.field = null;
			}
		}

		private void setRemarks(Attribute<?,?> attr) {
			String remarks = attributes.getValue("remarks");
			if ( remarks != null ) {
				attr.getField(0).setRemarks(remarks);
			}
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void setValue(Attribute attr) {
			AttributeDefinition defn = (AttributeDefinition) attr.getDefinition();
			String body = content == null ? null : content.toString();
			Object val = defn.createValue(body);
			attr.setValue(val);
			if ( attr instanceof CodeAttribute ) {
				String qualifier = attributes.getValue("qualifier");
				attr.getField(1).setValue(qualifier);
			}
		}

		private void setValues(TaxonAttribute attr) {
			String body = content == null ? null : content.toString();
			if ( "id".equals(field) ) {
				attr.getField(0).setValueFromString(body);						
			} else if ( "scientific_name".equals(field) ) {
				attr.getField(1).setValueFromString(body);
			} else if ( "vernacular_name".equals(field) ) {
				attr.getField(2).setValueFromString(body);
			} else if ( "vernacular_lang".equals(field) ) {
				// TODO Map to language variety and code instead
				attr.getField(2).setValueFromString(body);
			}
		}

		private void setValues(NumericRangeAttribute<?,?> attr) {
			String body = content == null ? null : content.toString();
			if ( "from".equals(field) ) {
				attr.getField(0).setValueFromString(body);						
			} else if ( "to".equals(field) ) {
				attr.getField(1).setValueFromString(body);
			}
		}

		private void setValues(CoordinateAttribute attr) {
			String body = content == null ? null : content.toString();
			if ( "x".equals(field) ) {
				attr.getField(0).setValueFromString(body);						
			} else if ( "y".equals(field) ) {
				attr.getField(1).setValueFromString(body);
			} else if ( "srs".equals(field) ) {
				attr.getField(2).setValueFromString(body);
			}
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
	
	public static void main(String[] args) {
		try {
			
			// Load IDML
			CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext();
			SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
			CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal("/home/gino/workspace/faofin/tz/naforma-idm/tanzania-naforma.idm.xml");
			
			// Load record
			long start = System.currentTimeMillis();
			CollectRecordContext recordContext = new CollectRecordContext();
			DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(survey, recordContext);
			CollectRecord record = dataUnmarshaller.parse("/home/gino/workspace/temp/tzdata/data/3/143_169/data.xml");
			long end = System.currentTimeMillis();
			System.out.println(record);
			System.out.println("Loaded in "+(end-start)+"ms");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidIdmlException e) {
			e.printStackTrace();
		} catch (DataUnmarshallerException e) {
			e.printStackTrace();
		}
	}
}
