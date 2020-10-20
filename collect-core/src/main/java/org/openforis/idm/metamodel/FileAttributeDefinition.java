/**
 * 
 */
package org.openforis.idm.metamodel;

import java.util.ArrayList;
import java.util.List;

import org.openforis.commons.collection.CollectionUtils;
import org.openforis.idm.model.File;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.Value;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public class FileAttributeDefinition extends AttributeDefinition {

	private static final long serialVersionUID = 1L;

	public static final String FILE_NAME_FIELD = "file_name";
	public static final String FILE_SIZE_FIELD = "file_size";

	private final FieldDefinitionMap fieldDefinitionByName = new FieldDefinitionMap(
		new FieldDefinition<String>(FILE_NAME_FIELD, "f", null, String.class, this),
		new FieldDefinition<Long>(FILE_SIZE_FIELD, "s", "size", Long.class, this)
	);
	
	private Integer maxSize;
	private List<String> extensions;
	
	FileAttributeDefinition(Survey survey, int id) {
		super(survey, id);
	}
	
	FileAttributeDefinition(Survey survey, FileAttributeDefinition source, int id) {
		super(survey, source, id);
		this.maxSize = source.maxSize;
		this.extensions = CollectionUtils.clone(source.extensions);
	}
	
	public Integer getMaxSize() {
		return this.maxSize;
	}

	public void setMaxSize(Integer maxSize) {
		this.maxSize = maxSize;
	}
	
	public List<String> getExtensions() {
		return CollectionUtils.unmodifiableList(extensions);
	}
	
	public void addExtension(String extension) {
		if ( extensions == null )  {
			extensions = new ArrayList<String>();
		}
		extensions.add(extension);
	}

	public void addExtensions(List<String> extensions) {
		if ( extensions != null ) {
			for (String extension : extensions) {
				addExtension(extension);
			}
		}
	}
	
	public void removeExtension(String extension) {
		extensions.remove(extension);
	}
	
	public void removeExtensions(List<String> extensions) {
		if (extensions != null ) {
			extensions.removeAll(extensions);
		}
	}
	
	public void removeAllExtensions() {
		if ( extensions == null ) {
			extensions = new ArrayList<String>();
		} else {
			extensions.clear();
		}
	}

	@Override
	public Node<?> createNode() {
		return new FileAttribute(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public File createValue(String string) {
		return new File((String) string, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public File createValue(Object val) {
		if (val instanceof File) {
			return new File((File) val);
		} else if (val instanceof String) {
			return createValue((String) val);
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <V extends Value> V createValueFromKeyFieldValues(List<String> fieldValues) {
		return (V) new File(fieldValues.get(0), Long.parseLong(fieldValues.get(1)));
	}
	
	@Override
	protected FieldDefinitionMap getFieldDefinitionMap() {
		return fieldDefinitionByName;
	}
	
	@Override
	public boolean hasMainField() {
		return true;
	}
	
	@Override
	public String getMainFieldName() {
		return FILE_NAME_FIELD;
	}

	@Override
	public Class<? extends Value> getValueType() {
		return File.class;
	}
}
