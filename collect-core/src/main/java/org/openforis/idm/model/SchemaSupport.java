package org.openforis.idm.model;

import java.io.IOException;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.ProtostuffException;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * @author G. Miceli
 */
public abstract class SchemaSupport<T> implements Schema<T> {

	private Class<T> typeClass;
	private String[] fields;

	public SchemaSupport(Class<T> typeClass, String... fields) {
		this.typeClass = typeClass;
		this.fields = fields;
	}
	
	@Override
	public String getFieldName(int number) {
		try {
			return fields[number];
		} catch ( ArrayIndexOutOfBoundsException e ) {
			return null;
		}
	}
	
	@Override
	public int getFieldNumber(String name) {
		for (int i = 0; i < fields.length; i++) {
			if ( fields[i].equals(name) ) {
				return i;
			}
		}
		return 0;
	}
	
	@Override
	public Class<T> typeClass() {
		return typeClass;
	}
	
	@Override
	public boolean isInitialized(T message) {
		return true;
	}

	@Override
	public T newMessage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String messageFullName() {
		return getClass().getName();
	}

	protected void readAndCheckFieldNumber(Input input, int i) throws IOException, ProtostuffException {
		int fieldNumber = input.readFieldNumber(this);
		if ( fieldNumber != i ) {
			throw new ProtostuffException("Corrupt NodeWrapper field number");
		}
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected Schema getSchema(Class cls) {
		return RuntimeSchema.getSchema(cls);		
	}

}
