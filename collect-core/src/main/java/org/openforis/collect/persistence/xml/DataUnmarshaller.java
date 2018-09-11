package org.openforis.collect.persistence.xml;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.Collect;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.User;
import org.openforis.collect.utils.Dates;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.path.Path;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author G. Miceli
 * @author S. Ricci
 *
 */
public class DataUnmarshaller {

	private static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";

	private DataHandler dataHandler;
	private XMLReader reader;
	private Version recordApplicationVersion = Collect.VERSION;
	
	public DataUnmarshaller(CollectSurvey survey) {
		this(survey, survey);
	}
	
	public DataUnmarshaller(CollectSurvey publishedSurvey, CollectSurvey recordSurvey) {
		super();
		this.dataHandler = new DataHandler(publishedSurvey, recordSurvey, true);
		initializeReader();
	}

	public ParseRecordResult parse(Reader reader) throws IOException {
		return parse(new InputSource(reader));
	}

	public ParseRecordResult parse(String filename) throws DataUnmarshallerException {
		FileReader reader = null;
		try {
			reader = new FileReader(filename);
			ParseRecordResult result = parse(reader); 
			return result;
		} catch (IOException e) {
			throw new DataUnmarshallerException(e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	private ParseRecordResult parse(InputSource source) throws IOException {
		ParseRecordResult result = new ParseRecordResult();
		try {
			reader.parse(source);
			List<NodeUnmarshallingError> failures = dataHandler.getFailures();
			if ( failures.isEmpty() ) {
				CollectRecord record = dataHandler.getRecord();
				result.setRecord(record);
				List<NodeUnmarshallingError> warns = dataHandler.getWarnings();
				if ( ! warns.isEmpty() ) {
					result.setMessage("Processed with errors: " + warns.toString());
					result.setWarnings(warns);
				}
				result.setSuccess(true);
			} else {
				result.setFailures(failures);
			}
		} catch (SAXException e) {
			NodeUnmarshallingError error = new NodeUnmarshallingError(e.toString());
			result.setFailures(Arrays.asList(error));
		}
		return result;
	}

	private void initializeReader() {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// create a parser
			SAXParser parser = factory.newSAXParser();
			// create the reader (scanner)
			this.reader = parser.getXMLReader();
			reader.setFeature(NAMESPACES_FEATURE, true);
			reader.setContentHandler(dataHandler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setRecordDependencyGraphsEnabled(boolean enabled) {
		this.dataHandler.recordDependencyGraphsEnabled = enabled;
	}
	
	public void setRecordValidationEnabled(boolean enabled) {
		this.dataHandler.recordValidationEnabled = enabled;
	}
	
	public void setIgnoreDuplicateRecordKeyValidationErrors(boolean ignoreDuplicateRecordKeyValidationErrors) {
		this.dataHandler.ignoreDuplicateRecordKeyValidationErrors = ignoreDuplicateRecordKeyValidationErrors;
	}
	
	public void setRecordApplicationVersion(Version version) {
		this.recordApplicationVersion = version;
	}

	public class DataHandler extends DefaultHandler {
		
		private static final String ATTRIBUTE_VERSION = "version";
		private static final String ATTRIBUTE_MODIFIED_BY = "modified_by";
		private static final String ATTRIBUTE_CREATED_BY = "created_by";
		private static final String ATTRIBUTE_DATE_MODIFIED = "modified";
		private static final String ATTRIBUTE_DATE_CREATED = "created";
		private static final String ATTRIBUTE_STATE = "state";
		private static final String ATTRIBUTE_SYMBOL = "symbol";
		private static final String ATTRIBUTE_REMARKS = "remarks";
		
		private static final String OLD_FILE_NAME_FIELD_NAME = "fileName";
		private static final String OLD_FILE_SIZE_FIELD_NAME = "fileSize";

		private CollectRecord record;
		protected Node<?> node;
		protected String field;
		private boolean failed;
		private List<NodeUnmarshallingError> failures;
		private List<NodeUnmarshallingError> warnings;
		private StringBuilder content;
		protected Attributes attributes;
		private CollectSurvey recordSurvey;
		private CollectSurvey publishedSurvey;
		private int ignoreLevels;
		private boolean recordDependencyGraphsEnabled = true;
		private boolean recordValidationEnabled = true;
		private boolean ignoreDuplicateRecordKeyValidationErrors = false;
		
		public DataHandler(CollectSurvey survey) {
			this(survey, survey, true);
		}

		public DataHandler(CollectSurvey publishedSurvey, CollectSurvey recordSurvey, boolean recordValidationEnabled) {
			this(publishedSurvey, recordSurvey, recordValidationEnabled, false);
		}
		
		public DataHandler(CollectSurvey publishedSurvey, CollectSurvey recordSurvey, boolean recordValidationEnabled,
				boolean ignoreDuplicateRecordKeyValidationErrors) {
			super();
			this.publishedSurvey = publishedSurvey;
			this.recordSurvey = recordSurvey;
			this.recordValidationEnabled = recordValidationEnabled;
			this.ignoreDuplicateRecordKeyValidationErrors = ignoreDuplicateRecordKeyValidationErrors;
		}

		@Override
		public void startDocument() throws SAXException {
			this.record = null;
			this.node = null;
			this.failed = false;
			this.field = null;
			this.failures = new ArrayList<NodeUnmarshallingError>();
			this.warnings = new ArrayList<NodeUnmarshallingError>();
			this.attributes = null;
			this.ignoreLevels = 0;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			String name = localName.isEmpty() ? qName : localName;
			try {
				if ( failed ) {
					return; 
				} else if ( ignoreLevels > 0 ) {
					pushIgnore();
					return;
				} else if ( node == null ) {
					// if root element, read audit data, version, and 
					startRecord(name, attributes);
				} else {
					resetContent();
					this.attributes = attributes;
					if ( node instanceof Entity ) {
						startChildNode(name, attributes);
					} else if ( node instanceof Attribute ) {
						startAttributeField(name, attributes);
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
				return node.getPath() + Path.SEPARATOR + field;
			}
		}

		public void startRecord(String rootEntityName, Attributes attributes) {
			String versionName = extractVersionName(attributes);
			record = new CollectRecord(publishedSurvey, versionName, rootEntityName, 
					recordDependencyGraphsEnabled, recordValidationEnabled, ignoreDuplicateRecordKeyValidationErrors);
			record.setApplicationVersion(recordApplicationVersion);
			String stateAttr = attributes.getValue(ATTRIBUTE_STATE);
			State state = State.fromCode(stateAttr);
			record.setState(state);

			Date created = Dates.parseDateTime(attributes.getValue(ATTRIBUTE_DATE_CREATED));
			record.setDataCreationDate(created);
			record.setCreationDate(created);
			Date modified =  Dates.parseDateTime(attributes.getValue(ATTRIBUTE_DATE_MODIFIED));
			record.setDataModifiedDate(modified);
			record.setModifiedDate(modified);

			String createdByUserName = attributes.getValue(ATTRIBUTE_CREATED_BY);
			User createdBy = new User(createdByUserName);
			record.setCreatedBy(createdBy);
			String modifiedByUserName = attributes.getValue(ATTRIBUTE_MODIFIED_BY);
			User modifiedBy = new User(modifiedByUserName);
			record.setModifiedBy(modifiedBy);
			
			node = record.getRootEntity();
		}
		
		protected String extractVersionName(Attributes attributes) {
			String versionName = null;
			String recordVersionName = attributes.getValue(ATTRIBUTE_VERSION);
			if ( StringUtils.isNotBlank(recordVersionName) ) {
				ModelVersion recordVersion = recordSurvey.getVersion(recordVersionName);
				if ( recordVersion == null ) {
					throw new IllegalArgumentException(String.format("Record version with name %s not found in the survey", recordVersionName));
				}
				int versionId = recordVersion.getId();
				ModelVersion version = publishedSurvey.getVersionById(versionId);
				if ( version == null ) {
					throw new IllegalArgumentException(String.format("Record version with id %d not found in the current survey", versionId));
				}
				versionName = version.getName();
			}
			return versionName;
		}

		public void startChildNode(String localName, Attributes attributes) {
			Entity entity = (Entity) node;
			NodeDefinition childDefn = getNodeDefinition(entity, localName);
			if ( childDefn == null ) {
				warn(localName, "Undefined node");
				pushIgnore();
			} else {
				ModelVersion version = record.getVersion();
				if ( version == null || version.isApplicable(childDefn)) {
					Node<?> newNode = childDefn.createNode();
					entity.add(newNode);
					Integer stateValue = getNodeState();
					if ( stateValue != null ) {
						entity.setChildState(localName, stateValue);
					}
					this.node = newNode;
				} else {
					warn(localName, "Node definition is not applicable to the record version");
					pushIgnore();
				}
			}
		}

		private NodeDefinition getNodeDefinition(Entity parentEntity, String localName) {
			Schema oldSchema = recordSurvey.getSchema();
			EntityDefinition newParentEntityDefn = parentEntity.getDefinition();
			EntityDefinition oldParentEntityDefn = (EntityDefinition) oldSchema.getDefinitionById(newParentEntityDefn.getId());
			NodeDefinition oldNodeDefn = oldParentEntityDefn.getChildDefinition(localName);
			if ( oldNodeDefn == null) {
				return null;
			} else if (publishedSurvey == recordSurvey) {
				return oldNodeDefn;
			} else {
				Schema newSchema = publishedSurvey.getSchema();
				NodeDefinition newDef = newSchema.getDefinitionById(oldNodeDefn.getId());
				return newDef;
			}
		}
		
		protected void startAttributeField(String localName, Attributes attributes) {
			this.field = localName;
		}

		protected void pushIgnore() {
			ignoreLevels++;
		}

		protected void warn(String localName, String msg) {
			String path = getPath() + "/" + localName;
			NodeUnmarshallingError nodeErrorItem = new NodeUnmarshallingError(record.getStep(), path, msg);
			warnings.add(nodeErrorItem);
		}

		protected void fail(String msg) {
			String path = getPath();
			Step step = record == null ? null : record.getStep();
			NodeUnmarshallingError nodeErrorItem = new NodeUnmarshallingError(step, path, msg);
			failures.add(nodeErrorItem);
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
			this.record.updateSummaryFields();
		}
		
		protected void endEntityElement() {
			Node<?> parent = node.getParent();
			removeIfEmpty(node);
			this.node = parent;
		}

		@SuppressWarnings({ "rawtypes" })
		protected void endAttributeElement() {
			Attribute attr = (Attribute) node;
			if (field != null) {
				Field<?> fld = getField();
				if ( fld == null ) {
					warn(field, "Can't parse field with type "+attr.getClass().getSimpleName());
				} else {
					setFieldData(fld);
				}
			}
			if ( field == null ) {
				Node<?> oldNode = node;
				this.node = node.getParent();
				removeIfEmpty(oldNode);
			} else {
				this.field = null;
			}
		}

		private Field<?> getField() {
			Attribute<?, ?> attr = (Attribute<?, ?>) node;
			if (attr instanceof FileAttribute) {
				FileAttribute fileAttr = (FileAttribute) attr;
				//backwards compatibility
				if ( OLD_FILE_NAME_FIELD_NAME.equals(field)) {
					return fileAttr.getFilenameField();
				} else if ( OLD_FILE_SIZE_FIELD_NAME.equals(field) ) {
					return fileAttr.getSizeField();
				} else {
					return fileAttr.getField(field);
				}
			} else {
				return attr.getField(field);
			}
		}

		protected void removeIfEmpty(Node<?> node) {
			if ( node != null && ! node.hasData() && node.getParent() != null ) {
				//if node is empty, remove it
				node.getParent().remove(node.getDefinition(), node.getIndex());
			}
		}
		
		protected Node<?> getNode() {
			return node;
		}
		
		protected void setFieldData(Field<?> fld) {
			fld.setValueFromString(content == null ? null : content.toString().trim());
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
			fld.getAttribute().updateSummaryInfo();
		}

		private Integer getNodeState()  {
			String state = attributes.getValue(ATTRIBUTE_STATE);
			if ( StringUtils.isNotBlank(state) ) {
				try {
					return Integer.parseInt(state);
				} catch (NumberFormatException e) {
					warn(field, "Error parsing node state from value '" + state + "': " + e.toString());
				}
			}
			return null;
		}
		
		private void resetContent() {
			if (content == null) {
				content = new StringBuilder();
			} else {
				content.setLength(0); //clear StringBuilder
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			if ( content != null && node instanceof Attribute ) {
				content.append(ch, start, length);
			}
		}
		
		public CollectRecord getRecord() {
			return record;
		}

		public CollectSurvey getRecordSurvey() {
			return recordSurvey;
		}
		
		public List<NodeUnmarshallingError> getFailures() {
			return CollectionUtils.unmodifiableList(failures);
		}
		
		public List<NodeUnmarshallingError> getWarnings() {
			return CollectionUtils.unmodifiableList(warnings);
		}
		
		public void setRecordValidationEnabled(boolean recordValidationEnabled) {
			this.recordValidationEnabled = recordValidationEnabled;
		}
		
		public void setIgnoreDuplicateRecordKeyValidationErrors(boolean ignoreDuplicateRecordKeyValidationErrors) {
			this.ignoreDuplicateRecordKeyValidationErrors = ignoreDuplicateRecordKeyValidationErrors;
		}
	}
	
	public class ParseRecordResult {
		
		private boolean success;
		private String message;
		private List<NodeUnmarshallingError> warnings;
		private List<NodeUnmarshallingError> failures;
		private CollectRecord record;

		public ParseRecordResult() {
		}
		
		public ParseRecordResult(CollectRecord record) {
			this();
			this.record = record;
		}

		public boolean hasFailures() {
			return failures != null && ! failures.isEmpty();
		}

		public boolean hasWarnings() {
			return warnings != null && ! warnings.isEmpty();
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public CollectRecord getRecord() {
			return record;
		}

		public void setRecord(CollectRecord record) {
			this.record = record;
		}

		public boolean isSuccess() {
			return success;
		}

		public void setSuccess(boolean success) {
			this.success = success;
		}

		public List<NodeUnmarshallingError> getWarnings() {
			return CollectionUtils.unmodifiableList(warnings);
		}

		public void setWarnings(List<NodeUnmarshallingError> warnings) {
			this.warnings = warnings;
		}

		public List<NodeUnmarshallingError> getFailures() {
			return CollectionUtils.unmodifiableList(failures);
		}

		public void setFailures(List<NodeUnmarshallingError> failures) {
			this.failures = failures;
		}

	}

}
