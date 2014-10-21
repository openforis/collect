package org.openforis.idm.model;

import java.io.IOException;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.ProtostuffException;

/**
 * @author G. Miceli
 */
public class AttributeSchema<T extends Attribute<?,?>> extends SchemaSupport<T> {

	private static FieldSchema ATTRIBUTE_FIELD_SCHEMA = new FieldSchema();

	public AttributeSchema(Class<T> typeClass) {
		super(typeClass, "fields");
	}
	
	@Override
	public boolean isInitialized(T attr) {
		return attr.definitionId != null;
	}

	@Override
	public T newMessage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String messageName() {
		return typeClass().getSimpleName().replace("Attribute", "").toLowerCase();
	}

	@Override
	public String messageFullName() {
		return typeClass().getName();
	}

	@Override
	public void writeTo(Output output, T attr) throws IOException {
		int cnt = attr.getFieldCount();
		for (int i = 0; i < cnt; i++) {
			Field<?> fld = attr.getField(i);
			output.writeObject(1, fld, ATTRIBUTE_FIELD_SCHEMA, true);
		}
	}

	@Override
	public void mergeFrom(Input input, T attr) throws IOException {
		int cnt = attr.getFieldCount();
        for(int number = input.readFieldNumber(this), i=0;;
        		number = input.readFieldNumber(this), i++)
        {
        	if ( number == 0 ) {
        		break;
        	} else if ( i >= cnt ) {
            	throw new ProtostuffException("Too many attribute fields");
        	} else if ( number != 1 ) {
            	throw new ProtostuffException("Unexpected field number");
        	} else {
    			Field<?> fld = attr.getField(i);
    			input.mergeObject(fld, ATTRIBUTE_FIELD_SCHEMA);
            }
        }
	}
}
