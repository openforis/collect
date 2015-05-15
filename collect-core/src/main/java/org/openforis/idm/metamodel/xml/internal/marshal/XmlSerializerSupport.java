package org.openforis.idm.metamodel.xml.internal.marshal;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.openforis.idm.metamodel.xml.IdmlConstants;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

/**
 * @author G. Miceli
 *
 * @param <P>
 */
public abstract class XmlSerializerSupport<T, P> {

	private XmlSerializer xs;
	private List<XmlSerializerSupport<?,T>> childMarshallers;
	private String encoding;
	private String tagNamespace;
	private String tagName;
	private boolean includeEmpty;
	private Writer writer;
	private String listWrapperTag;
	private XmlSerializerSupport<?, ?> parentMarshaller;
	
	protected XmlSerializerSupport() {
		this(null);
	}
	
	protected XmlSerializerSupport(String tag) {
		this(IdmlConstants.IDML3_NAMESPACE_URI, tag);
	}

	protected XmlSerializerSupport(String tagNamespace, String tagName) {
		this.includeEmpty = false; 
		this.tagNamespace = tagNamespace;
		this.tagName = tagName;
	}

	protected XmlSerializer getXmlSerializer() {
		return xs;
	}
	
	public boolean isIncludeEmpty() {
		return includeEmpty;
	}

	public void setIncludeEmpty(boolean includeEmpty) {
		this.includeEmpty = includeEmpty;
	}

	public void setListWrapperTag(String listWrapperTag) {
		this.listWrapperTag = listWrapperTag;
	}
	
	protected XmlSerializerSupport<?, ?> getParentMarshaller() {
		return parentMarshaller;
	}
	
	protected void setParentMarshaller(XmlSerializerSupport<?, ?> parentMarshaller) {
		this.parentMarshaller = parentMarshaller;
	}
	
	synchronized
	public void marshal(T sourceObject, OutputStream os, String enc) throws IOException {
		XmlSerializer ser = createXmlSerializer();
		ser.setOutput(os, enc);
		Writer writer = new OutputStreamWriter(os, enc);
		this.writer = writer;
		marshal(sourceObject, enc, ser);
	}
	
	synchronized
	public void marshal(T sourceObject, Writer wr, String enc) throws IOException {
		XmlSerializer ser = createXmlSerializer();
		ser.setOutput(wr);
		this.writer = wr;
		marshal(sourceObject, enc, ser);
	}

	private void marshal(T sourceObject, String enc, XmlSerializer ser)
			throws UnsupportedEncodingException, IOException {
		ser.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
	    this.xs = ser;
		this.encoding = enc;
		marshal(sourceObject);
	}

	
	private static XmlSerializer createXmlSerializer() {
		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			return factory.newSerializer();
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Main method which calls start, attributes, body and end
	 * @param sourceObject
	 * @throws IOException
	 */
	protected void marshal(T sourceObject) throws IOException {
		if ( includeEmpty || sourceObject != null ) {
			start(sourceObject);
			attributes(sourceObject);
			body(sourceObject);
			end(sourceObject);
		}
	}

	protected void start(T sourceObject) throws IOException {
		if ( tagName != null ) {
			xs.startTag(tagNamespace, tagName);
		}
	}

	protected void attributes(T sourceObject) throws IOException {
		// no-op
	}

	protected void body(T sourceObject) throws IOException {
		marshalChildren(sourceObject);
	}

	protected void marshalChildren(T parentObject) throws IOException {
		if ( childMarshallers != null ) {
			for (XmlSerializerSupport<?,T> ser : childMarshallers) {
				prepareChildMarshaller(ser);
				ser.marshalInstances(parentObject);
			}
		}
	}

	/**
	 * Override this method to extract instances from parent.  
	 * Should call marshal() on List or single instances 
	 * @param parentObject
	 * @throws IOException
	 */
	protected void marshalInstances(P parentObject) throws IOException {
		// TODO Auto-generated method stub
	}

	protected void marshal(List<? extends T> sourceObjects) throws IOException {
		if ( sourceObjects == null || sourceObjects.isEmpty() ) {
			if ( includeEmpty ) {
				startList();
				marshal((T) null);
				endList();
			}
		} else {
			startList();
			for (T obj : sourceObjects) {
				marshal(obj);
			}
			endList();
		}
	}

	protected void startList() throws IOException {
		if ( listWrapperTag != null ) {
			startTag(listWrapperTag);
		}
	}
	
	protected void endList() throws IOException {
		if ( listWrapperTag != null ) {
			endTag(listWrapperTag);
		}
	}

	protected void end(T sourceObject) throws IOException {
		if ( tagName != null ) {
			xs.endTag(tagNamespace, tagName);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addChildMarshallers(XmlSerializerSupport<?,?>... marshallers) {
		if ( childMarshallers == null ) {
			this.childMarshallers = new ArrayList<XmlSerializerSupport<?,T>>(marshallers.length); 
		}
		for (XmlSerializerSupport im : marshallers) {
			childMarshallers.add(im);
			im.setParentMarshaller(this);
		}
	}

	protected final void prepareChildMarshaller(XmlSerializerSupport<?,?> im) {
		im.xs = this.xs;
		im.encoding = this.encoding;
		im.writer = this.writer;
	}

	protected XmlSerializerSupport<?, ?> getRootMarshaller() {
		XmlSerializerSupport<?, ?> current = this;
		XmlSerializerSupport<?, ?> parent = this.getParentMarshaller();
		while ( parent != null ) {
			current = parent;
			parent = current.getParentMarshaller();
		}
		return current;
	}
	
	public void setPrefix(String prefix, String namespaceUri) throws IOException{
		xs.setPrefix(prefix, namespaceUri);
	}

	protected void setDefaultNamespace(String namespaceUri) throws IOException {
		setPrefix("", namespaceUri);
	}

	protected void attribute(String name, String value) throws IOException {
		if ( value != null && ! value.isEmpty() ) {
			xs.attribute("", name, value);
		}
	}
	
	protected void attribute(String name, Object value) 
			throws IOException {
		if ( value != null ) {
			attribute(name, value.toString());
		}
	}

	protected void attribute(String name, Object value, Object defaultValue) 
			throws IOException {
		if ( value != null && ! value.equals(defaultValue) ) {
			attribute(name, value.toString());
		}
	}
	
	protected void attribute(String ns, String name, String value) 
				throws IOException {
		xs.attribute(ns, name, value);
	}

	protected void cdsect(String cdata) throws IOException {
		xs.cdsect(cdata);
	}

	protected void comment(String comment) throws IOException {
		xs.comment(comment);
	}

	protected void endDocument() throws IOException {
		xs.endDocument();
	}

	protected void endTag(String ns, String name) throws IOException {
		xs.endTag(ns, name);
	}

	protected void endTag(String name) throws IOException {
		xs.endTag(tagNamespace, name);
	}

	protected void startDocument() throws IOException {
		xs.startDocument(encoding, true);
	}

	protected void startTag(String ns, String name) throws IOException{
		xs.startTag(ns, name);
	}

	protected void startTag(String name) throws IOException{
		xs.startTag(tagNamespace, name);
	}

	protected void text(String text) throws IOException  {
		xs.text(text);
	}
	
	protected void writeXml(String xml) throws IOException {
		xs.flush();
		writer.write(xml);
		writer.flush();
	}
}
