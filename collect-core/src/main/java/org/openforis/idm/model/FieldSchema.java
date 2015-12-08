package org.openforis.idm.model;

import java.io.IOException;

import com.dyuproject.protostuff.Input;
import com.dyuproject.protostuff.Output;
import com.dyuproject.protostuff.ProtostuffException;

/**
 * @author G. Miceli
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class FieldSchema extends SchemaSupport<Field> {

	private static final int VALUE_FIELD_NUMBER = 1;
	private static final int SYMBOL_FIELD_NUMBER = 2;
	private static final int REMARKS_FIELD_NUMBER = 3;
	private static final int STATE_FIELD_NUMBER = 4;
	
	public FieldSchema() {
		super(Field.class, "value", "symbol", "remarks", "state" );
	}

	@Override
	public String messageName() {
		return "field";
	}

	@Override
	public void writeTo(Output out, Field fld) throws IOException {
		if ( fld.value != null ) { 
			if ( fld.valueType == Boolean.class ) {
				out.writeInt32(VALUE_FIELD_NUMBER, (Boolean) fld.value ? -1 : 0, false);
			} else if ( fld.valueType == Integer.class ) {
				out.writeInt32(VALUE_FIELD_NUMBER, (Integer) fld.value, false);
			} else if ( fld.valueType ==  Long.class ) {
				out.writeInt64(VALUE_FIELD_NUMBER, (Long) fld.value, false);
			} else if ( fld.valueType == Double.class ) {
				out.writeDouble(VALUE_FIELD_NUMBER, (Double) fld.value, false);
			} else if ( fld.valueType == String.class ) {
				out.writeString(VALUE_FIELD_NUMBER, (String) fld.value, false);
			} else {
				throw new UnsupportedOperationException("Cannot serialize "+Field.class.getSimpleName()+"<"+fld.valueType.getClass().getSimpleName()+">");
			}
		}
		if ( fld.symbol != null ) {
			out.writeString(SYMBOL_FIELD_NUMBER, fld.symbol.toString(), false);
		}
		if ( fld.remarks != null ) {
			out.writeString(REMARKS_FIELD_NUMBER, fld.remarks, false);
		}
		if( fld.state != null ){
			out.writeInt32(STATE_FIELD_NUMBER, fld.state.intValue(), false);
		}
	}

	@Override
	public void mergeFrom(Input in, Field fld) throws IOException {
        for(int number = in.readFieldNumber(this);;
        		number = in.readFieldNumber(this))
        {
			switch (number) {
			case 0:
				return;
			case VALUE_FIELD_NUMBER:
				Object val;
				if ( fld.valueType == Boolean.class ) {
					val = in.readInt32() != 0;
				} else if ( fld.valueType == Integer.class ) {
					val = in.readInt32();
				} else if ( fld.valueType ==  Long.class ) {
					val = in.readInt64();
				} else if ( fld.valueType ==  Double.class ) {
					val = in.readDouble();
				} else if ( fld.valueType == String.class ) {
					val = in.readString();
				} else {
					throw new UnsupportedOperationException("Cannot deserialize type "+Field.class.getSimpleName()+"<"+fld.valueType.getClass().getSimpleName()+">");
				}
				fld.setValue(val);
				break;
			case SYMBOL_FIELD_NUMBER:
				fld.symbol = in.readString().charAt(0);
				break;
			case REMARKS_FIELD_NUMBER:
				fld.remarks = in.readString();
				break;
			case STATE_FIELD_NUMBER:
				fld.state = State.parseState(in.readInt32());
				break;
			default:
            	throw new ProtostuffException("Unexpected field number");
			}
        }
	}

}