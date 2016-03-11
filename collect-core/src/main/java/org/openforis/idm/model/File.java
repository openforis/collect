package org.openforis.idm.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author G. Miceli
 * @author M. Togna
 */
public final class File extends AbstractValue {

	public static final String FILENAME_FIELD = "filename";
	public static final String SIZE_FIELD = "size";
	
	private final String filename;
	private final Long size;

	public File(String filename, Long size) {
		this.filename = filename;
		this.size = size;
	}

	public File(File val) {
		this(val.filename, val.size);
	}

	public String getFilename() {
		return filename;
	}

	public Long getSize() {
		return size;
	}

	@Override
	@SuppressWarnings("serial")
	public Map<String, Object> toMap() {
		return new HashMap<String, Object>() {{
			put(FILENAME_FIELD, filename);
			put(SIZE_FIELD, size);
		}};
	}
	
	@Override
	public String toPrettyFormatString() {
		return toInternalString();
	}
	
	@Override
	public String toInternalString() {
		return filename;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + ((size == null) ? 0 : size.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		File other = (File) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (size == null) {
			if (other.size != null)
				return false;
		} else if (!size.equals(other.size))
			return false;
		return true;
	}
	
}
