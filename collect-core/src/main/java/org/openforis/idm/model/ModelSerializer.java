package org.openforis.idm.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * @author G. Miceli
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ModelSerializer {

	private static EntitySchema ENTITY_SCHEMA;

	static {
		/* Important: Schemas must be registered in depth-first post-order!!! */
		RuntimeSchema.register(Field.class, new FieldSchema());
		register(new AttributeSchema(BooleanAttribute.class));
		register(new AttributeSchema(CodeAttribute.class));
		register(new AttributeSchema(CoordinateAttribute.class));
		register(new AttributeSchema(DateAttribute.class));
		register(new AttributeSchema(FileAttribute.class));
		register(new AttributeSchema(IntegerAttribute.class));
		register(new AttributeSchema(IntegerRangeAttribute.class));
		register(new AttributeSchema(RealAttribute.class));
		register(new AttributeSchema(RealRangeAttribute.class));
		register(new AttributeSchema(TaxonAttribute.class));
		register(new AttributeSchema(TextAttribute.class));
		register(new AttributeSchema(TimeAttribute.class));
		ENTITY_SCHEMA = new EntitySchema();
		RuntimeSchema.register(Entity.class, ENTITY_SCHEMA);
	}

	private static void register(AttributeSchema schema) {
		RuntimeSchema.register(schema.typeClass(), schema);
	}

	private LinkedBuffer buffer;
	
	public ModelSerializer(int bufferSize) {
		this.buffer = LinkedBuffer.allocate(bufferSize);
	}
	
	synchronized
	public byte[] toByteArray(Entity entity) {
		try {
			return ProtostuffIOUtil.toByteArray(entity, ENTITY_SCHEMA, buffer);
		} finally {
			buffer.clear();
		}
	}
	
	synchronized
	public void writeTo(OutputStream output, Entity entity) throws IOException {
		try {
	//		XmlIOUtil.writeTo(output, entity, ENTITY_SCHEMA);		
			ProtostuffIOUtil.writeTo(output, entity, ENTITY_SCHEMA, buffer);
		} finally {
			buffer.clear();
		}
	}
	
	synchronized
	public void writeTo(String filename, Entity entity) throws IOException {
		OutputStream out = new FileOutputStream(filename);
		try {
			writeTo(out, entity);
		} finally {
			buffer.clear();
			out.flush();
			out.close();
		}
	}
	
	synchronized
	public void mergeFrom(byte[] data, Entity entity) {
		try {
			//System.out.print("entity = " + entity);
			ProtostuffIOUtil.mergeFrom(data, entity, ENTITY_SCHEMA);
		} finally {
			buffer.clear();
		}
	}
	
	synchronized
	public void mergeFrom(InputStream input, Entity entity) throws IOException {
		try {
			ProtostuffIOUtil.mergeFrom(input, entity, ENTITY_SCHEMA);
		} finally {
			buffer.clear();
		}
	}

	synchronized
	public void mergeFrom(String filename, Entity entity) throws IOException {
		InputStream in = new FileInputStream(filename);
		try {
			mergeFrom(in, entity);
		} finally {
			buffer.clear();
			in.close();
		}
	}
}
