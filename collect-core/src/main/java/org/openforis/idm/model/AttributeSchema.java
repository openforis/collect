package org.openforis.idm.model;

import java.io.IOException;
import java.util.Locale;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.ProtostuffException;

/**
 * @author G. Miceli
 */
public class AttributeSchema<T extends Attribute<?,?>> extends SchemaSupport<T> {

	private static final int ATTRIBUTE_FIELD_FIELD_NUMBER = 1;
	private static FieldSchema ATTRIBUTE_FIELD_SCHEMA = new FieldSchema();

	public AttributeSchema(Class<T> typeClass) {
		super(typeClass, "fields");
	}
	
	@Override
	public boolean isInitialized(T attr) {
		return ! attr.detached;
	}

	@Override
	public T newMessage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String messageName() {
		return typeClass().getSimpleName().replace("Attribute", "").toLowerCase(Locale.ENGLISH);
	}

	@Override
	public String messageFullName() {
		return typeClass().getName();
	}

	@Override
	public void writeTo(Output output, T attr) throws IOException {
		for (Field<?> fld : attr.getFields()) {
			output.writeObject(ATTRIBUTE_FIELD_FIELD_NUMBER, fld, ATTRIBUTE_FIELD_SCHEMA, true);
		}
	}

	@Override
	public void mergeFrom(Input input, T attr) throws IOException {
		int cnt = attr.getFieldCount();
        for(int number = input.readFieldNumber(this), fieldIndex=0; number > 0;
        		number = input.readFieldNumber(this), fieldIndex++) {
        	if ( fieldIndex >= cnt ) {
            	throw new ProtostuffException("Too many attribute fields");
        	} else if ( number != 1 ) {
            	throw new ProtostuffException("Unexpected field number");
        	} else {
    			Field<?> fld = attr.getField(fieldIndex);
    			input.mergeObject(fld, ATTRIBUTE_FIELD_SCHEMA);
            }
        }
        attr.updateSummaryInfo();
	}
}
