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
				out.writeInt32(1, (Boolean) fld.value ? -1 : 0, false);
			} else if ( fld.valueType == Integer.class ) {
				out.writeInt32(1, (Integer) fld.value, false);
			} else if ( fld.valueType ==  Long.class ) {
				out.writeInt64(1, (Long) fld.value, false);
			} else if ( fld.valueType == Double.class ) {
				out.writeDouble(1, (Double) fld.value, false);
			} else if ( fld.valueType == String.class ) {
				out.writeString(1, (String) fld.value, false);
			} else {
				throw new UnsupportedOperationException("Cannot serialize "+Field.class.getSimpleName()+"<"+fld.valueType.getClass().getSimpleName()+">");
			}
		}
		if ( fld.symbol != null ) {
			out.writeString(2, fld.symbol.toString(), false);
		}
		if ( fld.remarks != null ) {
			out.writeString(3, fld.remarks, false);
		}
		if( fld.state != null ){
			out.writeInt32(4, fld.state.intValue(), false);
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
			case 1:
				if ( fld.valueType == Boolean.class ) {
					fld.value = in.readInt32() != 0;
				} else if ( fld.valueType == Integer.class ) {
					fld.value = in.readInt32();
				} else if ( fld.valueType ==  Long.class ) {
					fld.value = in.readInt64();
				} else if ( fld.valueType ==  Double.class ) {
					fld.value = in.readDouble();
				} else if ( fld.valueType == String.class ) {
					fld.value = in.readString();
				} else {
					throw new UnsupportedOperationException("Cannot deserialize type "+Field.class.getSimpleName()+"<"+fld.valueType.getClass().getSimpleName()+">");
				}
				break;
			case 2:
				fld.symbol = in.readString().charAt(0);
				break;
			case 3:
				fld.remarks = in.readString();
				break;
			case 4:
				State state = State.parseState(in.readInt32());
				fld.state = state;
				break;
			default:
            	throw new ProtostuffException("Unexpected field number");
			}
        }
	}

}