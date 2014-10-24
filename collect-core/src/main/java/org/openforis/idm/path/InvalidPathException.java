package org.openforis.idm.path;

/**
 * @author G. Miceli
 * @author M. Togna
 */
// TODO Replace with checked exception
public class InvalidPathException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String path;

	public InvalidPathException(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	@Override
	public String getMessage() {
		return "Invalid path: "+path;
	}
}
