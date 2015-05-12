package org.openforis.collect.persistence.xml;

import java.util.List;

import org.openforis.commons.collection.CollectionUtils;

/**
 * 
 * @author G. Miceli
 *
 */
public class DataUnmarshallerException extends Exception {

	private static final long serialVersionUID = 1L;
	private List<String> messages;

	public DataUnmarshallerException() {
		super();
	}

	public DataUnmarshallerException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataUnmarshallerException(String message) {
		super(message);
	}

	public DataUnmarshallerException(Throwable cause) {
		super(cause);
	}

	public DataUnmarshallerException(List<String> messages) {
		this.messages = messages;
	}
	
	public List<String> getMessages() {
		return CollectionUtils.unmodifiableList(messages);
//		StringBuilder sb = new StringBuilder();
//		if ( messages == null ) {
//			sb.append(getCause().getMessage());
//		} else {
//			for (int i = 0; i < messages.size(); i++) {
//				String msg = messages.get(i);
//				if ( i > 0 ) {
//					sb.append(", ");
//				}
//				sb.append(msg);
//			}
//		}
//		return sb.toString();
	}
}
